package org.astropeci.urmwstats.template.render;

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
    public String renderText() {
        return String.format(
                "```%s\n%s```",
                language == null ? "" : language,
                renderContents()
        );
    }

    @Override
    public String name() {
        return "code block";
    }
}
