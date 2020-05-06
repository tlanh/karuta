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
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.eventbus.KEvent;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.json.XML;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param accept
     * @param cutoff
     * @param request
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{node-id}", produces = {"application/json", "application/xml"},
        consumes = "application/xml")
    public String getNode(@CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") long groupId,
                          @PathVariable("node-id") UUID nodeId,
                          @RequestHeader("Accept") String accept,
                          @RequestParam("level") Integer cutoff,
                          HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {

            String returnValue = nodeManager
                    .getNode(MimeTypeUtils.TEXT_XML, nodeId, false, ui.userId, groupId, null, cutoff);
            if (returnValue.length() != 0) {
                if (accept.equals(MediaType.APPLICATION_JSON))
                    returnValue = XML.toJSONObject(returnValue).toString();
            }
            return returnValue;
        } catch (DoesNotExistException ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.NOT_ACCEPTABLE, "Incorrect Mime Type");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch nodes and children from node uuid <br>
     * GET /rest/api/nodes/node/{node-id}/children
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param accept
     * @param cutoff
     * @param request
     * @return nodes in the ASM format
     */
    @GetMapping(value = "/node/{node-id}/children", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String getNodeWithChildren(@CookieValue("user") String user,
                                      @CookieValue("credential") String token,
                                      @RequestParam("group") long groupId,
                                      @PathVariable("node-id") UUID nodeId,
                                      @RequestHeader("Accept") String accept,
                                      @RequestParam("level") Integer cutoff,
                                      HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = nodeManager
                    .getNode(MimeTypeUtils.TEXT_XML, nodeId, true, ui.userId, groupId, null, cutoff);
            if (returnValue.length() != 0) {
                if (accept.equals(MediaType.APPLICATION_JSON)) {
                    returnValue = XML.toJSONObject(returnValue).toString();
                }
            }
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.NOT_ACCEPTABLE, "Incorrect Mime Type");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch nodes metdata <br>
     * GET /rest/api/nodes/node/{node-id}/metadatawad
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param accept
     * @param request
     * @return <metadata-wad/>
     */
    @GetMapping(value = "/node/{nodeid}/metadatawad", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String getNodeMetadataWad(@CookieValue("user") String user,
                                     @CookieValue("credential") String token,
                                     @RequestParam("group") long groupId,
                                     @PathVariable("nodeid") UUID nodeId,
                                     @RequestHeader("Accept") String accept,
                                     HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {

            String returnValue = nodeManager.getNodeMetadataWad(MimeTypeUtils.TEXT_XML, nodeId, ui.userId, groupId)
                    .toString();
            if (returnValue.length() != 0) {
                if (accept.equals(MediaType.APPLICATION_JSON))
                    returnValue = XML.toJSONObject(returnValue).toString();
            }
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch rights per role for a node. <br>
     * GET /rest/api/nodes/node/{node-id}/rights
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param accept
     * @param request
     * @return <node uuid=""> <role name=""> <right RD="" WR="" DL="" /> </role>
     *         </node>
     */
    @GetMapping(value = "/node/{node-id}/rights", consumes = "application/xml",
            produces = { "application/json", "application/xml"})
    public String getNodeRights(@CookieValue("user") String user,
                                @CookieValue("credential") String token,
                                @RequestParam("group") long groupId,
                                @PathVariable("node-id") UUID nodeId,
                                @RequestHeader("Accept") String accept,
                                HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {

            GroupRights gr = nodeManager.getRights(ui.userId, groupId, nodeId);
            String returnValue = null;
            if (gr != null) {
                if (accept.equals(MediaType.APPLICATION_JSON))
                    returnValue = XML.toJSONObject(returnValue).toString();
            } else {
                throw new RestWebApplicationException(HttpStatus.FORBIDDEN,
                        "Vous n'avez pas les droits necessaires");
            }

            return returnValue;
        } catch (RestWebApplicationException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NullPointerException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch portfolio id from a given node id. <br>
     * GET /rest/api/nodes/node/{node-id}/portfolioid
     *
     * @param user
     * @param token
     * @param nodeId
     * @param accept
     * @param request
     * @return portfolioid
     */
    @GetMapping(value = "/node/{node-id}/portfolioid", produces = "text/plain")
    public String getNodePortfolioId(@CookieValue("user") String user,
                                     @CookieValue("credential") String token,
                                     @PathVariable("node-id") UUID nodeId,
                                     @RequestHeader("Accept") String accept,
                                     HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);
        try {
            return nodeManager.getPortfolioIdFromNode(ui.userId, nodeId).toString();
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND,
                    "Error, this shouldn't happen. No Portfolio related to node : '" + nodeId + "' was found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Change nodes rights. <br>
     * POST /rest/api/nodes/node/{node-id}/rights
     *
     * @param xmlNode            <node uuid=""> <role name="">
     *                           <right RD="" WR="" DL="" /> </role> </node>
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param accept
     * @param request
     * @return
     */
    @PostMapping(value = "/node/{node-id}/rights", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String postNodeRights(@RequestBody String xmlNode,
                                 @CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @RequestParam("group") int groupId,
                                 @PathVariable("node-id") UUID nodeId,
                                 @RequestHeader("Accept") String accept,
                                 HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
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
        } catch (NullPointerException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

        return "";
    }

    /**
     * Get the single first semantic tag node inside specified portfolio <br>
     * GET /rest/api/nodes/firstbysemantictag/{portfolio-uuid}/{semantictag}
     *
     * @param user
     * @param token
     * @param groupId
     * @param portfolioId
     * @param semantictag
     * @param request
     * @return node in ASM format
     */
    @GetMapping(value = "/firstbysemantictag/{portfolio-uuid}/{semantictag}", consumes = "application/xml",
        produces = "application/xml")
    public String getNodeBySemanticTag(@CookieValue("user") String user,
                                       @CookieValue("credential") String token,
                                       @RequestParam("group") long groupId,
                                       @PathVariable("portfolio-uuid") UUID portfolioId,
                                       @PathVariable("semantictag") String semantictag,
                                       HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            return nodeManager
                    .getNodeBySemanticTag(MimeTypeUtils.TEXT_XML, portfolioId, semantictag, ui.userId, groupId);
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "no node found for tag :" + semantictag);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Get multiple semantic tag nodes inside specified portfolio. <br>
     * GET /rest/api/nodes/nodes/bysemantictag/{portfolio-uuid}/{semantictag}
     *
     * @param user
     * @param token
     * @param groupId
     * @param portfolioId
     * @param semantictag
     * @param request
     * @return nodes in ASM format
     */
    @GetMapping(value = "/bysemantictag/{portfolio-uuid}/{semantictag}", consumes = "application/xml",
        produces = "application/xml")
    public String getNodesBySemanticTag(@CookieValue("user") String user,
                                        @CookieValue("credential") String token,
                                        @RequestParam("group") long groupId,
                                        @PathVariable("portfolio-uuid") UUID portfolioId,
                                        @PathVariable("semantictag") String semantictag,
                                        HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            return nodeManager
                    .getNodesBySemanticTag(MimeTypeUtils.TEXT_XML, ui.userId, groupId, portfolioId, semantictag);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Rewrite node <br>
     * PUT /rest/api/nodes/node/{node-id}
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{node-id}", produces = "application/xml")
    public String putNode(@RequestBody String xmlNode,
                          @CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") long groupId,
                          @PathVariable("node-id") UUID nodeId,
                          HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);
        try {
            String returnValue = nodeManager.changeNode(MimeTypeUtils.TEXT_XML, nodeId, xmlNode, ui.userId, groupId)
                    .toString();
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Rewrite node metadata. <br>
     * PUT /rest/api/nodes/node/{node-id}/metadata
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param groupId
     * @param info
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/metadata", produces = "application/xml")
    public String putNodeMetadata(@RequestBody String xmlNode,
                                  @CookieValue("user") String user,
                                  @CookieValue("credential") String token,
                                  @RequestParam("group") int groupId,
                                  @RequestParam("info") String info,
                                  @PathVariable("nodeid") UUID nodeId,
                                  HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        Date time = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(time);
        String logformat = "";
        if ("false".equals(info))
            logformat = logFormatShort;
        else
            logformat = logFormat;

        try {
            String returnValue = nodeManager
                    .changeNodeMetadata(MimeTypeUtils.TEXT_XML, nodeId, xmlNode, ui.userId, groupId).toString();
            logger.info(String.format(logformat, "OK", nodeId, "metadata", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (BusinessException ex) {
            logger.error(String.format(logformat, "ERR", nodeId, "metadata", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Rewrite node wad metadata. <br>
     * PUT /rest/api/nodes/node/{node-id}/metadatawas
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param groupId
     * @param info
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/metadatawad", produces = "application/xml")
    public String putNodeMetadataWad(@RequestBody String xmlNode,
                                     @CookieValue("user") String user,
                                     @CookieValue("credential") String token,
                                     @RequestParam("group") Long groupId,
                                     @RequestParam("info") String info,
                                     @PathVariable("nodeid") UUID nodeId,
                                     HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        Date time = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(time);
        String logformat = "";
        if ("false".equals(info))
            logformat = logFormatShort;
        else
            logformat = logFormat;

        try {
            String returnValue = nodeManager
                    .changeNodeMetadataWad(MimeTypeUtils.TEXT_XML, nodeId, xmlNode, ui.userId, groupId).toString();
            logger.info(String.format(logformat, "OK", nodeId, "metadatawad", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (BusinessException ex) {
            logger.error(String.format(logformat, "ERR", nodeId, "metadatawad", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Rewrite node epm metadata.<br>
     * PUT /rest/api/nodes/node/{node-id}/metadataepm
     *
     * @param xmlNode
     * @param nodeId
     * @param groupId
     * @param info
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/metadataepm", produces = "application/xml")
    public String putNodeMetadataEpm(@RequestBody String xmlNode,
                                     @PathVariable("nodeid") UUID nodeId,
                                     @RequestParam("group") long groupId,
                                     @RequestParam("info") String info,
                                     HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, null, null, null);

        Date time = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(time);
        String logformat = "";
        if ("false".equals(info))
            logformat = logFormatShort;
        else
            logformat = logFormat;

        try {
            String returnValue = nodeManager
                    .changeNodeMetadataEpm(MimeTypeUtils.TEXT_XML, nodeId, xmlNode, ui.userId, groupId).toString();
            logger.info(String.format(logformat, "OK", nodeId, "metadataepm", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Node " + nodeId + " not found");
        } catch (BusinessException ex) {
            logger.error(String.format(logformat, "ERR", nodeId, "metadataepm", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Rewrite node nodecontext. <br>
     * PUT /rest/api/nodes/node/{node-id}/nodecontext parameters: return:
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param groupId
     * @param info
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/nodecontext", produces = "application/xml")
    public String putNodeNodeContext(@RequestBody String xmlNode,
                                     @CookieValue("user") String user,
                                     @CookieValue("credential") String token,
                                     @RequestParam("group") long groupId,
                                     @RequestParam("info") String info,
                                     @PathVariable("nodeid") UUID nodeId,
                                     HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        Date time = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(time);
        String logformat = "";
        if ("false".equals(info))
            logformat = logFormatShort;
        else
            logformat = logFormat;

        try {
            String returnValue = nodeManager
                    .changeNodeContext(MimeTypeUtils.TEXT_XML, nodeId, xmlNode, ui.userId, groupId).toString();
            logger.info(String.format(logformat, "OK", nodeId, "nodecontext", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            return returnValue;
        } catch (BusinessException ex) {
            logger.error(String.format(logformat, "ERR", nodeId, "nodecontext", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Rewrite node resource. <br>
     * PUT /rest/api/nodes/node/{node-id}/noderesource
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param groupId
     * @param info
     * @param nodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/node/{nodeid}/noderesource", produces = "application/xml")
    public String putNodeNodeResource(@RequestBody String xmlNode,
                                      @CookieValue("user") String user,
                                      @CookieValue("credential") String token,
                                      @RequestParam("group") long groupId,
                                      @RequestParam("info") String info,
                                      @PathVariable("nodeid") UUID nodeId,
                                      HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        Date time = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(time);
        String logformat = "";
        if ("false".equals(info))
            logformat = logFormatShort;
        else
            logformat = logFormat;

        try {
            String returnValue = nodeManager
                    .changeNodeResource(MimeTypeUtils.TEXT_XML, nodeId, xmlNode, ui.userId, groupId).toString();
            logger.info(String.format(logformat, "OK", nodeId, "noderesource", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            return returnValue;
        } catch (BusinessException ex) {
            logger.error(String.format(logformat, "ERR", nodeId, "noderesource", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlNode));
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Instanciate a node with right parsing <br>
     * POST /rest/api/nodes/node/import/{dest-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param parentId
     * @param semtag
     * @param code
     * @param sourceId
     * @param request
     * @return
     */
    @PostMapping("/node/import/{dest-id}")
    public String postImportNode(@CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @RequestParam("group") long groupId,
                                 @PathVariable("dest-id") UUID parentId,
                                 @RequestParam("srcetag") String semtag,
                                 @RequestParam("srcecode") String code,
                                 @RequestParam("uuid") UUID sourceId,
                                 HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        if (ui.userId == 0)
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, "Vous n'êtes pas connecté");

        try {
            return nodeManager.importNode(MimeTypeUtils.TEXT_XML, parentId, semtag, code, sourceId, ui.userId, groupId).toString();
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Raw copy a node. <br>
     * POST /rest/api/nodes/node/copy/{dest-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param parentId
     * @param semtag
     * @param code
     * @param sourceId
     * @param request
     * @return
     */
    @PostMapping("/node/copy/{dest-id}")
    public String postCopyNode(@CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @PathVariable("dest-id") UUID parentId,
                               @RequestParam("srcetag") String semtag,
                               @RequestParam("srcecode") String code,
                               @RequestParam("uuid") UUID sourceId,
                               HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        if (ui.userId == 0)
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, "Vous n'êtes pas connecté");

        try {
            return nodeManager.copyNode(MimeTypeUtils.TEXT_XML, parentId, semtag, code, sourceId, ui.userId, groupId);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch nodes. <br>
     * GET /rest/api/nodes
     *
     * @param user
     * @param token
     * @param groupId
     * @param parentId
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
    public String getNodes(@CookieValue("user") String user,
                           @CookieValue("credential") String token,
                           @RequestParam("group") long groupId,
                           @PathVariable("dest-id") String parentId,
                           @RequestParam("portfoliocode") String portfoliocode,
                           @RequestParam("semtag") String semtag,
                           @RequestParam("semtag_parent") String semtag_parent,
                           @RequestParam("code_parent") String code_parent,
                           @RequestParam("level") Integer cutoff,
                           HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            return nodeManager.getNodes(MimeTypeUtils.TEXT_XML, portfoliocode, semtag, ui.userId, groupId,
                    semtag_parent, code_parent, cutoff);
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Portfolio inexistant");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, "Vous n'avez pas les droits d'acces");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("getNodes", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Insert XML in a node. Mostly used by admin, other people use the import/copy
     * node <br>
     * POST /rest/api/nodes/node/{parent-id}
     *
     * @param xmlNode
     * @param user
     * @param token
     * @param group
     * @param parentId
     * @param groupId
     * @return
     */
    @PostMapping(value = "/node/{parent-id}", consumes = "application/xml", produces = "application/xml")
    public ResponseEntity<String> postNode(@RequestBody String xmlNode,
                                           @CookieValue("user") String user,
                                           @CookieValue("credential") String token,
                                           @RequestParam("group") Integer group,
                                           @PathVariable("parent-id") UUID parentId,
                                           @RequestParam("group") long groupId,
                                           HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        KEvent event = new KEvent();
        event.eventType = KEvent.EventType.NODE;
        event.requestType = KEvent.RequestType.POST;
        event.uuid = parentId;
        event.inputData = xmlNode;

        try {

            if (ui.userId == 0) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Not logged in");
            } else {
                String returnValue = nodeManager
                        .addNode(MimeTypeUtils.TEXT_XML, parentId, xmlNode, ui.userId, groupId, false);
                ResponseEntity<String> response = ResponseEntity
                                                    .status(event.status)
                                                    .header(HttpHeaders.CONTENT_TYPE, event.mediaType)
                                                    .body(returnValue);
                event.status = 200;
                // eventbus.processEvent(event); ???
                return response;
            }
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, "Vous n'avez pas les droits d'acces");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Move a node up between siblings. <br>
     * POST /rest/api/nodes/node/{node-id}/moveup
     *
     * @param nodeId
     * @param request
     * @return
     */
    @PostMapping(value = "/node/{node-id}/moveup", consumes = "application/xml", produces = "application/xml")
    public ResponseEntity<String> postMoveNodeUp(@PathVariable("node-id") UUID nodeId,
                                                 HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, null, null, null); // FIXME
        ResponseEntity<String> response = null;

        try {
            if (nodeId == null) {
                response = ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Missing uuid");
            } else {

                Long returnValue = nodeManager.moveNodeUp(nodeId);

                if (returnValue == -1L) {
                    response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Non-existing node");
                }
                if (returnValue == -2L) {
                    response = ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot move first node");
                } else {
                    response = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
                }
            }
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return response;
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
                                                       HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, null, null, null); // FIXME
        try {
            boolean returnValue = nodeManager.changeParentNode(ui.userId, nodeId, parentId);

            if (!returnValue) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cannot move");
            } else {
                return ResponseEntity.status(HttpStatus.OK).build();
            }
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Execute a macro command on a node, changing rights related. <br>
     * POST /rest/api/nodes/node/{node-id}/action/{action-name} *
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param macro
     * @param request
     * @return
     */
    @PostMapping(value = "/node/{node-id}/action/{action-name}", consumes = "application/xml",
        produces = "application/xml")
    public String postActionNode(@CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @RequestParam("group") int groupId,
                                 @PathVariable("node-id") UUID nodeId,
                                 @PathVariable("action-name") String macro,
                                 HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {

            String returnValue = nodeManager.executeMacroOnNode(ui.userId, nodeId, macro);
            if (returnValue == "erreur") {
                throw new RestWebApplicationException(HttpStatus.FORBIDDEN, "Vous n'avez pas les droits d'acces");
            }

            return returnValue;
        } catch (RestWebApplicationException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Delete a node<br>
     * DELETE /rest/api/nodes/node/{node-uuid}
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/node/{node-uuid}", produces = "application/xml")
    public String deleteNode(@CookieValue("user") String user,
                             @CookieValue("credential") String token,
                             @RequestParam("group") long groupId,
                             @PathVariable("node-uuid") UUID nodeId,
                             HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            nodeManager.removeNode(nodeId, ui.userId, groupId);
            return "";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch node content. <br>
     * GET /rest/api/nodes/{node-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param lang
     * @param xslFile
     * @param accept
     * @param request
     * @return
     */
    @GetMapping(value = "/{node-id}", consumes = "application/xml")
    public String getNodeWithXSL(@CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @RequestParam("group") long groupId,
                                 @PathVariable("node-id") UUID nodeId,
                                 @RequestParam("lang") String lang,
                                 @RequestParam("xsl-file") String xslFile,
                                 @RequestHeader("Accept") String accept,
                                 HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            // When we need more parameters, arrange this with format
            // "par1:par1val;par2:par2val;..."
            String parameters = "lang:" + lang;

            javax.servlet.http.HttpSession session = request.getSession(true);
            String ppath = session.getServletContext().getRealPath(File.separator);

            /// webapps...
            ppath = ppath.substring(0, ppath.lastIndexOf(File.separator, ppath.length() - 2) + 1);
            xslFile = ppath + xslFile;
            String returnValue = nodeManager
                    .getNodeWithXSL(MimeTypeUtils.TEXT_XML, nodeId, xslFile, parameters, ui.userId, groupId);
            if (returnValue.length() != 0) {
                if (MediaType.APPLICATION_JSON.equals(accept))
                    returnValue = XML.toJSONObject(returnValue).toString();
            }

            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND,
                    "Node " + nodeId + " not found or xsl not found :" + ex.getMessage());
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (NullPointerException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND,
                    "Node " + nodeId + " not found or xsl not found :" + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
        }
        return "";
    }

    /**
     *
     * POST /rest/api/nodes/{node-id}/frommodelbysemantictag/{semantic-tag}
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeId
     * @param semantictag
     * @param request
     * @return
     */
    @PostMapping(value = "/{node-id}/frommodelbysemantictag/{semantic-tag}", consumes = "application/xml",
        produces = "application/xml")
    public String postNodeFromModelBySemanticTag(@CookieValue("user") String user,
                                                 @CookieValue("credential") String token,
                                                 @RequestParam("group") long groupId,
                                                 @PathVariable("node-id") UUID nodeId,
                                                 @PathVariable("semantic-tag") String semantictag,
                                                 HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = nodeManager
                    .addNodeFromModelBySemanticTag(MimeTypeUtils.TEXT_XML, nodeId, semantictag, ui.userId, groupId);
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas les droits d'acces " + ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}
