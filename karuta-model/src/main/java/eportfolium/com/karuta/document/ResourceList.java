package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName("resources")
public class ResourceList {
    private List<ResourceDocument> resources;

    public ResourceList(List<ResourceDocument> resources) {
        this.resources = resources;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "resource")
    public List<ResourceDocument> getResources() {
        return resources;
    }
}
