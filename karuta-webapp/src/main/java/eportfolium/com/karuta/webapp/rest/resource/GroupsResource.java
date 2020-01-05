package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/groups")
public class GroupsResource extends AbstractResource {

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private PortfolioManager portfolioManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Get groups from a user id <br>
	 * GET /rest/api/groups
	 * 
	 * @param user
	 * @param token
	 * @param groupId            group id
	 * @param sc
	 * @param httpServletRequest
	 * @return <groups> <group id="gid" owner="uid" templateId="rrgid">GROUP
	 *         LABEL</group> ... </groups>
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroups(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return groupManager.getUserGroups(ui.userId);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get roles in a portfolio <br>
	 * GET /rest/api/groups/{portfolio-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param portfolioUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/{portfolio-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupsPortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return portfolioManager.getRolesByPortfolio(portfolioUuid, ui.userId);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
