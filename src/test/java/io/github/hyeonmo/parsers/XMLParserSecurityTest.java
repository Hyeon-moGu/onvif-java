package io.github.hyeonmo.parsers;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class XMLParserSecurityTest {
    @Test
    public void doctypePayloadsAreRejectedBeforeAnyExternalResolution() throws Exception {
        String[] declarations = {
                "<!DOCTYPE r [<!ENTITY e SYSTEM 'file:///onvif-test-do-not-read'>]>",
                "<!DOCTYPE r SYSTEM 'file:///onvif-test-do-not-read'>",
                "<!DOCTYPE r [<!ENTITY % e SYSTEM 'file:///onvif-test-do-not-read'>%e;]>",
                "<!DOCTYPE r [<!ENTITY e 'expanded-content'>]>"
        };
        for (String declaration : declarations) {
            AtomicInteger resolutions = new AtomicInteger();
            DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> {
                resolutions.incrementAndGet();
                throw new SAXException("External entity resolution must not occur");
            });
            try {
                builder.parse(new InputSource(new StringReader(declaration + "<r/>")));
                fail("DOCTYPE must be rejected");
            } catch (SAXException expected) {
                assertEquals(0, resolutions.get());
            } finally {
                builder.setEntityResolver(null);
            }
        }
    }

    @Test
    public void hardeningDisablesXIncludeAndEntityExpansion() {
        DocumentBuilderFactory factory = XMLParserUtils.hardenAgainstXxe(DocumentBuilderFactory.newInstance());
        assertFalse(factory.isXIncludeAware());
        assertFalse(factory.isExpandEntityReferences());
    }

    @Test
    public void reusedBuilderRecoversAfterMalformedXmlWithoutLosingNamespaces() throws Exception {
        DocumentBuilder builder = XMLParserUtils.getDocumentBuilder();
        try {
            builder.parse(new InputSource(new StringReader("<broken>")));
            fail("Malformed XML must fail");
        } catch (SAXException expected) {
            // The next response on this thread must still be parsable.
        }
        assertEquals("urn:test", builder.parse(new InputSource(new StringReader("<v:r xmlns:v='urn:test'/>")))
                .getDocumentElement().getNamespaceURI());
    }
}
