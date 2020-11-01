package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.template.TemplateParseException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Element;

public class DescriptionNode extends AbstractTextContainerNode {

    public DescriptionNode(Element element) {
        super(element, "embed description");
    }

    public void renderDescription(EmbedBuilder embed) {
        String description = renderContents();

        if (description.length() > MessageEmbed.TEXT_MAX_LENGTH) {
            throw new TemplateRenderException("title is too large");
        }

        embed.setDescription(description);
    }

    @Override
    public String name() {
        return "description";
    }
}
