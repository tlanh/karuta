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
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * Managing and listing Credential groups
 *
 * @author mlengagne
 *
 */
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
    public String postUserGroup(@RequestParam("label") String groupName) throws RestWebApplicationException {

        try {
            Long response = groupManager.addCredentialGroup(groupName);
            logger.debug("Group " + groupName + " successfully added");
            return Long.toString(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.NOT_MODIFIED, "Error in creation");
        }
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
                                       @RequestParam String label) throws RestWebApplicationException {
        try {
            boolean isOK = false;
            if (label != null) {
                isOK = groupManager.renameCredentialGroup(groupId, label);
            } else {
                isOK = securityManager.addUserInCredentialGroups(user, Arrays.asList(groupId));
                logger.debug("putUserInUserGroup successful, user was correctly added to the group " + groupId);
            }
            if (isOK)
                return ResponseEntity
                            .status(HttpStatus.OK)
                            .body("Changed");
            else
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body("Not OK");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Get users by usergroup or if there's no group id give he list of user group.
     * <br>
     * GET /rest/api/usersgroups
     *
     * @param cgId               group id
     * @param userId
     * @param groupName
     * @return Without group id <groups> <group id={groupid}> <label>{group
     *         name}</label> </group> ... </groups>
     *
     *         - With group id <group id={groupid}> <user id={userid}></user> ...
     *         </group>
     */
    @GetMapping
    public String getUsersByUserGroup(@RequestParam("group") Long cgId,
                                      @RequestParam("user") Long userId,
                                      @RequestParam("label") String groupName) throws RestWebApplicationException {
        String xmlUsers = "";

        try {
            if (groupName != null) {
                CredentialGroup crGroup = groupManager.getCredentialGroupByName(groupName);
                if (crGroup == null) {
                    throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "");
                }
                xmlUsers = Long.toString(crGroup.getId());
            } else if (userId != null)
                xmlUsers = groupManager.getCredentialGroupByUser(userId);
            else if (cgId == null)
                xmlUsers = groupManager.getCredentialGroupList();
            else
                xmlUsers = userManager.getUsersByCredentialGroup(cgId);
        } catch (RestWebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return xmlUsers;
    }

    /**
     * Remove a user from a user group, or remove a usergroup <br>
     * DELETE /rest/api/usersgroups
     *
     * @param group              group id
     * @param user               user id
     * @return Code 200
     */
    @DeleteMapping
    public String deleteUsersByUserGroup(@RequestParam Long group, @RequestParam Long user) throws RestWebApplicationException {
        Boolean isOK = false;

        try {
            if (user == null)
                isOK = groupManager.removeCredentialGroup(group);
            else
                isOK = securityManager.deleteUserFromCredentialGroup(user, group);

            if (isOK)
                return "Deleted";
            else
                return "Not OK";
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
