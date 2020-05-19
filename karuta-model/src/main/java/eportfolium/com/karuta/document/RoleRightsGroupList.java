package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("rolerightsgroups")
public class RoleRightsGroupList {
    private List<RoleRightsGroupDocument> groups;

    public RoleRightsGroupList(List<RoleRightsGroupDocument> groups) {
        this.groups = groups;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "rolerightgroup")
    public List<RoleRightsGroupDocument> getGroups() {
        return groups;
    }
}
