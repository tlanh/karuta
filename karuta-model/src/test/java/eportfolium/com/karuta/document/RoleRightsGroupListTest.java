package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.Portfolio;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

public class RoleRightsGroupListTest extends DocumentTest {
    @Test
    public void basicSerialization() throws JsonProcessingException {
        GroupRightInfo groupRightInfo = new GroupRightInfo();

        UUID portfolioId = UUID.randomUUID();

        groupRightInfo.setId(90L);
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(new Portfolio(portfolioId));

        RoleRightsGroupDocument roleRightsGroup = new RoleRightsGroupDocument(groupRightInfo);
        RoleRightsGroupList list = new RoleRightsGroupList(Collections.singletonList(roleRightsGroup));

        String output = mapper.writeValueAsString(list);

        assertContains("<rolerightsgroups>", output);
        assertContains("<rolerightgroup id=\"90\">", output);
        assertContains("<label>designer</label>", output);
        assertContains("<portfolio>" + portfolioId + "</portfolio>", output);
        assertContains("</rolerightgroup></rolerightsgroups>", output);

        // For legacy reasons, this particular document goes all plural
        // for the root node and all singular for the children ones.
        //
        // On the other hand, serializing a single RoleRightGroupDocument
        // will produce a tag with "right" as plural but "group" as singular.
        refuteContains("<rolerightsgroup ", output);
        refuteContains("<rolerightsgroup>", output);
    }
}