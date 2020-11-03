package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.TimeUtils;
import org.astropeci.urmwstats.command.CommandUtil;
import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Element;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EmbedNode implements RenderNode {

    private final List<FieldNode> fields = new ArrayList<>();
    private final AuthorNode author;
    private final TitleNode title;
    private final DescriptionNode description;
    private final FooterNode footer;
    private final Instant timestamp;
    private final Color color;
    private final String iconUrl;
    private final String imageUrl;

    public EmbedNode(Element element) {
        AuthorNode author = null;
        TitleNode title = null;
        DescriptionNode description = null;
        FooterNode footer = null;

        for (RenderNode node : RenderUtil.parsedChildrenWithoutWhitespace(element)) {
            if (node instanceof FieldNode) {
                if (fields.size() == 25) {
                    throw new TemplateCompileException("embeds can only have 25 fields");
                }

                fields.add((FieldNode) node);
            } else if (node instanceof AuthorNode) {
                if (author != null) {
                    throw new TemplateCompileException("embeds can only have one author");
                }

                author = (AuthorNode) node;
            } else if (node instanceof TitleNode) {
                if (title != null) {
                    throw new TemplateCompileException("embeds can only have one title");
                }

                title = (TitleNode) node;
            } else if (node instanceof DescriptionNode) {
                if (description != null) {
                    throw new TemplateCompileException("embeds can only have one description");
                }

                description = (DescriptionNode) node;
            } else if (node instanceof FooterNode) {
                if (footer != null){
                    throw new TemplateCompileException("embeds can only have one footer");
                }

                footer = (FooterNode) node;
            } else {
                throw new TemplateCompileException("\"" + node.name() + "\" is not valid inside an embed");
            }
        }

        String timestamp = element.getAttribute("timestamp");
        if (timestamp.isEmpty()) {
            this.timestamp = null;
        } else {
            this.timestamp = TimeUtils.parseDate(timestamp);
            if (this.timestamp == null) {
                throw new TemplateCompileException("could not decipher time \"" + timestamp + "\"");
            }
        }

        String color = element.getAttribute("color");
        if (color.isEmpty()) {
            this.color = null;
        } else {
            try {
                this.color = Color.decode(color);
            } catch (NumberFormatException e) {
                throw new TemplateCompileException("could not decipher hex color \"" + color + "\"");
            }
        }

        String iconUrl = element.getAttribute("iconUrl");
        if (iconUrl.isEmpty()) {
            this.iconUrl = null;
        } else {
            this.iconUrl = iconUrl;
        }

        String imageUrl = element.getAttribute("imageUrl");
        if (imageUrl.isEmpty()) {
            this.imageUrl = null;
        } else {
            this.imageUrl = imageUrl;
        }

        this.author = author;
        this.title = title;
        this.description = description;
        this.footer = footer;
    }

    public MessageEmbed renderEmbed(RenderContext ctx) {
        EmbedBuilder builder = CommandUtil.coloredEmbedBuilder();

        if (author != null) author.renderAuthor(builder, ctx);
        if (title != null) title.renderTitle(builder, ctx);
        if (description != null) description.renderDescription(builder, ctx);
        if (footer != null) footer.renderFooter(builder, ctx);
        if (timestamp != null) builder.setTimestamp(timestamp);
        if (color != null) builder.setColor(color);
        if (iconUrl != null) builder.setThumbnail(iconUrl);
        if (imageUrl != null) builder.setImage(imageUrl);

        fields.forEach(field -> builder.addField(field.renderField(ctx)));

        if (!builder.isValidLength()) {
            throw new TemplateRenderException("embed is too large");
        }

        if (builder.isEmpty()) {
            builder.addBlankField(false);
        }

        return builder.build();
    }

    @Override
    public String name() {
        return "embed";
    }
}
