package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;

@JsonRootName("user")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class CredentialDocument {
    private Long id;
    private String username;

    private String password;
    private String prevpass;

    private String firstname;
    private String lastname;
    private String email;
    private Integer admin;
    private Integer designer;
    private String other;
    private Integer active;
    private Integer substitute;
    private Long substituteId;

    public CredentialDocument(Credential credential) {
        this.id = credential.getId();
        this.username = credential.getLogin();
        this.firstname = credential.getDisplayFirstname();
        this.lastname = credential.getDisplayLastname();
        this.email = credential.getEmail();
    }

    public CredentialDocument(Credential credential, boolean extra) {
        this(credential);

        CredentialSubstitution cs = credential.getCredentialSubstitution();

        this.admin = credential.getIsAdmin();
        this.designer = credential.getIsDesigner();
        this.other = credential.getOther();
        this.active = credential.getActive();

        if (cs != null && cs.getId() != null) {
            this.substitute = 1;
            this.substituteId = cs.getId().getId();
        } else {
            this.substitute = 0;
            this.substituteId = 0L;
        }
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public Long getId() {
        return id;
    }

    @JsonGetter("username")
    public String getUsername() {
        return username;
    }

    @JsonGetter("firstname")
    public String getFirstname() {
        return firstname;
    }

    @JsonGetter("lastname")
    public String getLastname() {
        return lastname;
    }

    @JsonGetter("email")
    public String getEmail() {
        return email;
    }

    @JsonGetter("admin")
    public Integer getAdmin() {
        return admin;
    }

    @JsonGetter("designer")
    public Integer getDesigner() {
        return designer;
    }

    @JsonGetter("other")
    public String getOther() {
        return other;
    }

    @JsonGetter("active")
    public Integer getActive() {
        return active;
    }

    @JsonGetter("substitute")
    public Integer getSubstitute() {
        return substitute;
    }

    public Long getSubstituteId() {
        return substituteId;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public String getPrevpass() {
        return prevpass;
    }

    @JsonProperty
    public void setPrevpass(String prevpass) {
        this.prevpass = prevpass;
    }
}
