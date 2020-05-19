package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRightInfo;

import java.util.List;
import java.util.UUID;

@JsonRootName("role")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class RoleDocument {
    private Long id;
    private Long owner;
    private String label;
    private UUID portfolioId;

    private List<RightDocument> rights;

    public RoleDocument(GroupRightInfo gri) {
        this.id = gri.getId();
        this.owner = gri.getOwner();
        this.label = gri.getLabel();
        this.portfolioId = gri.getPortfolio().getId();
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public Long getId() {
        return id;
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

    @JsonGetter("portfolio_id")
    public UUID getPortfolioId() {
        return portfolioId;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "right")
    public List<RightDocument> getRights() {
        return rights;
    }

    public void setRights(List<RightDocument> rights) {
        this.rights = rights;
    }
}
