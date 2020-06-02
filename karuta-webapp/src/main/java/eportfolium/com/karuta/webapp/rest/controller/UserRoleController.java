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
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserRoleController extends AbstractController {

    @Autowired
    private SecurityManager securityManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Add user to a role.
     *
     * POST /rest/api/roleUser
     */
    @PostMapping(value = "/roleUser", produces = "application/xml")
    public String postRoleUser(@RequestParam  long grid,
                               @RequestParam("user-id") Long userid,
                               HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return securityManager.addUserRole(ui.userId, grid, userid);
    }
}

