package org.astropeci.urmwstats.template.render;

import org.w3c.dom.Element;

public class UntilNode extends AbstractTimeContainerNode {

    public UntilNode(Element element) {
        super(element, "until node");
    }

    @Override
    public String name() {
        return "until";
    }
}
