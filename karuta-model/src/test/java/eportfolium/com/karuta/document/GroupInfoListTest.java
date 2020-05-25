package eportfolium.com.karuta.document;


import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import org.junit.Test;

import java.util.Collections;

public class GroupInfoListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupRightInfo.setId(12L);

        GroupInfo groupInfo = new GroupInfo();

        groupInfo.setLabel("designer");
        groupInfo.setOwner(1);
        groupInfo.setGroupRightInfo(groupRightInfo);

        GroupInfoDocument document = new GroupInfoDocument(groupInfo);
        GroupInfoList list = new GroupInfoList(Collections.singletonList(document));

        String output = mapper.writeValueAsString(list);

        assertContains("<groups>", output);

        assertContains("<group owner=\"1\" templateId=\"12\">", output);
        assertContains("<label>designer</label>", output);
    }
}