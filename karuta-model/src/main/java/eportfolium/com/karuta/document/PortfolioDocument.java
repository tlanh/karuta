package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonRootName("portfolio")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class PortfolioDocument {
    private UUID id;
    private UUID rootNodeId;
    private boolean owner;
    private Long ownerId;
    private String code;
    private Long gid;
    private Date modifDate;

    private List<NodeDocument> nodes;

    public PortfolioDocument(UUID id) {
        this.id = id;
    }

    public PortfolioDocument(UUID id, Long gid) {
        this(id);

        this.gid = gid;
    }

    public PortfolioDocument(UUID id, boolean owner) {
        this(id);

        this.owner = owner;
        this.code = "";
    }

    public PortfolioDocument(UUID id, boolean owner, String code, List<NodeDocument> nodes) {
        this(id, owner);

        this.code = code;
        this.nodes = nodes;
    }

    public PortfolioDocument(Portfolio portfolio, boolean owner) {
        this(portfolio.getId(), owner);

        Node rootNode = portfolio.getRootNode();

        this.rootNodeId = rootNode.getId();
        this.ownerId = portfolio.getModifUserId();
        this.modifDate = portfolio.getModifDate();

        NodeDocument child = new NodeDocument(rootNode);

        child.setLabel(rootNode.getLabel());
        child.setDescription(rootNode.getDescr());
        child.setSemtag(rootNode.getSemtag());

        List<MetadataDocument> metadata = new ArrayList<>();

        if (rootNode.getMetadataWad() != null)
            metadata.add(new MetadataWadDocument(rootNode.getMetadata()));

        if (rootNode.getMetadataEpm() != null)
            metadata.add(new MetadataEpmDocument(rootNode.getMetadataEpm()));

        if (rootNode.getMetadata() != null)
            metadata.add(new MetadataDocument(rootNode.getMetadata()));

        child.setMetadata(metadata);
        child.setResources(Stream.of(
                rootNode.getResResource(),
                rootNode.getContextResource(),
                rootNode.getResource())
                    .filter(Objects::nonNull)
                    .map(r -> new ResourceDocument(r, rootNode))
                    .collect(Collectors.toList()));

        this.nodes = Collections.singletonList(child);
    }

    @JsonGetter("code")
    @JacksonXmlProperty(isAttribute = true)
    public String getCode() {
        return code;
    }

    @JsonGetter("version")
    public int getVersion() {
        return 4; // For backward compatibility ; cut if useless
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getId() {
        return id;
    }

    @JsonGetter("root_node_id")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getRootNodeId() {
        return rootNodeId;
    }

    @JsonGetter("owner")
    @JacksonXmlProperty(isAttribute = true)
    public boolean getOwner() {
        return owner;
    }

    @JsonGetter("owner_id")
    @JacksonXmlProperty(isAttribute = true)
    public Long getOwnerId() {
        return ownerId;
    }

    @JsonGetter("modified")
    @JacksonXmlProperty(isAttribute = true)
    public Date getModifDate() {
        return modifDate;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    public List<NodeDocument> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeDocument> nodes) {
        this.nodes = nodes;
    }
}