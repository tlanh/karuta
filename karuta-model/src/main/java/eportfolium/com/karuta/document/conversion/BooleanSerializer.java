package eportfolium.com.karuta.document.conversion;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class BooleanSerializer extends JsonSerializer<Boolean> {

    @Override
    public void serialize(Boolean value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value) {
            jsonGenerator.writeString("Y");
        } else {
            jsonGenerator.writeString("N");
        }
    }
}
