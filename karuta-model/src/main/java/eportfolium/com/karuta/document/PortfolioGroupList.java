package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("portfolio")
public class PortfolioGroupList {
    private List<PortfolioGroupDocument> groups;

    public PortfolioGroupList(List<PortfolioGroupDocument> groups) {
        this.groups = groups;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "group")
    public List<PortfolioGroupDocument> getGroups() {
        return groups;
    }
}
