package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.ResourceTable;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static org.junit.Assert.*;

public class ResourceDocumentTest extends DocumentTest {
    @Test
    public void basicSerializationWithId() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        ResourceDocument document = new ResourceDocument(id);

        String output = mapper.writeValueAsString(document);

        assertEquals("<asmResource id=\"" + id + "\"/>", output);
    }

    @Test
    public void basicSerialiationWithResourceEntity() throws JsonProcessingException {
        UUID resourceId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        Date date = new Calendar.Builder()
                        .setDate(2020, 10, 10)
                        .setTimeOfDay(10, 10, 10)
                        .setTimeZone(TimeZone.getTimeZone("UTC"))
                        .build()
                        .getTime();

        String rawXml = "<foo lang=\"fr\"></foo><bar></bar>";

        ResourceTable resource = new ResourceTable();
        Node node = new Node();

        resource.setId(resourceId);
        resource.setXsiType("foo");
        resource.setModifDate(date);
        resource.setContent(rawXml);

        node.setId(nodeId);

        ResourceDocument document = new ResourceDocument(resource, node);

        document.setLang("fr");
        document.setCode("bar");

        String output = mapper.writeValueAsString(document);

        assertContains("<asmResource id=\"" + resourceId + "\" ", output);
        assertContains("contextid=\"" + nodeId + "\" ", output);
        assertContains("xsi_type=\"foo\"", output);
        assertContains("last_modif=\"2020-11-10T10:10:10.000+00:00\">", output);

        assertContains("<lang>fr</lang>", output);
        assertContains("<code>bar</code>", output);

        assertContains("<content>" + rawXml + "</content>", output);
    }

    @Test
    public void deserialization() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        String xml = "<asmResource xsi_type=\"foo\" id=\"" + id + "\" " +
                            "contextid=\"" + nodeId + "\">" +
                        "<lang>fr</lang>" +
                        "<code>foo</code>" +
                        "<foo lang=\"fr\">v</foo>" +
                        "<bar></bar>" +
                "</asmResource>";

        ResourceDocument document = mapper.readerFor(ResourceDocument.class)
                                        .readValue(xml);

        assertEquals(id, document.getId());
        assertEquals(nodeId, document.getNodeId());

        assertEquals("foo", document.getXsiType());

        assertEquals("fr", document.getLang());
        assertEquals("foo", document.getCode());

        assertEquals("<foo lang=\"fr\">v</foo><bar></bar>", document.getContent());
    }
}
