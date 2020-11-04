package org.astropeci.urmwstats.template;

import com.google.common.collect.MapMaker;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LiveTemplateRepository {

    private final JDA jda;
    private final MongoDatabase db;
    private final TemplateRepository templateRepository;

    private final Map<String, LiveTemplateTracker> trackers;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, Message> messageCache = new MapMaker()
            .expiration(1, TimeUnit.HOURS)
            .makeMap();

    public LiveTemplateRepository(JDA jda, MongoDatabase db, TemplateRepository templateRepository) {
        MongoCollection<LiveTemplateTracker> collection = db.getCollection("liveTemplates", LiveTemplateTracker.class);
        List<LiveTemplateTracker> trackers = collection.find().into(new ArrayList<>());

        log.info("Loaded {} live templates", trackers.size());
        this.trackers = trackers.stream().collect(Collectors.toMap(LiveTemplateTracker::getMessageId, Function.identity()));

        jda.addEventListener(new ListenerAdapter() {
            public void onMessageDelete(MessageDeleteEvent event) {
                delete(event.getMessageId());
            }
        });

        this.jda = jda;
        this.db = db;
        this.templateRepository = templateRepository;
    }

    public Set<LiveTemplateTracker> all() {
        @Cleanup("unlock") Lock lock = this.lock.readLock();
        lock.lock();

        return new HashSet<>(trackers.values());
    }

    public void create(Message message, String templateName, List<String> variables) {
        String messageId = message.getId();

        LiveTemplateTracker tracker = new LiveTemplateTracker(
                message.getGuild().getId(),
                message.getChannel().getId(),
                messageId,
                templateName,
                variables,
                Instant.now().truncatedTo(ChronoUnit.MINUTES)
        );

        @Cleanup("unlock") Lock lock = this.lock.writeLock();
        lock.lock();

        log.info("Registering live template on {} using {} {}", messageId, templateName, variables);
        MongoCollection<LiveTemplateTracker> collection = db.getCollection("liveTemplates", LiveTemplateTracker.class);
        collection.replaceOne(Filters.eq("messageId", messageId), tracker, new ReplaceOptions().upsert(true));

        trackers.put(messageId, tracker);
    }

    public void delete(String messageId) {
        @Cleanup("unlock") Lock lock = this.lock.writeLock();
        lock.lock();

        log.info("Removing live template on {}", messageId);
        MongoCollection<LiveTemplateTracker> collection = db.getCollection("liveTemplates", LiveTemplateTracker.class);
        collection.deleteOne(Filters.eq("messageId", messageId));

        trackers.remove(messageId);
    }

    @Scheduled(cron = "0 * * * * *")
    private void update() {
        long start = System.currentTimeMillis();

        @Cleanup("unlock") Lock lock = this.lock.writeLock();
        lock.lock();

        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        List<LiveTemplateTracker> trackers = new ArrayList<>(this.trackers.values());
        Collections.shuffle(trackers);

        for (int i = 0; i < trackers.size(); i++) {
            if (System.currentTimeMillis() - start > 15 * 1000 || i > 15) {
                return;
            }

            LiveTemplateTracker tracker = trackers.get(i);

            boolean preserve;
            try {
                preserve = updateTracker(tracker, now);
            } catch (RuntimeException e) {
                log.error("Failed to update tracker {}", tracker, e);
                preserve = true;
            }

            if (!preserve) {
                delete(tracker.getMessageId());
            }
        }
    }

    private boolean updateTracker(LiveTemplateTracker tracker, Instant now) {
        Message target = getMessage(tracker);
        if (target == null) {
            return false;
        }

        Template template = templateRepository.get(tracker.getTemplateName());

        RichMessage message;
        boolean preserve = false;

        if (template == null) {
            log.warn("Cannot find template for {}", tracker);
            message = RichMessage.of("`<missing-template>`");
        } else {
            RenderContext context = new RenderContext(tracker.getVariables(), tracker.getBaselineTime(), now);

            try {
                message = template.render(context);
            } catch (TemplateRenderException e) {
                message = RichMessage.of("`<render-error>` ```" + e.getMessage() + "```");
            }

            preserve = context.isTimeDependent();
        }

        message.edit(target, jda, false);
        return preserve;
    }

    private Message getMessage(LiveTemplateTracker tracker) {
        Message cached = messageCache.get(tracker.getMessageId());
        if (cached != null) {
            return cached;
        }

        Guild guild = jda.getGuildById(tracker.getGuildId());
        if (guild == null) {
            log.warn("Cannot find guild for {}", tracker);
            return null;
        }

        MessageChannel channel = guild.getTextChannelById(tracker.getChannelId());
        if (channel == null) {
            log.warn("Cannot find channel for {}", tracker);
            return null;
        }

        Message message;
        try {
            message = channel.retrieveMessageById(tracker.getMessageId()).complete();
        } catch (ErrorResponseException e) {
            log.warn("Cannot find message for {}", tracker);
            return null;
        }

        messageCache.put(tracker.getMessageId(), message);
        return message;
    }
}
