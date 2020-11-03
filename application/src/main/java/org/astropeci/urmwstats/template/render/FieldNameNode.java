package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.RenderContext;
import org.w3c.dom.Element;

public class FieldNameNode extends AbstractTextContainerNode {

    public FieldNameNode(Element element) {
        super(element, "field name");
    }

    @Override
    public String renderContents(RenderContext ctx) {
        return super.renderContents(ctx);
    }

    @Override
    public String name() {
        return "name";
    }
}
