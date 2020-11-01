package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.template.TemplateParseException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class MessageNode {

    private final List<TopLevelNode> children = new ArrayList<>();
    private final EmbedNode embed;

    public MessageNode(Document document) {
        EmbedNode embed = null;

        for (RenderNode node : RenderUtil.parsedChildren(document.getDocumentElement())) {
            if (node instanceof TopLevelNode) {
                children.add((TopLevelNode) node);
            } else if (node instanceof EmbedNode) {
                if (embed != null) {
                    throw new TemplateParseException("messages can only have one embed");
                }

                embed = (EmbedNode) node;
            } else {
                throw new TemplateParseException("\"" + node.name() + "\" is not valid as a top level node");
            }
        }

        this.embed = embed;
    }

    public Message render() {
        MessageBuilder message = new MessageBuilder();

        for (TopLevelNode child : children) {
            child.renderTopLevel(message);
        }

        if (embed != null) {
            message.setEmbed(embed.renderEmbed());
        }

        if (embed == null && message.getStringBuilder().toString().isBlank()) {
            message.setContent("`<empty>`");
        }

        if (message.length() > Message.MAX_CONTENT_LENGTH) {
            throw new TemplateRenderException("message is too large");
        }

        return message.build();
    }
}
