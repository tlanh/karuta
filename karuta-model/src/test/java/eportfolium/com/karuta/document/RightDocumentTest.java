package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

public class RightDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        RightDocument document = new RightDocument(true, true, false, false);
        String output = mapper.writeValueAsString(document);

        assertContains("<right>", output);
        assertContains("<RD>true</RD>", output);
        assertContains("<WR>true</WR>", output);
        assertContains("<SB>false</SB>", output);
        assertContains("<DL>false</DL>", output);
        assertContains("</right>", output);
    }
}