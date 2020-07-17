package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;
import java.util.UUID;

@JsonRootName("portfolio")
public class GroupUserList {
    private UUID id;
    private List<GroupUserDocument> groups;

    public GroupUserList(UUID id, List<GroupUserDocument> groups) {
        this.id = id;
        this.groups = groups;
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getId() {
        return id;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "rrg")
    public List<GroupUserDocument> getGroups() {
        return groups;
    }
}
