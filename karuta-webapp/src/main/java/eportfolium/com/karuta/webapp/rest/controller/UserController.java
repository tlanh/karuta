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
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
    		 Authentication authentication,
    		 HttpServletRequest request) {
    		 HttpSession session = request.getSession(false);
    		 SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
    		 authentication = securityContext.getAuthentication();
    		 CredentialDocument userInfo = (CredentialDocument)authentication.getDetails();
    		 
    		 return new HttpEntity<>(securityManager.addUsers(xmluser));
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
    public HttpEntity<Object> getUsers(@RequestParam(value="username", required = false)String username,
                           @RequestParam(value="firstname", required = false)String firstname,
                           @RequestParam(value="lastname", required = false)String lastname,
                           Authentication authentication,
                           HttpServletRequest request) {

    	HttpSession session = request.getSession(false);
    	SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
    	authentication = securityContext.getAuthentication();
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        if (userInfo.isAdmin() || userInfo.isDesigner())
            return new HttpEntity<>(userManager.getUserList(username, firstname, lastname));
        else
            return new HttpEntity<>(userManager.getUserInfos(userInfo.getId()));

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
     * GET /rest/api/users/Portfolio/{portfolioId}/Role/{role}/users
     */
    @GetMapping(value = "/Portfolio/{portfolioId}/Role/{role}/users", produces = "application/xml")
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
    @DeleteMapping(produces = "application/xml")
    public String deleteUsers(@RequestParam("userId") Long userId) {
        securityManager.removeUsers(userId);

        return "user " + userId + " deleted";
    }

    /**
     * Delete specific user.
     *
     * DELETE /rest/api/users/user/{user-id}
     */
    @DeleteMapping(value = "/user/{user-id}", produces = "application/xml")
    public String deleteUser(@PathVariable("user-id") Long userid) {
        securityManager.removeUsers(userid);

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
                                    Authentication authentication) throws BusinessException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        if (userInfo.isAdmin() || userInfo.isDesigner()) {
            return new HttpEntity<>(securityManager.changeUser(userInfo.getId(), userid, user));

        } else if (userInfo.getId() == userid) { /// Changing self
            return new HttpEntity<>(securityManager.changeUserInfo(userInfo.getId(), userid, user));
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
