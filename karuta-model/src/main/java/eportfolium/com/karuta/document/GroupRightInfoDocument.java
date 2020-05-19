package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRightInfo;

@JsonRootName("groupRightInfo")
public class GroupRightInfoDocument {
    private Long id;
    private String label;
    private Long owner;

    public GroupRightInfoDocument(GroupRightInfo groupRightInfo) {
        this.id = groupRightInfo.getId();
        this.label = groupRightInfo.getLabel();
        this.owner = groupRightInfo.getOwner();
    }

    @JsonGetter("grid")
    @JacksonXmlProperty(isAttribute = true)
    public Long getId() {
        return id;
    }

    @JsonGetter("label")
    public String getLabel() {
        return label;
    }

    @JsonGetter("owner")
    public Long getOwner() {
        return owner;
    }
}
