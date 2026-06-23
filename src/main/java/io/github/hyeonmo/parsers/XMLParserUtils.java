package io.github.hyeonmo.parsers;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

public class XMLParserUtils {

    private static final ThreadLocal<DocumentBuilderFactory> docBuilderFactory = ThreadLocal.withInitial(() -> {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        hardenAgainstXxe(factory);
        return factory;
    });

    /**
     * Hardens a {@link DocumentBuilderFactory} against XML External Entity (XXE)
     * attacks. ONVIF responses come from remote devices (and potentially
     * attackers on the network), so untrusted XML must never resolve external
     * entities, DTDs, or XInclude references.
     *
     * @param factory the factory to secure; returned for convenience
     * @return the same factory, configured securely
     */
    public static DocumentBuilderFactory hardenAgainstXxe(DocumentBuilderFactory factory) {
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            // Feature not supported by this parser; fall back to the attribute-based hardening below.
        }
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

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
