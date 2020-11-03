package org.astropeci.urmwstats.template.render;


import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReactionsNode implements RenderNode {

    private final List<ReactionNode> reactions;

    public ReactionsNode(Element element) {
        List<ReactionNode> reactions = new ArrayList<>();

        for (RenderNode node : RenderUtil.parsedChildrenWithoutWhitespace(element)) {
            if (node instanceof ReactionNode) {
                reactions.add((ReactionNode) node);
            } else {
                throw new TemplateCompileException("\"" + node.name() + "\" is not valid inside reaction sets");
            }
        }

        this.reactions = reactions;
    }

    public List<String> renderReactions(RenderContext ctx) {
        return reactions.stream()
                .flatMap(node -> Optional.ofNullable(node.renderReaction(ctx)).stream())
                .collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "reactions";
    }
}
