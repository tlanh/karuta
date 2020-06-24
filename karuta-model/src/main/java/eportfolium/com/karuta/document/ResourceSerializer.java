package eportfolium.com.karuta.document;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class ResourceSerializer extends JsonSerializer<ResourceDocument>
{
	@Override
	public void serialize( ResourceDocument value, JsonGenerator gen, SerializerProvider serializers ) throws IOException
	{
		String format = "<asmResource contextid=\"%s\" id=\"%s\" last_modif=\"%s\" xsi_type=\"%s\">%s</asmResource>";
		gen.writeRaw(String.format(format, value.getNodeId(), value.getId(), value.getModifDate(), value.getXsiType(), value.getContent()));
	}
}
