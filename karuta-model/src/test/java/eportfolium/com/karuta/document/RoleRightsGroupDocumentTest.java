package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

public class RoleRightsGroupDocumentTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        UUID portfolioId = UUID.randomUUID();

        groupRightInfo.setId(12L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(new Portfolio(portfolioId));

        RoleRightsGroupDocument document = new RoleRightsGroupDocument(groupRightInfo);
        String output = mapper.writeValueAsString(document);

        assertContains("<rolerightsgroup id=\"12\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<portfolio>" + portfolioId + "</portfolio>", output);
    }

    @Test
    public void serializationWithGroupUsers() throws JsonProcessingException {
        GroupInfo groupInfo = new GroupInfo();
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        GroupUser groupUser = new GroupUser();
        Credential user = new Credential();

        UUID portfolioId = UUID.randomUUID();

        user.setId(42L);
        user.setLogin("johndoe");

        groupRightInfo.setId(39L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(new Portfolio(portfolioId));

        groupInfo.setGroupRightInfo(groupRightInfo);
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(user);

        GroupUserDocument groupUserDocument = new GroupUserDocument(groupUser);

        RoleRightsGroupDocument document = new RoleRightsGroupDocument(
                90L,
                Collections.singletonList(groupUserDocument)
        );

        String output = mapper.writeValueAsString(document);

        assertContains("<rolerightsgroup id=\"90\">", output);
        assertContains("<group id=\"39\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<portfolio>" + portfolioId + "</portfolio>", output);
        assertContains("<users><user id=\"42\">", output);
        assertContains("<username>johndoe</username>", output);
        assertContains("</user></users></group>", output);
    }
}