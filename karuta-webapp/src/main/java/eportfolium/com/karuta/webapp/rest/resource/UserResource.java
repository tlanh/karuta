package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

/**
 * @author mlengagne
 *
 */
public class UserResource extends AbstractResource {

	@Autowired
	private UserManager userManager;

	@Autowired
	private SecurityManager securityManager;

	@InjectLogger
	private static Logger logger;

	/***
	 * 
	 * Get user list*GET/rest/api/users*parameters:*return:*<users>*<user id="uid">
	 * <username></username> <firstname></firstname> <lastname></lastname>
	 * <admin>1/0</admin> <designer>1/0</designer> <email></email>
	 * <active>1/0</active> <substitute>1/0</substitute> </user> ... </users>
	 **/

	@Path("/users")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUsers(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("username") String username, @QueryParam("firstname") String firstname,
			@QueryParam("lastname") String lastname, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {

		Credential credential = checkCredential(httpServletRequest, user, token, null);

		if (credential.getId() == 0)
			throw new RestWebApplicationException(Status.FORBIDDEN, "Not logged in");
		try {
			String xmlGroups = "";
			if (securityManager.isAdmin(credential.getId()) || securityManager.isCreator(credential.getId()))
				xmlGroups = userManager.getUserList(credential.getId(), username, firstname, lastname);
			else if (credential.getId() != 0)
				xmlGroups = userManager.getInfUser(credential.getId());
			else
				throw new RestWebApplicationException(Status.FORBIDDEN, "Not authorized");

			return xmlGroups;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get a specific user info GET /rest/api/users/user/{user-id} parameters:
	 * return: <user id="uid"> <username></username> <firstname></firstname>
	 * <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
	 * <email></email> <active>1/0</active> <substitute>1/0</substitute> </user>
	 **/
	@Path("/users/user/{user-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUser(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("user-id") int userid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		try {
			String xmluser = userManager.getInfUser(Long.valueOf(userid));
			return xmluser;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, ex.getResponse().getEntity().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get user id from username GET /rest/api/users/user/username/{username}
	 * parameters: return: userid (long)
	 **/
	@Path("/users/user/username/{username}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUserId(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("username") String username) {

		try {
			Long userid = userManager.getUserId(username);
			if (PhpUtil.empty(userid)) {
				throw new RestWebApplicationException(Status.NOT_FOUND, "User not found");
			} else {
				return userid.toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, "Error : " + ex.getMessage());
		}
	}

	/**
	 * Get a list of role/group for this user GET
	 * /rest/api/users/user/{user-id}/groups parameters: return: <profiles>
	 * <profile> <group id="gid"> <label></label> <role></role> </group> </profile>
	 * </profiles>
	 **/
	@Path("/users/user/{user-id}/groups")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupsUser(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("user-id") long userIdCible) {
		try {
			String xmlgroupsUser = userManager.getUserRolesByUserId(userIdCible);
			return xmlgroupsUser;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch userlist from a role and portfolio id GET
	 * /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/users parameters:
	 * return:
	 **/
	@Path("/users/Portfolio/{portfolio-id}/Role/{role}/users")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUsersByRole(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @PathParam("portfolio-id") String portfolioUuid,
			@PathParam("role") String role, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		Credential ui = checkCredential(httpServletRequest, user, token, group); // FIXME

		try {
			String xmlUsers = userManager.getUsersByRole(ui.getId(), portfolioUuid, role);
			return xmlUsers;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * !! This or the other gets deleted (redundant) Delete users DELETE
	 * /rest/api/users parameters: return:
	 **/
	@Path("/users")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteUsers(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("userId") Long userId) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			if (!securityManager.isAdmin(ui.getId()) && !ui.getId().equals(userId))
				throw new RestWebApplicationException(Status.FORBIDDEN, "No admin right");

			securityManager.deleteUsers(ui.getId(), userId);
			return "user " + userId + " deleted";
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "user " + userId + " not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Delete specific user DELETE /rest/api/users/user/{user-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param userid
	 * @return
	 */
	@Path("/users/user/{user-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteUser(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("user-id") Long userid) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			// Not (admin or self)
			if (!securityManager.isAdmin(ui.getId()) && !ui.getId().equals(userid))
				throw new RestWebApplicationException(Status.FORBIDDEN, "No admin right");

			securityManager.deleteUsers(ui.getId(), userid);
			return "user " + userid + " deleted";
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "user " + userid + " not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add a user POST /rest/api/users content: <users> <user id="uid">
	 * <username></username> <firstname></firstname> <lastname></lastname>
	 * <admin>1/0</admin> <designer>1/0</designer> <email></email>
	 * <active>1/0</active> <substitute>1/0</substitute> </user> ... </users>
	 * 
	 * @param xmluser
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/users")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postUser(String xmluser, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String xmlUser = securityManager.addUsers(xmluser, ui.getId());
			if (xmlUser == null) {
				return Response.status(Status.CONFLICT).entity("Existing user or invalid input").build();
			}

			return Response.ok(xmlUser).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.BAD_REQUEST, ex.getMessage());
		}
	}

	/**
	 * Fetch rights in a role GET /rest/api/roles/role/{role-id}
	 * 
	 * FIXME: Might be redundant
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
	@Path("/roles/role/{role-id}")
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
	 * List roles GET /rest/api/rolerightsgroups
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
	@Path("/rolerightsgroups")
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
	 * List all users in a specified roles GET /rest/api/rolerightsgroups/all/users
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param portId
	 * @return
	 */
	@Path("/rolerightsgroups/all/users")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getPortfolioRightInfo(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("portfolio") String portId) {
		if (!isUUID(portId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = checkCredential(httpServletRequest, user, token, group); // FIXME
		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (portId != null) {
				returnValue = userManager.findUserRolesByPortfolio(portId, ui.getId());
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
	@Path("/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getRightInfo(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId) {
		Credential ui = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (rrgId != null) {
				returnValue = userManager.findUserRole(ui.getId(), rrgId);
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
	 * Add user in a role POST
	 * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
	 * parameters: return:
	 **/
	@Path("/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRightGroupUsers(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId,
			@PathParam("user-id") Long queryuser) {
		Credential ui = checkCredential(httpServletRequest, user, token, group);
		try {
			return securityManager.addUserRole2(ui.getId(), rrgId, queryuser);
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add user in a role POST
	 * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users
	 * parameters: return:
	 **/
	@Path("/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRightGroupUser(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId) {
		Credential ui = checkCredential(httpServletRequest, user, token, group);

		try {
			return securityManager.addUsersToRole(ui.getId(), rrgId, xmlNode);
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
