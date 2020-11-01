package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.TemplateParseException;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractTextContainerNode implements RenderNode {

    private final List<TextNode> children = new ArrayList<>();

    public AbstractTextContainerNode(Element element, String description) {
        for (RenderNode node : RenderUtil.parsedChildren(element)) {
            if (!(node instanceof TextNode)) {
                throw new TemplateParseException("\"" + node.name() + "\" is not valid inside a " + description);
            }

            children.add((TextNode) node);
        }
    }

    protected String renderContents() {
        return children.stream().map(TextNode::renderText).collect(Collectors.joining());
    }
}
