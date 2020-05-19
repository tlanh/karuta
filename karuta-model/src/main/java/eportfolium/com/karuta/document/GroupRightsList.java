package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("groupRights")
public class GroupRightsList {
    public List<GroupRightsDocument> rights;

    public GroupRightsList(List<GroupRightsDocument> rights) {
        this.rights = rights;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "groupRight")
    public List<GroupRightsDocument> getRights() {
        return rights;
    }
}
