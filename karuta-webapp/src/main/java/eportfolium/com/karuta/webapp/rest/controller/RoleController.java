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

import eportfolium.com.karuta.document.RoleDocument;
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
     * Fetch rights in a role.
     *
     * GET /rest/api/roles/role/{roleId}
     */
    @GetMapping(value = "/role/{roleId}")
    public HttpEntity<RoleDocument> getRole(@PathVariable Long roleId) {
        return new HttpEntity<>(userManager.getRole(roleId));
    }

    /**
     * Fetch a role in a portfolio.
     *
     * GET /rest/api/roles/portfolio/{portfolioId}
     */
    @GetMapping(value = "/portfolio/{portfolioId}")
    public String getRolePortfolio(@RequestParam String role,
                                   @PathVariable UUID portfolioId) {
        return portfolioManager.getRoleByPortfolio(role, portfolioId);
    }

    /**
     * Modify a role.
     *
     * PUT /rest/api/roles/role/{roleId}
     */
    @PutMapping(value = "/role/{roleId}")
    public String putRole(@RequestBody RoleDocument role,
                          @PathVariable long roleId) {
        return securityManager.changeRole(roleId, role).toString();
    }
}
