package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import org.junit.Test;

public class GroupRightInfoDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        groupRightInfo.setId(89L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setOwner(1);

        GroupRightInfoDocument document = new GroupRightInfoDocument(groupRightInfo);
        String output = mapper.writeValueAsString(document);

        assertContains("<groupRightInfo grid=\"89\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<owner>1</owner>", output);
    }
}