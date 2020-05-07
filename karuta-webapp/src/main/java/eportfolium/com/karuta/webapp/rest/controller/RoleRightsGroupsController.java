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
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
                                 @RequestParam("portfolio") UUID portfolio,
                                 @RequestParam("user") Long queryuser,
                                 @RequestParam("role") String role,
                                 HttpServletRequest request) throws RestWebApplicationException {

        checkCredential(request, user, token, group); // FIXME ?

        try {
            // Retourne le contenu du type
            return userManager.getRoleList(portfolio, queryuser, role);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("getRightsGroup", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * List all users in a specified roles. <br>
     * GET /rest/api/rolerightsgroups/all/users
     *
     * @param user
     * @param token
     * @param group
     * @param portfolioId
     * @param request
     * @return
     */
    @GetMapping(value = "/all/users", produces = "application/xml")
    public String getPortfolioRightInfo(@CookieValue("user") String user,
                                        @CookieValue("credential") String token,
                                        @CookieValue("group") String group,
                                        @RequestParam("portfolio") UUID portfolioId,
                                        HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, group); // FIXME
        String returnValue = "";

        try {
            // Retourne le contenu du type
            if (portfolioId != null) {
                returnValue = userManager.getUserRolesByPortfolio(portfolioId, ui.userId);
            }
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("getPortfolioRightInfo", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
                               HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, group);

        String returnValue = "";

        try {
            // Retourne le contenu du type
            if (rrgId != null) {
                returnValue = userManager.getUserRole(rrgId);
            }
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("getRightInfo", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Add user in a role. <br>
     * POST
     * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
     *
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
    public String postRightGroupUsers(@CookieValue("user") String user,
                                      @CookieValue("credential") String token,
                                      @CookieValue("group") String group,
                                      @PathVariable("rolerightsgroup-id") Long rrgId,
                                      @PathVariable("user-id") Long queryuser,
                                      HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, group);
        try {
            return securityManager.addUserRole(ui.userId, rrgId, queryuser);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
    public String postRightGroupUser(@RequestBody String xmlNode,
                                     @CookieValue("user") String user,
                                     @CookieValue("credential") String token,
                                     @CookieValue("group") String group,
                                     @PathVariable("rolerightsgroup-id") Long rrgId,
                                     HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, group);

        try {
            return securityManager.addUsersToRole(ui.userId, rrgId, xmlNode);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Delete a role. <br>
     * DELETE /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
     *
     * @param user
     * @param token
     * @param group
     * @param groupRightInfoId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/rolerightsgroup/{rolerightsgroup-id}", produces = "application/xml")
    public String deleteRightGroup(@CookieValue("user") String user,
                                   @CookieValue("credential") String token,
                                   @CookieValue("group") String group,
                                   @PathVariable("rolerightsgroup-id") Long groupRightInfoId,
                                   HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            securityManager.removeRole(ui.userId, groupRightInfoId);
            return "";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
    public String deleteRightGroupUser(@CookieValue("user") String user,
                                       @CookieValue("credential") String token,
                                       @CookieValue("group") String group,
                                       @PathVariable("rolerightsgroup-id") Long rrgId,
                                       @PathVariable("user-id") Integer queryuser,
                                       HttpServletRequest httpServletRequest) throws RestWebApplicationException {
        UserInfo ui = checkCredential(httpServletRequest, user, token, null);

        try {
            securityManager.removeUserRole(ui.userId, rrgId);
            return "";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Remove all users from a role. <br>
     * DELETE /rest/api/rolerightsgroups/all/users
     *
     * @param user
     * @param token
     * @param group
     * @param portfolioId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/all/users", produces = "application/xml")
    public String deletePortfolioRightInfo(@CookieValue("user") String user,
                                           @CookieValue("credential") String token,
                                           @CookieValue("group") String group,
                                           @RequestParam("portfolio") UUID portfolioId,
                                           HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, group);

        String returnValue = "";
        try {
            // Retourne le contenu du type
            if (portfolioId != null) {
                securityManager.removeUsersFromRole(ui.userId, portfolioId);
            }
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
    public String putRightInfo(@RequestBody String xmlNode,
                               @CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @CookieValue("group") String group,
                               @PathVariable("rolerightsgroup-id") Long rrgId,
                               HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, group);

        try {
            // Retourne le contenu du type
            if (rrgId != null) {
                securityManager.changeRole(ui.userId, rrgId, xmlNode);
            }
            return "";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
     * @param portfolioId
     * @param request
     * @return
     */
    @PostMapping(value = "/{portfolio-id}", produces = "application/xml")
    public String postRightGroups(@RequestBody String xmlNode,
                                  @CookieValue("user") String user,
                                  @CookieValue("credential") String token,
                                  @CookieValue("group") String group,
                                  @PathVariable("portfolio-id") UUID portfolioId,
                                  HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, group);

        try {
            return portfolioManager.addRoleInPortfolio(ui.userId, portfolioId, xmlNode);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}

