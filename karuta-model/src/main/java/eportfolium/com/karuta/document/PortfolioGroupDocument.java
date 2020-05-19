package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.PortfolioGroup;

import java.util.List;

@JsonRootName("group")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class PortfolioGroupDocument {
    private Long id;
    private String type;
    private String label;
    private List<PortfolioDocument> portfolios;

    public PortfolioGroupDocument(Long id, List<PortfolioDocument> portfolios) {
        this.id = id;
        this.portfolios = portfolios;
    }

    public PortfolioGroupDocument(PortfolioGroup group) {
        this.id = group.getId();
        this.label = group.getLabel();
        this.type = group.getType().toLowerCase();
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public Long getId() {
        return id;
    }

    @JsonGetter("type")
    @JacksonXmlProperty(isAttribute = true)
    public String getType() {
        return type;
    }

    @JsonGetter("label")
    public String getLabel() {
        return label;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "portfolio")
    public List<PortfolioDocument> getPortfolios() {
        return portfolios;
    }
}
