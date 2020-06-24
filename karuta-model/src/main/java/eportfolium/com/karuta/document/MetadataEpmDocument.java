package eportfolium.com.karuta.document;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("metadata-epm")
public class MetadataEpmDocument extends MetadataDocument {

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "metadata-epm", isAttribute = true)
  private Map<String, String> attributes = new HashMap<String, String>();

	public static MetadataEpmDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata-epm " + (xml != null ? xml : "") + " />";

        return xmlMapper.readerFor(MetadataEpmDocument.class)
                .readValue(withTag);
    }

}
