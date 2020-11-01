package org.astropeci.urmwstats.template.render;

import org.w3c.dom.Element;

public class FieldValueNode extends AbstractTextContainerNode {

    public FieldValueNode(Element element) {
        super(element, "field value");
    }

    @Override
    public String renderContents() {
        return super.renderContents();
    }

    @Override
    public String name() {
        return "value";
    }
}
