package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRightInfo;

import java.util.List;
import java.util.UUID;

@JsonRootName("rolerightsgroup")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class RoleRightsGroupDocument {
    private Long id;
    private String label;
    private UUID portfolioId;
    private List<GroupUserDocument> groups;

    public RoleRightsGroupDocument(Long id, List<GroupUserDocument> groups) {
        this.id = id;
        this.groups = groups;
    }

    public RoleRightsGroupDocument(GroupRightInfo groupRightInfo) {
        this.id = groupRightInfo.getId();
        this.label = groupRightInfo.getLabel();
        this.portfolioId = groupRightInfo.getPortfolio().getId();
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public Long getId() {
        return id;
    }

    @JsonGetter("label")
    public String getLabel() {
        return label;
    }

    @JsonGetter("portfolio")
    public UUID getPortfolioId() {
        return portfolioId;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "group")
    public List<GroupUserDocument> getGroups() {
        return groups;
    }
}
