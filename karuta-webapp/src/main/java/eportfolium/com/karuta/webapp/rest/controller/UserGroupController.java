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
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Managing and listing user groups
 *
 * @author mlengagne
 *
 */
@RestController
@RequestMapping("/groupsUsers")
public class UserGroupController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private SecurityManager securityManager;

    /**
     * Insert a user in a user group. <br>
     * POST /rest/api/groupsUsers
     *
     * @param groupId            group: gid
     * @param userId             userId
     * @param request
     * @return <ok/>
     */
    @PostMapping(produces = "application/xml")
    public String postGroupsUsers(@RequestParam("group") long groupId,
                                  @RequestParam("userId") long userId,
                                  HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request);

        try {
            securityManager.addUserToGroup(ui.userId, userId, groupId);
            return "<ok/>";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}

