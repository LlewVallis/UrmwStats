package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.RichMessage;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class MessageNode {

    private final List<TopLevelNode> children = new ArrayList<>();
    private final EmbedNode embed;
    private final ReactionsNode reactions;

    public MessageNode(Document document) {
        EmbedNode embed = null;
        ReactionsNode reactions = null;

        for (RenderNode node : RenderUtil.parsedChildren(document.getDocumentElement())) {
            if (node instanceof TopLevelNode) {
                children.add((TopLevelNode) node);
            } else if (node instanceof EmbedNode) {
                if (embed != null) {
                    throw new TemplateCompileException("messages can only have one embed");
                }

                embed = (EmbedNode) node;
            } else if (node instanceof ReactionsNode) {
                if (reactions != null) {
                    throw new TemplateCompileException("messages can only have one set of reactions");
                }

                reactions = (ReactionsNode) node;
            } else {
                throw new TemplateCompileException("\"" + node.name() + "\" is not valid as a top level node");
            }
        }

        this.embed = embed;
        this.reactions = reactions;
    }

    public RichMessage render(RenderContext ctx) {
        MessageBuilder message = new MessageBuilder();

        for (TopLevelNode child : children) {
            child.renderTopLevel(message, ctx);
        }

        if (embed != null) {
            message.setEmbed(embed.renderEmbed(ctx));
        }

        if (embed == null && message.getStringBuilder().toString().isBlank()) {
            message.setContent("`<empty>`");
        }

        if (message.length() > Message.MAX_CONTENT_LENGTH) {
            throw new TemplateRenderException("message is too large");
        }

        List<String> reactionList = new ArrayList<>();
        if (reactions != null) {
            reactionList = reactions.renderReactions(ctx);
        }

        return new RichMessage(message.build(), reactionList);
    }
}
