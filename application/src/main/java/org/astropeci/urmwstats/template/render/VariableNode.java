package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.RenderContext;
import org.w3c.dom.Element;

import java.util.List;

public class VariableNode extends AbstractTextContainerNode implements TextNode {

    private final int index;

    public VariableNode(Element element, int index) {
        super(element, "variable");
        this.index = index;
    }

    @Override
    public String renderText(RenderContext ctx) {
        List<String> variables = ctx.getVariables();
        if (index < variables.size()) {
            return variables.get(index);
        } else {
            return renderContents(ctx);
        }
    }

    @Override
    public String name() {
        return "variable" + (index + 1);
    }
}
