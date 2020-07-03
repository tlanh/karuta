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

import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.document.CredentialList;
import eportfolium.com.karuta.document.GroupUserList;
import eportfolium.com.karuta.document.RoleDocument;
import eportfolium.com.karuta.document.RoleRightsGroupDocument;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/rolerightsgroups")
public class RoleRightsGroupsController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private UserManager userManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private GroupManager groupManager;

    /**
     * List roles.
     *
     * GET /rest/api/rolerightsgroups
     */
    @GetMapping
    public HttpEntity<Object> getGroups(@RequestParam UUID portfolio,
                                        @RequestParam Long user,
                                        @RequestParam String role) {

        if (portfolio != null && role != null && user == null) {
            GroupRightInfo gri = groupManager.getByPortfolioAndLabel(portfolio, role);

            return new HttpEntity<>(gri.getId());
        } else {
            return new HttpEntity<>(userManager.getRoleList(portfolio, user));
        }
    }

    /**
     * List all users in a specified roles.
     *
     * GET /rest/api/rolerightsgroups/all/users
     */
    @GetMapping(value = "/all/users")
    public HttpEntity<GroupUserList> getPortfolioUsers(@RequestParam UUID portfolio,
                                                       @AuthenticationPrincipal UserInfo userInfo) {

        return new HttpEntity<>(userManager.getUserRolesByPortfolio(portfolio, userInfo.getId()));
    }

    /**
     * List rights in the specified role
     *
     * GET /rest/api/rolerightsgroups/rolerightsgroup/{id}
     */
    @GetMapping(value = "/rolerightsgroup/{id}")
    public HttpEntity<RoleRightsGroupDocument> getRightInfo(@PathVariable Long id) {
        return new HttpEntity<>(userManager.getUserRole(id));
    }

    /**
     * Add user in a role.
     *
     * POST /rest/api/rolerightsgroups/rolerightsgroup/{id}/users/user/{userId}
     */
    @PostMapping(value = "/rolerightsgroup/{id}/users/user/{userId}")
    public String addUserRole(@PathVariable Long id,
                              @PathVariable Long userId) {
        return securityManager.addUserRole(id, userId);
    }

    /**
     * Add user in a role.
     *
     * POST /rest/api/rolerightsgroups/rolerightsgroup/{id}/users
     */
    @PostMapping(value = "/rolerightsgroup/{id}/users")
    public ResponseEntity<String> postUsers(@RequestBody CredentialList users,
                                            @PathVariable Long id) {
        securityManager.addUsersToRole(id, users);

        return ResponseEntity.ok().build();
    }

    /**
     * Delete a role.
     *
     * DELETE /rest/api/rolerightsgroups/rolerightsgroup/{id}
     */
    @DeleteMapping(value = "/rolerightsgroup/{id}")
    public String removeRole(@PathVariable Long id) {
        securityManager.removeRole(id);

        return "";
    }

    /**
     * Remove user from a role.
     *
     * DELETE /rest/api/rolerightsgroups/rolerightsgroup/{id}/users/user/{user-id}
     */
    @DeleteMapping(value = "/rolerightsgroup/{id}/users/user/{userId}")
    public String removeUserRole(@PathVariable Long id,
                                 @PathVariable Long userId) {
        securityManager.removeUserRole(userId, id);

        return "";
    }

    /**
     * Remove all users from a role.
     *
     * DELETE /rest/api/rolerightsgroups/all/users
     */
    @DeleteMapping(value = "/all/users")
    public String removeUsersFromRole(@RequestParam UUID portfolio) {
        securityManager.removeUsersFromRole(portfolio);

        return "";
    }

    /**
     * Change a right in role.
     *
     * PUT /rest/api/rolerightsgroups/rolerightsgroup/{id}
     */
    @PutMapping(value = "/rolerightsgroup/{id}")
    public String changeROle(@RequestBody RoleDocument role,
                             @PathVariable Long id) {

        securityManager.changeRole(id, role);

        return "";
    }
}
