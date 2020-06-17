package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;

@JsonRootName("metadata-epm")
public class MetadataEpmDocument extends MetadataDocument {

	public static MetadataEpmDocument from(String xml) throws JsonProcessingException {
        String withTag = "<metadata-epm " + (xml != null ? xml : "") + " />";

        return xmlMapper.readerFor(MetadataEpmDocument.class)
                .readValue(withTag);
    }

}
