package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import org.junit.Test;

import java.util.Collections;

public class ProfileListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        GroupInfo groupInfo = new GroupInfo();

        groupRightInfo.setId(42L);
        groupRightInfo.setLabel("all");

        groupInfo.setId(12L);
        groupInfo.setLabel("designer");
        groupInfo.setOwner(1);
        groupInfo.setGroupRightInfo(groupRightInfo);

        GroupInfoDocument groupInfoDocument = new GroupInfoDocument(groupInfo);
        ProfileList list = new ProfileList(Collections.singletonList(groupInfoDocument));

        String output = mapper.writeValueAsString(list);

        assertContains("<profiles>", output);
        assertContains("<profile>", output);
        assertContains("<group id=\"12\" owner=\"1\" templateId=\"42\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<role>all</role>", output);
        assertContains("</group></profile>", output);
    }
}