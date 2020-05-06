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

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/groupRightsInfos")
public class GroupRightsInfoController extends AbstractController {

    @Autowired
    private PortfolioManager portfolioManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Get role list from portfolio from uuid. <br>
     * GET /rest/api/groupRightsInfos
     *
     * @param user
     * @param token
     * @param groupId
     * @param portfolioId        portfolio uuid
     * @param request
     * @return <groupRightsInfos> <groupRightInfo grid="grouprightid">
     *         <label></label> <owner>UID</owner> </groupRightInfo>
     *         </groupRightsInfos>
     */
    @GetMapping(produces = "application/xml")
    public String getGroupRightsInfos(@CookieValue("user") String user,
                                      @CookieValue("credential") String token,
                                      @RequestParam("group") int groupId,
                                      @RequestParam("portfolioId") UUID portfolioId,
                                      HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            return portfolioManager.getGroupRightsInfos(ui.userId, portfolioId);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}

