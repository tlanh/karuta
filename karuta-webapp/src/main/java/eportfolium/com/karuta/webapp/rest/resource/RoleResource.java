package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/roles")
public class RoleResource extends AbstractResource {

	@InjectLogger
	private static Logger logger;

	@Autowired
	private UserManager userManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private PortfolioManager portfolioManager;

	/**
	 * Fetch rights in a role. <br>
	 * GET /rest/api/roles/role/{role-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param roleId
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @return
	 */
	@Path("/role/{role-id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getRole(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("role-id") Long roleId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept) {
		// checkCredential(httpServletRequest, user, token, null); FIXME
		try {
			return userManager.getRole(roleId).toString();
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Role " + roleId + " not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch a role in a portfolio. <br>
	 * GET /rest/api/roles/portfolio/{portfolio-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param role
	 * @param portfolioId
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @return
	 */
	@Path("/portfolio/{portfolio-id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getRolePortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @QueryParam("role") String role,
			@PathParam("portfolio-id") String portfolioId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept) {
		if (!isUUID(portfolioId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String returnValue = portfolioManager
					.findRoleByPortfolio(MimeTypeUtils.TEXT_XML, role, portfolioId, ui.userId).toString();
			return returnValue;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

	}

	/**
	 * Modify a role. <br>
	 * PUT /rest/api/roles/role/{role-id}
	 * 
	 * @param xmlRole
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param roleId
	 * @return
	 */
	@Path("/role/{role-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putRole(String xmlRole, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("role-id") int roleId) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String returnValue = securityManager.addOrUpdateRole(xmlRole, ui.userId).toString();
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
