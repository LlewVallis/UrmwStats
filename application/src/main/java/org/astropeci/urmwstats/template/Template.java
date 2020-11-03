package org.astropeci.urmwstats.template;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Message;
import org.astropeci.urmwstats.template.render.MessageNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Template {

    private final MessageNode messageNode;

    @SneakyThrows({ IOException.class, ParserConfigurationException.class })
    public static Template compile(String source) {
        source = "<message>" + source + "</message>";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setCoalescing(true);
        factory.setIgnoringComments(true);

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document;
        try {
            document = builder.parse(new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8)));
        } catch (SAXException e) {
            throw new TemplateCompileException("malformed XML syntax");
        }

        return new Template(new MessageNode(document));
    }

    public RichMessage render(RenderContext ctx) {
        return messageNode.render(ctx);
    }
}
