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
import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.CredentialList;
import eportfolium.com.karuta.document.ProfileList;
import eportfolium.com.karuta.document.RoleGroupList;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {

    @Autowired
    private UserManager userManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private GroupManager groupManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Add a user.
     *
     * POST /rest/api/users
     */
    @PostMapping
    public HttpEntity<CredentialList> postUser(@RequestBody CredentialList xmluser) {
        return new HttpEntity<>(securityManager.addUsers(xmluser));
    }

    /**
     * Get user list.
     *
     * GET/rest/api/users*parameters
     */
    @GetMapping
    public HttpEntity<Object> getUsers(@RequestParam(required = false) String username,
                           @RequestParam(required = false) String firstname,
                           @RequestParam(required = false) String lastname,
                           Authentication authentication) {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        if (userInfo.isAdmin() || userInfo.isDesigner())
            return new HttpEntity<>(userManager.getUserList(username, firstname, lastname));
        else
            return new HttpEntity<>(userManager.getUserInfos(userInfo.getId()));

    }

    /**
     * Get a specific user info.
     *
     * GET /rest/api/users/user/{id}
     */
    @GetMapping(value = "/user/{id}")
    public HttpEntity<CredentialDocument> getUser(@PathVariable Long id) {
        return new HttpEntity<>(userManager.getUserInfos(id));
    }

    /**
     * Get user id from username.
     *
     * GET /rest/api/users/user/username/{username}
     *
     * @return userid (long)
     */
    @GetMapping(value = "/user/username/{username}")
    public HttpEntity<String> getUserId(@PathVariable String username) {
        Long userid = userManager.getUserId(username);

        if (userid == null || userid == 0) {
            return ResponseEntity.notFound().build();
        } else {
            return new HttpEntity<>(userid.toString());
        }
    }

    /**
     * Get a list of role/group for this user.
     *
     * GET /rest/api/users/user/{user-id}/groups.
     */
    @GetMapping(value = "/user/{id}/groups")
    public HttpEntity<ProfileList> getGroupsUser(@PathVariable("id") long id) {
        return new HttpEntity<>(userManager.getUserRolesByUserId(id));
    }

    /**
     * Fetch userlist from a role and portfolio id.
     *
     * GET /rest/api/users/Portfolio/{portfolioId}/Role/{role}/users
     */
    @GetMapping(value = "/Portfolio/{portfolioId}/Role/{role}/users")
    public HttpEntity<CredentialList> getUsersByRole(@PathVariable UUID portfolioId,
                                                     @PathVariable String role) {
        return new HttpEntity<>(userManager.getUsersByRole(portfolioId, role));
    }

    /**
     * Delete users.
     *
     * DELETE /rest/api/users
     *
     * @see #deleteUser(Long)
     */
    @DeleteMapping
    public String deleteUsers(@RequestParam long userId) {
        securityManager.removeUsers(userId);

        return "user " + userId + " deleted";
    }

    /**
     * Delete specific user.
     *
     * DELETE /rest/api/users/user/{id}
     */
    @DeleteMapping(value = "/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        securityManager.removeUsers(id);

        return "user " + id + " deleted";
    }

    /**
     * Modify user info.
     *
     * PUT /rest/api/users/user/{id}
     */
    @PutMapping(value = "/user/{id}")
    public HttpEntity<String> putUser(@RequestBody CredentialDocument user,
                                      @PathVariable long id,
                                      Authentication authentication) throws BusinessException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        if (userInfo.isAdmin() || userInfo.isDesigner()) {
            return new HttpEntity<>(securityManager.changeUser(userInfo.getId(), id, user).toString());

        } else if (userInfo.getId() == id) { /// Changing self
            return new HttpEntity<>(securityManager.changeUserInfo(userInfo.getId(), id, user).toString());

        } else {
            return ResponseEntity.status(403).body("Not authorized");
        }
    }

    /**
     * Fetch groups from a role and portfolio id.
     *
     * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/groups.
     */
    @GetMapping(value = "/Portfolio/{portfolio-id}/Role/{role}/groups")
    public HttpEntity<RoleGroupList> getGroupsByRole(@PathVariable("portfolio-id") UUID portfolioId,
                                                     @PathVariable("role") String role) {
        return new HttpEntity<>(groupManager.getGroupsByRole(portfolioId, role));
    }
}
