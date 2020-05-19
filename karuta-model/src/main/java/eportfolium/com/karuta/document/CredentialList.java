package eportfolium.com.karuta.document;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "users")
public class CredentialList {
    private List<CredentialDocument> users;

    public CredentialList(List<CredentialDocument> users) {
        this.users = users;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "user")
    public List<CredentialDocument> getUsers() {
        return users;
    }
}
