package eportfolium.com.karuta.document;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class DateDeserializer extends JsonDeserializer<Object> {
		@Override
		public Date deserialize( JsonParser p, DeserializationContext ctxt ) throws IOException, JsonProcessingException
		{
      String value = p.readValueAs(String.class);
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
      DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date ret = null;
			try
			{
				ret = dateFormat.parse(value);
			}
			catch( ParseException e ) { }
			
			if( ret == null )
			{
				try
				{
					ret = dateFormat2.parse(value);
				}
				catch( ParseException e ) { }
			}
      
			return ret;
		}
}
