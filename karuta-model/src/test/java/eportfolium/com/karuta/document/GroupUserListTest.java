package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

public class GroupUserListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupUser groupUser = new GroupUser();
        GroupInfo groupInfo = new GroupInfo();
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        Credential user = new Credential();

        UUID portfolioId = UUID.randomUUID();

        groupRightInfo.setId(12L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(new Portfolio(portfolioId));

        user.setId(34L);
        user.setLogin("johndoe");

        groupInfo.setGroupRightInfo(groupRightInfo);

        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(user);

        GroupUserDocument document = new GroupUserDocument(groupUser);
        GroupUserList list = new GroupUserList(portfolioId, Collections.singletonList(document));
        String output = mapper.writeValueAsString(list);

        assertContains("<portfolio>", output);
        assertContains("<rrg id=\"12\">", output);
        assertContains("<users>", output);
        assertContains("<user id=\"34\">", output);
        assertContains("<username>johndoe</username>", output);
    }

}