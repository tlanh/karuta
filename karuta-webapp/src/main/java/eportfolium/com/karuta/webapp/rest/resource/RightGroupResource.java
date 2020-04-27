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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("RightGroup")
public class RightGroupResource extends AbstractResource {

	@Autowired
	private GroupManager groupManager;

	@InjectLogger
	private static Logger logger;

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
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response postRightGroup(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") Long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("groupRightId") Long groupRightId) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			groupManager.changeUserGroup(groupRightId, groupId, ui.userId);
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

}
