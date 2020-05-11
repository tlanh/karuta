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

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/nodes")
public class NodesController extends AbstractController {

    @Autowired
    private NodeManager nodeManager;

    @InjectLogger
    private Logger logger;

    /**
     * Get a node without children. <br>
     * GET /rest/api/nodes/node/{node-id}
     *
     * @param groupId
     * @param nodeId
     * @param cutoff
     * @param request
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{node-id}", produces = {"application/json", "application/xml"},
        consumes = "application/xml")
    public String getNode(@RequestParam("group") long groupId,
                          @PathVariable("node-id") UUID nodeId,
                          @RequestParam("level") Integer cutoff,
                          HttpServletRequest request) throws ParserConfigurationException, BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager.getNode(nodeId, false, ui.userId, groupId, null, cutoff);
    }

    /**
     * Fetch nodes and children from node uuid <br>
     * GET /rest/api/nodes/node/{node-id}/children
     *
     * @param groupId
     * @param nodeId
     * @param cutoff
     * @param request
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{node-id}/children", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String getNodeWithChildren(@RequestParam("group") long groupId,
                                      @PathVariable("node-id") UUID nodeId,
                                      @RequestParam("level") Integer cutoff,
                                      HttpServletRequest request) throws ParserConfigurationException, BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager.getNode(nodeId, true, ui.userId, groupId, null, cutoff);
    }

    /**
     * Fetch nodes metdata <br>
     * GET /rest/api/nodes/node/{node-id}/metadatawad
     *
     * @param groupId
     * @param nodeId
     * @param request
     * @return <metadata-wad/>
     */
    @GetMapping(value = "/node/{nodeid}/metadatawad",
            produces = {"application/json", "application/xml"})
    public String getNodeMetadataWad(@RequestParam("group") long groupId,
                                     @PathVariable("nodeid") UUID nodeId,
                                     HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager.getNodeMetadataWad(nodeId, ui.userId, groupId);
    }

    /**
     * Fetch rights per role for a node. <br>
     * GET /rest/api/nodes/node/{node-id}/rights
     *
     * @param groupId
     * @param nodeId
     * @param request
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
     * Fetch portfolio id from a given node id. <br>
     * GET /rest/api/nodes/node/{node-id}/portfolioid
     *
     * @param nodeId
     * @param request
     * @return portfolioid
     */
    @GetMapping(value = "/node/{node-id}/portfolioid", produces = "text/plain")
    public String getNodePortfolioId(@PathVariable("node-id") UUID nodeId,
                                     HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return nodeManager.getPortfolioIdFromNode(ui.userId, nodeId).toString();
    }

    /**
     * Change nodes rights. <br>
     * POST /rest/api/nodes/node/{node-id}/rights
     *
     * @param xmlNode            <node uuid=""> <role name="">
     *                           <right RD="" WR="" DL="" /> </role> </node>
     * @param nodeId
     * @param request
     * @return
     */
    @PostMapping(value = "/node/{node-id}/rights", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String postNodeRights(@RequestBody String xmlNode,
                                 @PathVariable("node-id") UUID nodeId,
                                 HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(new ByteArrayInputStream(xmlNode.getBytes("UTF-8")));

        XPath xPath = XPathFactory.newInstance().newXPath();
        String xpathRole = "//*[local-name()='role']";
        XPathExpression findRole = xPath.compile(xpathRole);
        NodeList roles = (NodeList) findRole.evaluate(doc, XPathConstants.NODESET);

        // Pour tous les rôles que nous devons modifier.
        for (int i = 0; i < roles.getLength(); ++i) {
            Node rolenode = roles.item(i);
            String roleName = rolenode.getAttributes().getNamedItem("name").getNodeValue();
            Node right = rolenode.getFirstChild();

            //
            if ("user".equals(roleName)) {
                /// on utilise le nom utilisateur comme rôle
                // FIXME
            }

            if ("#text".equals(right.getNodeName()))
                right = right.getNextSibling();

            if ("right".equals(right.getNodeName())) // Modification des droits du noeud.
            {
                NamedNodeMap rights = right.getAttributes();

                GroupRights nodeRights = new GroupRights();

                String val = rights.getNamedItem("RD").getNodeValue();
                if (val != null)
                    nodeRights.setRead("Y".equals(val));
                val = rights.getNamedItem("WR").getNodeValue();
                if (val != null)
                    nodeRights.setWrite("Y".equals(val));
                val = rights.getNamedItem("DL").getNodeValue();
                if (val != null)
                    nodeRights.setDelete("Y".equals(val));
                val = rights.getNamedItem("SB").getNodeValue();
                if (val != null)
                    nodeRights.setSubmit("Y".equals(val));

                // Executer le changement de droits.
                nodeManager.changeRights(ui.userId, nodeId, roleName, nodeRights);
            } else if ("action".equals(right.getNodeName())) // Using an action on node
            {
                // réinitialiser les droits
                nodeManager.executeMacroOnNode(ui.userId, nodeId, "reset");
            }
        }
        logger.info("Change rights " + HttpStatus.OK);

        return "";
    }

    /**
     * Get the single first semantic tag node inside specified portfolio <br>
     * GET /rest/api/nodes/firstbysemantictag/{portfolio-uuid}/{semantictag}
     *
     * @param groupId
     * @param portfolioId
     * @param semantictag
     * @param request
     * @return node in ASM format
     */
    @GetMapping(value = "/firstbysemantictag/{portfolio-uuid}/{semantictag}", consumes = "application/xml",
        produces = "application/xml")
    public String getNodeBySemanticTag(@RequestParam("group") long groupId,
                                       @PathVariable("portfolio-uuid") UUID portfolioId,
                                       @PathVariable("semantictag") String semantictag,
                                       HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager
                .getNodeBySemanticTag(portfolioId, semantictag, ui.userId, groupId);
    }

    /**
     * Get multiple semantic tag nodes inside specified portfolio. <br>
     * GET /rest/api/nodes/nodes/bysemantictag/{portfolio-uuid}/{semantictag}
     *
     * @param groupId
     * @param portfolioId
     * @param semantictag
     * @param request
     * @return nodes in ASM format
     */
    @GetMapping(value = "/bysemantictag/{portfolio-uuid}/{semantictag}", consumes = "application/xml",
        produces = "application/xml")
    public String getNodesBySemanticTag(@RequestParam("group") long groupId,
                                        @PathVariable("portfolio-uuid") UUID portfolioId,
                                        @PathVariable("semantictag") String semantictag,
                                        HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        return nodeManager.getNodesBySemanticTag(ui.userId, groupId, portfolioId, semantictag);

    }

    /**
     * Rewrite node <br>
     * PUT /rest/api/nodes/node/{node-id}
     *
     * @param xmlNode
     * @param groupId
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{node-id}", produces = "application/xml")
    public String putNode(@RequestBody String xmlNode,
                          @RequestParam("group") long groupId,
                          @PathVariable("node-id") UUID nodeId,
                          HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNode(nodeId, xmlNode, ui.userId, groupId)
                    .toString();
    }

    /**
     * Rewrite node metadata. <br>
     * PUT /rest/api/nodes/node/{node-id}/metadata
     *
     * @param xmlNode
     * @param groupId
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/metadata", produces = "application/xml")
    public String putNodeMetadata(@RequestBody String xmlNode,
                                  @RequestParam("group") int groupId,
                                  @PathVariable("nodeid") UUID nodeId,
                                  HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager
                .changeNodeMetadata(nodeId, xmlNode, ui.userId, groupId);
    }

    /**
     * Rewrite node wad metadata. <br>
     * PUT /rest/api/nodes/node/{node-id}/metadatawas
     *
     * @param xmlNode
     * @param groupId
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/metadatawad", produces = "application/xml")
    public String putNodeMetadataWad(@RequestBody String xmlNode,
                                     @RequestParam("group") Long groupId,
                                     @PathVariable("nodeid") UUID nodeId,
                                     HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager
                .changeNodeMetadataWad(nodeId, xmlNode, ui.userId, groupId);
    }

    /**
     * Rewrite node epm metadata.<br>
     * PUT /rest/api/nodes/node/{node-id}/metadataepm
     *
     * @param xmlNode
     * @param nodeId
     * @param groupId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/metadataepm", produces = "application/xml")
    public String putNodeMetadataEpm(@RequestBody String xmlNode,
                                     @PathVariable("nodeid") UUID nodeId,
                                     @RequestParam("group") long groupId,
                                     HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNodeMetadataEpm(nodeId, xmlNode, ui.userId, groupId);
    }

    /**
     * Rewrite node nodecontext. <br>
     * PUT /rest/api/nodes/node/{node-id}/nodecontext parameters: return:
     *
     * @param xmlNode
     * @param groupId
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/nodecontext", produces = "application/xml")
    public String putNodeNodeContext(@RequestBody String xmlNode,
                                     @RequestParam("group") long groupId,
                                     @PathVariable("nodeid") UUID nodeId,
                                     HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNodeContext(nodeId, xmlNode, ui.userId, groupId);
    }

    /**
     * Rewrite node resource. <br>
     * PUT /rest/api/nodes/node/{node-id}/noderesource
     *
     * @param xmlNode
     * @param groupId
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/noderesource", produces = "application/xml")
    public String putNodeNodeResource(@RequestBody String xmlNode,
                                      @RequestParam("group") long groupId,
                                      @PathVariable("nodeid") UUID nodeId,
                                      HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.changeNodeResource(nodeId, xmlNode, ui.userId, groupId);
    }

    /**
     * Instanciate a node with right parsing <br>
     * POST /rest/api/nodes/node/import/{dest-id}
     *
     * @param groupId
     * @param parentId
     * @param semtag
     * @param code
     * @param sourceId
     * @param request
     * @return
     */
    @PostMapping("/node/import/{dest-id}")
    public String postImportNode(@RequestParam("group") long groupId,
                                 @PathVariable("dest-id") UUID parentId,
                                 @RequestParam("srcetag") String semtag,
                                 @RequestParam("srcecode") String code,
                                 @RequestParam("uuid") UUID sourceId,
                                 HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.importNode(parentId, semtag, code, sourceId, ui.userId, groupId).toString();
    }

    /**
     * Raw copy a node. <br>
     * POST /rest/api/nodes/node/copy/{dest-id}
     *
     * @param groupId
     * @param parentId
     * @param semtag
     * @param code
     * @param sourceId
     * @param request
     * @return
     */
    @PostMapping("/node/copy/{dest-id}")
    public String postCopyNode(@RequestParam("group") long groupId,
                               @PathVariable("dest-id") UUID parentId,
                               @RequestParam("srcetag") String semtag,
                               @RequestParam("srcecode") String code,
                               @RequestParam("uuid") UUID sourceId,
                               HttpServletRequest request) {

        UserInfo ui = checkCredential(request);

        return nodeManager.copyNode(parentId, semtag, code, sourceId, ui.userId, groupId);
    }

    /**
     * Fetch nodes. <br>
     * GET /rest/api/nodes
     *
     * @param groupId
     * @param portfoliocode      mandatory
     * @param semtag             mandatory, find the semtag under portfoliocode, or
     *                           the selection from semtag_parent/code_parent
     * @param semtag_parent
     * @param code_parent        From a code_parent, find the children that have
     *                           semtag_parent
     * @param cutoff
     * @param request
     * @return
     */
    @GetMapping(consumes = "application/xml", produces = "application/xml")
    public String getNodes(@RequestParam("group") long groupId,
                           @RequestParam("portfoliocode") String portfoliocode,
                           @RequestParam("semtag") String semtag,
                           @RequestParam("semtag_parent") String semtag_parent,
                           @RequestParam("code_parent") String code_parent,
                           @RequestParam("level") Integer cutoff,
                           HttpServletRequest request) throws BusinessException {
        UserInfo ui = checkCredential(request);

        return nodeManager.getNodes(portfoliocode, semtag, ui.userId, groupId,
                    semtag_parent, code_parent, cutoff);

    }

    /**
     * Insert XML in a node. Mostly used by admin, other people use the import/copy
     * node <br>
     * POST /rest/api/nodes/node/{parent-id}
     *
     * @param xmlNode
     * @param parentId
     * @param groupId
     * @return
     */
    @PostMapping(value = "/node/{parent-id}", consumes = "application/xml", produces = "application/xml")
    public ResponseEntity<String> postNode(@RequestBody String xmlNode,
                                           @PathVariable("parent-id") UUID parentId,
                                           @RequestParam("group") long groupId,
                                           HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);

        String returnValue = nodeManager
                .addNode(parentId, xmlNode, ui.userId, groupId, false);

        return ResponseEntity
                .status(200)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .body(returnValue);
    }

    /**
     * Move a node up between siblings. <br>
     * POST /rest/api/nodes/node/{node-id}/moveup
     *
     * @param nodeId
     * @return
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
     * Move a node to another parent. <br>
     * POST /rest/api/nodes/node/{node-id}/parentof/{parent-id}
     *
     * @param nodeId
     * @param parentId
     * @param request
     * @return
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
     * Execute a macro command on a node, changing rights related. <br>
     * POST /rest/api/nodes/node/{node-id}/action/{action-name} *
     *
     * @param nodeId
     * @param macro
     * @param request
     * @return
     */
    @PostMapping(value = "/node/{node-id}/action/{action-name}", consumes = "application/xml",
        produces = "application/xml")
    public String postActionNode(@PathVariable("node-id") UUID nodeId,
                                 @PathVariable("action-name") String macro,
                                 HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        String returnValue = nodeManager.executeMacroOnNode(ui.userId, nodeId, macro);

        if (returnValue == "erreur") {
            throw new GenericBusinessException("Vous n'avez pas les droits d'acces");
        }

        return returnValue;

    }

    /**
     * Delete a node<br>
     * DELETE /rest/api/nodes/node/{node-uuid}
     *
     * @param groupId
     * @param nodeId
     * @param request
     * @return
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
     * Fetch node content. <br>
     * GET /rest/api/nodes/{node-id}
     *
     * @param groupId
     * @param nodeId
     * @param lang
     * @param xslFile
     * @param request
     * @return
     */
    @GetMapping(value = "/{node-id}", consumes = "application/xml")
    public String getNodeWithXSL(@RequestParam("group") long groupId,
                                 @PathVariable("node-id") UUID nodeId,
                                 @RequestParam("lang") String lang,
                                 @RequestParam("xsl-file") String xslFile,
                                 HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        // When we need more parameters, arrange this with format
        // "par1:par1val;par2:par2val;..."
        String parameters = "lang:" + lang;

        javax.servlet.http.HttpSession session = request.getSession(true);
        String ppath = session.getServletContext().getRealPath(File.separator);

        /// webapps...
        ppath = ppath.substring(0, ppath.lastIndexOf(File.separator, ppath.length() - 2) + 1);
        xslFile = ppath + xslFile;

        return nodeManager.getNodeWithXSL(nodeId, xslFile, parameters, ui.userId, groupId);
    }

    /**
     *
     * POST /rest/api/nodes/{node-id}/frommodelbysemantictag/{semantic-tag}
     *
     * @param groupId
     * @param nodeId
     * @param semantictag
     * @param request
     * @return
     */
    @PostMapping(value = "/{node-id}/frommodelbysemantictag/{semantic-tag}", consumes = "application/xml",
        produces = "application/xml")
    public String postNodeFromModelBySemanticTag(@RequestParam("group") long groupId,
                                                 @PathVariable("node-id") UUID nodeId,
                                                 @PathVariable("semantic-tag") String semantictag,
                                                 HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        return nodeManager.addNodeFromModelBySemanticTag(nodeId, semantictag, ui.userId, groupId);
    }

}
