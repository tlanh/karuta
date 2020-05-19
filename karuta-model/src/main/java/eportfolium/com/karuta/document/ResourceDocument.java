package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.ResourceTable;

import java.util.Date;
import java.util.UUID;

@JsonRootName("asmResource")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ResourceDocument {
    private UUID id;
    private UUID nodeId;
    private String xsiType;
    private Date modifDate;
    private String content;

    private String lang;
    private String code;

    // For file resources
    private String filename;
    private String fileid;

    public ResourceDocument(UUID id) {
        this.id = id;
    }

    public ResourceDocument(ResourceTable resource, Node node) {
        this(resource.getId());

        this.nodeId = node.getId();
        this.xsiType = resource.getXsiType();
        this.modifDate = resource.getModifDate();

        this.content = resource.getContent();
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getId() {
        return id;
    }

    @JsonGetter("contextid")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    @JsonGetter("xsi_type")
    @JacksonXmlProperty(isAttribute = true)
    public String getXsiType() {
        return xsiType;
    }

    @JsonGetter("last_modif")
    @JacksonXmlProperty(isAttribute = true)
    public Date getModifDate() {
        return modifDate;
    }

    @JsonRawValue
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @JsonGetter("lang")
    public String getLang() {
        return lang;
    }

    @JsonGetter("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonGetter("filename")
    public String getFilename() {
        return filename;
    }

    @JsonGetter("fileid")
    public String getFileid() {
        return fileid;
    }


}
