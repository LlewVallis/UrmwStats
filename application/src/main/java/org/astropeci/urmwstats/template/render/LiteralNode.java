package org.astropeci.urmwstats.template.render;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LiteralNode implements TextNode {

    private final String content;

    @Override
    public String renderText() {
        return content;
    }

    @Override
    public String name() {
        return "text";
    }
}
