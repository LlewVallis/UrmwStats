package org.astropeci.urmwstats.command.commands.template;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.command.HelpSection;
import org.astropeci.urmwstats.template.Template;
import org.astropeci.urmwstats.template.TemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ListTemplatesCommand implements Command {

    private final TemplateRepository templateRepository;

    @Override
    public String label() {
        return "list-templates";
    }

    @Override
    public String usage() {
        return "list-templates";
    }

    @Override
    public String helpDescription() {
        return "List all saved templates";
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
        if (arguments.size() != 0) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        Map<String, Template> templates = templateRepository.all();
        if (templates.size() == 0) {
            event.getChannel().sendMessage("📑 No templates have been saved yet").queue();
        } else {
            StringBuilder builder = new StringBuilder("📑 Templates:");

            templates.forEach((name, template) -> {
                builder.append("\n        • `").append(name).append("`");
            });

            event.getChannel().sendMessage(builder).queue();
        }
    }
}
