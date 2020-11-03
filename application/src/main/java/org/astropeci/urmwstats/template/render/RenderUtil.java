package org.astropeci.urmwstats.template.render;

import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.template.RenderContext;
import org.astropeci.urmwstats.template.TemplateCompileException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class RenderUtil {

    private static final Pattern VARIABLE_TAG_NAME_PATTERN = Pattern.compile("var\\d+");

    public List<Node> children(Node parent) {
        List<Node> result = new ArrayList<>();
        NodeList childNodes = parent.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            result.add(childNodes.item(i));
        }

        return result;
    }

    public List<RenderNode> parsedChildren(Node parent) {
        return children(parent).stream()
                .map(RenderUtil::parse)
                .collect(Collectors.toList());
    }

    public List<RenderNode> parsedChildrenWithoutWhitespace(Node parent) {
        return parsedChildren(parent).stream()
                .filter(node -> !isWhitespace(node))
                .collect(Collectors.toList());
    }

    public boolean isWhitespace(RenderNode node) {
        return node instanceof LiteralNode && ((LiteralNode) node).isBlank();
    }

    public RenderNode parse(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            return new LiteralNode(node.getNodeValue());
        }

        if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.getTagName();

            if (VARIABLE_TAG_NAME_PATTERN.matcher(tagName).matches()) {
                String indexString = tagName.substring(3);

                int index;
                try {
                    index = Integer.parseInt(indexString) - 1;
                } catch (NumberFormatException e) {
                    throw new TemplateCompileException("variable index too large");
                }

                if (index == -1) {
                    throw new TemplateCompileException("variable indexes start at 1");
                }

                return new VariableNode(element, index);
            }

            switch (tagName) {
                case "br": return new LinebreakNode(element);
                case "code": return new CodeBlockNode(element);
                case "embed": return new EmbedNode(element);
                case "author": return new AuthorNode(element);
                case "title": return new TitleNode(element);
                case "description": return new DescriptionNode(element);
                case "footer": return new FooterNode(element);
                case "inline-field": return new FieldNode(element, true);
                case "field": return new FieldNode(element, false);
                case "name": return new FieldNameNode(element);
                case "value": return new FieldValueNode(element);
                case "reactions": return new ReactionsNode(element);
                case "reaction": return new ReactionNode(element);
            }
        }

        throw new TemplateCompileException("\"" + node.getNodeName() + "\" is not recognised");
    }

    public void validateUrl(String url) {
        if (url.isEmpty()) {
            return;
        }

        if (url.length() > MessageEmbed.URL_MAX_LENGTH) {
            throw new TemplateCompileException("URL is too large");
        }

        if (!EmbedBuilder.URL_PATTERN.matcher(url).matches()) {
            throw new TemplateCompileException("URL is malformed");
        }
    }
}
