package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.RenderContext;
import org.w3c.dom.Element;

public class FieldValueNode extends AbstractTextContainerNode {

    public FieldValueNode(Element element) {
        super(element, "field value");
    }

    @Override
    public String renderContents(RenderContext ctx) {
        return super.renderContents(ctx);
    }

    @Override
    public String name() {
        return "value";
    }
}
