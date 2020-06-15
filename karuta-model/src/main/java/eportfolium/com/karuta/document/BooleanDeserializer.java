package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

class BooleanDeserializer extends JsonDeserializer<Object> {

@Override
public Object deserialize( JsonParser jp, DeserializationContext context ) throws IOException, JsonProcessingException
{
  String object = jp.readValueAs(String.class);
  if ("Y".equals(object)) {
  	return true;
  }
  return false;
}
}