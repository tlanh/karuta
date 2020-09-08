package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeRightsDocumentTest extends DocumentTest {
    @Test
    public void serialization() throws JsonProcessingException {
        UUID nodeId = UUID.randomUUID();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("foo");

        GroupRights groupRights = new GroupRights();
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);

        groupRights.setRead(true);
        groupRights.setWrite(true);
        groupRights.setDelete(true);
        groupRights.setSubmit(true);

        NodeRightsDocument nodeRightsDocument = new NodeRightsDocument(nodeId, groupRights);

        String output = mapper.writeValueAsString(nodeRightsDocument);

        assertContains("<node uuid=\"" + nodeId.toString() +"\">", output);
        assertContains("<role name=\"foo\"><right RD=\"true\" WR=\"true\" DL=\"true\" SB=\"true\"/></role>", output);
    }

    @Test
    public void basicDeserialization() throws JsonProcessingException {
        UUID id = UUID.randomUUID();

        String xml = "<node uuid=\"" + id +"\">" +
                    "<role name=\"designer\">" +
                        "<right RD=\"true\" WR=\"true\" SB=\"true\" DL=\"true\"/>" +
                    "</role>" +
                "</node>";

        NodeRightsDocument nodeRightsDocument = mapper.readValue(xml, NodeRightsDocument.class);

        assertEquals(id, nodeRightsDocument.getUuid());
        assertEquals("designer", nodeRightsDocument.getRole().getName());

        assertTrue(nodeRightsDocument.getRole().getRight().getRD());
        assertTrue(nodeRightsDocument.getRole().getRight().getWR());
        assertTrue(nodeRightsDocument.getRole().getRight().getDL());
        assertTrue(nodeRightsDocument.getRole().getRight().getSB());
    }
}
