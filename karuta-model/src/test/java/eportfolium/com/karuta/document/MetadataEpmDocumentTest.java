package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetadataEpmDocumentTest extends DocumentTest {
    @Test
    public void from() throws JsonProcessingException {
        String xml = "public=\"true\" sharedResource=\"true\" " +
                "sharedNode=\"true\" sharedNodeResource=\"true\" semantictag=\"foo\"";

        MetadataEpmDocument document = MetadataEpmDocument.from(xml);

        assertTrue(document.getPublic());
        assertTrue(document.getSharedResource());
        assertTrue(document.getSharedNode());
        assertTrue(document.getSharedNodeResource());

        assertEquals("foo", document.getSemantictag());
    }
}