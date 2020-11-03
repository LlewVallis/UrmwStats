package org.astropeci.urmwstats.command.commands.template;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.astropeci.urmwstats.command.Command;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.template.TemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspectTemplateCommand implements Command {

    private final TemplateRepository templateRepository;

    @Override
    public String label() {
        return "inspect-template";
    }

    @Override
    public String usage() {
        return "inspect-template <name>";
    }

    @Override
    public String helpDescription() {
        return "View a template's source code";
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
        if (arguments.size() != 1) {
            CommandUtil.throwWrongNumberOfArguments();
        }

        String name = CommandUtil.templateName(arguments.get(0));
        String source = templateRepository.getSource(name);

        if (source == null) {
            event.getChannel().sendMessage("🔍 There is no template named `" + name + "`").queue();
        } else {
            event.getChannel().sendMessageFormat("📖 Source code for `%s` ```xml\n%s\n```", name, source).queue();
        }
    }
}
