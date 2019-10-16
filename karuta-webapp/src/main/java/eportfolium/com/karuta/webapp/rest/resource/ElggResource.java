package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.socialnetwork.Elgg;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/elgg")
public class ElggResource extends AbstractResource {

	@Autowired
	private ConfigurationManager configurationManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * elgg related. <br>
	 * GET /rest/api/elgg/site/river_feed
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param type
	 * @param limit
	 * @return
	 */
	@Path("/site/river_feed")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getElggSiteRiverFeed(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("type") Integer type,
			@QueryParam("limit") String limit) {
		int iLimit;
		try {
			iLimit = Integer.parseInt(limit);
		} catch (Exception ex) {
			iLimit = 20;
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		System.out.println(ui.User);

		// Elgg variables
		String elggDefaultApiUrl = configurationManager.get("elggDefaultApiUrl");
		String elggDefaultSiteUrl = configurationManager.get("elggDefaultSiteUrl");
		String elggApiKey = configurationManager.get("elggApiKey");
		String elggDefaultUserPassword = configurationManager.get("elggDefaultUserPassword");

		try {
			Elgg elgg = new Elgg(elggDefaultApiUrl, elggDefaultSiteUrl, elggApiKey, ui.User, elggDefaultUserPassword);
			return elgg.getSiteRiverFeed(iLimit);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(javaUtils.getCompleteStackTrace(ex) + Status.INTERNAL_SERVER_ERROR.getStatusCode());
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Elgg related. <br>
	 * POST /rest/api/elgg/wire
	 * 
	 * @param message
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param type
	 * @return
	 */
	@Path("/wire")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String getElggSiteRiverFeed(String message, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("type") Integer type) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		// Elgg variables
		String elggDefaultApiUrl = configurationManager.get("elggDefaultApiUrl");
		String elggDefaultSiteUrl = configurationManager.get("elggDefaultSiteUrl");
		String elggApiKey = configurationManager.get("elggApiKey");
		String elggDefaultUserPassword = configurationManager.get("elggDefaultUserPassword");

		try {
			Elgg elgg = new Elgg(elggDefaultApiUrl, elggDefaultSiteUrl, elggApiKey, ui.User, elggDefaultUserPassword);
			return elgg.postWire(message);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(javaUtils.getCompleteStackTrace(ex) + Status.INTERNAL_SERVER_ERROR.getStatusCode());
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
