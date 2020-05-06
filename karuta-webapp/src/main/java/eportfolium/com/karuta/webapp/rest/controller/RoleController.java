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

import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
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
     * Fetch rights in a role. <br>
     * GET /rest/api/roles/role/{role-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param roleId
     * @param request
     * @return
     */
    @GetMapping(value = "/role/{role-id}", produces = {"application/json", "application/xml"})
    public String getRole(@CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") int groupId,
                          @PathVariable("role-id") Long roleId,
                          HttpServletRequest request) throws RestWebApplicationException {
        // checkCredential(httpServletRequest, user, token, null); FIXME
        try {
            return userManager.getRole(roleId);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Role " + roleId + " not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch a role in a portfolio. <br>
     * GET /rest/api/roles/portfolio/{portfolio-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param role
     * @param portfolioId
     * @param request
     * @return
     */
    @GetMapping(value = "/portfolio/{portfolio-id}", produces = {"application/json", "application/xml"})
    public String getRolePortfolio(@CookieValue("user") String user,
                                   @CookieValue("credential") String token,
                                   @RequestParam("group") int groupId,
                                   @RequestParam("role") String role,
                                   @RequestParam("portfolio-id") UUID portfolioId,
                                   HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = portfolioManager
                    .getRoleByPortfolio(MimeTypeUtils.TEXT_XML, role, portfolioId, ui.userId);
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

    }

    /**
     * Modify a role. <br>
     * PUT /rest/api/roles/role/{role-id}
     *
     * @param xmlRole
     * @param user
     * @param token
     * @param groupId
     * @param roleId
     * @param request
     * @return
     */
    @PutMapping(value = "/role/{role-id}", produces = "application/xml")
    public String putRole(@RequestBody String xmlRole,
                          @CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") int groupId,
                          @PathVariable("role-id") long roleId,
                          HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);
        try {
            return securityManager.changeRole(ui.userId, roleId, xmlRole).toString();
        } catch (DoesNotExistException e) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Role with id " + roleId + " not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
