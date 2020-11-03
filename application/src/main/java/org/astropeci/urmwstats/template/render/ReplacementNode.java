package org.astropeci.urmwstats.template.render;

import org.w3c.dom.Element;

public class ReplacementNode extends AbstractTextContainerNode {

    public ReplacementNode(Element element) {
        super(element, "countdown replacement");
    }

    @Override
    public String name() {
        return "replacement";
    }
}
