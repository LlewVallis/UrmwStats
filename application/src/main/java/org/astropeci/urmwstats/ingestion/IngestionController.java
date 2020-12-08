package org.astropeci.urmwstats.ingestion;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.astropeci.urmwstats.data.Achievement;
import org.astropeci.urmwstats.data.Player;
import org.astropeci.urmwstats.data.RepositoryCoordinator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("ingestion")
public class IngestionController extends ListenerAdapter implements AutoCloseable {

    private static final String LEADERBOARD_CHANNEL = "trueskill-urmwbot";
    private static final String HISTORY_CHANNEL = "trueskill-history-urmwbot";
    private static final String ACHIEVEMENT_CHANNEL = "achievements";

    private static final int MESSAGE_HISTORY_LIMIT = 10000;
    private static final int UPDATE_DELAY_MILLIS = 5000;

    private final RepositoryCoordinator repositoryCoordinator;
    private final LeaderboardParser leaderboardParser;
    private final HistoryParser historyParser;
    private final AchievementsParser achievementsParser;
    private final JDA jda;

    private final AtomicBoolean updateRequested = new AtomicBoolean(true);
    private final Thread thread = new Thread(this::pollForUpdates, "Discord Ingestion Thread");

    public IngestionController(
            RepositoryCoordinator repositoryCoordinator,
            LeaderboardParser leaderboardParser,
            HistoryParser historyParser,
            AchievementsParser achievementsParser,
            JDA jda
    ) {
        this.repositoryCoordinator = repositoryCoordinator;
        this.leaderboardParser = leaderboardParser;
        this.historyParser = historyParser;
        this.achievementsParser = achievementsParser;
        this.jda = jda;

        log.info("Launching ingestion engine");
        jda.addEventListener(this);
        thread.start();
    }

    private void pollForUpdates() {
        try {
            while (!Thread.interrupted()) {
                if (updateRequested.get()) {
                    Thread.sleep(UPDATE_DELAY_MILLIS);
                    updateRequested.set(false);

                    try {
                        performUpdate();
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error("Unhandled exception whilst ingesting data", e);
                    }
                }

                Thread.sleep(500);
            }
        } catch (InterruptedException ignored) { }

        log.info("Terminating ingestion thread");
    }

    @SneakyThrows({ ExecutionException.class })
    private void performUpdate() throws InterruptedException {
        log.info("Performing ingestion");
        MessageChannel leaderboardChannel = getChannelByName(LEADERBOARD_CHANNEL);
        MessageChannel historyChannel = getChannelByName(HISTORY_CHANNEL);
        MessageChannel achievementChannel = getChannelByName(ACHIEVEMENT_CHANNEL);

        CompletableFuture<List<Message>> leaderboardMessagesFuture = readMessages(leaderboardChannel);
        CompletableFuture<List<Message>> historyMessagesFuture = readMessages(historyChannel);
        CompletableFuture<List<Message>> achievementMessagesFuture = readMessages(achievementChannel);

        log.info("Fetching messages for ingestion");
        List<Message> leaderboardMessages = leaderboardMessagesFuture.get();
        List<Message> historyMessages = historyMessagesFuture.get();
        List<Message> achievementMessages = achievementMessagesFuture.get();

        Collections.reverse(leaderboardMessages);
        Collections.reverse(historyMessages);
        Collections.reverse(achievementMessages);

        log.info("Ingesting messages");
        List<Player> players = leaderboardParser.parse(leaderboardMessages);
        HistoryParser.History matches = historyParser.parse(historyMessages, players);
        List<Achievement> achievements = achievementsParser.parse(achievementMessages, players);

        repositoryCoordinator.update(players, matches.getMatches(), matches.getTourneys(), achievements);

        log.info("Successfully ingested and updated");
    }

    private MessageChannel getChannelByName(String name) {
        Set<MessageChannel> matches = jda.getTextChannels().stream()
                .filter(channel -> channel.getName().equals(name))
                .collect(Collectors.toSet());

        if (matches.size() == 0) {
            throw new IllegalStateException("there is no channel called " + name);
        }

        if (matches.size() > 1) {
            log.warn("Found more than one channel called " + name);
        }

        return matches.stream().findAny().get();
    }

    private CompletableFuture<List<Message>> readMessages(MessageChannel channel) {
        return channel.getIterableHistory().takeAsync(MESSAGE_HISTORY_LIMIT);
    }

    private boolean isTrackedChannel(MessageChannel channel) {
        return channel.getName().equals(LEADERBOARD_CHANNEL) || channel.getName().equals(HISTORY_CHANNEL);
    }

    private void scheduleUpdate() {
        if (!updateRequested.getAndSet(true)) {
            log.info("Scheduled ingestion");
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (isTrackedChannel(event.getChannel())) {
            scheduleUpdate();
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (isTrackedChannel(event.getChannel())) {
            scheduleUpdate();
        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        if (isTrackedChannel(event.getChannel())) {
            scheduleUpdate();
        }
    }

    @Override
    @SneakyThrows({ InterruptedException.class })
    public void close() {
        log.info("Closing ingestion engine");
        thread.interrupt();
        thread.join();

        log.info("Disconnecting from the Discord API");
        jda.shutdown();
    }
}
