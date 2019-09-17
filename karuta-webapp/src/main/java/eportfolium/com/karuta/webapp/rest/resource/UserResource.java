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

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;

public class UserResource extends AbstractResource {

	@Autowired
	private UserManager userManager;

	@Autowired
	private SecurityManager securityManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Get groups from a user id GET /rest/api/groups parameters: - group: group id
	 * return: <groups> <group id="gid" owner="uid" templateId="rrgid">GROUP
	 * LABEL</group> ... </groups>
	 **/
	@Path("/groups")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroups(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String xmlGroups = userManager.getUserGroups(ui.getId());
			return xmlGroups;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

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
				xmlGroups = userManager.getListUsers(credential.getId(), username, firstname, lastname);
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
			Long userid = userManager.getUserID(username);
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
}
