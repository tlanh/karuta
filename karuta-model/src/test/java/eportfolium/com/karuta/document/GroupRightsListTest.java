package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;

import eportfolium.com.karuta.model.bean.GroupRightsId;
import org.junit.Test;

import java.util.Collections;

public class GroupRightsListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRights groupRights = new GroupRights();
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        GroupInfo groupInfo = new GroupInfo();

        groupInfo.setId(42L);
        groupRightInfo.setId(65L);
        groupRightInfo.setGroupInfo(groupInfo);
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);

        GroupRightsDocument document = new GroupRightsDocument(groupRights);
        GroupRightsList list = new GroupRightsList(Collections.singletonList(document));

        String output = mapper.writeValueAsString(list);

        assertContains("<groupRights>", output);
        assertContains("<groupRight gid=\"42\" templateId=\"65\">", output);
    }
}