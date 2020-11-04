package org.astropeci.urmwstats.command.commands.template;

import net.dv8tion.jda.api.JDA;
import org.astropeci.urmwstats.template.LiveTemplateRepository;
import org.astropeci.urmwstats.template.TemplateRepository;
import org.springframework.stereotype.Component;

@Component
public class TemplateCommand extends AbstractTemplateCommand {

    public TemplateCommand(JDA jda, TemplateRepository templateRepository, LiveTemplateRepository liveTemplateRepository) {
        super(jda, templateRepository, liveTemplateRepository, false);
    }

    @Override
    public String label() {
        return "template";
    }

    @Override
    public String usage() {
        return "template [channel|message] <name> [variables...]";
    }

    @Override
    public String helpDescription() {
        return "Sends a template to the specified or current channel, substituting the provided variables. If a message URL is provided the message will be edited with the template";
    }
}
