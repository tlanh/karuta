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

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groupsUsers")
public class UserGroupController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private SecurityManager securityManager;

    /**
     * Insert a user in a user group.
     *
     * POST /rest/api/groupsUsers
     */
    @PostMapping(produces = "application/xml")
    public String addUserToGroup(@RequestParam long group,
                                 @RequestParam long userId) {
        securityManager.addUserToGroup(userId, group);

        return "<ok/>";
    }

}

