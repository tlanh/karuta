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

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
@RequestMapping("/rolerightsgroups")
public class RoleRightsGroupsController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private UserManager userManager;

    @Autowired
    private PortfolioManager portfolioManager;

    @Autowired
    private SecurityManager securityManager;

    /**
     * List roles. <br>
     * GET /rest/api/rolerightsgroups
     *
     * @param portfolio
     * @param queryuser
     * @param role
     * @param request
     * @return
     */
    @GetMapping(produces = "application/xml")
    public String getRightsGroup(@RequestParam("portfolio") UUID portfolio,
                                 @RequestParam("user") Long queryuser,
                                 @RequestParam("role") String role,
                                 HttpServletRequest request) throws BusinessException {

        checkCredential(request);

        return userManager.getRoleList(portfolio, queryuser, role);
    }

    /**
     * List all users in a specified roles. <br>
     * GET /rest/api/rolerightsgroups/all/users
     *
     * @param portfolioId
     * @param request
     * @return
     */
    @GetMapping(value = "/all/users", produces = "application/xml")
    public String getPortfolioRightInfo(@RequestParam("portfolio") UUID portfolioId,
                                        HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        // Retourne le contenu du type
        if (portfolioId != null) {
            return userManager.getUserRolesByPortfolio(portfolioId, ui.userId);
        } else {
            return "";
        }
    }

    /**
     * List rights in the specified role <br>
     * GET /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
     *
     * @param rrgId
     * @param request
     * @return
     */
    @GetMapping(value = "/rolerightsgroup/{rolerightsgroup-id}", produces = "application/xml")
    public String getRightInfo(@PathVariable("rolerightsgroup-id") Long rrgId,
                               HttpServletRequest request) {
        UserInfo ui = checkCredential(request);

        if (rrgId != null) {
            return userManager.getUserRole(rrgId);
        } else {
            return "";
        }
    }

    /**
     * Add user in a role. <br>
     * POST
     * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
     *
     * @param rrgId
     * @param queryuser
     * @param request
     * @return
     */
    @PostMapping(value = "/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}",
            produces = "application/xml")
    public String postRightGroupUsers(@PathVariable("rolerightsgroup-id") Long rrgId,
                                      @PathVariable("user-id") Long queryuser,
                                      HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return securityManager.addUserRole(ui.userId, rrgId, queryuser);
    }

    /**
     * Add user in a role. <br>
     * POST /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users
     *
     * @param xmlNode
     * @param rrgId
     * @param request
     * @return
     */
    @PostMapping(value = "/rolerightsgroup/{rolerightsgroup-id}/users", produces = "application/xml")
    public String postRightGroupUser(@RequestBody String xmlNode,
                                     @PathVariable("rolerightsgroup-id") Long rrgId,
                                     HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return securityManager.addUsersToRole(ui.userId, rrgId, xmlNode);
    }

    /**
     * Delete a role. <br>
     * DELETE /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
     *
     * @param groupRightInfoId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/rolerightsgroup/{rolerightsgroup-id}", produces = "application/xml")
    public String deleteRightGroup(@PathVariable("rolerightsgroup-id") Long groupRightInfoId,
                                   HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);

        securityManager.removeRole(ui.userId, groupRightInfoId);

        return "";
    }

    /**
     * Remove user from a role. <br>
     * DELETE
     * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
     *
     **/
    @DeleteMapping(value = "/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}",
            produces = "application/xml")
    public String deleteRightGroupUser(@PathVariable("rolerightsgroup-id") Long rrgId,
                                       @PathVariable("user-id") Integer queryuser,
                                       HttpServletRequest httpServletRequest) throws BusinessException {
        UserInfo ui = checkCredential(httpServletRequest);

        securityManager.removeUserRole(ui.userId, rrgId);

        return "";
    }

    /**
     * Remove all users from a role. <br>
     * DELETE /rest/api/rolerightsgroups/all/users
     *
     * @param portfolioId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/all/users", produces = "application/xml")
    public String deletePortfolioRightInfo(@RequestParam("portfolio") UUID portfolioId,
                                           HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);

        if (portfolioId != null) {
            securityManager.removeUsersFromRole(ui.userId, portfolioId);
        }

        return "";
    }

    /**
     * Change a right in role. <br>
     * PUT /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
     *
     * @param xmlNode
     * @param rrgId
     * @param request
     * @return
     */
    @PutMapping(value = "/rolerightsgroup/{rolerightsgroup-id}", produces = "application/xml")
    public String putRightInfo(@RequestBody String xmlNode,
                               @PathVariable("rolerightsgroup-id") Long rrgId,
                               HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        if (rrgId != null) {
            securityManager.changeRole(ui.userId, rrgId, xmlNode);
        }

        return "";
    }

    /**
     * Add a role in the portfolio <br>
     * POST /rest/api/rolerightsgroups/{portfolio-id}
     *
     * @param xmlNode
     * @param portfolioId
     * @param request
     * @return
     */
    @PostMapping(value = "/{portfolio-id}", produces = "application/xml")
    public String postRightGroups(@RequestBody String xmlNode,
                                  @PathVariable("portfolio-id") UUID portfolioId,
                                  HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        return portfolioManager.addRoleInPortfolio(ui.userId, portfolioId, xmlNode);
    }

}

