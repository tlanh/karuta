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
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.business.security.IsAdmin;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/nodes")
public class NodesController extends AbstractController {

    @Autowired
    private NodeManager nodeManager;

    @InjectLogger
    private Logger logger;

    /**
     * Get a node without children.
     *
     * GET /rest/api/nodes/node/{id}
     *
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{id}")
    public HttpEntity<String> getNode(@PathVariable UUID id,
                                      @RequestParam(required = false) Integer level,
                                      @AuthenticationPrincipal UserInfo userInfo) throws JsonProcessingException {

        return new HttpEntity<>(nodeManager.getNode(id, userInfo.getId(), level));
    }

    /**
     * Fetch rights per role for a node.
     *
     * GET /rest/api/nodes/node/{id}/rights
     */
    @GetMapping(value = "/node/{id}/rights")
    public NodeRightsDocument getNodeRights(@PathVariable UUID id, @AuthenticationPrincipal UserInfo userInfo) {
        return nodeManager.getRights(id, userInfo.getId());
    }

    /**
     * Fetch portfolio id from a given node id.
     *
     * GET /rest/api/nodes/node/{id}/portfolioid
     *
     * @return portfolioid
     */
    @GetMapping(value = "/node/{id}/portfolioid")
    public String getPortfolioId(@PathVariable UUID id) {
        return nodeManager.getPortfolioIdFromNode(id).toString();
    }

    /**
     * Change nodes rights.
     *
     * POST /rest/api/nodes/node/{id}/rights
     */
    @PostMapping(value = "/node/{id}/rights")
    @IsAdmin
    public String postNodeRights(@RequestBody NodeRightsDocument rights, @PathVariable UUID id) {

        NodeRightsDocument.RoleElement role = rights.getRole();

        GroupRights nodeRights = new GroupRights();

        nodeRights.setRead(role.getRight().getRD());
        nodeRights.setWrite(role.getRight().getWR());
        nodeRights.setDelete(role.getRight().getDL());
        nodeRights.setSubmit(role.getRight().getSB());

        nodeManager.changeRights(id, role.getName(), nodeRights);

        return "";
    }

    /**
     * Rewrite node metadata.
     *
     * PUT /rest/api/nodes/node/{id}/metadata
     */
    @PutMapping(value = "/node/{id}/metadata")
    public String putMetadata(@RequestBody MetadataDocument metadata,
                              @PathVariable UUID id,
                              @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        return nodeManager
                .changeNodeMetadata(id, metadata, userInfo.getId());
    }

    /**
     * Rewrite node wad metadata.
     *
     * PUT /rest/api/nodes/node/{id}/metadatawas
     */
    @PutMapping(value = "/node/{id}/metadatawad")
    public String putMetadataWad(@RequestBody MetadataWadDocument metadata,
                                 @PathVariable UUID id) throws JsonProcessingException {

        return nodeManager.changeNodeMetadataWad(id, metadata);
    }

    /**
     * Rewrite node epm metadata.
     *
     * PUT /rest/api/nodes/node/{id}/metadataepm
     */
    @PutMapping(value = "/node/{id}/metadataepm")
    public String putMetadataEpm(@RequestBody MetadataEpmDocument metadata,
                                 @PathVariable UUID id) throws JsonProcessingException {

        return nodeManager.changeNodeMetadataEpm(id, metadata);
    }

    /**
     * Rewrite node nodecontext.
     *
     * PUT /rest/api/nodes/node/{id}/nodecontext
     */
    @PutMapping(value = "/node/{id}/nodecontext")
    public String putNodeContext(@RequestBody ResourceDocument resource,
                                 @PathVariable UUID id,
                                 @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        return nodeManager.changeNodeContext(id, resource, userInfo.getId());
    }

    /**
     * Rewrite node resource.
     *
     * PUT /rest/api/nodes/node/{id}/noderesource
     */
    @PutMapping(value = "/node/{id}/noderesource")
    public String putNodeNodeResource(@RequestBody ResourceDocument resource,
                                      @PathVariable UUID id,
                                      @AuthenticationPrincipal UserInfo userInfo) throws Exception {

        return nodeManager.changeNodeResource(id, resource, userInfo.getId());
    }

    /**
     * Instanciate a node with right parsing
     *
     * POST /rest/api/nodes/node/import/{parentId}
     */
    @PostMapping("/node/import/{parentId}")
    public String importNode(@PathVariable UUID parentId,
                             @RequestParam(required = false) String srcetag,
                             @RequestParam(required = false) String srcecode,
                             @RequestParam(required = false) UUID uuid,
                             @AuthenticationPrincipal UserInfo userInfo) throws JsonProcessingException, BusinessException {

        return nodeManager.importNode(parentId, srcetag, srcecode, uuid, userInfo.getId()).toString();
    }

    /**
     * Raw copy a node.
     *
     * POST /rest/api/nodes/node/copy/{parentId}
     */
    @PostMapping("/node/copy/{parentId}")
    public UUID copyNode(@PathVariable UUID parentId,	/// Destination
                         /// Either (srcetag and srcecode) or uuid
                         @RequestParam(required = false) String srcetag,
                         @RequestParam(required = false) String srcecode,
                         @RequestParam(required = false) UUID uuid,
                         @AuthenticationPrincipal UserInfo userInfo) throws JsonProcessingException, BusinessException {

        return nodeManager.copyNode(parentId, srcetag, srcecode, uuid, userInfo.getId());
    }

    /**
     * Fetch nodes.
     *
     * GET /rest/api/nodes
     *
     * @param portfoliocode      mandatory
     * @param semtag             mandatory, find the semtag under portfoliocode, or
     *                           the selection from semtag_parent/code_parent
     * @param code_parent        From a code_parent, find the children that have
     *                           semtag_parent
     */
    @GetMapping
    public HttpEntity<NodeList> getNodes(@RequestParam String portfoliocode,
                                         @RequestParam String semtag,
                                         @RequestParam (required = false) String semtag_parent,
                                         @RequestParam (required = false) String code_parent,
                                         @RequestParam (required = false) Integer level,
                                         @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        return new HttpEntity<>(nodeManager.getNodes(portfoliocode, semtag, userInfo.getId(),
                    semtag_parent, code_parent, level));

    }

    /**
     * Move a node up between siblings.
     *
     * POST /rest/api/nodes/node/{id}/moveup
     */
    @PostMapping(value = "/node/{id}/moveup")
    public ResponseEntity<String> moveNodeUp(@PathVariable UUID id) {

        Long returnValue = nodeManager.moveNodeUp(id);

        if (returnValue == -1L) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Non-existing node");
        } else if (returnValue == -2L) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot move first node");
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    /**
     * Move a node to another parent.
     *
     * POST /rest/api/nodes/node/{id}/parentof/{parentId}
     */
    @PostMapping(value = "/node/{id}/parentof/{parentId}")
    public ResponseEntity<String> changeNodeParent(@PathVariable UUID id,
                                                   @PathVariable UUID parentId) {

        boolean returnValue = nodeManager.changeParentNode(id, parentId);

        if (!returnValue) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot move");
        } else {
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Execute a macro command on a node, changing rights related.
     *
     * POST /rest/api/nodes/node/{id}/action/{action}
     */
    @PostMapping(value = "/node/{id}/action/{action}")
    public HttpEntity<String> postActionNode(@PathVariable UUID id,
                                 @PathVariable String action,
                                 @AuthenticationPrincipal UserInfo userInfo) throws JsonProcessingException {

        String returnValue = nodeManager.executeMacroOnNode(userInfo.getId(), id, action);

        if (returnValue.equals("erreur")) {
            return ResponseEntity.status(403).body("Vous n'avez pas les droits d'acces");
        }

        return new HttpEntity<>(returnValue);

    }

    /**
     * Delete a node.
     *
     * DELETE /rest/api/nodes/node/{id}
     */
    @DeleteMapping(value = "/node/{id}")
    public String deleteNode(@PathVariable UUID id) {
        nodeManager.removeNode(id);

        return "";
    }
}
