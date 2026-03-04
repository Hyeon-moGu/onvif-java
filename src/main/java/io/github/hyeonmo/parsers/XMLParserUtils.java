package io.github.hyeonmo.parsers;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

public class XMLParserUtils {

    private static final ThreadLocal<DocumentBuilderFactory> docBuilderFactory = ThreadLocal.withInitial(() -> {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    });

    private static final ThreadLocal<DocumentBuilder> documentBuilder = ThreadLocal.withInitial(() -> {
        try {
            return docBuilderFactory.get().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error initializing DocumentBuilder", e);
        }
    });

    private static final ThreadLocal<XPathFactory> xpathFactory = ThreadLocal.withInitial(XPathFactory::newInstance);

    private static final ThreadLocal<XPath> xpath = ThreadLocal.withInitial(() -> xpathFactory.get().newXPath());

    public static DocumentBuilder getDocumentBuilder() {
        return documentBuilder.get();
    }

    public static XPath getXPath() {
        return xpath.get();
    }
}
