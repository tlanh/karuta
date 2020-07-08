package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;

import java.util.*;

@JsonRootName("node")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonPropertyOrder({ "metadata-wad", "metadata-wpm", "metadata", "asmResource", "asmContext" })
public class NodeDocument {
    private UUID id;
    protected String type;
    private String xsiType;
    private String label;
    private String code;
    private String description;

    private String semtag;
    private String format;
    private String action;

    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean read;
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean write;
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean delete;
    @JsonDeserialize(using = BooleanDeserializer.class)
    private boolean submit;

    private String role;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.S")
    private Date modifDate;

    private MetadataDocument metadataDocument;
    private MetadataEpmDocument metadataEpmDocument;
    private MetadataWadDocument metadataWadDocument;
    private List<ResourceDocument> resourceDocuments;
    private List<NodeDocument> children = new ArrayList<NodeDocument>();

    transient private NodeDocument parent;

    public NodeDocument() { }

    public NodeDocument(UUID id) {
        this.id = id;

        this.resourceDocuments = new ArrayList<ResourceDocument>();
        this.children = new ArrayList<NodeDocument>();

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
    @JacksonXmlProperty(isAttribute = true, localName = "xsi_type")
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
    @JacksonXmlProperty(isAttribute = true, localName = "last_modif")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.S")
    public Date getModifDate() {
        return modifDate;
    }

//    @JsonUnwrapped
//    @JsonRawValue
    @JacksonXmlProperty(localName = "metadata-wad")
    public MetadataWadDocument getMetadataWad() {
        return metadataWadDocument;
    }

//    @JsonUnwrapped
//    @JsonRawValue
    @JacksonXmlProperty(localName = "metadata-epm")
    public MetadataEpmDocument getMetadataEpm() {
        return metadataEpmDocument;
    }

//    @JsonUnwrapped
//    @JsonRawValue
    @JacksonXmlProperty(localName = "metadata")
    public MetadataDocument getMetadata() {
        return metadataDocument;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "asmResource")
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

    @JsonRawValue
    @JacksonXmlProperty(localName = "metadata")
    public void setMetadata(MetadataDocument document) {
        this.metadataDocument = document;
    }

    @JsonRawValue
    @JacksonXmlProperty(localName = "metadata-epm")
    public void setMetadataEpm(MetadataEpmDocument document) {
        this.metadataEpmDocument = document;
    }

    @JsonRawValue
    @JacksonXmlProperty(localName = "metadata-wad")
    public void setMetadataWad(MetadataWadDocument document) {
        this.metadataWadDocument = document;
    }

    public void setResources(List<ResourceDocument> documents) {
        this.resourceDocuments = documents;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "asmContext")
    public void setContext( NodeDocument context ) {
    	context.type = "asmContext";
    	children.add(context);
    }
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "asmStructure")
    public void setStructure( NodeDocument context ) {
    	context.type = "asmStructure";
    	children.add(context);
    }
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "asmUnit")
    public void setUnit( NodeDocument context ) {
    	context.type = "asmUnit";
    	children.add(context);
    }
    
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "asmUnitStructure")
    public void setUnitStructure( NodeDocument context ) {
    	context.type = "asmUnitStructure";
    	children.add(context);
    }
    
    public void addChildren(NodeDocument children) {
      children.setParent(this);
      this.children.add(children);
  }
    
    public void setChildren(List<NodeDocument> children) {
        children.forEach(c -> c.setParent(this));
        this.children = children;
    }
}
