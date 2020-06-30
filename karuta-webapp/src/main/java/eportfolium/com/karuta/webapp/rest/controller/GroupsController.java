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
import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.document.GroupInfoList;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
     * Get groups from a user id.
     *
     * GET /rest/api/groups
     *
     * @return <groups> <group id="gid" owner="uid" templateId="rrgid">GROUP
     *         LABEL</group> ... </groups>
     */
    @GetMapping
    public HttpEntity<GroupInfoList> getUserGroups(Authentication authentication) {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(groupManager.getUserGroups(userInfo.getId()));
    }

    /**
     * Get roles in a portfolio.
     *
     * GET /rest/api/groups/{id}
     */
    @GetMapping(value = "/{id}")
    public HttpEntity<GroupInfoList> getRoles(@PathVariable UUID id,
                                              Authentication authentication) {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(portfolioManager.getRolesByPortfolio(id, userInfo.getId()));
    }
}
