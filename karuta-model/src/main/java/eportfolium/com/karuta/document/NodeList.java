package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("nodes")
public class NodeList {
    private List<NodeDocument> nodes;

    public NodeList(List<NodeDocument> nodes) {
        this.nodes = nodes;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "node")
    public List<NodeDocument> getNodes() {
        return nodes;
    }
}
