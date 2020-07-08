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
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/portfoliogroups")
public class PortfolioGroupController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private PortfolioManager portfolioManager;

    /**
     * Create a new portfolio group.
     *
     * POST /rest/api/portfoliogroups
     *
     * @param label - Name of the group we are creating
     * @param type - group/portfolio
     * @return groupid
     */

    @PostMapping
    public ResponseEntity<String> create(@RequestParam String label,
                                         @RequestParam String type,
                                         @RequestParam Long parent) {

        return ResponseEntity.ok()
                    .body(portfolioManager.addPortfolioGroup(label, type, parent).toString());

    }

    /**
     * Put a portfolio in portfolio group.
     *
     * PUT /rest/api/portfoliogroups
     */
    @PutMapping
    public ResponseEntity<String> addPortfolio(@RequestParam Long group,
                                               @RequestParam(required = false) UUID uuid,
                                               @RequestParam(required = false) String label) {
        return ResponseEntity.ok()
                .body(Integer.toString(portfolioManager.addPortfolioInGroup(uuid, group, label)));
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
     * @return group id or empty str if group id not found
     */
    @GetMapping
    public HttpEntity<Object> getPortfolio(@RequestParam(required = false) Long group,
                                           @RequestParam(required = false) UUID uuid,
                                           @RequestParam(required = false) String label) {

        if (label != null) {
            Long groupid = portfolioManager.getPortfolioGroupIdFromLabel(label);

            if (groupid == -1) {
                return ResponseEntity.notFound().build();
            } else {
                return new HttpEntity<>(groupid.toString());
            }
        } else if (uuid != null) {
            return ResponseEntity.ok()
                    .body(portfolioManager.getPortfolioGroupListFromPortfolio(uuid));
        } else if (group == null) {
            // TODO: Fix this to return a PortfolioGroupList
            return ResponseEntity.ok()
                    .body(portfolioManager.getPortfolioGroupList());
        } else {
            return ResponseEntity.ok()
                    .body(portfolioManager.getPortfoliosByPortfolioGroup(group));
        }
    }

    /**
     * Remove a portfolio from a portfolio group, or remove a portfoliogroup.
     *
     * DELETE /rest/api/portfoliogroups
     *
     * @param group - group id.
     * @param uuid  - portfolio id.
     * @return Code 200
     */
    @DeleteMapping
    public ResponseEntity<String> deletePortfolio(@RequestParam long group,
                                                  @RequestParam(required = false) UUID uuid) {
        if (uuid == null) {
            return ResponseEntity.ok()
                    .body(Boolean.toString(portfolioManager.removePortfolioGroups(group)));
        } else {
            return ResponseEntity.ok()
                    .body(Boolean.toString(portfolioManager.removePortfolioFromPortfolioGroups(uuid, group)));
        }
    }
}

