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

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/usergroups")
public class CredentialGroupController {

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private GroupManager groupManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Create a new user group.
     *
     * POST /rest/api/usersgroups
     *
     * @param label - Name of the group we are creating
     * @return groupid
     */
    @PostMapping()
    public String post(@RequestParam String label) {
        return groupManager.addCredentialGroup(label).toString();
    }

    /**
     * Put a user in user group.
     *
     * PUT /rest/api/usersgroups
     *
     * @param group - Group id.
     * @param user - User id.
     * @param label - New name of the group.
     */
    @PutMapping
    public ResponseEntity<String> addUser(@RequestParam Long group,
                                          @RequestParam(required = false) Long user,
                                          @RequestParam(required = false) String label) {
        boolean isOK;

        if (label != null) {
            isOK = groupManager.renameCredentialGroup(group, label);
        } else {
            isOK = securityManager.addUserInCredentialGroups(user, Collections.singletonList(group));
        }

        if (isOK)
            return ResponseEntity
                        .status(HttpStatus.OK)
                        .body("Changed");
        else
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("Not OK");
    }

    /**
     * Get users by usergroup or if there's no group id give he list of user group.
     *
     * GET /rest/api/usersgroups
     *
     * @return Without group id <groups> <group id={groupid}> <label>{group
     *         name}</label> </group> ... </groups>
     *
     *         - With group id <group id={groupid}> <user id={userid}></user> ...
     *         </group>
     */
    @GetMapping
    public HttpEntity<Object> getUsers(@RequestParam(required = false) Long group,
                                       @RequestParam(required = false) Long user,
                                       @RequestParam(required = false) String label) {

        if (label != null) {
            CredentialGroup crGroup = groupManager.getCredentialGroupByName(label);

            if (crGroup == null) {
                return ResponseEntity.notFound().build();
            }

            // TODO: Check whether we return just a number in original implementation
            return new HttpEntity<>(crGroup.getId().toString());

        } else if (user != null) {
            return new HttpEntity<>(groupManager.getCredentialGroupByUser(user));

        } else if (group == null) {
            return new HttpEntity<>(groupManager.getCredentialGroupList());

        } else {
            return new HttpEntity<>(userManager.getUsersByCredentialGroup(group));
        }
    }

    /**
     * Remove a user from a user group, or remove a usergroup
     *
     * DELETE /rest/api/usersgroups
     */
    @DeleteMapping
    public String deleteUser(@RequestParam Long group,
                             @RequestParam(required = false) Long user) {
        if (user == null)
            groupManager.removeCredentialGroup(group);
        else
            securityManager.deleteUserFromCredentialGroup(user, group);

        return "Deleted";
    }
}
