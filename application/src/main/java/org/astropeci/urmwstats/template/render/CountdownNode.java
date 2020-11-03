package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.TimeUtil;
import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.w3c.dom.Element;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CountdownNode implements TextNode {

    private final List<TextNode> before;
    private final List<TextNode> after;
    private final UntilNode until;
    private final ReplacementNode replacement;

    public CountdownNode(Element element) {
        List<TextNode> before = new ArrayList<>();
        List<TextNode> after = new ArrayList<>();
        UntilNode until = null;
        ReplacementNode replacement = null;

        for (RenderNode node : RenderUtil.parsedChildren(element)) {
            if (node instanceof UntilNode) {
                if (until != null) {
                    throw new TemplateCompileException("countdowns can only have one until node");
                }

                until = (UntilNode) node;
            } else if (node instanceof ReplacementNode) {
                if (replacement != null) {
                    throw new TemplateCompileException("countdowns can only have one replacement node");
                }

                replacement = (ReplacementNode) node;
            } else if (node instanceof TextNode) {
                if (until == null) {
                    before.add((TextNode) node);
                } else {
                    after.add((TextNode) node);
                }
            } else {
                throw new TemplateCompileException("\"" + node.name() + "\" is not valid inside a countdown");
            }
        }

        if (until == null) {
            throw new TemplateCompileException("countdowns must have an until node");
        }

        if (replacement == null) {
            throw new TemplateCompileException("countdowns must have a replacement node");
        }

        this.before = before;
        this.after = after;
        this.until = until;
        this.replacement = replacement;
    }

    @Override
    public String renderText(RenderContext ctx) {
        Instant targetTime = until.renderTime(ctx);

        Duration duration = Duration.between(ctx.getNow(), targetTime);

        if (duration.toMinutes() < 1) {
            return replacement.renderContents(ctx);
        } else {
            StringBuilder result = new StringBuilder();

            for (TextNode node : before) {
                result.append(node.renderText(ctx));
            }

            result.append(TimeUtil.durationString(duration));

            for (TextNode node : after) {
                result.append(node.renderText(ctx));
            }

            return result.toString();
        }
    }

    @Override
    public String name() {
        return "countdown";
    }
}
