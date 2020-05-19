package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.CredentialGroup;

import java.util.List;

@JsonRootName("group")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CredentialGroupDocument {
    private Long id;
    private String label;
    private List<CredentialDocument> users;

    public CredentialGroupDocument(CredentialGroup cg) {
        this.id = cg.getId();
        this.label = cg.getLabel();
    }

    public CredentialGroupDocument(Long id, List<CredentialDocument> users) {
        this.id = id;
        this.users = users;
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

    @JacksonXmlProperty(localName = "user")
    public List<CredentialDocument> getUsers() {
        return users;
    }
}
