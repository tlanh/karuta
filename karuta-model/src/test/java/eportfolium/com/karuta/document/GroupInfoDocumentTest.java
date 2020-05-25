package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import org.junit.Test;

public class GroupInfoDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupInfo groupInfo = new GroupInfo();
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupRightInfo.setId(72L);
        groupRightInfo.setLabel("all");

        groupInfo.setId(56L);
        groupInfo.setOwner(1);
        groupInfo.setLabel("designer");
        groupInfo.setGroupRightInfo(groupRightInfo);

        GroupInfoDocument document = new GroupInfoDocument(groupInfo);
        String output = mapper.writeValueAsString(document);

        assertContains("<group id=\"56\" owner=\"1\" templateId=\"72\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<roleId>72</roleId>", output);
        assertContains("<role>all</role>", output);
        assertContains("<groupid>56</groupid>", output);
    }

}