package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupUser;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@JsonRootName("rrg")
public class GroupUserDocument {
    private Long id;
    private String label;
    private CredentialDocument user;
    private UUID portfolioId;

    public GroupUserDocument(GroupUser groupUser) {
        GroupRightInfo groupRightInfo = groupUser.getGroupInfo().getGroupRightInfo();

        this.id = groupRightInfo.getId();
        this.label = groupRightInfo.getLabel();
        this.user = new CredentialDocument(groupUser.getCredential());
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

    @JacksonXmlElementWrapper(localName = "users")
    @JacksonXmlProperty(localName = "user")
    public Set<CredentialDocument> getUsers() {
        return Collections.singleton(user);
    }
}
