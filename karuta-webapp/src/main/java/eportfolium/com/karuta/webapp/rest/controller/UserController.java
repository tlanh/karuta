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

import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.CredentialList;
import eportfolium.com.karuta.document.ProfileList;
import eportfolium.com.karuta.document.RoleGroupList;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     *
     * @param xmluser            <users> <user id="uid"> <username></username>
     *                           <firstname></firstname> <lastname></lastname>
     *                           <admin>1/0</admin> <designer>1/0</designer>
     *                           <email></email> <active>1/0</active>
     *                           <substitute>1/0</substitute> </user> ... </users>
     * @return
     */
    @PostMapping(consumes = "application/xml", produces = "application/xml")
    public HttpEntity<CredentialList> postUser(@RequestBody CredentialList xmluser,
                                               HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(securityManager.addUsers(xmluser, ui.userId));
    }

    /**
     * Get user list.
     *
     * GET/rest/api/users*parameters:*return:
     *
     * @return *<users>*<user id="uid"> <username></username>
     *         <firstname></firstname> <lastname></lastname> <admin>1/0</admin>
     *         <designer>1/0</designer> <email></email> <active>1/0</active>
     *         <substitute>1/0</substitute> </user> ... </users>
     */
    @GetMapping(produces = "application/xml")
    public HttpEntity<Object> getUsers(@RequestParam("username") String username,
                           @RequestParam("firstname") String firstname,
                           @RequestParam("lastname") String lastname,
                           HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        if (ui.userId == 0)
            throw new GenericBusinessException("Not logged in");

        if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId))
            return new HttpEntity<>(userManager.getUserList(username, firstname, lastname));
        else if (ui.userId != 0)
            return new HttpEntity<>(userManager.getUserInfos(ui.userId));
        else
            throw new GenericBusinessException("Not authorized");
    }

    /**
     * Get a specific user info.
     *
     * GET /rest/api/users/user/{user-id}
     *
     * @return <user id="uid"> <username></username> <firstname></firstname>
     *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
     *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
     *         </user>
     */
    @GetMapping(value = "/user/{user-id}", produces = "application/xml")
    public HttpEntity<CredentialDocument> getUser(@PathVariable("user-id") int userid) {
        return new HttpEntity<>(userManager.getUserInfos(Long.valueOf(userid)));
    }

    /**
     * Get user id from username.
     *
     * GET /rest/api/users/user/username/{username}
     *
     * @return userid (long)
     */
    @GetMapping(value = "/user/username/{username}", produces = "application/xml")
    public String getUserId(@PathVariable("username") String username) throws BusinessException {
        Long userid = userManager.getUserId(username);

        if (userid == null || userid == 0) {
            // FIXME: Should we return 404 ?
            throw new GenericBusinessException("User not found");
        } else {
            return userid.toString();
        }
    }

    /**
     * Get a list of role/group for this user.
     *
     * GET /rest/api/users/user/{user-id}/groups
     *
     * @return <profiles> <profile> <group id="gid"> <label></label> <role></role>
     *         </group> </profile> </profiles>
     */
    @GetMapping(value = "/user/{user-id}/groups", produces = "application/xml")
    public HttpEntity<ProfileList> getGroupsUser(@PathVariable("user-id") long userIdCible) {
        return new HttpEntity<>(userManager.getUserRolesByUserId(userIdCible));
    }

    /**
     * Fetch userlist from a role and portfolio id.
     *
     * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/users
     */
    @GetMapping(value = "/Portfolio/{portfolio-id}/Role/{role}/users", produces = "application/xml")
    public HttpEntity<CredentialList> getUsersByRole(@PathVariable("portfolio-id") UUID portfolioId,
                                                     @PathVariable("role") String role,
                                                     HttpServletRequest request) {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(userManager.getUsersByRole(ui.userId, portfolioId, role));
    }

    /**
     * Delete users.
     *
     * DELETE /rest/api/users
     *
     * @see #deleteUser(Long, HttpServletRequest)
     */
    @DeleteMapping(produces = "application/xml")
    public String deleteUsers(@RequestParam("userId") Long userId,
                              HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        if (!securityManager.isAdmin(ui.userId) && ui.userId != userId)
            throw new GenericBusinessException("No admin right");

        securityManager.removeUsers(ui.userId, userId);

        return "user " + userId + " deleted";
    }

    /**
     * Delete specific user.
     *
     * DELETE /rest/api/users/user/{user-id}
     */
    @DeleteMapping(value = "/user/{user-id}", produces = "application/xml")
    public String deleteUser(@PathVariable("user-id") Long userid,
                             HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        securityManager.removeUsers(ui.userId, userid);

        return "user " + userid + " deleted";
    }

    /**
     * Modify user info.
     *
     * PUT /rest/api/users/user/{user-id} body: <user id="uid">
     * <username></username> <firstname></firstname> <lastname></lastname>
     * <admin>1/0</admin> <designer>1/0</designer> <email></email>
     * <active>1/0</active> <substitute>1/0</substitute> </user>
     *
     * @return <user id="uid"> <username></username> <firstname></firstname>
     *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
     *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
     *         </user>
     */
    @PutMapping(value = "/user/{user-id}", produces = "application/xml")
    public HttpEntity<Long> putUser(@RequestBody CredentialDocument user,
                          @PathVariable("user-id") long userid,
                          HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId)) {
            return new HttpEntity<>(securityManager.changeUser(ui.userId, userid, user));

        } else if (ui.userId == userid) { /// Changing self
            return new HttpEntity<>(securityManager.changeUserInfo(ui.userId, userid, user));

        } else {
            throw new GenericBusinessException("Not authorized");
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
