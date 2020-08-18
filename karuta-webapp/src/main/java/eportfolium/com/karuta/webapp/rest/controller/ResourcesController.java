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
import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@RestController
@RequestMapping("/resources")
public class ResourcesController extends AbstractController {

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private HttpServletRequest httpServletRequest;
    
    @InjectLogger
    private static Logger logger;

  	@Autowired
  	private ConfigurationManager configurationManager;

    /**
     * Fetch resource from node uuid.
     *
     * GET /rest/api/resources/resource/{nodeId}
     */
    @GetMapping(value = "/resource/{nodeId}")
    public HttpEntity<ResourceDocument> getResource(@PathVariable UUID nodeId,
                                                    @AuthenticationPrincipal UserInfo userInfo) {

        if (!resourceManager.hasRight(userInfo.getId(), nodeId, GroupRights.READ))
            return ResponseEntity.status(403).build();

        return new HttpEntity<>(resourceManager.getResource(nodeId));
    }

    @GetMapping("/resource/file/{id}")
    public void fetchResource(@PathVariable UUID id,
                              @RequestParam(defaultValue = "fr") String lang,
                              @RequestParam(required = false) String size,
                              @AuthenticationPrincipal UserInfo userInfo,
                              HttpServletResponse response) throws IOException {
        OutputStream output = response.getOutputStream();

        if (!resourceManager.hasRight(userInfo.getId(), id, GroupRights.READ)) {
            response.setStatus(403);
            return;
        }

      	String contextPath = httpServletRequest.getContextPath();
        ResourceDocument document = resourceManager.fetchResource(id, output, lang, "T".equals(size), contextPath);

        if (document == null) {
            response.setStatus(404);
        } else {
            String name = document.getFilename(lang);
            String type = document.getType(lang);

            response.setContentType(type);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
        }
    }

    @PostMapping("/resource/file/{id}")
    public ResponseEntity<String> rewriteFile(@PathVariable UUID id,
                              @RequestParam(defaultValue = "fr") String lang,
                              @RequestParam(required = false) String size,
                              @RequestParam("uploadfile") MultipartFile uploadfile,
                              @AuthenticationPrincipal UserInfo userInfo) throws IOException, BusinessException {
    	
    	String contextPath = httpServletRequest.getContextPath();
    	String retval = resourceManager.updateContent(id, userInfo.getId(), uploadfile.getInputStream(), lang, "T".equals(size), contextPath);

        if ( retval != null ) {
      		String url = configurationManager.get("fileserver") + "/";
  				
      		/// FIXME: Eh
      		String format = "{\"files\":[{\"name\":\"%s\",\"size\":%s,\"type\":\"%s\",\"url\":\"%s\",\"fileid\":\"%s\"}]}";
      		String json = String.format(format, uploadfile.getOriginalFilename(), uploadfile.getSize(), uploadfile.getContentType(), url+retval, retval);
      		
            return ResponseEntity.ok(json);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(retval);
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
                              @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, JsonProcessingException {

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
