package eportfolium.com.karuta.webapp.rest.resource;

import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

/**
 * Managing and listing Credential groups
 * 
 * @author mlengagne
 *
 */
@Path("/usersgroups")
public class CredentialGroupResource extends AbstractResource {

	@Autowired
	private UserManager userManager;

	@Autowired
	private GroupManager groupManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Create a new user group <br>
	 * POST /rest/api/usersgroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupName          Name of the group we are creating
	 * @return groupid
	 */

	@POST
	public String postUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("label") String groupName) {

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
	@PUT
	public Response putUserInUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("group") Long groupId, @QueryParam("user") Long user, @QueryParam("label") String label) {

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
	@GET
	public String getUsersByUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("group") Long groupId, @QueryParam("user") Long userId, @QueryParam("label") String groupName) {
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
	@DELETE
	public String deleteUsersByUserGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("group") Long group, @QueryParam("user") Long user) {

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

}
