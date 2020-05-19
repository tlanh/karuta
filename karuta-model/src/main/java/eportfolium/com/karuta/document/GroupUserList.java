package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;
import java.util.UUID;

@JsonRootName("portfolio")
public class GroupUserList {
    private UUID portfolioId;
    private List<GroupUserDocument> groups;

    public GroupUserList(UUID portfolioId, List<GroupUserDocument> groups) {
        this.portfolioId = portfolioId;
        this.groups = groups;
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getPortfolioId() {
        return portfolioId;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "rrg")
    public List<GroupUserDocument> getGroups() {
        return groups;
    }
}
