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

import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author mlengagne
 *
 */

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
     * Add a user. <br>
     * POST /rest/api/users
     *
     * @param xmluser            <users> <user id="uid"> <username></username>
     *                           <firstname></firstname> <lastname></lastname>
     *                           <admin>1/0</admin> <designer>1/0</designer>
     *                           <email></email> <active>1/0</active>
     *                           <substitute>1/0</substitute> </user> ... </users>
     * @param request
     * @return
     */
    @PostMapping(consumes = "application/xml", produces = "application/xml")
    public ResponseEntity<String> postUser(@RequestBody String xmluser,
                                           HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);
        String xmlUser = securityManager.addUsers(xmluser, ui.userId);

        if (xmlUser == null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Existing user or invalid input");
        }

        return ResponseEntity.ok(xmlUser);
    }

    /***
     *
     * Get user list. <br>
     * GET/rest/api/users*parameters:*return:
     *
     * @param username
     * @param firstname
     * @param lastname
     * @param request
     * @return *<users>*<user id="uid"> <username></username>
     *         <firstname></firstname> <lastname></lastname> <admin>1/0</admin>
     *         <designer>1/0</designer> <email></email> <active>1/0</active>
     *         <substitute>1/0</substitute> </user> ... </users>
     */
    @GetMapping(produces = "application/xml")
    public String getUsers(@RequestParam("username") String username,
                           @RequestParam("firstname") String firstname,
                           @RequestParam("lastname") String lastname,
                           HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        if (ui.userId == 0)
            throw new GenericBusinessException("Not logged in");

        if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId))
            return userManager.getUserList(ui.userId, username, firstname, lastname);
        else if (ui.userId != 0)
            return userManager.getUserInfos(ui.userId);
        else
            throw new GenericBusinessException("Not authorized");
    }

    /**
     * Get a specific user info. <br>
     * GET /rest/api/users/user/{user-id}
     *
     * @param userid
     * @return <user id="uid"> <username></username> <firstname></firstname>
     *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
     *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
     *         </user>
     */
    @GetMapping(value = "/user/{user-id}", produces = "application/xml")
    public String getUser(@PathVariable("user-id") int userid) {
        return userManager.getUserInfos(Long.valueOf(userid));
    }

    /**
     * Get user id from username. <br>
     * GET /rest/api/users/user/username/{username}
     *
     * @param username
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
     * Get a list of role/group for this user. <br>
     * GET /rest/api/users/user/{user-id}/groups
     *
     * @param userIdCible
     * @return <profiles> <profile> <group id="gid"> <label></label> <role></role>
     *         </group> </profile> </profiles>
     */
    @GetMapping(value = "/user/{user-id}/groups", produces = "application/xml")
    public String getGroupsUser(@PathVariable("user-id") long userIdCible) {
        return userManager.getUserRolesByUserId(userIdCible);
    }

    /**
     * Fetch userlist from a role and portfolio id. <br>
     * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/users
     *
     * @param portfolioId
     * @param role
     * @param request
     * @return
     */
    @GetMapping(value = "/Portfolio/{portfolio-id}/Role/{role}/users", produces = "application/xml")
    public String getUsersByRole(@PathVariable("portfolio-id") UUID portfolioId,
                                 @PathVariable("role") String role,
                                 HttpServletRequest request) {

        UserInfo ui = checkCredential(request);

        return userManager.getUsersByRole(ui.userId, portfolioId, role);
    }

    /**
     * Delete users. <br>
     * DELETE /rest/api/users
     *
     * @see #deleteUser(Long, HttpServletRequest)
     *
     * @param userId
     * @param request
     * @return
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
     * Delete specific user. <br>
     * DELETE /rest/api/users/user/{user-id}
     *
     * @param userid
     * @param request
     * @return
     */
    @DeleteMapping(value = "/user/{user-id}", produces = "application/xml")
    public String deleteUser(@PathVariable("user-id") Long userid,
                             HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        securityManager.removeUsers(ui.userId, userid);

        return "user " + userid + " deleted";
    }

    /**
     * Modify user info. <br>
     * PUT /rest/api/users/user/{user-id} body: <user id="uid">
     * <username></username> <firstname></firstname> <lastname></lastname>
     * <admin>1/0</admin> <designer>1/0</designer> <email></email>
     * <active>1/0</active> <substitute>1/0</substitute> </user>
     *
     * @param xmlInfUser
     * @param userid
     * @param request
     * @return <user id="uid"> <username></username> <firstname></firstname>
     *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
     *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
     *         </user>
     */
    @PutMapping(value = "/user/{user-id}", produces = "application/xml")
    public String putUser(@RequestBody String xmlInfUser,
                          @PathVariable("user-id") long userid,
                          HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId)) {
            return securityManager.changeUser(ui.userId, userid, xmlInfUser);

        } else if (ui.userId == userid) { /// Changing self
            String ip = request.getRemoteAddr();
            logger.info(String.format("[%s] ", ip));

            return securityManager.changeUserInfo(ui.userId, userid, xmlInfUser);

        } else {
            throw new GenericBusinessException("Not authorized");
        }
    }

    /**
     * Fetch groups from a role and portfolio id <br>
     * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/groups.
     *
     * @param portfolioId
     * @param role
     * @return
     */
    @GetMapping(value = "/Portfolio/{portfolio-id}/Role/{role}/groups", produces = "application/xml")
    public String getGroupsByRole(@PathVariable("portfolio-id") UUID portfolioId,
                                  @PathVariable("role") String role) {
        return groupManager.getGroupsByRole(portfolioId, role);
    }
}
