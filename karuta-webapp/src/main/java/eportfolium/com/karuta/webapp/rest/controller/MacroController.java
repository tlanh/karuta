package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

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
    public String postMacro(String xmlNode,
                            @CookieValue("user") String user,
                            @CookieValue("credential") String token,
                            @CookieValue("group") String group,
                            @PathVariable("uuid") String uuid,
                            @PathVariable("macro-name") String macroName,
                            HttpServletRequest httpServletRequest) {
        if (!isUUID(uuid)) {
            throw new RestWebApplicationException(Response.Status.BAD_REQUEST, "Not UUID");
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
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
