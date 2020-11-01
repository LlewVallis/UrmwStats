package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.MessageBuilder;

public interface TopLevelNode extends RenderNode {

    void renderTopLevel(MessageBuilder message);
}
