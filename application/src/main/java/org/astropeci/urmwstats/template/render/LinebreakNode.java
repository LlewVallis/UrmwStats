package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.w3c.dom.Element;

public class LinebreakNode implements TextNode {

    public LinebreakNode(Element element) {
        if (element.getChildNodes().getLength() > 0) {
            throw new TemplateCompileException("linebreak elements cannot have children");
        }
    }

    @Override
    public String renderText(RenderContext ctx) {
        return "\n";
    }

    @Override
    public String name() {
        return "linebreak";
    }
}
