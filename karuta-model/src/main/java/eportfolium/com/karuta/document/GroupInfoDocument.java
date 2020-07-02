package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupInfo;

@JsonRootName("group")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class GroupInfoDocument {
    private Long id;
    private Long owner;
    private Long templateId;
    private String label;
    private Long roleId;
    private String role;

    public GroupInfoDocument(GroupInfo groupInfo) {
        this.id = groupInfo.getId();
        this.owner = groupInfo.getOwner();
        this.templateId = groupInfo.getGroupRightInfo().getId();
        this.label = groupInfo.getLabel();
        this.roleId = groupInfo.getGroupRightInfo().getId();
        this.role = groupInfo.getGroupRightInfo().getLabel();
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public Long getId() {
        return id;
    }

    @JsonGetter("templateId")
    @JacksonXmlProperty(isAttribute = true)
    public Long getTemplateId() {
        return templateId;
    }

    @JsonGetter("owner")
    @JacksonXmlProperty(isAttribute = true)
    public Long getOwner() {
        return owner;
    }

    @JsonGetter("label")
    public String getLabel() {
        return label;
    }

    @JsonGetter("roleId")
    public Long getRoleId() {
        return roleId;
    }

    // FIXME: Sometimes return the label in v3 ; check with v2
    @JsonGetter("role")
    public String getRole() {
        return role;
    }
}
