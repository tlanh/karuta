package eportfolium.com.karuta.document;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@JsonRootName("metadata")
public class MetadataDocument {
    protected static XmlMapper xmlMapper = new XmlMapper();
    
    private Map<String, String> attributes = new HashMap<String, String>();
    
    public MetadataDocument() { }

    public static MetadataDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata " + (xml != null ? xml : "") + " />";

        return xmlMapper.readerFor(MetadataDocument.class)
                    .readValue(withTag);
    }

    @JsonAnyGetter
		public Map<String, String> getAttributes()
		{
			return attributes;
		}

    public String getStringAttributes(  )
    {
    	StringBuilder strattrib = new StringBuilder("");
    	for (String key : attributes.keySet())
    		strattrib.append(key + "=\"" + attributes.get(key) + "\" ");
    	
//    	strattrib.delete(strattrib.length()-1, strattrib.length());
    	return strattrib.toString().trim();
    }
    
		public void setAttributes( Map<String, String> attributes )
		{
			this.attributes = attributes;
		}
		
    @JsonAnySetter
    public void setAttributeField(String key, String value) {
        this.attributes.put(key, value);
    }

}
