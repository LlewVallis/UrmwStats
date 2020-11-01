package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.MessageBuilder;

public interface TextNode extends TopLevelNode {

    String renderText();

    @Override
    default void renderTopLevel(MessageBuilder message) {
        message.getStringBuilder().append(renderText());
    }
}
