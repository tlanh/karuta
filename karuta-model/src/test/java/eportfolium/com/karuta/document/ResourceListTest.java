package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.*;

public class ResourceListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        UUID resourceId = UUID.randomUUID();
        ResourceDocument resource = new ResourceDocument(resourceId);

        ResourceList list = new ResourceList(Collections.singletonList(resource));
        String output = mapper.writeValueAsString(list);

        assertEquals("<resources><resource id=\"" + resourceId +"\"/></resources>",
                output);
    }
}