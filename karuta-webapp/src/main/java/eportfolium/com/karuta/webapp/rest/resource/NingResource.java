package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.socialnetwork.Ning;

@Path("/ning")
public class NingResource extends AbstractResource {

	@InjectLogger
	private static Logger logger;

	/**
	 * Ning related. <br>
	 * GET /rest/api/ning/activities
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param type
	 * @return
	 */
	@Path("/activities")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getNingActivities(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("type") Integer type) {
		checkCredential(httpServletRequest, user, token, group);

		Ning ning = new Ning();
		return ning.getXhtmlActivites();
	}

}
