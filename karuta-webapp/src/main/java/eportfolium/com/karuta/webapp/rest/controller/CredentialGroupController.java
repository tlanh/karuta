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

import java.util.Arrays;

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
     * Create a new user group <br>
     * POST /rest/api/usersgroups
     *
     * @param groupName          Name of the group we are creating
     * @return groupid
     */
    @PostMapping()
    public String postUserGroup(@RequestParam("label") String groupName) {
        return Long.toString(groupManager.addCredentialGroup(groupName));
    }

    /**
     * Put a user in user group <br>
     * PUT /rest/api/usersgroups
     *
     * @param groupId            group id
     * @param user               user id
     * @param label              new name of the group.
     * @return Code 200
     */
    @PutMapping
    public ResponseEntity<String> putUserInUserGroup(@RequestParam("group") Long groupId,
                                       @RequestParam("user") Long user,
                                       @RequestParam String label) {
        boolean isOK;

        if (label != null) {
            isOK = groupManager.renameCredentialGroup(groupId, label);
        } else {
            isOK = securityManager.addUserInCredentialGroups(user, Arrays.asList(groupId));
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
    public HttpEntity<Object> getUsersByUserGroup(@RequestParam("group") Long cgId,
                                                  @RequestParam("user") Long userId,
                                                  @RequestParam("label") String groupName) {

        if (groupName != null) {
            CredentialGroup crGroup = groupManager.getCredentialGroupByName(groupName);

            if (crGroup == null) {
                return ResponseEntity.notFound().build();
            }

            // TODO: Check whether we return just a number in original implementation
            return new HttpEntity<>(crGroup.getId());

        } else if (userId != null) {
            return new HttpEntity<>(groupManager.getCredentialGroupByUser(userId));

        } else if (cgId == null) {
            return new HttpEntity<>(groupManager.getCredentialGroupList());

        } else {
            return new HttpEntity<>(userManager.getUsersByCredentialGroup(cgId));
        }
    }

    /**
     * Remove a user from a user group, or remove a usergroup
     *
     * DELETE /rest/api/usersgroups
     */
    @DeleteMapping
    public String deleteUsersByUserGroup(@RequestParam Long group, @RequestParam Long user) {
        if (user == null)
            groupManager.removeCredentialGroup(group);
        else
            securityManager.deleteUserFromCredentialGroup(user, group);

        return "Deleted";
    }
}
