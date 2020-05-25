package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;

import java.util.UUID;

public class GroupUserDocumentTest extends DocumentTest {
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
        user.setDisplayFirstname("John");
        user.setDisplayLastname("Doe");
        user.setEmail("john@doe.com");

        groupInfo.setGroupRightInfo(groupRightInfo);

        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(user);

        GroupUserDocument document = new GroupUserDocument(groupUser);
        String output = mapper.writeValueAsString(document);

        assertContains("<rrg id=\"12\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<portfolio>" + portfolioId +"</portfolio>", output);
        assertContains("<users>", output);
        assertContains("<user id=\"34\">", output);
        assertContains("<firstname>John</firstname>", output);
        assertContains("<lastname>Doe</lastname>", output);
        assertContains("</user></users>", output);
    }
}