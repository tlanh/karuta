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
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/rights")
public class RightsController extends AbstractController {

    @Autowired
    private NodeManager nodeManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Change rights for a node. <br>
     * POST /rest/api/rights
     *
     * @param xmlNode
     * @param request
     * @return
     */
    @PostMapping(produces = "application/xml")
    public String postChangeRights(@RequestBody String xmlNode,
                                   HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        nodeManager.changeRights(xmlNode, ui.userId, ui.subId, "");

        return "";
    }

}
