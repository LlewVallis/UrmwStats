package org.astropeci.urmwstats.template.render;

import org.w3c.dom.Element;

public class FieldNameNode extends AbstractTextContainerNode {

    public FieldNameNode(Element element) {
        super(element, "field name");
    }

    @Override
    public String renderContents() {
        return super.renderContents();
    }

    @Override
    public String name() {
        return "name";
    }
}
