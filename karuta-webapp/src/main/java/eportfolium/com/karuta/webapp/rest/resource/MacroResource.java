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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

/**
 * Partie utilisation des macro-commandes et gestion
 * 
 * @author mlengagne
 *
 */
public class MacroResource extends AbstractResource {

	@InjectLogger
	private static Logger logger;

	@Autowired
	private NodeManager nodeManager;

	/**
	 * Executing pre-defined macro command on a node. <br>
	 * POST /rest/api/action/{uuid}/{macro-name}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param uuid
	 * @param macroName
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/action/{uuid}/{macro-name}")
	@POST
	@Consumes(MediaType.APPLICATION_XML + "," + MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String postMacro(String xmlNode, @CookieParam("user") String user, @CookieParam("credential") String token,
			@CookieParam("group") String group, @PathParam("uuid") String uuid,
			@PathParam("macro-name") String macroName, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(uuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			// On exécute l'action sur le noeud uuid.
			if (uuid != null && macroName != null) {
				returnValue = nodeManager.executeMacroOnNode(ui.userId, uuid, macroName);
			}
			// Erreur de requête
			else {
				returnValue = "";
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

}
