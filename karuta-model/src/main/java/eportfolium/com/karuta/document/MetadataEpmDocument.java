package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("metadata-epm")
public class MetadataEpmDocument extends MetadataDocument {
    public MetadataEpmDocument(String content) {
        super(content);
    }
}
