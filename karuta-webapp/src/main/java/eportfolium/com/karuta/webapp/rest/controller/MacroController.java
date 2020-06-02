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

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
public class MacroController extends AbstractController {
    @InjectLogger
    private static Logger logger;

    @Autowired
    private NodeManager nodeManager;

    /**
     * Executing pre-defined macro command on a node.
     *
     * POST /rest/api/action/{uuid}/{macro-name}
     */
    @PostMapping(value = "/action/{uuid}/{macro-name}", produces = "text/plain")
    public String postMacro(@PathVariable("uuid") UUID uuid,
                            @PathVariable("macro-name") String macroName,
                            HttpServletRequest httpServletRequest)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(httpServletRequest);

        if (uuid != null && macroName != null) {
            return nodeManager.executeMacroOnNode(ui.userId, uuid, macroName);
        } else {
            return "";
        }
    }
}
