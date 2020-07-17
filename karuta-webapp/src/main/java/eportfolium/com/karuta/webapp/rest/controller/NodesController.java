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
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
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
    public HttpEntity<NodeDocument> getNode(@PathVariable UUID id,
                                      @RequestParam(required = false) Integer level,
                                      @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        return new HttpEntity<>(nodeManager.getNode(id, false, userInfo.getId(), level));
    }

    /**
     * Fetch nodes and children from node uuid.
     *
     * GET /rest/api/nodes/node/{id}/children
     *
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{id}/children")
    public HttpEntity<NodeDocument> getNodeWithChildren(@PathVariable UUID id,
                                                        @RequestParam Integer level,
                                                        @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        return new HttpEntity<>(nodeManager.getNode(id, true, userInfo.getId(), level));
    }

    /**
     * Fetch nodes metdata
     *
     * GET /rest/api/nodes/node/{id}/metadatawad
     *
     * @return <metadata-wad/>
     */
    @GetMapping(value = "/node/{id}/metadatawad")
    public HttpEntity<MetadataWadDocument> getNodeMetadataWad(@PathVariable UUID id,
                                                              @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {
        return new HttpEntity<>(nodeManager.getNodeMetadataWad(id, userInfo.getId()));
    }

    /**
     * Fetch rights per role for a node.
     *
     * GET /rest/api/nodes/node/{id}/rights
     */
    @GetMapping(value = "/node/{id}/rights")
    public NodeRightsDocument getNodeRights(@PathVariable UUID id, @AuthenticationPrincipal UserInfo userInfo) {
        // TODO: Check with original code ; implementation is wrong for sure
        GroupRights gr = nodeManager.getRights(userInfo.getId(), id);
        return new NodeRightsDocument(id, gr);
    }

    /**
     * Fetch portfolio id from a given node id.
     *
     * GET /rest/api/nodes/node/{id}/portfolioid
     *
     * @return portfolioid
     */
    @GetMapping(value = "/node/{id}/portfolioid", produces = "text/plain")
    public String getPortfolioId(@PathVariable UUID id,
                                 @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        return nodeManager.getPortfolioIdFromNode(userInfo.getId(), id).toString();
    }

    /**
     * Change nodes rights.
     *
     * POST /rest/api/nodes/node/{id}/rights
     *
     * @param roleList           <node uuid=""> <role name="">
     *                           <right RD="" WR="" DL="" /> </role> </node>
     */
    @PostMapping(value = "/node/{id}/rights")
    @IsAdmin
    public String postNodeRights(@RequestBody RoleList roleList,
                                 @PathVariable UUID id,
                                 @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        if (roleList.getAction() != null)
            nodeManager.executeMacroOnNode(userInfo.getId(), id, "reset");

        roleList.getRoles().forEach(role -> {
            role.getRights().forEach(right -> {
                GroupRights nodeRights = new GroupRights();

                nodeRights.setRead(right.getRD());
                nodeRights.setWrite(right.getWR());
                nodeRights.setDelete(right.getDL());
                nodeRights.setSubmit(right.getSB());

                nodeManager.changeRights(id, role.getLabel(), nodeRights);
            });
        });

        return "";
    }

    /**
     * Get the single first semantic tag node inside specified portfolio
     *
     * GET /rest/api/nodes/firstbysemantictag/{portfolioId}/{semantictag}
     *
     * @return node in ASM format
     */
    @GetMapping(value = "/firstbysemantictag/{portfolioId}/{semantictag}")
    public HttpEntity<NodeDocument> getNodeBySemanticTag(@PathVariable UUID portfolioId,
                                                         @PathVariable String semantictag,
                                                         @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        return new HttpEntity<>(nodeManager
                .getNodeBySemanticTag(portfolioId, semantictag, userInfo.getId()));
    }

    /**
     * Get multiple semantic tag nodes inside specified portfolio.
     *
     * GET /rest/api/nodes/nodes/bysemantictag/{portfolioId}/{semantictag}
     *
     * @return nodes in ASM format
     */
    @GetMapping(value = "/bysemantictag/{portfolioId}/{semantictag}")
    public HttpEntity<NodeList> getNodesBySemanticTag(@PathVariable UUID portfolioId,
                                                      @PathVariable String semantictag,
                                                      @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        return new HttpEntity<>(nodeManager
                        .getNodesBySemanticTag(userInfo.getId(), portfolioId, semantictag));

    }

    /**
     * Rewrite node.
     *
     * PUT /rest/api/nodes/node/{id}
     */
    @PutMapping(value = "/node/{id}")
    public String putNode(@RequestBody NodeDocument node,
                          @PathVariable UUID id,
                          @AuthenticationPrincipal UserInfo userInfo) throws Exception {

        return nodeManager.changeNode(id, node, userInfo.getId())
                    .toString();
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
                                 @PathVariable UUID id,
                                 @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, JsonProcessingException {

        return nodeManager
                .changeNodeMetadataWad(id, metadata, userInfo.getId());
    }

    /**
     * Rewrite node epm metadata.
     *
     * PUT /rest/api/nodes/node/{id}/metadataepm
     */
    @PutMapping(value = "/node/{id}/metadataepm")
    public String putMetadataEpm(@RequestBody MetadataEpmDocument metadata,
                                 @PathVariable UUID id,
                                 @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, JsonProcessingException {

        return nodeManager.changeNodeMetadataEpm(id, metadata, userInfo.getId());
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
    public UUID importNode(@PathVariable UUID parentId,
                           @RequestParam(required = false) String srcetag,
                           @RequestParam(required = false) String srcecode,
                           @RequestParam(required = false) UUID uuid,
                           @AuthenticationPrincipal UserInfo userInfo) throws JsonProcessingException, BusinessException {

        return nodeManager.importNode(parentId, srcetag, srcecode, uuid, userInfo.getId());
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
                                         @RequestParam String semtag_parent,
                                         @RequestParam String code_parent,
                                         @RequestParam Integer level,
                                         @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        return new HttpEntity<>(nodeManager.getNodes(portfoliocode, semtag, userInfo.getId(),
                    semtag_parent, code_parent, level));

    }

    /**
     * Insert XML in a node. Mostly used by admin, other people use the import/copy
     * node.
     *
     * POST /rest/api/nodes/node/{parentId}
     */
    @PostMapping(value = "/node/{parentId}")
    public HttpEntity<NodeList> postNode(@RequestBody NodeDocument node,
                                         @PathVariable UUID parentId,
                                         @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, JsonProcessingException {

        return new HttpEntity<>(nodeManager
                .addNode(parentId, node, userInfo.getId(), false));
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
    public ResponseEntity<String> changeNodeParent(@PathVariable UUID nodeId,
                                                   @PathVariable UUID parentId,
                                                   @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        boolean returnValue = nodeManager.changeParentNode(userInfo.getId(), nodeId, parentId);

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
    public String postActionNode(@PathVariable UUID id,
                                 @PathVariable String action,
                                 @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        String returnValue = nodeManager.executeMacroOnNode(userInfo.getId(), id, action);

        if (returnValue.equals("erreur")) {
            throw new GenericBusinessException("Vous n'avez pas les droits d'acces");
        }

        return returnValue;

    }

    /**
     * Delete a node.
     *
     * DELETE /rest/api/nodes/node/{id}
     */
    @DeleteMapping(value = "/node/{id}")
    public String deleteNode(@PathVariable UUID id,
                             @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        nodeManager.removeNode(id, userInfo.getId());

        return "";
    }

    /**
     * Fetch node content.
     *
     * GET /rest/api/nodes/{id}
     */
    @GetMapping(value = "/{id}")
    public HttpEntity<NodeDocument> getNodeWithXSL(@PathVariable UUID id,
                                                   @RequestParam String lang,
                                                   @RequestParam("xsl-file") String xslFile,
                                                   @AuthenticationPrincipal UserInfo userInfo,
                                                   HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        // When we need more parameters, arrange this with format
        // "par1:par1val;par2:par2val;..."
        String parameters = "lang:" + lang;

        javax.servlet.http.HttpSession session = request.getSession(true);
        String ppath = session.getServletContext().getRealPath(File.separator);

        /// webapps...
        ppath = ppath.substring(0, ppath.lastIndexOf(File.separator, ppath.length() - 2) + 1);
        xslFile = ppath + xslFile;

        return new HttpEntity<>(nodeManager.getNodeWithXSL(id, xslFile,
                parameters, userInfo.getId()));
    }

    /**
     * POST /rest/api/nodes/{id}/frommodelbysemantictag/{tag}
     */
    @PostMapping(value = "/{id}/frommodelbysemantictag/{tag}")
    public HttpEntity<NodeList> addNodeFromModelByTag(@PathVariable UUID id,
                                                      @PathVariable String tag,
                                                      @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        return new HttpEntity<>(nodeManager
                        .addNodeFromModelBySemanticTag(id, tag, userInfo.getId()));
    }

}
