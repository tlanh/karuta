package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Resource;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonRootName("asmResource")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class ResourceDocument {
    private UUID id;
    private UUID nodeId;
    private String xsiType;
    private Date modifDate;
    private String content = "";

    private String code;

    // For file resources
    private List<Map<String, String>> filename;
    private List<Map<String, String>> fileid;
    private List<Map<String, String>> type;

    public ResourceDocument() { }

    public ResourceDocument(UUID id) {
        this.id = id;
    }

    public ResourceDocument(Resource resource, Node node) {
        this(resource.getId());

        this.nodeId = node.getId();
        this.xsiType = resource.getXsiType();
        this.modifDate = resource.getModifDate();

        this.content = resource.getContent();

        if (resource.getContent() != null) {
            try {
                XmlMapper mapper = new XmlMapper();
                ResourceDocument subset = mapper.readerFor(ResourceDocument.class)
                        .readValue("<asmResource>" + resource.getContent() + "</asmResource>");

                this.filename = subset.getFilename();
                this.fileid = subset.getFileid();
                this.type = subset.getType();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @JsonGetter("id")
    @JacksonXmlProperty(isAttribute = true)
    public UUID getId() {
        return id;
    }

    @JsonGetter("contextid")
    @JacksonXmlProperty(isAttribute = true, localName = "contextid")
    public UUID getNodeId() {
        return nodeId;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    @JsonGetter("xsi_type")
    @JacksonXmlProperty(isAttribute = true, localName = "xsi_type")
    public String getXsiType() {
        return xsiType;
    }

    @JsonGetter("last_modif")
    @JacksonXmlProperty(isAttribute = true, localName = "last_modif")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.S")
    public Date getModifDate() {
        return modifDate;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @JsonGetter("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonGetter("filename")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Map<String, String>> getFilename() {
        return filename;
    }

    public String getFilename(String lang) {
        return findValueForLang(filename, lang);
    }

    public void setFilename(List<Map<String, String>> filename) {
        this.filename = filename;
        this.dumpMap("filename", filename);
    }

    @JsonGetter("fileid")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Map<String, String>> getFileid() {
        return fileid;
    }

    public String getFileid(String lang) {
        return findValueForLang(fileid, lang);
    }

    public void setFileid(List<Map<String, String>> fileid) {
        this.fileid = fileid;
        this.dumpMap("fileid", fileid);
    }

    @JsonGetter("type")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Map<String, String>> getType() { return type; }

    public String getType(String lang) {
        return findValueForLang(type, lang);
    }

    public void setType(List<Map<String, String>> type) {
        this.type = type;
        this.dumpMap("type", type);
    }

    @JsonRawValue
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String getContent() {
        return content;
    }

    /// Raw value does nothing, it's already processed
    @JsonRawValue
    @JsonAnySetter
    public void ignored(String name, Object value) {
        StringBuilder builder = new StringBuilder("<");
        builder.append(name);

        if (value instanceof Map) {
            Map<String, Object> attributes = (Map<String, Object>)value;

            attributes.forEach((n, v) -> {
                if (!n.equals(""))
                    builder.append(" ").append(n).append("=\"").append(v).append("\"");
            });

            builder.append(">");
            if( attributes.get("") != null )
            {
            	String processed = attributes.get("").toString();
            	processed = processed.replace("&", "&amp;");
            	processed = processed.replace("<", "&lt;");
            	processed = processed.replace(">", "&gt;");
            	
                builder.append(processed);
            }
        } else {
        	builder.append(">");
        	if( value != null )
            builder.append(value);
        }

        builder.append("</")
                .append(name)
                .append(">");

        content += builder.toString();
    }

    private String findValueForLang(List<Map<String, String>> values, String lang) {
        if (values == null)
            return "";

        return values.stream()
                .filter(f -> f.get("lang").equals(lang))
                .findFirst()
                .map(m -> m.get("value"))
                .orElse("");
    }

    private void dumpMap(String name, List<Map<String, String>> values) {
        if (this.content == null)
            this.content = "";

        StringBuilder stringBuilder = new StringBuilder("");

        values.forEach(map -> {
            stringBuilder.append("<").append(name);
            map.forEach((k, v) -> stringBuilder.append(" ").append(k).append("=\"").append(v).append("\""));
            stringBuilder.append(" />");
        });

        content += stringBuilder.toString();
    }
}
