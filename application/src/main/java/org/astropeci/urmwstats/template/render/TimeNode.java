package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.RenderContext;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeNode extends AbstractTimeContainerNode implements TextNode {

    public TimeNode(Element element) {
        super(element, "time node");
    }

    @Override
    public String renderText(RenderContext ctx) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'UTC' MMM d");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(Date.from(renderTime(ctx)));
    }

    @Override
    public String name() {
        return "time";
    }
}
