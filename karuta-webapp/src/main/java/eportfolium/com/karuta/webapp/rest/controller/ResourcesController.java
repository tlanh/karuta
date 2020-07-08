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
import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
                                                    @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        return new HttpEntity<>(resourceManager.getResource(nodeId, userInfo.getId()));
    }

    @GetMapping("/resource/file/{id}")
    public void fetchResource(@PathVariable UUID id,
                              @RequestParam String lang,
                              @RequestParam String size,
                              @AuthenticationPrincipal UserInfo userInfo,
                              HttpServletResponse response) throws IOException, BusinessException {
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
                              @AuthenticationPrincipal UserInfo userInfo,
                              HttpServletRequest request) throws IOException, BusinessException {
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
                              @RequestParam (defaultValue = "-1")long group,
                              @PathVariable UUID parentNodeId,
                              HttpServletRequest request) throws BusinessException, JsonProcessingException {

    	HttpSession session = request.getSession(false);
    	SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
    	Authentication authentication = securityContext.getAuthentication();
    	CredentialDocument userInfo = (CredentialDocument)authentication.getDetails();

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
                               @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        return resourceManager.addResource(parentNodeId, resource, userInfo.getId());
    }

    /**
     * (?) POST /rest/api/resources
     */
    @PostMapping
    public String postResources(@RequestBody ResourceDocument document,
                                @RequestParam UUID resource,
                                @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {
        return resourceManager.addResource(resource, document, userInfo.getId());
    }

    /**
     * Delete a resource
     *
     * DELETE /rest/api/resources/{id}
     */
    @DeleteMapping(value = "/{id}")
    public String deleteResource(@PathVariable UUID id,
                                 @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {
        resourceManager.removeResource(id, userInfo.getId());

        return "";
    }

}
