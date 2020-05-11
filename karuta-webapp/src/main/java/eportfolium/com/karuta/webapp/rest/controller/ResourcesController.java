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

import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author mlengagne
 *
 */
@RestController
@RequestMapping("/resources")
public class ResourcesController extends AbstractController {

    @Autowired
    private ResourceManager resourceManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Fetch resource from node uuid. <br>
     * GET /rest/api/resources/resource/{node-parent-id}
     *
     * @param groupId
     * @param nodeParentId
     * @param request
     * @return
     */
    @GetMapping(value = "/resource/{node-parent-id}", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String getResource(@RequestParam("group") long groupId,
                              @PathVariable("node-parent-id") UUID nodeParentId,
                              HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return resourceManager.getResource(MimeTypeUtils.TEXT_XML, nodeParentId, ui.userId, groupId);
    }

    /**
     * Fetch all resource in a portfolio. <br>
     * GET /rest/api/resources/portfolios/{portfolio-id}
     *
     * @param groupId
     * @param portfolioId      portfolio-id
     * @param request
     * @return
     */
    @GetMapping(value = "/portfolios/{portfolio-id}", produces = {"application/json", "application/xml"})
    public String getResources(@RequestParam("group") long groupId,
                               @PathVariable("portfolio-id") UUID portfolioId,
                               HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return resourceManager.getResources(MimeTypeUtils.TEXT_XML, portfolioId, ui.userId, groupId);
    }

    /**
     * Modify resource content. <br>
     * PUT /rest/api/resources/resource/{node-parent-uuid}
     *
     * @param xmlResource
     * @param groupId
     * @param parentNodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/resource/{node-parent-uuid}", produces = "application/xml")
    public String putResource(@RequestBody String xmlResource,
                              @RequestParam("group") long groupId,
                              @PathVariable("node-parent-uuid") UUID parentNodeId,
                              HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return resourceManager
                .changeResource(MimeTypeUtils.TEXT_XML, parentNodeId, xmlResource, ui.userId, groupId).toString();
    }

    /**
     * Add a resource (?). <br>
     * POST /rest/api/resources/{node-parent-uuid}
     *
     * @param xmlResource
     * @param groupId
     * @param parentNodeId
     * @param request
     * @return
     */
    @PostMapping(value = "/{node-parent-uuid}", produces = "application/xml")
    public String postResource(@RequestBody String xmlResource,
                               @RequestParam("group") long groupId,
                               @PathVariable("node-parent-uuid") UUID parentNodeId,
                               HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return resourceManager
                    .addResource(MimeTypeUtils.TEXT_XML, parentNodeId, xmlResource, ui.userId, groupId);
    }

    /**
     * (?) POST /rest/api/resources
     *
     * @param xmlResource
     * @param groupId
     * @param resource
     * @param request
     * @return
     */
    @PostMapping(produces = "application/xml")
    public String postResources(@RequestBody String xmlResource,
                               @RequestParam("group") long groupId,
                               @RequestParam("resource") UUID resource,
                               HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);

        return resourceManager
                    .addResource(MimeTypeUtils.TEXT_XML, resource, xmlResource, ui.userId, groupId);
    }

    /**
     * Delete a resource <br>
     * DELETE /rest/api/resources/{resource-id}
     *
     * @param groupId
     * @param resourceId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/{resource-id}", produces = "application/xml")
    public String deleteResource(@RequestParam("group") long groupId,
                                 @PathVariable("resource-id") UUID resourceId,
                                 HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        resourceManager.removeResource(resourceId, ui.userId, groupId);

        return "";
    }

}
