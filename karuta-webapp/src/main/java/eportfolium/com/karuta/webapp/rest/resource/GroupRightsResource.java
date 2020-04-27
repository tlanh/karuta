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
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/groupRights")
public class GroupRightsResource extends AbstractResource {

	@Autowired
	private GroupManager groupManager;

	@InjectLogger
	private static Logger logger;

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
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupRights(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return groupManager.getGroupRights(ui.userId, groupId);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Delete a right definition. <br>
	 * DELETE /rest/api/groupRights
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param groupRightId
	 * @return
	 */

	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteGroupRights(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("groupRightId") Long groupRightId) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			groupManager.removeRights(groupId, ui.userId);
			return "supprimé";
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Group " + groupId + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
