package eportfolium.com.karuta.document;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.document.conversion.BooleanDeserializer;
import eportfolium.com.karuta.document.conversion.BooleanSerializer;

@JsonRootName("metadata")
public class MetadataDocument {
    protected static XmlMapper xmlMapper = new XmlMapper();

    @JsonDeserialize(using = BooleanDeserializer.class)
    protected Boolean isPublic;
    @JsonDeserialize(using = BooleanDeserializer.class)
    protected Boolean isPrivate;

    @JsonDeserialize(using = BooleanDeserializer.class)
    protected boolean sharedResource = false;
    @JsonDeserialize(using = BooleanDeserializer.class)
    protected boolean sharedNode = false;
    @JsonDeserialize(using = BooleanDeserializer.class)
    protected boolean sharedNodeResource;

    protected String semantictag;

    protected Map<String, String> attributes = new HashMap<>();

    public MetadataDocument() { }

    public static MetadataDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata " + processEntities(xml) + " />";

        return xmlMapper.readerFor(MetadataDocument.class)
                    .readValue(withTag);

    }

    @JsonGetter("public")
    @JacksonXmlProperty(isAttribute = true)
    @JsonSerialize(using = BooleanSerializer.class)
    public Boolean getPublic() {
        return isPublic;
    }

    @JsonDeserialize(using = BooleanDeserializer.class)
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        this.isPrivate = !isPublic;
    }

    @JsonGetter("private")
    @JacksonXmlProperty(isAttribute = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonSerialize(using = BooleanSerializer.class)
    public Boolean getPrivate() {
        return isPrivate;
    }

    @JsonDeserialize(using = BooleanDeserializer.class)
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        this.isPublic = !isPrivate;
    }

    @JsonGetter("sharedResource")
    @JacksonXmlProperty(isAttribute = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonSerialize(using = BooleanSerializer.class)
    public Boolean getSharedResource() {
        return sharedResource;
    }

    @JsonDeserialize(using = BooleanDeserializer.class)
    public void setSharedResource(boolean sharedResource) {
        this.sharedResource = sharedResource;
    }

    @JsonGetter("sharedNode")
    @JacksonXmlProperty(isAttribute = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonSerialize(using = BooleanSerializer.class)
    public Boolean getSharedNode() {
        return sharedNode;
    }

    @JsonDeserialize(using = BooleanDeserializer.class)
    public void setSharedNode(boolean sharedNode) {
        this.sharedNode = sharedNode;
    }

    @JsonGetter("sharedNodeResource")
    @JacksonXmlProperty(isAttribute = true)
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonSerialize(using = BooleanSerializer.class)
    public Boolean getSharedNodeResource() {
        return sharedNodeResource;
    }

    @JsonDeserialize(using = BooleanDeserializer.class)
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

    @JsonIgnore
    public Map<String, String> getAttributes() {
        return attributes;
    }
    
    @JsonAnySetter
    public void setAttribute(String k, String v) {
    	attributes.put(k, v);
    }

    protected static String processEntities(String xml) {
        if (xml == null)
            return "";

        return xml.replace("&nbsp;", "&amp;nbsp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replaceAll("\\(\\s*\"", "(&quot;")
                .replaceAll("\"\\s*\\)", "&quot;)");
    }
}
