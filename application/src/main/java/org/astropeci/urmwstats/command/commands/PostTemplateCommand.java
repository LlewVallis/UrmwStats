package org.astropeci.urmwstats.command.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandException;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.template.Template;
import org.astropeci.urmwstats.template.TemplateParseException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class PostTemplateCommand implements Command {

    @Override
    public String label() {
        return "post-template";
    }

    @Override
    public String usage() {
        return "post-template <code-block>";
    }

    @Override
    public String helpDescription() {
        return "Compiles, renders and posts a template to the current channel";
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
        if (arguments.size() == 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        String source = String.join(" ", arguments);

        if (!source.startsWith("```") || !source.endsWith("```") || source.length() < 6) {
            throw new CommandException("❌ The template must be wrapped in a code block");
        }

        source = source.substring(3, source.length() - 3);
        if (Pattern.compile("^xml\\s").matcher(source).find()) {
            source = source.substring(3);
        }

        source = "<message>" + source + "</message>";

        Template template;
        try {
            template = Template.compile(source);
        } catch (TemplateParseException e) {
            throw new CommandException("❌ Compilation error ```" + e.getMessage() + "```");
        }

        Message message;
        try {
            message = template.render();
        } catch (TemplateRenderException e) {
            throw new CommandException("❌ Rendering error ```" + e.getMessage() + "```");
        }

        event.getChannel().sendMessage(message).queue();
    }
}
