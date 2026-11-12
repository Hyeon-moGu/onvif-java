package io.github.hyeonmo.core;

import io.github.hyeonmo.parsers.XMLParserUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.junit.Assert.*;

public class AuthXMLBuilderTest {
    @Test
    public void passwordDigestMatchesNonceCreatedAndPasswordBytes() throws Exception {
        AuthXMLBuilder builder = new AuthXMLBuilder("user", "test-password", 3600000);
        String xml = builder.getAuthHeader() + "<Request/>" + builder.getAuthEnd();
        Document document = XMLParserUtils.getDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        String security = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
        String utility = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        byte[] nonce = Base64.getDecoder().decode(document.getElementsByTagNameNS(security, "Nonce").item(0).getTextContent());
        String created = document.getElementsByTagNameNS(utility, "Created").item(0).getTextContent();
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(nonce);
        digest.update(created.getBytes(StandardCharsets.UTF_8));
        digest.update("test-password".getBytes(StandardCharsets.UTF_8));
        assertEquals(16, nonce.length);
        assertEquals(Base64.getEncoder().encodeToString(digest.digest()),
                document.getElementsByTagNameNS(security, "Password").item(0).getTextContent());
        assertFalse(xml.contains("test-password"));
    }
}
