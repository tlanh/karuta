package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoginDocumentTest extends DocumentTest {
    @Test
    public void deserialization() throws JsonProcessingException {
        String xml = "<credential>" +
                    "<login>johndoe</login>" +
                    "<password>s3cr3t</password>" +
                "</credential>";

        LoginDocument document = mapper.readerFor(LoginDocument.class)
                                    .readValue(xml);

        assertEquals("johndoe", document.getLogin());
        assertEquals("s3cr3t", document.getPassword());
        assertNull(document.getSubstitute());
    }

    @Test
    public void deserializationWithSubstitute() throws JsonProcessingException {
        String xml = "<credential>" +
                    "<login>johndoe#johnny</login>" +
                    "<password>s3cr3t</password>" +
                "</credential>";

        LoginDocument document = mapper.readerFor(LoginDocument.class)
                                    .readValue(xml);

        assertEquals("johndoe", document.getLogin());
        assertEquals("johnny", document.getSubstitute());
        assertEquals("s3cr3t", document.getPassword());
    }
}