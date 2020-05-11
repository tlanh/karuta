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
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Managing and listing portfolio groups.
 *
 * @author mlengagne
 *
 */
@RestController
@RequestMapping("/portfoliogroups")
public class PortfolioGroupController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private PortfolioManager portfolioManager;

    /**
     * Create a new portfolio group. <br>
     * POST /rest/api/portfoliogroups
     *
     * @param groupname          Name of the group we are creating
     * @param type               group/portfolio
     * @param parent             parentid
     * @param request
     * @return groupid
     */

    @PostMapping
    public ResponseEntity<Long> postPortfolioGroup(@RequestParam("label") String groupname,
                                       @RequestParam("type") String type,
                                       @RequestParam("parent") Long parent,
                                       HttpServletRequest request) {
        UserInfo ui = checkCredential(request);

        return ResponseEntity.ok()
                    .body(portfolioManager.addPortfolioGroup(groupname, type, parent, ui.userId));

    }

    /**
     * Put a portfolio in portfolio group. <br>
     * PUT /rest/api/portfoliogroups
     *
     * @param group              group id
     * @param uuid               portfolio id
     * @param label
     * @param request
     * @return Code 200
     */
    @PutMapping
    public ResponseEntity<Integer> putPortfolioInPortfolioGroup(@RequestParam("group") Long group,
                                                 @RequestParam("uuid") UUID uuid,
                                                 @RequestParam("label") String label,
                                                 HttpServletRequest request) {
        UserInfo ui = checkCredential(request);

        return ResponseEntity.ok()
                .body(portfolioManager.addPortfolioInGroup(uuid, group, label, ui.userId));
    }

    /**
     * Get portfolio by portfoliogroup, or if there's no group id give, give the
     * list of portfolio group GET /rest/api/portfoliogroups<br>
     *
     * - Without group id <groups> <group id={groupid}> <label>{group name}</label>
     * </group> ... </groups>
     *
     * - With group id <group id={groupid}> <portfolio id={uuid}></portfolio> ...
     * </group>
     *
     * @param group              group id
     * @param portfolioId
     * @param groupLabel         group label
     * @param request
     * @return group id or empty str if group id not found
     */
    @GetMapping
    public ResponseEntity<String> getPortfolioByPortfolioGroup(@RequestParam("group") Long group,
                                               @RequestParam("uuid") UUID portfolioId,
                                               @RequestParam("label") String groupLabel,
                                               HttpServletRequest request) {
        UserInfo ui = checkCredential(request);

        if (groupLabel != null) {
            Long groupid = portfolioManager.getPortfolioGroupIdFromLabel(groupLabel, ui.userId);

            if (groupid == -1) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok()
                        .body(Long.toString(groupid));
            }
        } else if (portfolioId != null) {
            return ResponseEntity.ok()
                    .body(portfolioManager.getPortfolioGroupListFromPortfolio(portfolioId));
        } else if (group == null) {
            return ResponseEntity.ok()
                    .body(portfolioManager.getPortfolioGroupList());
        } else {
            return ResponseEntity.ok()
                    .body(portfolioManager.getPortfoliosByPortfolioGroup(group));
        }
    }

    /**
     * Remove a portfolio from a portfolio group, or remove a portfoliogroup. <br>
     * DELETE /rest/api/portfoliogroups
     *
     * @param groupId            group id
     * @param uuid               portfolio id
     * @return Code 200
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deletePortfolioByGroup(@RequestParam("group") long groupId,
                                                          @RequestParam("uuid") UUID uuid) {
        if (uuid == null) {
            return ResponseEntity.ok()
                    .body(portfolioManager.removePortfolioGroups(groupId));
        } else {
            return ResponseEntity.ok()
                    .body(portfolioManager.removePortfolioFromPortfolioGroups(uuid, groupId));
        }
    }
}

