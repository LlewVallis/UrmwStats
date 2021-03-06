package org.astropeci.urmwstats.command.commands.template;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.template.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTemplateCommand implements Command {

    private static final Pattern CHANNEL_PATTERN = Pattern.compile("<#\\d+>");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("https://discord\\.com/channels/(\\d+)/(\\d+)/(\\d+)");

    private final JDA jda;
    private final TemplateRepository templateRepository;
    private final LiveTemplateRepository liveTemplateRepository;
    private final boolean createLiveTemplates;

    private interface Target {

        Message send(RichMessage message, MessageReceivedEvent event);
    }

    @Value
    private class ChannelTarget implements Target {

        MessageChannel channel;

        @Override
        public Message send(RichMessage message, MessageReceivedEvent event) {
            if (!(channel instanceof TextChannel) && createLiveTemplates) {
                throw new CommandException("❌ Live templates cannot be used in DMs");
            }

            Message postedMessage;
            try {
                postedMessage = message.send(channel);
            } catch (RuntimeException e) {
                log.warn("Failed to send template to channel {}", channel.getName(), e);
                throw new CommandException("❌ Could not send to <#" + channel.getId() + ">");
            }

            if (!channel.equals(event.getChannel())) {
                event.getChannel().sendMessage("📨 Successfully sent to <#" + channel.getId() + ">").queue();
            }

            return postedMessage;
        }
    }

    @Value
    private class MessageTarget implements Target {

        Message message;

        @Override
        public Message send(RichMessage message, MessageReceivedEvent event) {
            try {
                liveTemplateRepository.delete(this.message.getId());
                message.edit(this.message, jda, true);
            } catch (RuntimeException e) {
                log.warn("Failed to edit message with template", e);
                throw new CommandException("❌ Could not edit the message");
            }

            event.getChannel().sendMessage("✏️ Successfully edited the message").queue();
            return this.message;
        }
    }

    @Override
    public HelpSection section() {
        return HelpSection.TEMPLATE;
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() < 1) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        String firstArgument = arguments.get(0);
        boolean targetArgument = CHANNEL_PATTERN.matcher(firstArgument).matches() || MESSAGE_PATTERN.matcher(firstArgument).matches();

        if (targetArgument && arguments.size() < 2) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        Target target;
        String name;
        List<String> variables;

        if (targetArgument) {
            target = parseTarget(arguments.get(0), event);
            name = CommandUtil.templateName(arguments.get(1));
            variables = arguments.subList(2, arguments.size());
        } else {
            target = new ChannelTarget(event.getChannel());
            name = CommandUtil.templateName(arguments.get(0));
            variables = arguments.subList(1, arguments.size());
        }

        Template template = templateRepository.get(name);
        if (template == null) {
            throw new CommandException("🔍 There is no template named `" + name + "`");
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        RenderContext context = new RenderContext(variables, now, now);

        RichMessage message;
        try {
            message = template.render(context);
        } catch (TemplateRenderException e) {
            throw new CommandException("❌ Rendering error ```" + e.getMessage() + "```");
        }

        Message targetMessage = target.send(message, event);
        if (createLiveTemplates) {
            if (context.isTimeDependent()) {
                liveTemplateRepository.create(targetMessage, name, variables);
            }
        } else if (context.isUsingCountdowns()) {
            event.getChannel().sendMessage("⚠️ Use `%live-template` to create templates with updating countdowns").queue();
        }
    }

    private Target parseTarget(String input, MessageReceivedEvent event) {
        if (CHANNEL_PATTERN.matcher(input).matches()) {
            MessageChannel channel = CommandUtil.parseChannel(input, event, jda);
            return new ChannelTarget(channel);
        } else {
            Matcher matcher = MESSAGE_PATTERN.matcher(input);

            boolean matched = matcher.matches();
            assert matched;

            String guildId = matcher.group(1);
            String channelId = matcher.group(2);
            String messageId = matcher.group(3);

            Message message;
            try {
                Guild guild = jda.getGuildById(guildId);
                if (guild == null) throwMessageNotFound();
                MessageChannel channel = guild.getTextChannelById(channelId);
                if (channel == null) throwMessageNotFound();

                try {
                    message = channel.retrieveMessageById(messageId).complete();
                } catch (ErrorResponseException e) {
                    return throwMessageNotFound();
                }
            } catch (NumberFormatException e) {
                return throwMessageNotFound();
            }

            if (message.getAuthor().getIdLong() != jda.getSelfUser().getIdLong()) {
                throw new CommandException("❌ The target message was sent by another user");
            }

            return new MessageTarget(message);
        }
    }

    private <T> T throwMessageNotFound() {
        throw new CommandException("❌ That message either doesn't exist or URMW Stats doesn't have permission to see it");
    }
}
