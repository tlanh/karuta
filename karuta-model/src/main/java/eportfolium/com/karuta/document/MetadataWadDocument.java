package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;

@JsonRootName("metadata-wad")
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class MetadataWadDocument extends MetadataDocument {
	
    public static MetadataWadDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata-wad " + (xml != null ? xml : "")  + " />";

        return xmlMapper.readerFor(MetadataWadDocument.class)
                .readValue(withTag);
    }
}
