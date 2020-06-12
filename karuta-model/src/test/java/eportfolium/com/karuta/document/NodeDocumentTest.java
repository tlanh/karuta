package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Resource;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class NodeDocumentTest extends DocumentTest {
    @Test
    public void constructorWithUUIDSetsEmptyLists() {
        UUID id = UUID.randomUUID();
        NodeDocument document = new NodeDocument(id);

        assertEquals(id, document.getId());

        assertNotNull(document.getResources());
        assertNotNull(document.getChildren());

        assertEquals(0, document.getResources().size());
        assertEquals(0, document.getChildren().size());
    }

    @Test
    public void setChildrenSetChildParent() {
        UUID random = UUID.randomUUID();

        NodeDocument parent = new NodeDocument(random);

        NodeDocument child1 = new NodeDocument(random);
        NodeDocument child2 = new NodeDocument(random);

        parent.setChildren(Arrays.asList(child1, child2));

        assertNull(parent.getParent());
        assertEquals(2, parent.getChildren().size());

        assertEquals(parent, child1.getParent());
        assertEquals(parent, child2.getParent());
    }

    @Test
    public void basicSerialization() throws JsonProcessingException {
        UUID id = UUID.randomUUID();

        Node node = new Node();
        Date date = new Calendar.Builder()
                        .setDate(2020, 11, 10)
                        .setTimeOfDay(10, 10, 10)
                        .setTimeZone(TimeZone.getTimeZone("UTC"))
                        .build()
                        .getTime();

        node.setId(id);
        node.setXsiType("foo");
        node.setAsmType("asmUnit");
        node.setModifDate(date);

        NodeDocument document = new NodeDocument(node);
        String output = mapper.writeValueAsString(document);

        assertContains("<node id=\"" + id + "\" type=\"asmUnit\"", output);
        assertContains("xsi_type=\"foo\"", output);
        assertContains("last_modif=\"2020-12-10T10:10:10.000+00:00\"", output);
    }

    @Test
    public void serializationWithGroupRights() throws JsonProcessingException {
        GroupRights groupRights = new GroupRights();

        groupRights.setRulesId("baz");
        groupRights.setRead(true);
        groupRights.setWrite(true);
        groupRights.setDelete(true);
        groupRights.setSubmit(true);

        UUID id = UUID.randomUUID();
        Node node = new Node();

        node.setId(id);
        node.setAsmType("asmStructure");

        NodeDocument document = new NodeDocument(node, groupRights, "foo");
        String output = mapper.writeValueAsString(document);

        assertContains("<node id=\"" + id + "\" type=\"asmStructure\"", output);

        assertContains("read=\"true\"", output);
        assertContains("write=\"true\"", output);
        assertContains("delete=\"true\"", output);
        assertContains("submit=\"true\"", output);
        assertContains("action=\"baz\"", output);

        assertContains("role=\"foo\"", output);
    }

    @Test
    public void parentIsNotSerialised() throws JsonProcessingException {
        UUID random = UUID.randomUUID();

        NodeDocument parent = new NodeDocument(random);
        NodeDocument child = new NodeDocument(random);

        parent.setChildren(Collections.singletonList(child));
        assertEquals(parent, child.getParent());

        String output = mapper.writeValueAsString(child);

        refuteContains("parent", output);
    }

    @Test
    public void serializationWithMetadata() throws JsonProcessingException {
        MetadataDocument metadata = MetadataDocument.from("public=\"true\"");
        MetadataWadDocument metadataWad = MetadataWadDocument.from("seenoderoles=\"true\"");
        MetadataEpmDocument metadataEpm = MetadataEpmDocument.from("public=\"true\"");

        Node node = new Node();
        NodeDocument document = new NodeDocument(node);

        document.setMetadata(metadata);
        document.setMetadataWad(metadataWad);
        document.setMetadataEpm(metadataEpm);

        String output = mapper.writeValueAsString(document);

        assertContains("<metadata public=\"true\"", output);
        assertContains("<metadata-wad seenoderoles=\"true\"", output);
        assertContains("<metadata-epm public=\"true\"", output);
    }

    @Test
    public void serializationWithResources() throws JsonProcessingException {
        UUID resourceId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        Date date = new Calendar.Builder()
                            .setDate(2020, 11, 10)
                            .setTimeOfDay(10, 10, 10)
                            .setTimeZone(TimeZone.getTimeZone("UTC"))
                            .build()
                            .getTime();

        Resource resource = new Resource();
        Node node = new Node();

        resource.setXsiType("nodeRes");
        resource.setModifDate(date);

        node.setId(nodeId);

        ResourceDocument resource1 = new ResourceDocument(resourceId);
        ResourceDocument resource2 = new ResourceDocument(resource, node);

        NodeDocument document = new NodeDocument(node);

        document.setResources(Arrays.asList(resource1, resource2));

        String output = mapper.writeValueAsString(document);

        assertContains("<asmResource id=\"" + resourceId + "\"/>", output);

        assertContains("<asmResource contextid=\"" + nodeId +"\"", output);
        assertContains("xsi_type=\"nodeRes\"", output);
        assertContains("last_modif=\"2020-12-10T10:10:10.000+00:00\"", output);
    }

    @Test
    public void deserialization() throws JsonProcessingException {
        String xml = "<node type=\"asmStructure\" xsi_type=\"bar\">" +
                        "<label>foo</label>" +
                        "<code>bar</code>" +

                        "<metadata public=\"true\" />" +
                        "<metadata-epm sharedNode=\"true\" />"+
                        "<metadata-wad seenoderoles=\"foo\" />" +

                        "<asmResource xsi_type=\"baz\" />" +
                    "</node>";

        NodeDocument document = mapper.readerFor(NodeDocument.class)
                                    .readValue(xml);

        assertEquals("asmStructure", document.getType());
        assertEquals("foo", document.getLabel());
        assertEquals("bar", document.getCode());

        assertTrue(document.getMetadata().getPublic());
        assertTrue(document.getMetadataEpm().getSharedNode());
        assertEquals("foo", document.getMetadataWad().getSeenoderoles());

        assertEquals(1, document.getResources().size());

        ResourceDocument resource = document.getResources().get(0);

        assertEquals("baz", resource.getXsiType());
    }
}
