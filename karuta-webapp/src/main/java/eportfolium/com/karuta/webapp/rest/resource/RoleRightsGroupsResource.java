package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/rolerightsgroups")
public class RoleRightsGroupsResource extends AbstractResource {

	@InjectLogger
	private static Logger logger;

	@Autowired
	private UserManager userManager;

	@Autowired
	private PortfolioManager portfolioManager;

	@Autowired
	private SecurityManager securityManager;

	/**
	 * List roles. <br>
	 * GET /rest/api/rolerightsgroups
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param portfolio
	 * @param queryuser
	 * @param role
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getRightsGroup(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("portfolio") String portfolio,
			@QueryParam("user") Long queryuser, @QueryParam("role") String role) {
		if (!isUUID(portfolio)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		// checkCredential(httpServletRequest, user, token, group); // FIXME

		try {
			// Retourne le contenu du type
			return userManager.getRoleList(portfolio, queryuser, role);
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("getRightsGroup", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * List all users in a specified roles. <br>
	 * GET /rest/api/rolerightsgroups/all/users
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param portId
	 * @return
	 */
	@Path("/all/users")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getPortfolioRightInfo(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("portfolio") String portId) {
		if (!isUUID(portId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, group); // FIXME
		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (portId != null) {
				returnValue = userManager.getUserRolesByPortfolio(portId, ui.userId);
			}
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("getPortfolioRightInfo", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * List rights in the specified role <br>
	 * GET /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param rrgId
	 * @return
	 */
	@Path("/rolerightsgroup/{rolerightsgroup-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getRightInfo(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (rrgId != null) {
				returnValue = userManager.getUserRole(rrgId);
			}
			return returnValue;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("getRightInfo", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add user in a role. <br>
	 * POST
	 * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param rrgId
	 * @param queryuser
	 * @return
	 */
	@Path("/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRightGroupUsers(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId,
			@PathParam("user-id") Long queryuser) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, group);
		try {
			return securityManager.addUserRole(ui.userId, rrgId, queryuser);
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add user in a role. <br>
	 * POST /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param rrgId
	 * @return
	 */
	@Path("/rolerightsgroup/{rolerightsgroup-id}/users")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRightGroupUser(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, group);

		try {
			return securityManager.addUsersToRole(ui.userId, rrgId, xmlNode);
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Delete a role. <br>
	 * DELETE /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param groupRightInfoId
	 * @return
	 */
	@Path("/rolerightsgroup/{rolerightsgroup-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteRightGroup(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long groupRightInfoId) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			securityManager.removeRole(ui.userId, groupRightInfoId);
			return "";
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Remove user from a role. <br>
	 * DELETE
	 * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
	 *
	 **/
	@Path("/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteRightGroupUser(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId,
			@PathParam("user-id") Integer queryuser) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		String returnValue = "";
		try {
			securityManager.removeUserRole(ui.userId, rrgId);
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Remove all users from a role. <br>
	 * DELETE /rest/api/rolerightsgroups/all/users
	 *
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param portId
	 * @return
	 */
	@Path("/all/users")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deletePortfolioRightInfo(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("portfolio") String portId) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (portId != null) {
				securityManager.removeUsersFromRole(ui.userId, portId);
			}
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Change a right in role. <br>
	 * PUT /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param rrgId
	 * @return
	 */
	@Path("/rolerightsgroup/{rolerightsgroup-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putRightInfo(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (rrgId != null) {
				securityManager.changeRole(ui.userId, rrgId, xmlNode);
			}
			return returnValue;
		} catch (DoesNotExistException e) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Role with id " + rrgId + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add a role in the portfolio <br>
	 * POST /rest/api/rolerightsgroups/{portfolio-id}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param portfolio
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/{portfolio-id}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRightGroups(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group,
			@PathParam("portfolio-id") String portfolio, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolio)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, group);
		String returnValue = "";
		try {
			returnValue = portfolioManager.addRoleInPortfolio(ui.userId, portfolio, xmlNode);
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
