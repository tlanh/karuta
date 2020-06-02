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

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("RightGroup")
public class RightGroupController extends AbstractController {

    @Autowired
    private GroupManager groupManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Change the group right associated to a user group.
     *
     * POST /rest/api/RightGroup
     */
    @PostMapping(produces = "application/xml")
    public ResponseEntity<String> post(@RequestParam Long group,
                                       @RequestParam Long groupRightId,
                                       HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        groupManager.changeUserGroup(groupRightId, group, ui.userId);

        return ResponseEntity.ok().build();
    }

}
