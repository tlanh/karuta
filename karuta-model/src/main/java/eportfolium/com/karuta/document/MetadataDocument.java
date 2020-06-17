package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonRootName("metadata")
public class MetadataDocument {
    protected static XmlMapper xmlMapper = new XmlMapper();

    
    private String attributes;
    
    public MetadataDocument() { }

    public static MetadataDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata " + (xml != null ? xml : "") + " />";

        return xmlMapper.readerFor(MetadataDocument.class)
                    .readValue(withTag);
    }

		public String getAttributes()
		{
			return attributes;
		}

		public void setAttributes( String attributes )
		{
			this.attributes = attributes;
		}
}
