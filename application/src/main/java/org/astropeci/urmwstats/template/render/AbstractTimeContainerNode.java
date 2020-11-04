package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.TimeUtil;
import org.astropeci.urmwstats.template.RenderContext;
import org.w3c.dom.Element;

import java.time.Instant;

public abstract class AbstractTimeContainerNode extends AbstractTextContainerNode {

    public AbstractTimeContainerNode(Element element, String description) {
        super(element, description);
    }

    public Instant renderTime(RenderContext ctx) {
        String content = renderContents(ctx);
        Instant time = TimeUtil.parseDate(content, ctx.getBaselineTime());
        if (time == null) {
            time = ctx.getNow();
        }

        return time;
    }
}
