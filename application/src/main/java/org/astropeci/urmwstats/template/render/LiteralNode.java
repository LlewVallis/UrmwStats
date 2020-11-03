package org.astropeci.urmwstats.template.render;

import lombok.RequiredArgsConstructor;
import org.astropeci.urmwstats.template.RenderContext;

@RequiredArgsConstructor
public class LiteralNode implements TextNode {

    private final String content;

    @Override
    public String renderText(RenderContext ctx) {
        return content;
    }

    @Override
    public String name() {
        return "text";
    }

    public boolean isBlank() {
        return content.isBlank();
    }
}
