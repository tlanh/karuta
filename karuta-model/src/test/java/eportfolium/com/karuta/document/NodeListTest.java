package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

public class NodeListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        NodeDocument first = new NodeDocument(firstId);
        NodeDocument second = new NodeDocument(secondId);

        NodeList list = new NodeList(Arrays.asList(first, second));
        String output = mapper.writeValueAsString(list);

        assertContains("<nodes>", output);
        assertContains("<node id=\"" + firstId + "\"", output);
        assertContains("<node id=\"" + secondId + "\"", output);
    }
}