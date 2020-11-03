package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractTextContainerNode implements RenderNode {

    private final List<TextNode> children = new ArrayList<>();

    public AbstractTextContainerNode(Element element, String description) {
        for (RenderNode node : RenderUtil.parsedChildren(element)) {
            if (!(node instanceof TextNode)) {
                throw new TemplateCompileException("\"" + node.name() + "\" is not valid inside a " + description);
            }

            children.add((TextNode) node);
        }
    }

    protected String renderContents(RenderContext ctx) {
        return children.stream().map(node -> node.renderText(ctx)).collect(Collectors.joining());
    }
}
