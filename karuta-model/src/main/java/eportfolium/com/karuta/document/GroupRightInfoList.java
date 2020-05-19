package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("groupRightsInfos")
public class GroupRightInfoList {
    public List<GroupRightInfoDocument> groups;

    public GroupRightInfoList(List<GroupRightInfoDocument> groups) {
        this.groups = groups;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "groupRightInfo")
    public List<GroupRightInfoDocument> getGroups() {
        return groups;
    }
}
