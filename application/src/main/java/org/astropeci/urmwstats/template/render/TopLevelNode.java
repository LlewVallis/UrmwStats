package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.MessageBuilder;
import org.astropeci.urmwstats.template.RenderContext;

public interface TopLevelNode extends RenderNode {

    void renderTopLevel(MessageBuilder message, RenderContext ctx);
}
