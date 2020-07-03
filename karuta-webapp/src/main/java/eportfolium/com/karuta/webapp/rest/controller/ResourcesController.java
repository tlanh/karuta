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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * GET /rest/api/resources/resource/{nodeId}
     */
    @GetMapping(value = "/resource/{nodeId}")
    public HttpEntity<ResourceDocument> getResource(@PathVariable UUID nodeId,
                                                    Authentication authentication) throws BusinessException {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(resourceManager.getResource(nodeId, userInfo.getId()));
    }

    /**
     * Fetch all resource in a portfolio.
     *
     * GET /rest/api/resources/portfolios/{id}
     */
    @GetMapping(value = "/portfolios/{id}")
    public HttpEntity<ResourceList> getResources(@RequestParam long group,
                                                 @PathVariable UUID id,
                                                 Authentication authentication) {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(resourceManager.getResources(id, userInfo.getId(), group));
    }

    @GetMapping("/resource/file/{id}")
    public void fetchResource(@PathVariable UUID id,
                              @RequestParam String lang,
                              @RequestParam String size,
                              Authentication authentication,
                              HttpServletResponse response) throws IOException, BusinessException {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();
        OutputStream output = response.getOutputStream();

        ResourceDocument document = resourceManager
                .fetchResource(id, userInfo.getId(), output, lang, "T".equals(size));

        if (document == null) {
            response.setStatus(404);
        } else {
            // FIXME: Take "lang" into account.
            String name = document.getFilename();
            String type = document.getType();

            response.setHeader("Content-Type", type);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
        }
    }

    @PutMapping("/resource/file/{id}")
    public String rewriteFile(@PathVariable UUID id,
                              @RequestParam String lang,
                              @RequestParam String size,
                              HttpServletRequest request,
                              Authentication authentication) throws IOException, BusinessException {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();
        InputStream content = request.getInputStream();

        if (resourceManager.updateContent(id, userInfo.getId(), content, lang, "T".equals(size))) {
            return "Updated";
        } else {
            return "Error while updating resource.";
        }
    }

    /**
     * Modify resource content.
     *
     * PUT /rest/api/resources/resource/{parentNodeId}
     */
    @PutMapping(value = "/resource/{parentNodeId}")
    public String putResource(@RequestBody ResourceDocument resource,
                              @PathVariable UUID parentNodeId,
                              Authentication authentication) throws BusinessException, JsonProcessingException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return resourceManager.changeResource(parentNodeId, resource, userInfo.getId())
                    .toString();
    }

    /**
     * Add a resource (?).
     *
     * POST /rest/api/resources/{parentNodeId}
     */
    @PostMapping(value = "/{parentNodeId}")
    public String postResource(@RequestBody ResourceDocument resource,
                               @PathVariable UUID parentNodeId,
                               Authentication authentication) throws BusinessException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return resourceManager.addResource(parentNodeId, resource, userInfo.getId());
    }

    /**
     * (?) POST /rest/api/resources
     */
    @PostMapping
    public String postResources(@RequestBody ResourceDocument document,
                                @RequestParam UUID resource,
                                Authentication authentication) throws BusinessException {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return resourceManager.addResource(resource, document, userInfo.getId());
    }

    /**
     * Delete a resource
     *
     * DELETE /rest/api/resources/{id}
     */
    @DeleteMapping(value = "/{id}")
    public String deleteResource(@PathVariable UUID id,
                                 Authentication authentication) throws BusinessException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        resourceManager.removeResource(id, userInfo.getId());

        return "";
    }

}
