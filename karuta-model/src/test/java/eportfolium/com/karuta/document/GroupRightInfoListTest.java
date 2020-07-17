package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import org.junit.Test;

import java.util.Collections;

public class GroupRightInfoListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupRightInfo.setId(12L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setOwner(1);

        GroupRightInfoDocument document = new GroupRightInfoDocument(groupRightInfo);
        GroupRightInfoList list = new GroupRightInfoList(Collections.singletonList(document));

        String output = mapper.writeValueAsString(list);

        assertContains("<groupRightsInfos>", output);

        assertContains("<groupRightInfo grid=\"12\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<owner>1</owner>", output);
    }
}