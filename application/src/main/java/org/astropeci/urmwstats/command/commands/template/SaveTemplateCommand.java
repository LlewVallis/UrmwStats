package org.astropeci.urmwstats.command.commands.template;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.astropeci.urmwstats.template.TemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SaveTemplateCommand implements Command {

    private final TemplateRepository templateRepository;

    @Override
    public String label() {
        return "save-template";
    }

    @Override
    public String usage() {
        return "save-template <name>\u00A0<code-block>";
    }

    @Override
    public String helpDescription() {
        return "Compiles and saves a template or edits an existing one";
    }

    @Override
    public int helpPriority() {
        return 2;
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }

    @Override
    public void execute(List<String> arguments, MessageReceivedEvent event) {
        if (arguments.size() != 2) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        String name = CommandUtil.templateName(arguments.get(0));
        String source = arguments.get(1);

        if (!source.startsWith("```") || !source.endsWith("```") || source.length() < 6) {
            throw new CommandException("❌ The template must be wrapped in a code block");
        }

        source = source.substring(3, source.length() - 3);
        if (Pattern.compile("^xml\\s").matcher(source).find()) {
            source = source.substring(3);
        }

        try {
            templateRepository.save(name, source);
        } catch (TemplateCompileException e) {
            throw new CommandException("❌ Compilation error ```" + e.getMessage() + "```");
        }

        event.getChannel().sendMessage("💾 Compiled and saved `" + name + "`").queue();
    }
}
