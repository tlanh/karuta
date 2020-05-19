package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("roles")
public class RoleList {
    private List<RoleDocument> roles;
    private String action;

    public RoleList(List<RoleDocument> roles) {
        this.roles = roles;
    }

    @JsonGetter("action")
    public String getAction() {
        return action;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "role")
    public List<RoleDocument> getRoles() {
        return roles;
    }
}
