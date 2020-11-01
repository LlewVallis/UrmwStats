package org.astropeci.urmwstats.template.render;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.astropeci.urmwstats.template.TemplateParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@UtilityClass
public class RenderUtil {

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
        return node instanceof LiteralNode && ((LiteralNode) node).renderText().isBlank();
    }

    public RenderNode parse(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            return new LiteralNode(node.getNodeValue());
        }

        if (node instanceof Element) {
            Element element = (Element) node;

            switch (element.getTagName()) {
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
            }
        }

        throw new TemplateParseException("\"" + node.getNodeName() + "\" is not recognised");
    }

    public void validateUrl(String url) {
        if (url.length() > MessageEmbed.URL_MAX_LENGTH) {
            throw new TemplateParseException("URL is too large");
        }

        if (!EmbedBuilder.URL_PATTERN.matcher(url).matches()) {
            throw new TemplateParseException("URL is malformed");
        }
    }

    public Instant parseDate(String input) {
        Parser parser = new Parser(TimeZone.getTimeZone("GMT"));
        List<DateGroup> groups = parser.parse(input);

        if (groups.size() != 1) {
            return null;
        }

        DateGroup group = groups.get(0);

        if (group.getDates().size() != 1) {
            return null;
        }

        if (group.isRecurring()) {
            return null;
        }

        Date result = group.getDates().get(0);
        return result.toInstant();
    }
}
