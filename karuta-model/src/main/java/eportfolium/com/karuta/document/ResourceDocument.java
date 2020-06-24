package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Resource;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonRootName("asmResource")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonSerialize(using = ResourceSerializer.class)
public class ResourceDocument {
    private UUID id;
    private UUID nodeId;
    private String xsiType;
    private Date modifDate;
    private String content;

//    private String lang;
//    private String code;

    // For file resources
//    private String filename;
//    private String fileid;

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

    @JsonIgnore
    public String getCode() {
      return getNodeContent("code", "");
    }

    public String getFileid() {			
      return getNodeContent("fileid", "");
    }
    
    private String getNodeContent( String tag, String lang )
    {
    	String data = null;
    	String l = "";
    	if( !"".equals(lang) )
    		l = String.format(".*lang=\"%s\".*", lang);
			String tagReg = String.format("<%s%s>([^<]*)</%s>", tag, l, tag);
			Pattern tagPat = Pattern.compile(tagReg);
			Matcher startMatcher = tagPat.matcher(content);
			if (startMatcher.find()) data = startMatcher.group(1);
			
			return data;
    }

    /// FIXME: First lang tag found. Doesn't make sense since resource can have multiple language (for now?)
    @JsonIgnore
    public String getLang() {
    	String lang = null;
			String tagReg = "lang=\"([^\"]*)";
			Pattern tagPat = Pattern.compile(tagReg);
			Matcher startMatcher = tagPat.matcher(content);
			if (startMatcher.find()) lang = startMatcher.group(1);
			
      return lang;
    }

    public String getFilename() {
      return getNodeContent("filename", "");
    }

    /*
    @JsonGetter("lang")
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
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
    //*/

    public String getContent() {
        return content;
    }

    // FIXME: Remove that once we no longer rely on raw XML storage.
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
                builder.append(attributes.get(""));
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
}
