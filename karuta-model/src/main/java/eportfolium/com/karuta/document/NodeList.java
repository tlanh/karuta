package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eportfolium.com.karuta.document.conversion.NodeListDocumentSerializer;

import java.util.List;

@JsonRootName("nodes")
@JsonSerialize(using = NodeListDocumentSerializer.class)
public class NodeList {
    private List<NodeDocument> nodes;

    public NodeList(List<NodeDocument> nodes) {
        this.nodes = nodes;
    }

    @JacksonXmlElementWrapper(useWrapping = true)
    @JacksonXmlProperty(localName = "node")
    public List<NodeDocument> getNodes() {
        return nodes;
    }
}
