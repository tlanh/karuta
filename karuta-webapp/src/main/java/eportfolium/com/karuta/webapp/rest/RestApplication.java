package eportfolium.com.karuta.webapp.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Classe de configuration de l'application REST
 *
 * @author mlengagne
 *
 */
@ApplicationPath("/")
public class RestApplication extends ResourceConfig {

	/**
	 * Constructeur par défaut.
	 */
	public RestApplication() {
		packages("eportfolium.com.karuta.webapp.rest");
		register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME), Level.INFO,
				LoggingFeature.Verbosity.PAYLOAD_ANY, 10000));
	}
}