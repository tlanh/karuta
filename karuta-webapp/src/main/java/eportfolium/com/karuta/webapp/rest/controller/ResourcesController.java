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

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.document.ResourceList;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class ResourcesController extends AbstractController {

    @Autowired
    private ResourceManager resourceManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Fetch resource from node uuid.
     *
     * GET /rest/api/resources/resource/{parentNodeId}
     */
    @GetMapping(value = "/resource/{parentNodeId}", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public HttpEntity<ResourceDocument> getResource(@RequestParam long group,
                                                    @PathVariable UUID parentNodeId,
                                                    Authentication authentication) throws BusinessException {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(resourceManager.getResource(parentNodeId, userInfo.getId(), group));
    }

    /**
     * Fetch all resource in a portfolio.
     *
     * GET /rest/api/resources/portfolios/{id}
     */
    @GetMapping(value = "/portfolios/{id}", produces = {"application/xml"})
    public HttpEntity<ResourceList> getResources(@RequestParam long group,
                                                 @PathVariable UUID id,
                                                 Authentication authentication) {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(resourceManager.getResources(id, userInfo.getId(), group));
    }

    /**
     * Modify resource content.
     *
     * PUT /rest/api/resources/resource/{parentNodeId}
     */
    @PutMapping(value = "/resource/{parentNodeId}", produces = "application/xml")
    public String putResource(@RequestBody ResourceDocument resource,
                              @RequestParam long group,
                              @PathVariable UUID parentNodeId,
                              Authentication authentication) throws BusinessException, JsonProcessingException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return resourceManager.changeResource(parentNodeId, resource, userInfo.getId(), group)
                    .toString();
    }

    /**
     * Add a resource (?).
     *
     * POST /rest/api/resources/{parentNodeId}
     */
    @PostMapping(value = "/{parentNodeId}", produces = "application/xml")
    public String postResource(@RequestBody ResourceDocument resource,
                               @RequestParam long group,
                               @PathVariable UUID parentNodeId,
                               Authentication authentication) throws BusinessException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return resourceManager.addResource(parentNodeId, resource, userInfo.getId(), group);
    }

    /**
     * (?) POST /rest/api/resources
     */
    @PostMapping(produces = "application/xml")
    public String postResources(@RequestBody ResourceDocument document,
                                @RequestParam long group,
                                @RequestParam UUID resource,
                                Authentication authentication) throws BusinessException {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return resourceManager.addResource(resource, document, userInfo.getId(), group);
    }

    /**
     * Delete a resource
     *
     * DELETE /rest/api/resources/{id}
     */
    @DeleteMapping(value = "/{id}", produces = "application/xml")
    public String deleteResource(@RequestParam long group,
                                 @PathVariable UUID id,
                                 Authentication authentication) throws BusinessException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        resourceManager.removeResource(id, userInfo.getId(), group);

        return "";
    }

}
