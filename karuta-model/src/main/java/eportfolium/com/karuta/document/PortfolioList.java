package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("portfolios")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class PortfolioList {
    private List<PortfolioDocument> portfolioDocuments;
    private Long count;

    public PortfolioList(Long count) {
        this.count = count;
    }

    public PortfolioList(List<PortfolioDocument> documents) {
        this.portfolioDocuments = documents;
    }

    @JsonGetter("count")
    @JacksonXmlProperty(isAttribute = true)
    public Long getCount() {
        return count != null ? count : portfolioDocuments.size();
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "portfolio")
    public List<PortfolioDocument> getPortfolios() {
        return portfolioDocuments;
    }
}
