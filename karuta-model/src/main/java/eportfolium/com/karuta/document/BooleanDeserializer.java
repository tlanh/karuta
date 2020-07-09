package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

class BooleanDeserializer extends JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        String value = jp.readValueAs(String.class);

        return "Y".equals(value) || "true".equals(value);
    }
}
