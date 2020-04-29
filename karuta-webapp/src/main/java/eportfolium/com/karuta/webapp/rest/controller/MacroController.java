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

package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Partie utilisation des macro-commandes et gestion
 *
 * @author mlengagne
 *
 */
@RestController
public class MacroController extends AbstractController {
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
     * @param httpServletRequest
     * @return
     */
    @PostMapping(value = "/action/{uuid}/{macro-name}",
            consumes = {"text/plain", "application/xml"}, produces = "text/plain")
    public String postMacro(@RequestBody String xmlNode,
                            @CookieValue("user") String user,
                            @CookieValue("credential") String token,
                            @CookieValue("group") String group,
                            @PathVariable("uuid") String uuid,
                            @PathVariable("macro-name") String macroName,
                            HttpServletRequest httpServletRequest) throws RestWebApplicationException {
        if (!isUUID(uuid)) {
            throw new RestWebApplicationException(HttpStatus.BAD_REQUEST, "Not UUID");
        }

        UserInfo ui = checkCredential(httpServletRequest, user, token, group);

        try {
            // On exécute l'action sur le noeud uuid.
            if (uuid != null && macroName != null) {
                return nodeManager.executeMacroOnNode(ui.userId, uuid, macroName);
            }
            // Erreur de requête
            else {
                return "";
            }
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
