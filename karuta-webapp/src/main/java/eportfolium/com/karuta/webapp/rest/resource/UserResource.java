/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
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
@Path("/users")
public class UserResource extends AbstractResource {

	@Autowired
	private UserManager userManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private GroupManager groupManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Add a user. <br>
	 * POST /rest/api/users
	 * 
	 * @param xmluser            <users> <user id="uid"> <username></username>
	 *                           <firstname></firstname> <lastname></lastname>
	 *                           <admin>1/0</admin> <designer>1/0</designer>
	 *                           <email></email> <active>1/0</active>
	 *                           <substitute>1/0</substitute> </user> ... </users>
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postUser(String xmluser, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String xmlUser = securityManager.addUsers(xmluser, ui.userId);
			if (xmlUser == null) {
				return Response.status(Status.CONFLICT).entity("Existing user or invalid input").build();
			}

			return Response.ok(xmlUser).build();
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.BAD_REQUEST, ex.getMessage());
		}
	}

	/***
	 * 
	 * Get user list. <br>
	 * GET/rest/api/users*parameters:*return:
	 * 
	 * @param user
	 * @param token
	 * @param username
	 * @param firstname
	 * @param lastname
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @return *<users>*<user id="uid"> <username></username>
	 *         <firstname></firstname> <lastname></lastname> <admin>1/0</admin>
	 *         <designer>1/0</designer> <email></email> <active>1/0</active>
	 *         <substitute>1/0</substitute> </user> ... </users>
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUsers(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("username") String username, @QueryParam("firstname") String firstname,
			@QueryParam("lastname") String lastname, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		if (ui.userId == 0)
			throw new RestWebApplicationException(Status.FORBIDDEN, "Not logged in");

		try {
			String xmlGroups = "";
			if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId))
				xmlGroups = userManager.getUserList(ui.userId, username, firstname, lastname);
			else if (ui.userId != 0)
				xmlGroups = userManager.getUserInfos(ui.userId);
			else
				throw new RestWebApplicationException(Status.FORBIDDEN, "Not authorized");

			return xmlGroups;
		} catch (RestWebApplicationException ex) {
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get a specific user info. <br>
	 * GET /rest/api/users/user/{user-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param userid
	 * @param sc
	 * @param httpServletRequest
	 * @return <user id="uid"> <username></username> <firstname></firstname>
	 *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
	 *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
	 *         </user>
	 */
	@Path("/user/{user-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUser(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("user-id") int userid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		try {
			return userManager.getUserInfos(Long.valueOf(userid));
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get user id from username. <br>
	 * GET /rest/api/users/user/username/{username}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param username
	 * @return userid (long)
	 */
	@Path("/user/username/{username}")
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
		} catch (RestWebApplicationException ex) {
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, "Error : " + ex.getMessage());
		}
	}

	/**
	 * Get a list of role/group for this user. <br>
	 * GET /rest/api/users/user/{user-id}/groups
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param userIdCible
	 * @return <profiles> <profile> <group id="gid"> <label></label> <role></role>
	 *         </group> </profile> </profiles>
	 */
	@Path("/user/{user-id}/groups")
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
	 * Fetch userlist from a role and portfolio id. <br>
	 * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/users
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param portfolioUuid
	 * @param role
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/Portfolio/{portfolio-id}/Role/{role}/users")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUsersByRole(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @PathParam("portfolio-id") String portfolioUuid,
			@PathParam("role") String role, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, group); // FIXME

		try {
			return userManager.getUsersByRole(ui.userId, portfolioUuid, role);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Delete users. <br>
	 * DELETE /rest/api/users
	 * 
	 * @see #deleteUser(String, String, long, ServletConfig, HttpServletRequest,
	 *      Long)
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteUsers(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("userId") Long userId) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		if (!securityManager.isAdmin(ui.userId) && ui.userId != userId)
			throw new RestWebApplicationException(Status.FORBIDDEN, "No admin right");

		try {
			securityManager.removeUsers(ui.userId, userId);
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
	 * Delete specific user. <br>
	 * DELETE /rest/api/users/user/{user-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param userid
	 * @return
	 */
	@Path("/user/{user-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteUser(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("user-id") Long userid) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			securityManager.removeUsers(ui.userId, userid);
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
	 * Modify user info. <br>
	 * PUT /rest/api/users/user/{user-id} body: <user id="uid">
	 * <username></username> <firstname></firstname> <lastname></lastname>
	 * <admin>1/0</admin> <designer>1/0</designer> <email></email>
	 * <active>1/0</active> <substitute>1/0</substitute> </user>
	 *
	 * @param xmlInfUser
	 * @param user
	 * @param token
	 * @param groupId
	 * @param userid
	 * @param sc
	 * @param httpServletRequest
	 * @return <user id="uid"> <username></username> <firstname></firstname>
	 *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
	 *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
	 *         </user>
	 */
	@Path("/user/{user-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putUser(String xmlInfUser, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("user-id") long userid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {

			String queryUser = "";
			if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId)) {
				queryUser = securityManager.changeUser(ui.userId, userid, xmlInfUser);
			} else if (ui.userId == userid) /// Changing self
			{
				String ip = httpServletRequest.getRemoteAddr();
				logger.info(String.format("[%s] ", ip));
				queryUser = securityManager.changeUserInfo(ui.userId, userid, xmlInfUser);
			} else
				throw new RestWebApplicationException(Status.FORBIDDEN, "Not authorized");

			return queryUser;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, "Error : " + ex.getMessage());
		}
	}

	/**
	 * Fetch groups from a role and portfolio id <br>
	 * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/groups.
	 * 
	 * @param user
	 * @param token
	 * @param group
	 * @param portfolioUuid
	 * @param role
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/Portfolio/{portfolio-id}/Role/{role}/groups")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupsByRole(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @PathParam("portfolio-id") String portfolioUuid,
			@PathParam("role") String role, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		try {
			return groupManager.getGroupsByRole(portfolioUuid, role);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

	}

}
