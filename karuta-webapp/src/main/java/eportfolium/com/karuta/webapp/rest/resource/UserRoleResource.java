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
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;

public class UserRoleResource extends AbstractResource {

	@Autowired
	private SecurityManager securityManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Add user to a role. <br>
	 * POST /rest/api/roleUser
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param grid
	 * @param userid
	 * @return
	 */
	@Path("/roleUser")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRoleUser(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("grid") long grid,
			@QueryParam("user-id") Long userid) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return securityManager.addUserRole(ui.userId, grid, userid).toString();
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}
}
