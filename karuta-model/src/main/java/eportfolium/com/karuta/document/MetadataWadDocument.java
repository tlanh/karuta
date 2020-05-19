package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Date;

@JsonRootName("metadata-wad")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class MetadataWadDocument extends MetadataDocument {
    private String seenoderoles;
    private String delnoderoles;
    private String editnoderoles;
    private String editresroles;
    private String submitroles;
    private String showtoroles;
    private String showroles;
    private String notifyroles;
    private String menuroles;

    private boolean submitted;
    private Date submitteddate;

    public MetadataWadDocument(String content) {
        super(content);
    }

    @JsonGetter("seenoderoles")
    @JacksonXmlProperty(isAttribute = true)
    public String getSeenoderoles() {
        return seenoderoles;
    }

    @JsonGetter("delnoderoles")
    @JacksonXmlProperty(isAttribute = true)
    public String getDelnoderoles() {
        return delnoderoles;
    }

    @JsonGetter("editnoderoles")
    @JacksonXmlProperty(isAttribute = true)
    public String getEditnoderoles() {
        return editnoderoles;
    }

    @JsonGetter("editresroles")
    @JacksonXmlProperty(isAttribute = true)
    public String getEditresroles() {
        return editresroles;
    }

    @JsonGetter("submitroles")
    @JacksonXmlProperty(isAttribute = true)
    public String getSubmitroles() {
        return submitroles;
    }

    @JsonGetter("showtoroles")
    @JacksonXmlProperty(isAttribute = true)
    public String getShowtoroles() {
        return showtoroles;
    }

    @JsonGetter("showroles")
    @JacksonXmlProperty(isAttribute = true)
    public String getShowroles() {
        return showroles;
    }

    @JsonGetter("notifyroles")
    @JacksonXmlProperty(isAttribute = true)
    public String getNotifyroles() {
        return notifyroles;
    }

    @JsonGetter("menuroles")
    @JacksonXmlProperty
    public String getMenuroles() {
        return menuroles;
    }

    @JsonProperty("submitted")
    public boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    @JsonProperty("submitteddate")
    public Date getSubmitteddate() {
        return submitteddate;
    }

    public void setSubmitteddate(Date submitteddate) {
        this.submitteddate = submitteddate;
    }
}
