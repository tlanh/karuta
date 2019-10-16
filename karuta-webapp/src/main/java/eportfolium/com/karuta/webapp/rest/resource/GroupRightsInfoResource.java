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
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/groupRightsInfos")
public class GroupRightsInfoResource extends AbstractResource {

	@Autowired
	private PortfolioManager portfolioManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Get role list from portfolio from uuid. <br>
	 * GET /rest/api/groupRightsInfos
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param portfolioId        portfolio uuid
	 * @return <groupRightsInfos> <groupRightInfo grid="grouprightid">
	 *         <label></label> <owner>UID</owner> </groupRightInfo>
	 *         </groupRightsInfos>
	 */

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupRightsInfos(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("portfolioId") String portfolioId) {
		if (!isUUID(portfolioId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			return portfolioManager.getGroupRightsInfos(ui.userId, portfolioId);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
