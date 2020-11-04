package org.astropeci.urmwstats.command.commands.template;

import net.dv8tion.jda.api.JDA;
import org.astropeci.urmwstats.template.LiveTemplateRepository;
import org.astropeci.urmwstats.template.TemplateRepository;
import org.springframework.stereotype.Component;

@Component
public class LiveTemplateCommand extends AbstractTemplateCommand {

    public LiveTemplateCommand(JDA jda, TemplateRepository templateRepository, LiveTemplateRepository liveTemplateRepository) {
        super(jda, templateRepository, liveTemplateRepository, true);
    }

    @Override
    public String label() {
        return "live-template";
    }

    @Override
    public String usage() {
        return "live-template [channel|message] <name> [variables...]";
    }

    @Override
    public String helpDescription() {
        return "Similar to the template command, except it creates live templates. Live templates update every minute if necessary to refresh time dependent elements";
    }
}
