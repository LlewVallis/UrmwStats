package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.template.TemplateParseException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Element;

public class TitleNode extends AbstractTextContainerNode {

    private final String url;

    public TitleNode(Element element) {
        super(element, "embed title");

        String url = element.getAttribute("url");
        if (url.isEmpty()) {
            this.url = null;
        } else {
            this.url = url;
        }

        RenderUtil.validateUrl(url);
    }

    public void renderTitle(EmbedBuilder embed) {
        String title = renderContents();

        if (title.length() > MessageEmbed.TITLE_MAX_LENGTH) {
            throw new TemplateRenderException("title is too large");
        }

        embed.setTitle(title, url);
    }

    @Override
    public String name() {
        return "title";
    }
}
