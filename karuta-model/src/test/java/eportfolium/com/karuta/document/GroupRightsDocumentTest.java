package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import org.junit.Test;

import java.util.UUID;

public class GroupRightsDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRights groupRights = new GroupRights();
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        GroupInfo groupInfo = new GroupInfo();

        UUID groupRightsId = UUID.randomUUID();

        groupInfo.setId(21L);
        groupInfo.setOwner(12);
        groupRightInfo.setId(39L);
        groupRightInfo.setGroupInfo(groupInfo);
        groupRightInfo.setOwner(78);

        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);
        groupRights.setGroupRightsId(groupRightsId);

        groupRights.setAdd(true);
        groupRights.setWrite(true);
        groupRights.setSubmit(true);
        groupRights.setTypesId("foo");

        GroupRightsDocument document = new GroupRightsDocument(groupRights);
        String output = mapper.writeValueAsString(document);

        assertContains("<groupRights gid=\"21\" templateId=\"39\">", output);
        assertContains("<item id=\"" + groupRightsId + "\" ", output);
        assertContains("add=\"true\" ", output);
        assertContains("write=\"true\" ", output);
        assertContains("submit=\"true\" ", output);

        assertContains("del=\"false\" ", output);
        assertContains("read=\"false\" ", output);

        assertContains("creator=\"12\" ", output);
        assertContains("owner=\"78\" ", output);

        assertContains("typeId=\"foo\"", output);

        // Ensure that <item> is not wrapped twice
        refuteContains("<item>", output);
    }

}