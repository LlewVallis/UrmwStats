package org.astropeci.urmwstats.template.render;

import org.astropeci.urmwstats.template.RenderContext;
import org.w3c.dom.Element;

public class CodeBlockNode extends AbstractTextContainerNode implements TextNode {

    private final String language;

    public CodeBlockNode(Element element) {
        super(element, "code block");

        String language = element.getAttribute("lang");
        if (language.isEmpty()) {
            this.language = null;
        } else {
            this.language = language;
        }
    }

    @Override
    public String renderText(RenderContext ctx) {
        return String.format(
                "```%s\n%s\n```",
                language == null ? "" : language,
                renderContents(ctx)
        );
    }

    @Override
    public String name() {
        return "code block";
    }
}
