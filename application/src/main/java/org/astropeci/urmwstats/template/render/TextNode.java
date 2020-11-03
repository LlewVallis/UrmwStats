package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.MessageBuilder;
import org.astropeci.urmwstats.template.RenderContext;

public interface TextNode extends TopLevelNode {

    String renderText(RenderContext ctx);

    @Override
    default void renderTopLevel(MessageBuilder message, RenderContext ctx) {
        message.getStringBuilder().append(renderText(ctx));
    }
}
