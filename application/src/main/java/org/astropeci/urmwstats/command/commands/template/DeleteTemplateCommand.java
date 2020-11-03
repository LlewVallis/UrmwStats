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
public class DeleteTemplateCommand implements Command {

    private final TemplateRepository templateRepository;

    @Override
    public String label() {
        return "delete-template";
    }

    @Override
    public String usage() {
        return "delete-template <name>";
    }

    @Override
    public String helpDescription() {
        return "Delete a template";
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
        boolean deleted = templateRepository.delete(name);

        if (deleted) {
            event.getChannel().sendMessage("🗑️ Deleted `" + name + "`").queue();
        } else {
            event.getChannel().sendMessage("🔍 There is no template named `" + name + "`").queue();
        }
    }
}
