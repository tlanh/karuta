package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JsonRootName("node")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class NodeDocument {
    private UUID id;
    private String type;
    private String xsiType;
    private String label;
    private String code;
    private String description;

    private String semtag;
    private String format;
    private String action;

    private boolean read;
    private boolean write;
    private boolean delete;
    private boolean submit;

    private String role;
    private Date modifDate;

    private List<MetadataDocument> metadataDocuments;
    private List<ResourceDocument> resourceDocuments;
    private List<NodeDocument> children;

    transient private NodeDocument parent;

    public NodeDocument(UUID id) {
        this.id = id;

        this.resourceDocuments = Collections.emptyList();
        this.metadataDocuments = Collections.emptyList();
        this.children = Collections.emptyList();

        this.parent = null;
    }

    public NodeDocument(Node node) {
        this(node.getId());

        this.type = node.getAsmType();
        this.xsiType = node.getXsiType();

        this.modifDate = node.getModifDate();
    }

    public NodeDocument(Node node, GroupRights groupRights, String role) {
        this(node);

        this.action = groupRights.getRulesId();

        this.read = groupRights.isRead();
        this.write = groupRights.isWrite();
        this.delete = groupRights.isDelete();
        this.submit = groupRights.isSubmit();

        this.role = role;
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getId() {
        return id;
    }

    @JsonGetter("type")
    @JacksonXmlProperty(isAttribute = true)
    public String getType() {
        return type;
    }

    @JsonGetter("action")
    @JacksonXmlProperty(isAttribute = true)
    public String getAction() {
        return action;
    }

    @JsonGetter("xsi_type")
    @JacksonXmlProperty(isAttribute = true)
    public String getXsiType() {
        return xsiType;
    }

    @JsonGetter("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonGetter("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonGetter("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonGetter("read")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getRead() {
        return read;
    }

    @JsonGetter("write")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getWrite() {
        return write;
    }

    @JsonGetter("delete")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getDelete() {
        return delete;
    }

    @JsonGetter("submit")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getSubmit() {
        return submit;
    }

    @JsonGetter("role")
    @JacksonXmlProperty(isAttribute = true)
    public String getRole() {
        return role;
    }

    @JsonGetter("last_modif")
    @JacksonXmlProperty(isAttribute = true)
    public Date getModifDate() {
        return modifDate;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    public List<MetadataDocument> getMetadata() {
        return metadataDocuments;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    public List<ResourceDocument> getResources() {
        return resourceDocuments;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "node")
    public List<NodeDocument> getChildren() {
        return children;
    }

    @JsonGetter("semtag")
    @JacksonXmlProperty(isAttribute = true)
    public String getSemtag() {
        return semtag;
    }

    public void setSemtag(String semtag) {
        this.semtag = semtag;
    }

    @JsonGetter("format")
    @JacksonXmlProperty(isAttribute = true)
    public String getFormat() {
        return format;
    }

    @JsonIgnore
    public NodeDocument getParent() {
        return parent;
    }

    private void setParent(NodeDocument parent) {
        this.parent = parent;
    }

    public void setMetadata(List<MetadataDocument> documents) {
        this.metadataDocuments = documents;
    }

    public void setResources(List<ResourceDocument> documents) {
        this.resourceDocuments = documents;
    }

    public void setChildren(List<NodeDocument> children) {
        children.forEach(c -> c.setParent(this));
        this.children = children;
    }
}
