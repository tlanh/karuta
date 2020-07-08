package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import org.junit.Test;

import java.util.UUID;

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
        assertContains("<role name=\"foo\"><rights RD=\"true\" WR=\"true\" DL=\"true\" SB=\"true\"/></role>", output);
    }
}
