package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("portfolios")
public class PortfolioList {
    private List<PortfolioDocument> portfolioDocuments;

    public PortfolioList(List<PortfolioDocument> documents) {
        this.portfolioDocuments = documents;
    }

    @JsonGetter("count")
    @JacksonXmlProperty(isAttribute = false)
    public int getCount() {
        return portfolioDocuments.size();
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "portfolio")
    public List<PortfolioDocument> getPortfolios() {
        return portfolioDocuments;
    }
}
