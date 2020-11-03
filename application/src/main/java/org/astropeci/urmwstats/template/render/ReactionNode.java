package org.astropeci.urmwstats.template.render;

import emoji4j.EmojiUtils;
import org.astropeci.urmwstats.template.RenderContext;
import org.w3c.dom.Element;

public class ReactionNode extends AbstractTextContainerNode {

    public ReactionNode(Element element) {
        super(element, "reaction");
    }

    public String renderReaction(RenderContext ctx) {
        String contents = renderContents(ctx).trim();

        if (EmojiUtils.isEmoji(contents)) {
            return EmojiUtils.getEmoji(contents).getEmoji();
        } else {
            return null;
        }
    }

    @Override
    public String name() {
        return "reaction";
    }
}
