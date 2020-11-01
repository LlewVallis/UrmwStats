package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.TemplateParseException;
import org.w3c.dom.Element;

public class LinebreakNode implements TextNode {

    public LinebreakNode(Element element) {
        if (element.getChildNodes().getLength() > 0) {
            throw new TemplateParseException("linebreak elements cannot have children");
        }
    }

    @Override
    public String renderText() {
        return "\n";
    }

    @Override
    public String name() {
        return "linebreak";
    }
}
