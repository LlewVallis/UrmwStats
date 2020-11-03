package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Element;

public class FooterNode extends AbstractTextContainerNode {

    private final String iconUrl;

    public FooterNode(Element element) {
        super(element, "embed footer");

        String iconUrl = element.getAttribute("iconUrl");
        if (iconUrl.isEmpty()) {
            this.iconUrl = null;
        } else {
            this.iconUrl = iconUrl;
        }

        RenderUtil.validateUrl(iconUrl);
    }

    public void renderFooter(EmbedBuilder embed, RenderContext ctx) {
        String footer = renderContents(ctx);

        if (footer.length() > MessageEmbed.TEXT_MAX_LENGTH) {
            throw new TemplateRenderException("footer is too large");
        }

        embed.setFooter(footer, iconUrl);
    }

    @Override
    public String name() {
        return "footer";
    }
}
