package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

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
     * @param user
     * @param token
     * @param group
     * @param portfolio
     * @param queryuser
     * @param role
     * @param request
     * @return
     */
    @GetMapping(produces = "application/xml")
    public String getRightsGroup(@CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @CookieValue("group") String group,
                                 @RequestParam("portfolio") String portfolio,
                                 @RequestParam("user") Long queryuser,
                                 @RequestParam("role") String role,
                                 HttpServletRequest request) {
        if (!isUUID(portfolio)) {
            throw new RestWebApplicationException(Response.Status.BAD_REQUEST, "Not UUID");
        }

        checkCredential(request, user, token, group); // FIXME ?

        try {
            // Retourne le contenu du type
            return userManager.getRoleList(portfolio, queryuser, role);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("getRightsGroup", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * List all users in a specified roles. <br>
     * GET /rest/api/rolerightsgroups/all/users
     *
     * @param user
     * @param token
     * @param group
     * @param portId
     * @param request
     * @return
     */
    @GetMapping(value = "/all/users", produces = "application/xml")
    public String getPortfolioRightInfo(@CookieValue("user") String user,
                                        @CookieValue("credential") String token,
                                        @CookieValue("group") String group,
                                        @RequestParam("portfolio") String portId,
                                        HttpServletRequest request) {
        if (!isUUID(portId)) {
            throw new RestWebApplicationException(Response.Status.BAD_REQUEST, "Not UUID");
        }
        UserInfo ui = checkCredential(request, user, token, group); // FIXME
        String returnValue = "";
        try {
            // Retourne le contenu du type
            if (portId != null) {
                returnValue = userManager.getUserRolesByPortfolio(portId, ui.userId);
            }
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("getPortfolioRightInfo", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * List rights in the specified role <br>
     * GET /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
     *
     * @param user
     * @param token
     * @param group
     * @param rrgId
     * @param request
     * @return
     */
    @GetMapping(value = "/rolerightsgroup/{rolerightsgroup-id}", produces = "application/xml")
    public String getRightInfo(@CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @CookieValue("group") String group,
                               @PathVariable("rolerightsgroup-id") Long rrgId,
                               HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, group);

        String returnValue = "";

        try {
            // Retourne le contenu du type
            if (rrgId != null) {
                returnValue = userManager.getUserRole(rrgId);
            }
            return returnValue;
        } catch (RestWebApplicationException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("getRightInfo", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Add user in a role. <br>
     * POST
     * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param group
     * @param rrgId
     * @param queryuser
     * @param request
     * @return
     */
    @PostMapping(value = "/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}",
            produces = "application/xml")
    public String postRightGroupUsers(String xmlNode,
                                      @CookieValue("user") String user,
                                      @CookieValue("credential") String token,
                                      @CookieValue("group") String group,
                                      @PathVariable("rolerightsgroup-id") Long rrgId,
                                      @PathVariable("user-id") Long queryuser,
                                      HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, group);
        try {
            return securityManager.addUserRole(ui.userId, rrgId, queryuser);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Add user in a role. <br>
     * POST /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param group
     * @param rrgId
     * @param request
     * @return
     */
    @PostMapping(value = "/rolerightsgroup/{rolerightsgroup-id}/users", produces = "application/xml")
    public String postRightGroupUser(String xmlNode,
                                     @CookieValue("user") String user,
                                     @CookieValue("credential") String token,
                                     @CookieValue("group") String group,
                                     @PathVariable("rolerightsgroup-id") Long rrgId,
                                     HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, group);

        try {
            return securityManager.addUsersToRole(ui.userId, rrgId, xmlNode);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Delete a role. <br>
     * DELETE /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param group
     * @param groupRightInfoId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/rolerightsgroup/{rolerightsgroup-id}", produces = "application/xml")
    public String deleteRightGroup(String xmlNode,
                                   @CookieValue("user") String user,
                                   @CookieValue("credential") String token,
                                   @CookieValue("group") String group,
                                   @PathVariable("rolerightsgroup-id") Long groupRightInfoId,
                                   HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            securityManager.removeRole(ui.userId, groupRightInfoId);
            return "";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Remove user from a role. <br>
     * DELETE
     * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
     *
     **/
    @DeleteMapping(value = "/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}",
            produces = "application/xml")
    public String deleteRightGroupUser(String xmlNode,
                                       @CookieValue("user") String user,
                                       @CookieValue("credential") String token,
                                       @CookieValue("group") String group,
                                       @PathVariable("rolerightsgroup-id") Long rrgId,
                                       @PathVariable("user-id") Integer queryuser,
                                       HttpServletRequest httpServletRequest) {
        UserInfo ui = checkCredential(httpServletRequest, user, token, null);

        try {
            securityManager.removeUserRole(ui.userId, rrgId);
            return "";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Remove all users from a role. <br>
     * DELETE /rest/api/rolerightsgroups/all/users
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param group
     * @param portId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/all/users", produces = "application/xml")
    public String deletePortfolioRightInfo(String xmlNode,
                                           @CookieValue("user") String user,
                                           @CookieValue("credential") String token,
                                           @CookieValue("group") String group,
                                           @RequestParam("portfolio") String portId,
                                           HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, group);

        String returnValue = "";
        try {
            // Retourne le contenu du type
            if (portId != null) {
                securityManager.removeUsersFromRole(ui.userId, portId);
            }
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Change a right in role. <br>
     * PUT /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param group
     * @param rrgId
     * @param request
     * @return
     */
    @PutMapping(value = "/rolerightsgroup/{rolerightsgroup-id}", produces = "application/xml")
    public String putRightInfo(String xmlNode,
                               @CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @CookieValue("group") String group,
                               @PathVariable("rolerightsgroup-id") Long rrgId,
                               HttpServletRequest request) {

        UserInfo ui = checkCredential(request, user, token, group);

        try {
            // Retourne le contenu du type
            if (rrgId != null) {
                securityManager.changeRole(ui.userId, rrgId, xmlNode);
            }
            return "";
        } catch (DoesNotExistException e) {
            throw new RestWebApplicationException(Response.Status.NOT_FOUND, "Role with id " + rrgId + " not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Add a role in the portfolio <br>
     * POST /rest/api/rolerightsgroups/{portfolio-id}
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param group
     * @param portfolio
     * @param request
     * @return
     */
    @PostMapping(value = "/{portfolio-id}", produces = "application/xml")
    public String postRightGroups(String xmlNode,
                                  @CookieValue("user") String user,
                                  @CookieValue("credential") String token,
                                  @CookieValue("group") String group,
                                  @PathVariable("portfolio-id") String portfolio,
                                  HttpServletRequest request) {
        if (!isUUID(portfolio)) {
            throw new RestWebApplicationException(Response.Status.BAD_REQUEST, "Not UUID");
        }

        UserInfo ui = checkCredential(request, user, token, group);
        String returnValue = "";
        try {
            returnValue = portfolioManager.addRoleInPortfolio(ui.userId, portfolio, xmlNode);
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}

