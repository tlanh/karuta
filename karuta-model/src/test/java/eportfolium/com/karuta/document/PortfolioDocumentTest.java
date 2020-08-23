package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.Resource;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class PortfolioDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        PortfolioDocument document = new PortfolioDocument(id);

        String output = mapper.writeValueAsString(document);

        assertContains("<portfolio id=\"" + id + "\" owner=\"false\">", output);
        assertContains("<version>4</version>", output);
    }

    @Test
    public void serializationWithGid() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        PortfolioDocument document = new PortfolioDocument(id, 12L);

        String output = mapper.writeValueAsString(document);

        assertContains("<portfolio id=\"" + id + "\" gid=\"12\" owner=\"false\"", output);
    }

    @Test
    public void serializationWithOwner() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        PortfolioDocument document = new PortfolioDocument(id, true);

        String output = mapper.writeValueAsString(document);

        assertContains("<portfolio id=\"" + id + "\" code=\"\" owner=\"true\">", output);
    }

    @Test
    public void serializationWithCodeAndNodes() throws JsonProcessingException {
        UUID nodeId = UUID.randomUUID();
        Node node = new Node();

        node.setId(nodeId);
        node.setAsmType("asmRoot");
        node.setXsiType("foo");

        NodeDocument nodeDocument = new NodeDocument(node);

        UUID id = UUID.randomUUID();
        String code = "karuta.model";
        PortfolioDocument document = new PortfolioDocument(id, true, code, nodeDocument);

        String output = mapper.writeValueAsString(document);

        assertContains("<portfolio id=\"" + id + "\" ", output);
        assertContains("code=\"" + code + "\"", output);
        assertContains("owner=\"true\"", output);

        assertContains("<asmRoot id=\"" + nodeId + "\" type=\"asmRoot\"", output);
        assertContains("xsi_type=\"foo\"", output);
    }

    @Test
    public void serializationWithPortfolio() throws JsonProcessingException {
        UUID portfolioId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        Date date = new Calendar.Builder()
                        .setDate(2020, 10, 10)
                        .setTimeOfDay(10, 10, 10)
                        .setTimeZone(TimeZone.getTimeZone("UTC"))
                        .build()
                        .getTime();

        Portfolio portfolio = new Portfolio();
        Node node = new Node();
        Resource resource = new Resource();

        resource.setId(resourceId);
        resource.setXsiType("quux");
        resource.setModifDate(date);
        resource.setContent("<foo></foo>");

        node.setId(nodeId);
        node.setMetadata("public=\"true\"");
        node.setMetadataEpm("public=\"true\"");
        node.setMetadataWad("seenoderoles=\"foo\"");
        node.setLabel("foo");
        node.setDescr("bar");
        node.setSemtag("baz");
        node.setResource(resource);

        portfolio.setId(portfolioId);
        portfolio.setRootNode(node);
        portfolio.setModifUserId(42L);
        portfolio.setModifDate(date);

        PortfolioDocument document = new PortfolioDocument(portfolio, true);
        String output = mapper.writeValueAsString(document);

        assertContains("<portfolio id=\"" + portfolioId + "\" ", output);
        assertContains("root_node_id=\"" + nodeId + "\" ", output);
        assertContains("owner=\"true\"", output);
        assertContains("modified=\"1605003010000\">", output);

        assertContains("<asmRoot id=\"" + nodeId + "\" semtag=\"baz\" ", output);
        assertContains("<label>foo</label>", output);
        assertContains("<description>bar</description>", output);
        assertContains("<metadata public=\"true\"/>", output);
        assertContains("<metadata-epm public=\"true\"/>", output);
        assertContains("<metadata-wad seenoderoles=\"foo\"", output);

        assertContains("<asmResource id=\"" + resourceId + "\" ", output);
        assertContains("contextid=\"" + nodeId +"\" xsi_type=\"quux\"", output);
        assertContains("last_modif=\"1605003010000\">", output);
        assertContains("<content><foo></foo></content>", output);
    }

    @Test
    public void deserialization() throws JsonProcessingException {
        UUID id = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        String xml = "<portfolio id=\"" + id + "\">" +
                        "<asmRoot type=\"asmRoot\" id=\"" + nodeId +"\">" +
                            "<asmResource code=\"foo\">" +
                                "<foo></foo>" +
                            "</asmResource>" +
                        "</asmRoot>" +
                "</portfolio>";

        PortfolioDocument document = mapper.readerFor(PortfolioDocument.class)
                                        .readValue(xml);


        assertEquals(id, document.getId());

        NodeDocument node = document.getRoot();

        assertEquals(nodeId, node.getId());
        assertEquals("asmRoot", node.getType());
        assertEquals(1, node.getResources().size());

        ResourceDocument resource = node.getResources().get(0);

        assertEquals("foo", resource.getCode());
        assertEquals("<code>foo</code><foo></foo>", resource.getContent());
    }
}
