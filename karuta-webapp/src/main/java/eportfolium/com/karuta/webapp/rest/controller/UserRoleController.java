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

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserRoleController extends AbstractController {

    @Autowired
    private SecurityManager securityManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Add user to a role. <br>
     * POST /rest/api/roleUser
     *
     * @param user
     * @param token
     * @param groupId
     * @param grid
     * @param userid
     * @param request
     * @return
     */
    @PostMapping(value = "/roleUser", produces = "application/xml")
    public String postRoleUser(@CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @RequestParam("grid") long grid,
                               @RequestParam("user-id") Long userid,
                               HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            return securityManager.addUserRole(ui.userId, grid, userid);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}

