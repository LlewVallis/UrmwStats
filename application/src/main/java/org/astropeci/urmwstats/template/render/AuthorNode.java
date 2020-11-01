package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.template.TemplateParseException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Element;

public class AuthorNode extends AbstractTextContainerNode {

    private final String url;
    private final String iconUrl;

    public AuthorNode(Element element) {
        super(element, "embed author");

        String url = element.getAttribute("url");
        if (url.isEmpty()) {
            this.url = null;
        } else {
            this.url = url;
        }

        String iconUrl = element.getAttribute("iconUrl");
        if (iconUrl.isEmpty()) {
            this.iconUrl = null;
        } else {
            this.iconUrl = iconUrl;
        }

        RenderUtil.validateUrl(url);
        RenderUtil.validateUrl(iconUrl);
    }

    public void renderAuthor(EmbedBuilder embed) {
        String author = renderContents();

        if (author.length() > MessageEmbed.TITLE_MAX_LENGTH) {
            throw new TemplateRenderException("author name is too large");
        }

        embed.setAuthor(author, url, iconUrl);
    }

    @Override
    public String name() {
        return "author";
    }
}
