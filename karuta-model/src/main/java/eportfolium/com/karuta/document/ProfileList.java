package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("profiles")
public class ProfileList {
    private List<GroupInfoDocument> groups;

    public ProfileList(List<GroupInfoDocument> groups) {
        this.groups = groups;
    }

    @JacksonXmlElementWrapper(localName = "profile")
    @JacksonXmlProperty(localName = "group")
    public List<GroupInfoDocument> getGroups() {
        return groups;
    }
}
