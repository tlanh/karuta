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
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/nodes")
public class NodesController extends AbstractController {

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private CredentialRepository credentialRepository;

    @InjectLogger
    private Logger logger;

    /**
     * Get a node without children.
     *
     * GET /rest/api/nodes/node/{node-id}
     *
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{node-id}", produces = {"application/json", "application/xml"},
        consumes = "application/xml")
    public HttpEntity<NodeDocument> getNode(@RequestParam("group") long groupId,
                                            @PathVariable("node-id") UUID nodeId,
                                            @RequestParam("level") Integer cutoff,
                                            HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager.getNode(nodeId, false,
                ui.userId, groupId, null, cutoff));
    }

    /**
     * Fetch nodes and children from node uuid.
     *
     * GET /rest/api/nodes/node/{node-id}/children
     *
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{node-id}/children", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public HttpEntity<NodeDocument> getNodeWithChildren(@RequestParam("group") long groupId,
                                                        @PathVariable("node-id") UUID nodeId,
                                                        @RequestParam("level") Integer cutoff,
                                                        HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager.getNode(nodeId, true,
                ui.userId, groupId, null, cutoff));
    }

    /**
     * Fetch nodes metdata
     *
     * GET /rest/api/nodes/node/{node-id}/metadatawad
     *
     * @return <metadata-wad/>
     */
    @GetMapping(value = "/node/{nodeid}/metadatawad",
            produces = {"application/json", "application/xml"})
    public HttpEntity<MetadataWadDocument> getNodeMetadataWad(@RequestParam("group") long groupId,
                                                              @PathVariable("nodeid") UUID nodeId,
                                                              HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager.getNodeMetadataWad(nodeId, ui.userId, groupId));
    }

    /**
     * Fetch rights per role for a node.
     *
     * GET /rest/api/nodes/node/{node-id}/rights
     *
     * @return <node uuid=""> <role name=""> <right RD="" WR="" DL="" /> </role>
     *         </node>
     */
    @GetMapping(value = "/node/{node-id}/rights", consumes = "application/xml",
            produces = { "application/json", "application/xml"})
    public String getNodeRights(@RequestParam("group") long groupId,
                                @PathVariable("node-id") UUID nodeId,
                                HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        // TODO: Check with original code ; implementation is wrong for sure
        GroupRights gr = nodeManager.getRights(ui.userId, groupId, nodeId);

        if (gr == null) {
            throw new GenericBusinessException("Vous n'avez pas les droits necessaires");
        }

        return gr.toString();
    }

    /**
     * Fetch portfolio id from a given node id.
     *
     * GET /rest/api/nodes/node/{node-id}/portfolioid
     *
     * @return portfolioid
     */
    @GetMapping(value = "/node/{node-id}/portfolioid", produces = "text/plain")
    public String getNodePortfolioId(@PathVariable("node-id") UUID nodeId,
                                     HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return nodeManager.getPortfolioIdFromNode(ui.userId, nodeId).toString();
    }

    /**
     * Change nodes rights.
     *
     * POST /rest/api/nodes/node/{node-id}/rights
     *
     * @param roleList           <node uuid=""> <role name="">
     *                           <right RD="" WR="" DL="" /> </role> </node>
     * @return
     */
    @PostMapping(value = "/node/{node-id}/rights", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String postNodeRights(@RequestBody RoleList roleList,
                                 @PathVariable("node-id") UUID nodeId,
                                 HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        if (!credentialRepository.isAdmin(ui.userId))
            throw new GenericBusinessException("403 FORBIDDEN : No admin right");

        if (roleList.getAction() != null)
            nodeManager.executeMacroOnNode(ui.userId, nodeId, "reset");

        roleList.getRoles().forEach(role -> {
            role.getRights().forEach(right -> {
                GroupRights nodeRights = new GroupRights();

                nodeRights.setRead(right.getRD());
                nodeRights.setWrite(right.getWR());
                nodeRights.setDelete(right.getDL());
                nodeRights.setSubmit(right.getSB());

                nodeManager.changeRights(ui.userId, nodeId, role.getLabel(), nodeRights);
            });
        });

        return "";
    }

    /**
     * Get the single first semantic tag node inside specified portfolio
     *
     * GET /rest/api/nodes/firstbysemantictag/{portfolio-uuid}/{semantictag}
     *
     * @return node in ASM format
     */
    @GetMapping(value = "/firstbysemantictag/{portfolio-uuid}/{semantictag}", consumes = "application/xml",
        produces = "application/xml")
    public HttpEntity<NodeDocument> getNodeBySemanticTag(@RequestParam("group") long groupId,
                                                         @PathVariable("portfolio-uuid") UUID portfolioId,
                                                         @PathVariable("semantictag") String semantictag,
                                                         HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager
                .getNodeBySemanticTag(portfolioId, semantictag, ui.userId, groupId));
    }

    /**
     * Get multiple semantic tag nodes inside specified portfolio.
     *
     * GET /rest/api/nodes/nodes/bysemantictag/{portfolio-uuid}/{semantictag}
     *
     * @return nodes in ASM format
     */
    @GetMapping(value = "/bysemantictag/{portfolio-uuid}/{semantictag}", consumes = "application/xml",
        produces = "application/xml")
    public HttpEntity<NodeList> getNodesBySemanticTag(@RequestParam("group") long groupId,
                                                      @PathVariable("portfolio-uuid") UUID portfolioId,
                                                      @PathVariable("semantictag") String semantictag,
                                                      HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager
                        .getNodesBySemanticTag(ui.userId, groupId, portfolioId, semantictag));

    }

    /**
     * Rewrite node.
     *
     * PUT /rest/api/nodes/node/{node-id}
     */
    @PutMapping(value = "/node/{node-id}", produces = "application/xml")
    public String putNode(@RequestBody NodeDocument node,
                          @RequestParam("group") long groupId,
                          @PathVariable("node-id") UUID nodeId,
                          HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNode(nodeId, node, ui.userId, groupId)
                    .toString();
    }

    /**
     * Rewrite node metadata.
     *
     * PUT /rest/api/nodes/node/{node-id}/metadata
     */
    @PutMapping(value = "/node/{nodeid}/metadata", produces = "application/xml")
    public String putNodeMetadata(@RequestBody MetadataDocument metadata,
                                  @RequestParam("group") int groupId,
                                  @PathVariable("nodeid") UUID nodeId,
                                  HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return nodeManager
                .changeNodeMetadata(nodeId, metadata, ui.userId, groupId);
    }

    /**
     * Rewrite node wad metadata.
     *
     * PUT /rest/api/nodes/node/{node-id}/metadatawas
     */
    @PutMapping(value = "/node/{nodeid}/metadatawad", produces = "application/xml")
    public String putNodeMetadataWad(@RequestBody MetadataWadDocument metadata,
                                     @RequestParam("group") Long groupId,
                                     @PathVariable("nodeid") UUID nodeId,
                                     HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return nodeManager
                .changeNodeMetadataWad(nodeId, metadata, ui.userId, groupId);
    }

    /**
     * Rewrite node epm metadata.
     *
     * PUT /rest/api/nodes/node/{node-id}/metadataepm
     */
    @PutMapping(value = "/node/{nodeid}/metadataepm", produces = "application/xml")
    public String putNodeMetadataEpm(@RequestBody MetadataEpmDocument metadata,
                                     @PathVariable("nodeid") UUID nodeId,
                                     @RequestParam("group") long groupId,
                                     HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNodeMetadataEpm(nodeId, metadata, ui.userId, groupId);
    }

    /**
     * Rewrite node nodecontext.
     *
     * PUT /rest/api/nodes/node/{node-id}/nodecontext
     */
    @PutMapping(value = "/node/{nodeid}/nodecontext", produces = "application/xml")
    public String putNodeNodeContext(@RequestBody ResourceDocument resource,
                                     @RequestParam("group") long groupId,
                                     @PathVariable("nodeid") UUID nodeId,
                                     HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNodeContext(nodeId, resource, ui.userId, groupId);
    }

    /**
     * Rewrite node resource.
     *
     * PUT /rest/api/nodes/node/{node-id}/noderesource
     */
    @PutMapping(value = "/node/{nodeid}/noderesource", produces = "application/xml")
    public String putNodeNodeResource(@RequestBody ResourceDocument resource,
                                      @RequestParam("group") long groupId,
                                      @PathVariable("nodeid") UUID nodeId,
                                      HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNodeResource(nodeId, resource, ui.userId, groupId);
    }

    /**
     * Instanciate a node with right parsing
     *
     * POST /rest/api/nodes/node/import/{dest-id}
     */
    @PostMapping("/node/import/{dest-id}")
    public UUID postImportNode(@RequestParam("group") long groupId,
                               @PathVariable("dest-id") UUID parentId,
                               @RequestParam("srcetag") String semtag,
                               @RequestParam("srcecode") String code,
                               @RequestParam("uuid") UUID sourceId,
                               HttpServletRequest request) throws JsonProcessingException, BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager.importNode(parentId, semtag, code, sourceId, ui.userId, groupId);
    }

    /**
     * Raw copy a node.
     *
     * POST /rest/api/nodes/node/copy/{dest-id}
     */
    @PostMapping("/node/copy/{dest-id}")
    public UUID postCopyNode(@RequestParam("group") long groupId,
                             @PathVariable("dest-id") UUID parentId,
                             @RequestParam("srcetag") String semtag,
                             @RequestParam("srcecode") String code,
                             @RequestParam("uuid") UUID sourceId,
                             HttpServletRequest request) throws JsonProcessingException, BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager.copyNode(parentId, semtag, code, sourceId, ui.userId, groupId);
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
    @GetMapping(consumes = "application/xml", produces = "application/xml")
    public HttpEntity<NodeList> getNodes(@RequestParam("group") long groupId,
                                         @RequestParam("portfoliocode") String portfoliocode,
                                         @RequestParam("semtag") String semtag,
                                         @RequestParam("semtag_parent") String semtag_parent,
                                         @RequestParam("code_parent") String code_parent,
                                         @RequestParam("level") Integer cutoff,
                                         HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager.getNodes(portfoliocode, semtag, ui.userId, groupId,
                    semtag_parent, code_parent, cutoff));

    }

    /**
     * Insert XML in a node. Mostly used by admin, other people use the import/copy
     * node.
     *
     * POST /rest/api/nodes/node/{parent-id}
     */
    @PostMapping(value = "/node/{parent-id}", consumes = "application/xml", produces = "application/xml")
    public HttpEntity<NodeList> postNode(@RequestBody NodeDocument node,
                                         @PathVariable("parent-id") UUID parentId,
                                         @RequestParam("group") long groupId,
                                         HttpServletRequest request) throws BusinessException, JsonProcessingException {
        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager
                .addNode(parentId, node, ui.userId, groupId, false));
    }

    /**
     * Move a node up between siblings.
     *
     * POST /rest/api/nodes/node/{node-id}/moveup
     */
    @PostMapping(value = "/node/{node-id}/moveup", consumes = "application/xml", produces = "application/xml")
    public ResponseEntity<String> postMoveNodeUp(@PathVariable("node-id") UUID nodeId) {

        if (nodeId == null) {
            return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Missing uuid");
        } else {
            Long returnValue = nodeManager.moveNodeUp(nodeId);

            if (returnValue == -1L) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Non-existing node");
            } else if (returnValue == -2L) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot move first node");
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        }
    }

    /**
     * Move a node to another parent.
     *
     * POST /rest/api/nodes/node/{node-id}/parentof/{parent-id}
     */
    @PostMapping(value = "/node/{node-id}/parentof/{parent-id}", consumes = "application/xml",
            produces = "application/xml")
    public ResponseEntity<String> postChangeNodeParent(@PathVariable("node-id") UUID nodeId,
                                                       @PathVariable("parent-id") UUID parentId,
                                                       HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        boolean returnValue = nodeManager.changeParentNode(ui.userId, nodeId, parentId);

        if (!returnValue) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot move");
        } else {
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Execute a macro command on a node, changing rights related.
     *
     * POST /rest/api/nodes/node/{node-id}/action/{action-name}
     */
    @PostMapping(value = "/node/{node-id}/action/{action-name}", consumes = "application/xml",
        produces = "application/xml")
    public String postActionNode(@PathVariable("node-id") UUID nodeId,
                                 @PathVariable("action-name") String macro,
                                 HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        String returnValue = nodeManager.executeMacroOnNode(ui.userId, nodeId, macro);

        if (returnValue == "erreur") {
            throw new GenericBusinessException("Vous n'avez pas les droits d'acces");
        }

        return returnValue;

    }

    /**
     * Delete a node.
     *
     * DELETE /rest/api/nodes/node/{node-uuid}
     */
    @DeleteMapping(value = "/node/{node-uuid}", produces = "application/xml")
    public String deleteNode(@RequestParam("group") long groupId,
                             @PathVariable("node-uuid") UUID nodeId,
                             HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        nodeManager.removeNode(nodeId, ui.userId, groupId);

        return "";
    }

    /**
     * Fetch node content.
     *
     * GET /rest/api/nodes/{node-id}
     */
    @GetMapping(value = "/{node-id}", consumes = "application/xml")
    public HttpEntity<NodeDocument> getNodeWithXSL(@RequestParam("group") long groupId,
                                                   @PathVariable("node-id") UUID nodeId,
                                                   @RequestParam("lang") String lang,
                                                   @RequestParam("xsl-file") String xslFile,
                                                   HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        // When we need more parameters, arrange this with format
        // "par1:par1val;par2:par2val;..."
        String parameters = "lang:" + lang;

        javax.servlet.http.HttpSession session = request.getSession(true);
        String ppath = session.getServletContext().getRealPath(File.separator);

        /// webapps...
        ppath = ppath.substring(0, ppath.lastIndexOf(File.separator, ppath.length() - 2) + 1);
        xslFile = ppath + xslFile;

        return new HttpEntity<>(nodeManager.getNodeWithXSL(nodeId, xslFile,
                parameters, ui.userId, groupId));
    }

    /**
     * POST /rest/api/nodes/{node-id}/frommodelbysemantictag/{semantic-tag}
     */
    @PostMapping(value = "/{node-id}/frommodelbysemantictag/{semantic-tag}", consumes = "application/xml",
        produces = "application/xml")
    public HttpEntity<NodeList> postNodeFromModelBySemanticTag(@RequestParam("group") long groupId,
                                                 @PathVariable("node-id") UUID nodeId,
                                                 @PathVariable("semantic-tag") String semantictag,
                                                 HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(nodeManager
                        .addNodeFromModelBySemanticTag(nodeId, semantictag, ui.userId, groupId));
    }

}
