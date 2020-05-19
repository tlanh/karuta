package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("metadata")
public class MetadataDocument {
    protected String content;
    protected boolean isPublic;
    protected boolean isPrivate;

    protected boolean sharedResource;
    protected boolean sharedNode;
    protected boolean sharedNodeResource;

    protected String semantictag;

    public MetadataDocument(String content) {
        this.content = content;
    }

    @JsonRawValue
    public String getContent() {
        return content;
    }

    @JsonGetter("public")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getPublic() {
        return isPublic;
    }

    @JsonGetter("private")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    @JsonGetter("sharedResource")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSharedResource() {
        return sharedResource;
    }

    @JsonGetter("sharedNode")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSharedNode() {
        return sharedNode;
    }

    @JsonGetter("sharedNodeResource")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSharedNodeResource() {
        return sharedNodeResource;
    }

    @JsonGetter("semantictag")
    @JacksonXmlProperty(isAttribute = true)
    public String getSemantictag() {
        return semantictag;
    }
}
