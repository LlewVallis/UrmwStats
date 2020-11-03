package org.astropeci.urmwstats.template.render;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.astropeci.urmwstats.template.TemplateRenderException;
import org.w3c.dom.Element;

public class FieldNode implements RenderNode {

    private final FieldNameNode name;
    private final FieldValueNode value;
    private final boolean inline;

    public FieldNode(Element element, boolean inline) {
        FieldNameNode name = null;
        FieldValueNode value = null;

        for (RenderNode node : RenderUtil.parsedChildrenWithoutWhitespace(element)) {
            if (node instanceof FieldNameNode) {
                if (name != null) {
                    throw new TemplateCompileException("embed fields can only have one name");
                }

                name = (FieldNameNode) node;
            }

            if (node instanceof FieldValueNode) {
                if (value != null) {
                    throw new TemplateCompileException("embed fields can only have one value");
                }

                value = (FieldValueNode) node;
            }
        }

        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    public MessageEmbed.Field renderField(RenderContext ctx) {
        String name = "";
        if (this.name != null) {
            name = this.name.renderContents(ctx);
        }

        String value = "";
        if (this.value != null) {
            value = this.value.renderContents(ctx);
        }

        if (name.length() > MessageEmbed.TITLE_MAX_LENGTH) {
            throw new TemplateRenderException("field name is too large");
        }

        if (value.length() > MessageEmbed.VALUE_MAX_LENGTH) {
            throw new TemplateRenderException("field value is too large");
        }

        return new MessageEmbed.Field(name, value, inline);
    }

    @Override
    public String name() {
        return "field";
    }
}
