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

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.document.GroupInfoList;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupsController extends AbstractController {

    @Autowired
    private GroupManager groupManager;

    @Autowired
    private PortfolioManager portfolioManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Get groups from a user id <br>
     * GET /rest/api/groups
     *
     * @param request
     * @return <groups> <group id="gid" owner="uid" templateId="rrgid">GROUP
     *         LABEL</group> ... </groups>
     */
    @GetMapping(produces = "application/xml")
    public HttpEntity<GroupInfoList> getGroups(HttpServletRequest request) {
        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(groupManager.getUserGroups(ui.userId));
    }

    /**
     * Get roles in a portfolio <br>
     * GET /rest/api/groups/{portfolio-id}
     *
     * @param portfolioId
     * @param request
     * @return
     */
    @GetMapping(value = "/{portfolio-id}", produces = "application/xml")
    public HttpEntity<GroupInfoList> getGroupsPortfolio(@PathVariable("portfolio-id") UUID portfolioId,
                                                        HttpServletRequest request) {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(portfolioManager.getRolesByPortfolio(portfolioId, ui.userId));
    }
}
