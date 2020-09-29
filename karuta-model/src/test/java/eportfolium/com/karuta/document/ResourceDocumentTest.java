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
        String code = "<code>bar</code>";

        document.setCode("bar");

        String output = mapper.writeValueAsString(document);

        assertContains("<asmResource id=\"" + resourceId + "\" ", output);
        assertContains("contextid=\"" + nodeId + "\" ", output);
        assertContains("xsi_type=\"foo\"", output);
        assertContains("last_modif=\"1605003010000\">", output);

        assertContains(code, output);

        assertContains("<content>" + rawXml + code + "</content>", output);
    }

    @Test
    public void deserialization() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        String filename1 = "<filename lang=\"fr\">foo.png</filename>";
        String filename2 = "<filename lang=\"en\">bar.png</filename>";

        String fileid1 = "<fileid lang=\"fr\">foo</fileid>";
        String fileid2 = "<fileid lang=\"en\">bar</fileid>";

        String type1 = "<type lang=\"fr\">image/png</type>";
        String type2 = "<type lang=\"en\">image/png</type>";

        String xml = "<asmResource xsi_type=\"foo\" id=\"" + id + "\" " +
                            "contextid=\"" + nodeId + "\">" +
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
        assertEquals("foo", document.getCode());

        assertContains("<foo lang=\"fr\">v</foo><bar></bar>", document.getContent());

        assertEquals(2, document.getFilename().size());

        assertEquals("fr", document.getFilename().get(0).getLang());
        assertEquals("foo.png", document.getFilename().get(0).getValue());

        assertEquals("en", document.getFilename().get(1).getLang());
        assertEquals("bar.png", document.getFilename().get(1).getValue());

        assertEquals(2, document.getFileid().size());

        assertEquals("fr", document.getFileid().get(0).getLang());
        assertEquals("foo", document.getFileid().get(0).getValue());

        assertEquals("en", document.getFileid().get(1).getLang());
        assertEquals("bar", document.getFileid().get(1).getValue());

        assertEquals(2, document.getType().size());

        assertEquals("fr", document.getType().get(0).getLang());
        assertEquals("image/png", document.getType().get(0).getValue());

        assertEquals("en", document.getType().get(1).getLang());
        assertEquals("image/png", document.getType().get(1).getValue());

        Arrays.asList(filename1, filename2, fileid1, fileid2, type1, type2).forEach(field -> {
            assertContains(field, document.getContent());
        });
    }

    @Test
    public void getLocaleSpecificFields() {
        ResourceDocument.FileidTag fileidTag1 = new ResourceDocument.FileidTag("fr", "foo");
        ResourceDocument.FileidTag fileidTag2 = new ResourceDocument.FileidTag("en", "bar");

        ResourceDocument.TypeTag typeTag1 = new ResourceDocument.TypeTag("fr", "foo");
        ResourceDocument.TypeTag typeTag2 = new ResourceDocument.TypeTag("en", "bar");

        ResourceDocument.FilenameTag nameTag1 = new ResourceDocument.FilenameTag("fr", "foo");
        ResourceDocument.FilenameTag nameTag2 = new ResourceDocument.FilenameTag("en", "bar");

        ResourceDocument resourceDocument = new ResourceDocument();
        resourceDocument.setFileid(Arrays.asList(fileidTag1, fileidTag2));
        resourceDocument.setType(Arrays.asList(typeTag1, typeTag2));
        resourceDocument.setFilename(Arrays.asList(nameTag1, nameTag2));

        assertEquals("foo", resourceDocument.getFilename("fr"));
        assertEquals("bar", resourceDocument.getFilename("en"));

        assertEquals("foo", resourceDocument.getType("fr"));
        assertEquals("bar", resourceDocument.getType("en"));

        assertEquals("foo", resourceDocument.getFileid("fr"));
        assertEquals("bar", resourceDocument.getFileid("en"));
    }

    @Test
    public void constructorLoadsContent() {
        String content = "<filename lang=\"fr\">foo</filename>" +
                "<fileid lang=\"fr\">bar</fileid>" +
                "<type lang=\"fr\">baz</type>";

        Node node = new Node();
        Resource resource = new Resource();
        resource.setContent(content);

        ResourceDocument resourceDocument = new ResourceDocument(resource, node);

        assertEquals("foo", resourceDocument.getFilename("fr"));
        assertEquals("bar", resourceDocument.getFileid("fr"));
        assertEquals("baz", resourceDocument.getType("fr"));
    }
}
