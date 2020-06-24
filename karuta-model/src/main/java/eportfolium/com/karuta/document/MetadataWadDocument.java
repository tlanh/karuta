package eportfolium.com.karuta.document;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("metadata-wad")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class MetadataWadDocument extends MetadataDocument {
	
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "metadata-wad", isAttribute = true)
  private Map<String, String> attributes = new HashMap<String, String>();

    public static MetadataWadDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata-wad " + (xml != null ? xml : "")  + " />";

        return xmlMapper.readerFor(MetadataWadDocument.class)
                .readValue(withTag);
    }
}
