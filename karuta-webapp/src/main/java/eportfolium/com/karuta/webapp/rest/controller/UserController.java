package eportfolium.com.karuta.webapp.rest.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.springframework.web.bind.annotation.*;

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
     * @param user
     * @param token
     * @param groupId
     * @param request
     * @return
     */
    @PostMapping(consumes = "application/xml", produces = "application/xml")
    public Response postUser(String xmluser,
                             @CookieValue("user") String user,
                             @CookieValue("credential") String token,
                             @RequestParam("group") int groupId,
                             HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String xmlUser = securityManager.addUsers(xmluser, ui.userId);
            if (xmlUser == null) {
                return Response.status(Status.CONFLICT).entity("Existing user or invalid input").build();
            }

            return Response.ok(xmlUser).build();
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.BAD_REQUEST, ex.getMessage());
        }
    }

    /***
     *
     * Get user list. <br>
     * GET/rest/api/users*parameters:*return:
     *
     * @param user
     * @param token
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
    public String getUsers(@CookieValue("user") String user,
                           @CookieValue("credential") String token,
                           @RequestParam("username") String username,
                           @RequestParam("firstname") String firstname,
                           @RequestParam("lastname") String lastname,
                           HttpServletRequest request) {

        UserInfo ui = checkCredential(request, user, token, null);

        if (ui.userId == 0)
            throw new RestWebApplicationException(Status.FORBIDDEN, "Not logged in");

        try {
            String xmlGroups = "";
            if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId))
                xmlGroups = userManager.getUserList(ui.userId, username, firstname, lastname);
            else if (ui.userId != 0)
                xmlGroups = userManager.getUserInfos(ui.userId);
            else
                throw new RestWebApplicationException(Status.FORBIDDEN, "Not authorized");

            return xmlGroups;
        } catch (RestWebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Get a specific user info. <br>
     * GET /rest/api/users/user/{user-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param userid
     * @param request
     * @return <user id="uid"> <username></username> <firstname></firstname>
     *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
     *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
     *         </user>
     */
    @GetMapping(value = "/user/{user-id}", produces = "application/xml")
    public String getUser(@CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") int groupId,
                          @PathVariable("user-id") int userid,
                          HttpServletRequest request) {
        try {
            return userManager.getUserInfos(Long.valueOf(userid));
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(Status.NOT_FOUND, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Get user id from username. <br>
     * GET /rest/api/users/user/username/{username}
     *
     * @param user
     * @param token
     * @param username
     * @return userid (long)
     */
    @GetMapping(value = "/user/username/{username}", produces = "application/xml")
    public String getUserId(@CookieValue("user") String user,
                            @CookieValue("credential") String token,
                            @PathVariable("username") String username) {
        // FIXME : Authentication ?

        try {
            Long userid = userManager.getUserId(username);
            if (PhpUtil.empty(userid)) {
                throw new RestWebApplicationException(Status.NOT_FOUND, "User not found");
            } else {
                return userid.toString();
            }
        } catch (RestWebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, "Error : " + ex.getMessage());
        }
    }

    /**
     * Get a list of role/group for this user. <br>
     * GET /rest/api/users/user/{user-id}/groups
     *
     * @param user
     * @param token
     * @param groupId
     * @param userIdCible
     * @return <profiles> <profile> <group id="gid"> <label></label> <role></role>
     *         </group> </profile> </profiles>
     */
    @GetMapping(value = "/user/{user-id}/groups", produces = "application/xml")
    public String getGroupsUser(@CookieValue("user") String user,
                                @CookieValue("credential") String token,
                                @RequestParam("group") int groupId,
                                @PathVariable("user-id") long userIdCible) {
        // FIXME : Authentication ?

        try {
            String xmlgroupsUser = userManager.getUserRolesByUserId(userIdCible);
            return xmlgroupsUser;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch userlist from a role and portfolio id. <br>
     * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/users
     *
     * @param user
     * @param token
     * @param group
     * @param portfolioUuid
     * @param role
     * @param request
     * @return
     */
    @GetMapping(value = "/Portfolio/{portfolio-id}/Role/{role}/users", produces = "application/xml")
    public String getUsersByRole(@CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @CookieValue("group") String group,
                                 @PathVariable("portfolio-id") String portfolioUuid,
                                 @PathVariable("role") String role,
                                 HttpServletRequest request) {
        if (!isUUID(portfolioUuid)) {
            throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
        }

        UserInfo ui = checkCredential(request, user, token, group); // FIXME

        try {
            return userManager.getUsersByRole(ui.userId, portfolioUuid, role);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Delete users. <br>
     * DELETE /rest/api/users
     *
     * @see #deleteUser(String, String, long, Long, HttpServletRequest)
     *
     * @param user
     * @param token
     * @param groupId
     * @param userId
     * @param request
     * @return
     */
    @DeleteMapping(produces = "application/xml")
    public String deleteUsers(@CookieValue("user") String user,
                              @CookieValue("credential") String token,
                              @RequestParam("group") int groupId,
                              @RequestParam("userId") Long userId,
                              HttpServletRequest request) {

        UserInfo ui = checkCredential(request, user, token, null);

        if (!securityManager.isAdmin(ui.userId) && ui.userId != userId)
            throw new RestWebApplicationException(Status.FORBIDDEN, "No admin right");

        try {
            securityManager.removeUsers(ui.userId, userId);
            return "user " + userId + " deleted";
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(Status.NOT_FOUND, "user " + userId + " not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Delete specific user. <br>
     * DELETE /rest/api/users/user/{user-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param userid
     * @param request
     * @return
     */
    @DeleteMapping(value = "/user/{user-id}", produces = "application/xml")
    public String deleteUser(@CookieValue("user") String user,
                             @CookieValue("credential") String token,
                             @RequestParam("group") long groupId,
                             @PathVariable("user-id") Long userid,
                             HttpServletRequest request) {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            securityManager.removeUsers(ui.userId, userid);
            return "user " + userid + " deleted";
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(Status.NOT_FOUND, "user " + userid + " not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Modify user info. <br>
     * PUT /rest/api/users/user/{user-id} body: <user id="uid">
     * <username></username> <firstname></firstname> <lastname></lastname>
     * <admin>1/0</admin> <designer>1/0</designer> <email></email>
     * <active>1/0</active> <substitute>1/0</substitute> </user>
     *
     * @param xmlInfUser
     * @param user
     * @param token
     * @param groupId
     * @param userid
     * @param request
     * @return <user id="uid"> <username></username> <firstname></firstname>
     *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
     *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
     *         </user>
     */
    @PutMapping(value = "/user/{user-id}", produces = "application/xml")
    public String putUser(String xmlInfUser,
                          @CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") int groupId,
                          @PathVariable("user-id") long userid,
                          HttpServletRequest request) {

        UserInfo ui = checkCredential(request, user, token, null);

        try {

            String queryUser = "";
            if (securityManager.isAdmin(ui.userId) || securityManager.isCreator(ui.userId)) {
                queryUser = securityManager.changeUser(ui.userId, userid, xmlInfUser);
            } else if (ui.userId == userid) /// Changing self
            {
                String ip = request.getRemoteAddr();
                logger.info(String.format("[%s] ", ip));
                queryUser = securityManager.changeUserInfo(ui.userId, userid, xmlInfUser);
            } else
                throw new RestWebApplicationException(Status.FORBIDDEN, "Not authorized");

            return queryUser;
        } catch (RestWebApplicationException ex) {
            throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, "Error : " + ex.getMessage());
        }
    }

    /**
     * Fetch groups from a role and portfolio id <br>
     * GET /rest/api/users/Portfolio/{portfolio-id}/Role/{role}/groups.
     *
     * @param user
     * @param token
     * @param group
     * @param portfolioUuid
     * @param role
     * @param request
     * @return
     */
    @GetMapping(value = "/Portfolio/{portfolio-id}/Role/{role}/groups", produces = "application/xml")
    public String getGroupsByRole(@CookieValue("user") String user,
                                  @CookieValue("credential") String token,
                                  @CookieValue("group") String group,
                                  @PathVariable("portfolio-id") String portfolioUuid,
                                  @PathVariable("role") String role,
                                  HttpServletRequest request) {
        // FIXME: Authentication ?

        if (!isUUID(portfolioUuid)) {
            throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
        }

        try {
            return groupManager.getGroupsByRole(portfolioUuid, role);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}