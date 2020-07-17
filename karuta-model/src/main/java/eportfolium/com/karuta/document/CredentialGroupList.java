package eportfolium.com.karuta.document;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "groups")
public class CredentialGroupList {
    private List<CredentialGroupDocument> groups;

    public CredentialGroupList(List<CredentialGroupDocument> groups) {
        this.groups = groups;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "group")
    public List<CredentialGroupDocument> getGroups() {
        return groups;
    }
}
