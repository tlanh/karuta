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

import javax.servlet.http.HttpServletRequest;

import eportfolium.com.karuta.document.RoleDocument;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/roles")
public class RoleController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private UserManager userManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PortfolioManager portfolioManager;

    /**
     * Fetch rights in a role. <br>
     * GET /rest/api/roles/role/{role-id}
     *
     * @param roleId
     * @return
     */
    @GetMapping(value = "/role/{role-id}", produces = {"application/xml"})
    public HttpEntity<RoleDocument> getRole(@PathVariable("role-id") Long roleId) {
        return new HttpEntity<>(userManager.getRole(roleId));
    }

    /**
     * Fetch a role in a portfolio. <br>
     * GET /rest/api/roles/portfolio/{portfolio-id}
     *
     * @param role
     * @param portfolioId
     * @param request
     * @return
     */
    @GetMapping(value = "/portfolio/{portfolio-id}", produces = {"application/xml"})
    public String getRolePortfolio(@RequestParam("role") String role,
                                   @RequestParam("portfolio-id") UUID portfolioId,
                                   HttpServletRequest request) {

        UserInfo ui = checkCredential(request);

        return portfolioManager.getRoleByPortfolio(role, portfolioId, ui.userId);
    }

    /**
     * Modify a role. <br>
     * PUT /rest/api/roles/role/{role-id}
     *
     * @param role
     * @param roleId
     * @param request
     * @return
     */
    @PutMapping(value = "/role/{role-id}", produces = "application/xml")
    public String putRole(@RequestBody RoleDocument role,
                          @PathVariable("role-id") long roleId,
                          HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return securityManager.changeRole(ui.userId, roleId, role).toString();
    }
}
