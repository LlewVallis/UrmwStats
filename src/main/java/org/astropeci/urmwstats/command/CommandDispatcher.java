package org.astropeci.urmwstats.command;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.astropeci.urmwstats.SecretProvider;
import org.astropeci.urmwstats.auth.RoleManager;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Slf4j
@Component
public class CommandDispatcher extends ListenerAdapter {

    private static final String PROCESSING_EMOJI = "🔄";
    private static final String SUCCESS_EMOJI = "✅";
    private static final String FAILURE_EMOJI = "❌";

    private static final String PRODUCTION_PREFIX = "prod%";
    private static final String TESTING_PREFIX = "dev%";

    private final List<Command> commands;
    private final TaskExecutor executor;
    private final RoleManager roleManager;

    private final Set<String> prefixes;
    private final String testingGuildId;
    private final boolean productionCommands;

    public CommandDispatcher(
            List<Command> commands,
            TaskExecutor executor,
            JDA jda,
            SecretProvider secretProvider,
            RoleManager roleManager,
            Environment environment
    ) {
        this.commands = commands;
        this.executor = executor;
        this.roleManager = roleManager;

        jda.addEventListener(this);

        prefixes = Set.of(
                String.format("<@!%s>", secretProvider.getDiscordClientId()),
                String.format("<@%s>", secretProvider.getDiscordClientId()),
                "%", PRODUCTION_PREFIX, TESTING_PREFIX
        );

        testingGuildId = secretProvider.getTestingGuildId();
        productionCommands = List.of(environment.getActiveProfiles()).contains("production-commands");
    }

    @PostConstruct
    private void postConstruct() {
        // Help command is specialised, add it separately.
        commands.add(new HelpCommand(commands, roleManager));
        commands.sort(Comparator.comparing(Command::label));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw().trim();

        Stream<String> prefixesStream = prefixes.stream();
        if (!event.isFromGuild()) {
            prefixesStream = Stream.concat(prefixesStream, Stream.of(""));
        }

        String prefix = prefixesStream
                .filter(message::startsWith)
                .findAny()
                .orElse(null);

        if (prefix == null) {
            return;
        }

        executor.execute(() -> dispatch(message, prefix, event));
    }

    private boolean shouldRun(String prefix, MessageReceivedEvent event) {
        if (prefix.equals(PRODUCTION_PREFIX)) {
            return productionCommands;
        } else if (prefix.equals(TESTING_PREFIX)) {
            if (productionCommands) {
                return false;
            }

            if (!roleManager.isAuthenticated(event.getAuthor().getId())) {
                sendNotPermitted(event);
                return false;
            } else {
                return true;
            }
        } else if (event.isFromGuild()) {
            return event.getGuild().getId().equals(testingGuildId) != productionCommands;
        } else {
            return productionCommands;
        }
    }

    private void dispatch(String message, String prefix, MessageReceivedEvent event) {
        if (!shouldRun(prefix, event)) {
            return;
        }

        message = message.substring(prefix.length()).trim();
        if (message.isEmpty()) {
            log.info("Ignoring blank command");
            return;
        }

        List<String> commandParts = List.of(message.split("\\s+"));

        log.info("Command started in #{} by {}: {}", event.getChannel().getName(), event.getAuthor().getName(), message);

        boolean foundCommand = false;
        for (Command command : commands) {
            if (commandParts.get(0).equals(command.label())) {
                handle(command, commandParts, event);
                foundCommand = true;
                break;
            }
        }

        if (!foundCommand) {
            handleNotFound(message, event);
        }

        log.info("Command finished in #{} by {}: {}", event.getChannel().getName(), event.getAuthor().getName(), message);
    }

    private void handleNotFound(String message, MessageReceivedEvent event) {
        log.info("Command '{}' was not found", message);

        if (event.isFromGuild()) {
            event.getChannel().sendMessage("🤷 Command not found").queue();
            event.getMessage().addReaction(FAILURE_EMOJI).queue();
        }
    }

    private void handle(Command command, List<String> commandParts, MessageReceivedEvent event) {
        log.debug("Dispatching command {}", command.getClass().getSimpleName());

        if (command.isStaffOnly() && !roleManager.isAuthenticated(event.getAuthor().getId())) {
            log.info("Denying command from {} since they are not staff", event.getAuthor().getName());
            sendNotPermitted(event);
            return;
        }

        CompletableFuture<?> typingFuture = event.getChannel().sendTyping().submit();
        CompletableFuture<?> processingReactionFuture = event.getMessage().addReaction(PROCESSING_EMOJI).submit();

        CompletableFuture.allOf(typingFuture, processingReactionFuture).join();

        List<String> arguments = commandParts.subList(1, commandParts.size());
        boolean success = execute(command, arguments, event);

        String statusEmoji = success ? SUCCESS_EMOJI : FAILURE_EMOJI;
        event.getMessage().addReaction(statusEmoji).queue();
        event.getMessage().removeReaction(PROCESSING_EMOJI).queue();
    }

    private boolean execute(Command command, List<String> arguments, MessageReceivedEvent event) {
        try {
            command.execute(arguments, event);
            return true;
        } catch (CommandException e) {
            log.info("Failing command due to expected error: {}", e.getMessage());
            event.getChannel().sendMessage(e.getMessage()).queue();
            return false;
        } catch (Throwable e) {
            log.error(
                    "Internal error whilst executing in #{} by {} as {} with args [{}]",
                    event.getChannel().getName(),
                    event.getAuthor().getName(),
                    command.getClass(),
                    String.join(", ", arguments),
                    e
            );

            event.getChannel().sendMessage("🔥 Internal error").queue();
            return false;
        }
    }

    private void sendNotPermitted(MessageReceivedEvent event) {
        event.getChannel().sendMessage("👮 You do not have permission").queue();
        event.getMessage().addReaction(FAILURE_EMOJI).queue();
    }
}
