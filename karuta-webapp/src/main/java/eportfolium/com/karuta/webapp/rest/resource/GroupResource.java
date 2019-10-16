package eportfolium.com.karuta.webapp.rest.resource;

import java.util.Arrays;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

/**
 * Managing and listing user groups
 * 
 * @author mlengagne
 *
 */
public class GroupResource extends AbstractResource {

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private UserManager userManager;

	@InjectLogger
	private static Logger logger;

	private GroupResource() {
	}

	/**
	 * Fetch groups from a role and portfolio id <br>
	 * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/groups :
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
	@Path("/users/Portfolio/{portfolio-id}/Role/{role}/groups")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupsByRole(@CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @PathParam("portfolio-id") String portfolioUuid,
			@PathParam("role") String role, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
//		Credential ui = checkCredential(httpServletRequest, user, token, group); // FIXME

		try {
			return groupManager.getGroupsByRole(portfolioUuid, role);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

	}

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
	@Path("/groups")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroups(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return groupManager.getUserGroups(ui.getId());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Create a new user group <br>
	 * POST /rest/api/usersgroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupName          Name of the group we are creating
	 * @return groupid
	 */
	@Path("/usersgroups")
	@POST
	public String postUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("label") String groupName) {
		// checkCredential(httpServletRequest, null, null, null); //FIXME
		try {
			Long response = groupManager.addUserGroup(groupName);
			logger.debug("Group " + groupName + " successfully added");
			return Long.toString(response);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.NOT_MODIFIED, "Error in creation");
		}

	}

	/**
	 * Put a user in user group <br>
	 * PUT /rest/api/usersgroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupId            group id
	 * @param user               user id
	 * @param label              new name of the group.
	 * @return Code 200
	 */
	@Path("/usersgroups")
	@PUT
	public Response putUserInUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("group") Long groupId, @QueryParam("user") Long user, @QueryParam("label") String label) {
		// checkCredential(httpServletRequest, null, null, null);// FIXME
		try {
			boolean isOK = false;
			if (label != null) {
				isOK = groupManager.renameUserGroup(groupId, label);
			} else {
				isOK = userManager.addUserInGroups(user, Arrays.asList(groupId));
				logger.debug("putUserInUserGroup successful, user was correctly added to the group " + groupId);
			}
			if (isOK)
				return Response.status(200).entity("Changed").build();
			else
				return Response.status(200).entity("Not OK").build();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get users by usergroup or if there's no group id give he list of user group.
	 * <br>
	 * GET /rest/api/usersgroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupId            group id
	 * @param userId
	 * @param groupName
	 * @return Without group id <groups> <group id={groupid}> <label>{group
	 *         name}</label> </group> ... </groups>
	 *
	 *         - With group id <group id={groupid}> <user id={userid}></user> ...
	 *         </group>
	 */
	@Path("/usersgroups")
	@GET
	public String getUsersByUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("group") Long groupId, @QueryParam("user") Long userId, @QueryParam("label") String groupName) {
//				checkCredential(httpServletRequest, null, null, null);// FIXME
		String xmlUsers = "";

		try {
			if (groupName != null) {
				CredentialGroup crGroup = groupManager.getGroupByName(groupName);
				if (crGroup == null) {
					throw new RestWebApplicationException(Status.NOT_FOUND, "");
				}
				xmlUsers = Long.toString(crGroup.getId());
			} else if (userId != null)
				xmlUsers = groupManager.getGroupByUser(userId);
			else if (groupId == null)
				xmlUsers = groupManager.getUserGroupList();
			else
				xmlUsers = userManager.getUsersByUserGroup(groupId);
		} catch (RestWebApplicationException ex) {
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		return xmlUsers;
	}

	/**
	 * Remove a user from a user group, or remove a usergroup <br>
	 * DELETE /rest/api/usersgroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param group              group id
	 * @param user               user id
	 * @return Code 200
	 */
	@Path("/usersgroups")
	@DELETE
	public String deleteUsersByUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("group") Long group, @QueryParam("user") Long user) {
		// checkCredential(httpServletRequest, null, null, null);// FIXME

		Boolean isOK = false;

		try {
			if (user == null)
				isOK = groupManager.deleteUsersGroups(group);
			else
				isOK = userManager.deleteUsersFromUserGroups(user, group);

			if (isOK)
				return "Deleted";
			else
				return "Not OK";
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add a user group <br>
	 * POST /rest/api/credential/group/{group-id}
	 *
	 * @param xmlgroup           <group grid="" owner="" label=""></group>
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @return <group grid="" owner="" label=""></group>
	 */
	@Path("group")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postGroup(String xmlgroup, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			return groupManager.addUserGroup(xmlgroup, ui.getId());
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Change the group right associated to a user group. <br>
	 * POST /rest/api/RightGroup
	 * 
	 * @param user
	 * @param token
	 * @param groupId            user group id
	 * @param sc
	 * @param httpServletRequest
	 * @param groupRightId       group right
	 * 
	 * @return
	 */
	@Path("RightGroup")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response postRightGroup(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") Long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("groupRightId") Long groupRightId) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			groupManager.changeUserGroup(groupRightId, groupId, ui.getId());
			logger.info("modifié");
			return null;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "User group not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

	}

	/**
	 * Get rights in a role from a groupid <br>
	 * GET /rest/api/groupRights
	 * 
	 * @param user
	 * @param token
	 * @param groupId            role id
	 * @param sc
	 * @param httpServletRequest
	 * @return <groupRights> <groupRight gid="groupid" templateId="grouprightid>
	 *         <item AD="True/False" creator="uid"; date=""; DL="True/False" id=uuid
	 *         owner=uid"; RD="True/False" SB="True"/"False" typeId=" ";
	 *         WR="True/False"/>"; </groupRight> </groupRights>
	 */
	@Path("/groupRights")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupRights(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return groupManager.getGroupRights(ui.getId(), groupId);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
