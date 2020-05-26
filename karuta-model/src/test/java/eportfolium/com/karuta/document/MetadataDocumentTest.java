package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetadataDocumentTest extends DocumentTest {
    @Test
    public void from() throws JsonProcessingException {
        String xml = "public=\"true\" sharedResource=\"true\" " +
                "sharedNode=\"true\" sharedNodeResource=\"true\" semantictag=\"foo\"";

        MetadataDocument document = MetadataDocument.from(xml);

        assertTrue(document.getPublic());
        assertTrue(document.getSharedResource());
        assertTrue(document.getSharedNode());
        assertTrue(document.getSharedNodeResource());

        assertEquals("foo", document.getSemantictag());
    }

    @Test
    public void settingPublicReflectPrivate() {
        MetadataDocument document = new MetadataDocument();

        document.setPublic(true);

        assertTrue(document.getPublic());
        assertFalse(document.getPrivate());

        document.setPublic(false);

        assertFalse(document.getPublic());
        assertTrue(document.getPrivate());
    }

    @Test
    public void settingPrivateReflectPublic() {
        MetadataDocument document = new MetadataDocument();

        document.setPrivate(true);

        assertTrue(document.getPrivate());
        assertFalse(document.getPublic());

        document.setPrivate(false);

        assertFalse(document.getPrivate());
        assertTrue(document.getPublic());
    }
}