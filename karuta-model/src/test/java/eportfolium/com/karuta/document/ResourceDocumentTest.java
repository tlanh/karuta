package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Resource;
import org.junit.Test;

import java.util.*;

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

        Resource resource = new Resource();
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

        String filename1 = "<filename lang=\"fr\" value=\"foo.png\" />";
        String filename2 = "<filename lang=\"en\" value=\"bar.png\" />";

        String fileid1 = "<fileid lang=\"fr\" value=\"foo\" />";
        String fileid2 = "<fileid lang=\"en\" value=\"bar\" />";

        String type1 = "<type lang=\"fr\" value=\"image/png\" />";
        String type2 = "<type lang=\"en\" value=\"image/png\" />";

        String xml = "<asmResource xsi_type=\"foo\" id=\"" + id + "\" " +
                            "contextid=\"" + nodeId + "\">" +
                        "<lang>fr</lang>" +
                        "<code>foo</code>" +
                        "<foo lang=\"fr\">v</foo>" +
                        "<bar></bar>" +

                        filename1 +
                        filename2 +

                        fileid1 +
                        fileid2 +

                        type1 +
                        type2 +
                "</asmResource>";

        ResourceDocument document = mapper.readerFor(ResourceDocument.class)
                                        .readValue(xml);

        assertEquals(id, document.getId());
        assertEquals(nodeId, document.getNodeId());

        assertEquals("foo", document.getXsiType());

        assertEquals("fr", document.getLang());
        assertEquals("foo", document.getCode());

        assertContains("<foo lang=\"fr\">v</foo><bar></bar>", document.getContent());

        final Set<String> keys = new HashSet<>(Arrays.asList("lang", "value"));

        assertEquals(2, document.getFilename().size());
        assertEquals(keys, document.getFilename().get(0).keySet());
        assertEquals(keys, document.getFilename().get(1).keySet());

        assertEquals(2, document.getFileid().size());
        assertEquals(keys, document.getFileid().get(0).keySet());
        assertEquals(keys, document.getFileid().get(1).keySet());

        assertEquals(2, document.getType().size());
        assertEquals(keys, document.getType().get(0).keySet());
        assertEquals(keys, document.getType().get(1).keySet());

        Arrays.asList(filename1, filename2, fileid1, fileid2, type1, type2).forEach(field -> {
            assertContains(field, document.getContent());
        });
    }

    @Test
    public void getLocaleSpecificFields() {
        Map<String, String> map1 = new HashMap<String, String>() {{
           put("lang", "fr");
           put("value", "foo");
        }};

        Map<String, String> map2 = new HashMap<String, String>() {{
            put("lang", "en");
            put("value", "bar");
        }};

        List<Map<String, String>> values = Arrays.asList(map1, map2);

        ResourceDocument resourceDocument = new ResourceDocument();
        resourceDocument.setFileid(values);
        resourceDocument.setType(values);
        resourceDocument.setFilename(values);

        assertEquals("foo", resourceDocument.getFilename("fr"));
        assertEquals("bar", resourceDocument.getFilename("en"));

        assertEquals("foo", resourceDocument.getType("fr"));
        assertEquals("bar", resourceDocument.getType("en"));

        assertEquals("foo", resourceDocument.getFileid("fr"));
        assertEquals("bar", resourceDocument.getFileid("en"));
    }

    @Test
    public void constructorLoadsContent() {
        String content = "<filename lang=\"fr\" value=\"foo\" />" +
                "<fileid lang=\"fr\" value=\"bar\" />" +
                "<type lang=\"fr\" value=\"baz\" />";

        Node node = new Node();
        Resource resource = new Resource();
        resource.setContent(content);

        ResourceDocument resourceDocument = new ResourceDocument(resource, node);

        assertEquals("foo", resourceDocument.getFilename("fr"));
        assertEquals("bar", resourceDocument.getFileid("fr"));
        assertEquals("baz", resourceDocument.getType("fr"));
    }
}
