package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("metadata")
public class MetadataDocument {
    protected static XmlMapper xmlMapper = new XmlMapper();

    protected boolean isPublic;
    protected boolean isPrivate;

    protected boolean sharedResource;
    protected boolean sharedNode;
    protected boolean sharedNodeResource;

    protected String semantictag;

    public MetadataDocument() { }

    public static MetadataDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata " + (xml != null ? xml : "") + " />";

        return xmlMapper.readerFor(MetadataDocument.class)
                    .readValue(withTag);

    }

    @JsonGetter("public")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        this.isPrivate = !isPublic;
    }

    @JsonGetter("private")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        this.isPublic = !isPrivate;
    }

    @JsonGetter("sharedResource")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSharedResource() {
        return sharedResource;
    }

    public void setSharedResource(boolean sharedResource) {
        this.sharedResource = sharedResource;
    }

    @JsonGetter("sharedNode")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSharedNode() {
        return sharedNode;
    }

    public void setSharedNode(boolean sharedNode) {
        this.sharedNode = sharedNode;
    }

    @JsonGetter("sharedNodeResource")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSharedNodeResource() {
        return sharedNodeResource;
    }

    public void setSharedNodeResource(boolean sharedNodeResource) {
        this.sharedNodeResource = sharedNodeResource;
    }

    @JsonGetter("semantictag")
    @JacksonXmlProperty(isAttribute = true)
    public String getSemantictag() {
        return semantictag;
    }

    public void setSemantictag(String semantictag) {
        this.semantictag = semantictag;
    }
}
