package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    public static MetadataWadDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata-wad " + (xml != null ? xml : "")  + " />";

        return xmlMapper.readerFor(MetadataWadDocument.class)
                .readValue(withTag);
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
    @JacksonXmlProperty(isAttribute = true)
    public String getMenuroles() {
        return menuroles;
    }

    @JsonProperty("submitted")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    @JsonProperty("submitteddate")
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(timezone = "UTC")
    public Date getSubmitteddate() {
        return submitteddate;
    }

    public void setSubmitteddate(Date submitteddate) {
        this.submitteddate = submitteddate;
    }
}
