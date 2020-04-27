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

package eportfolium.com.karuta.webapp.rest.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.json.XML;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.eventbus.KEvent;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/nodes")
public class NodeResource extends AbstractResource {

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
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @param cutoff
	 * @return nodes in the ASM format
	 */
	@Path("/node/{node-id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getNode(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("node-id") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId, @QueryParam("level") Integer cutoff) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {

			String returnValue = nodeManager
					.getNode(MimeTypeUtils.TEXT_XML, nodeUuid, false, ui.userId, groupId, null, cutoff).toString();
			if (returnValue.length() != 0) {
				if (accept.equals(MediaType.APPLICATION_JSON))
					returnValue = XML.toJSONObject(returnValue).toString();
			}
			return returnValue;
		} catch (DoesNotExistException ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.NOT_ACCEPTABLE, "Incorrect Mime Type");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch nodes and children from node uuid <br>
	 * GET /rest/api/nodes/node/{node-id}/children
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @param cutoff
	 * @return nodes in the ASM format
	 */
	@Path("/node/{node-id}/children")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getNodeWithChildren(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("node-id") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId, @QueryParam("level") Integer cutoff) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			String returnValue = nodeManager
					.getNode(MimeTypeUtils.TEXT_XML, nodeUuid, true, ui.userId, groupId, null, cutoff).toString();
			if (returnValue.length() != 0) {
				if (accept.equals(MediaType.APPLICATION_JSON)) {
					returnValue = XML.toJSONObject(returnValue).toString();
				}
			}
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (NullPointerException ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.NOT_ACCEPTABLE, "Incorrect Mime Type");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch nodes metdata <br>
	 * GET /rest/api/nodes/node/{node-id}/metadatawad
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @return <metadata-wad/>
	 */
	@Path("/node/{nodeid}/metadatawad")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getNodeMetadataWad(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("nodeid") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {

			String returnValue = nodeManager.getNodeMetadataWad(MimeTypeUtils.TEXT_XML, nodeUuid, ui.userId, groupId)
					.toString();
			if (returnValue.length() != 0) {
				if (accept.equals(MediaType.APPLICATION_JSON))
					returnValue = XML.toJSONObject(returnValue).toString();
			}
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch rights per role for a node. <br>
	 * GET /rest/api/nodes/node/{node-id}/rights
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @return <node uuid=""> <role name=""> <right RD="" WR="" DL="" /> </role>
	 *         </node>
	 */
	@Path("/node/{node-id}/rights")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getNodeRights(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("node-id") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {

			GroupRights gr = nodeManager.getRights(ui.userId, groupId, nodeUuid);
			String returnValue = null;
			if (gr != null) {
				if (accept.equals(MediaType.APPLICATION_JSON))
					returnValue = XML.toJSONObject(returnValue).toString();
			} else {
				throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'avez pas les droits necessaires");
			}

			return returnValue;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (NullPointerException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch portfolio id from a given node id. <br>
	 * GET /rest/api/nodes/node/{node-id}/portfolioid
	 * 
	 * @param user
	 * @param token
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @return portfolioid
	 */
	@Path("/node/{node-id}/portfolioid")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getNodePortfolioId(@CookieParam("user") String user, @CookieParam("credential") String token,
			@PathParam("node-id") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return nodeManager.getPortfolioIdFromNode(ui.userId, nodeUuid).toString();
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND,
					"Error, this shouldn't happen. No Portfolio related to node : '" + nodeUuid + "' was found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(ex.getStatus(), ex.getResponse().getEntity().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @return
	 */
	@Path("/node/{node-id}/rights")
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String postNodeRights(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId,
			@PathParam("node-id") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

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
						nodeRights.setRead("Y".equals(val) ? true : false);
					val = rights.getNamedItem("WR").getNodeValue();
					if (val != null)
						nodeRights.setWrite("Y".equals(val) ? true : false);
					val = rights.getNamedItem("DL").getNodeValue();
					if (val != null)
						nodeRights.setDelete("Y".equals(val) ? true : false);
					val = rights.getNamedItem("SB").getNodeValue();
					if (val != null)
						nodeRights.setSubmit("Y".equals(val) ? true : false);

					// Executer le changement de droits.
					nodeManager.changeRights(ui.userId, nodeUuid, roleName, nodeRights);
				} else if ("action".equals(right.getNodeName())) // Using an action on node
				{
					// réinitialiser les droits
					nodeManager.executeMacroOnNode(ui.userId, nodeUuid, "reset");
				}
			}
			logger.info("Change rights " + Status.OK.getStatusCode());
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (NullPointerException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param portfolioUuid
	 * @param semantictag
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @return node in ASM format
	 */
	@Path("/firstbysemantictag/{portfolio-uuid}/{semantictag}")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getNodeBySemanticTag(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("portfolio-uuid") String portfolioUuid,
			@PathParam("semantictag") String semantictag, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			return nodeManager
					.getNodeBySemanticTag(MimeTypeUtils.TEXT_XML, portfolioUuid, semantictag, ui.userId, groupId)
					.toString();
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "no node found for tag :" + semantictag);
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get multiple semantic tag nodes inside specified portfolio. <br>
	 * GET /rest/api/nodes/nodes/bysemantictag/{portfolio-uuid}/{semantictag}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param portfolioUuid
	 * @param semantictag
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @return nodes in ASM format
	 */
	@Path("/bysemantictag/{portfolio-uuid}/{semantictag}")
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getNodesBySemanticTag(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("portfolio-uuid") String portfolioUuid,
			@PathParam("semantictag") String semantictag, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			return nodeManager
					.getNodesBySemanticTag(MimeTypeUtils.TEXT_XML, ui.userId, groupId, portfolioUuid, semantictag)
					.toString();
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@Path("/node/{node-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putNode(String xmlNode, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("node-id") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String returnValue = nodeManager.changeNode(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, ui.userId, groupId)
					.toString();
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/node/{nodeid}/metadata")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putNodeMetadata(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId, @QueryParam("info") String info,
			@PathParam("nodeid") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

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
					.changeNodeMetadata(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, ui.userId, groupId).toString();
			logger.info(String.format(logformat, "OK", nodeUuid, "metadata", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (BusinessException ex) {
			logger.error(String.format(logformat, "ERR", nodeUuid, "metadata", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/node/{nodeid}/metadatawad")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putNodeMetadataWad(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") Long groupId, @QueryParam("info") String info,
			@PathParam("nodeid") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

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
					.changeNodeMetadataWad(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, ui.userId, groupId).toString();
			logger.info(String.format(logformat, "OK", nodeUuid, "metadatawad", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (BusinessException ex) {
			logger.error(String.format(logformat, "ERR", nodeUuid, "metadatawad", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Rewrite node epm metadata.<br>
	 * PUT /rest/api/nodes/node/{node-id}/metadataepm
	 * 
	 * @param xmlNode
	 * @param nodeUuid
	 * @param groupId
	 * @param info
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/node/{nodeid}/metadataepm")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putNodeMetadataEpm(String xmlNode, @PathParam("nodeid") String nodeUuid,
			@QueryParam("group") long groupId, @QueryParam("info") String info, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, null, null, null);

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
					.changeNodeMetadataEpm(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, ui.userId, groupId).toString();
			logger.info(String.format(logformat, "OK", nodeUuid, "metadataepm", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Node " + nodeUuid + " not found");
		} catch (BusinessException ex) {
			logger.error(String.format(logformat, "ERR", nodeUuid, "metadataepm", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Rewrite node nodecontext. <br>
	 * PUT /rest/api/nodes/node/{node-id}/nodecontext parameters: return:
	 **/
	/**
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param groupId
	 * @param info
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/node/{nodeid}/nodecontext")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putNodeNodeContext(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId, @QueryParam("info") String info,
			@PathParam("nodeid") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

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
					.changeNodeContext(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, ui.userId, groupId).toString();
			logger.info(String.format(logformat, "OK", nodeUuid, "nodecontext", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			return returnValue;
		} catch (BusinessException ex) {
			logger.error(String.format(logformat, "ERR", nodeUuid, "nodecontext", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/node/{nodeid}/noderesource")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putNodeNodeResource(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId, @QueryParam("info") String info,
			@PathParam("nodeid") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

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
					.changeNodeResource(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, ui.userId, groupId).toString();
			logger.info(String.format(logformat, "OK", nodeUuid, "noderesource", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			return returnValue;
		} catch (BusinessException ex) {
			logger.error(String.format(logformat, "ERR", nodeUuid, "noderesource", ui.userId, timeFormat,
					httpServletRequest.getRemoteAddr(), xmlNode));
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Instanciate a node with right parsing <br>
	 * POST /rest/api/nodes/node/import/{dest-id}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param groupId
	 * @param parentId
	 * @param sc
	 * @param httpServletRequest
	 * @param semtag
	 * @param code
	 * @param srcuuid
	 * @return
	 */
	@Path("/node/import/{dest-id}")
	@POST
	public String postImportNode(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId,
			@PathParam("dest-id") String parentId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("srcetag") String semtag,
			@QueryParam("srcecode") String code, @QueryParam("uuid") String srcuuid) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		if (ui.userId == 0)
			throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'êtes pas connecté");

		try {
			return nodeManager.importNode(MimeTypeUtils.TEXT_XML, parentId, semtag, code, srcuuid, ui.userId, groupId)
					.toString();
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Raw copy a node. <br>
	 * POST /rest/api/nodes/node/copy/{dest-id}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param groupId
	 * @param parentId
	 * @param sc
	 * @param httpServletRequest
	 * @param semtag
	 * @param code
	 * @param srcuuid
	 * @return
	 */
	@Path("/node/copy/{dest-id}")
	@POST
	public String postCopyNode(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId,
			@PathParam("dest-id") String parentId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("srcetag") String semtag,
			@QueryParam("srcecode") String code, @QueryParam("uuid") String srcuuid) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		if (ui.userId == 0)
			throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'êtes pas connecté");

		try {
			return nodeManager.copyNode(MimeTypeUtils.TEXT_XML, parentId, semtag, code, srcuuid, ui.userId, groupId)
					.toString();
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param sc
	 * @param httpServletRequest
	 * @param portfoliocode      mandatory
	 * @param semtag             mandatory, find the semtag under portfoliocode, or
	 *                           the selection from semtag_parent/code_parent
	 * @param semtag_parent
	 * @param code_parent        From a code_parent, find the children that have
	 *                           semtag_parent
	 * @param cutoff
	 * @return
	 */
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getNodes(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("dest-id") String parentId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("portfoliocode") String portfoliocode,
			@QueryParam("semtag") String semtag, @QueryParam("semtag_parent") String semtag_parent,
			@QueryParam("code_parent") String code_parent, @QueryParam("level") Integer cutoff) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			return nodeManager.getNodes(MimeTypeUtils.TEXT_XML, portfoliocode, semtag, ui.userId, groupId,
					semtag_parent, code_parent, cutoff).toString();
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Portfolio inexistant");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'avez pas les droits d'acces");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("getNodes", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
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
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @param groupId
	 * @return
	 */
	@Path("/node/{parent-id}")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postNode(String xmlNode, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") Integer group, @PathParam("parent-id") String parentId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId,
			@QueryParam("group") long groupId) {
		if (!isUUID(parentId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		KEvent event = new KEvent();
		event.eventType = KEvent.EventType.NODE;
		event.requestType = KEvent.RequestType.POST;
		event.uuid = parentId;
		event.inputData = xmlNode;

		try {

			if (ui.userId == 0) {
				return Response.status(403).entity("Not logged in").build();
			} else {
				String returnValue = nodeManager
						.addNode(MimeTypeUtils.TEXT_XML, parentId, xmlNode, ui.userId, groupId, false).toString();
				Response response;
				event.status = 200;
				response = Response.status(event.status).entity(returnValue).type(event.mediaType).build();
				// eventbus.processEvent(event); ???
				return response;
			}
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'avez pas les droits d'acces");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Move a node up between siblings. <br>
	 * POST /rest/api/nodes/node/{node-id}/moveup
	 * 
	 * @param xmlNode
	 * @param nodeId
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/node/{node-id}/moveup")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postMoveNodeUp(String xmlNode, @PathParam("node-id") String nodeId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(nodeId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, null, null, null);
		Response response = null;

		try {
			if (nodeId == null) {
				response = Response.status(400).entity("Missing uuid").build();
			} else {

				Long returnValue = nodeManager.moveNodeUp(nodeId);

				if (returnValue == -1L) {
					response = Response.status(404).entity("Non-existing node").build();
				}
				if (returnValue == -2L) {
					response = Response.status(409).entity("Cannot move first node").build();
				} else {
					response = Response.status(204).build();
				}
			}
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		return response;
	}

	/**
	 * Move a node to another parent. <br>
	 * POST /rest/api/nodes/node/{node-id}/parentof/{parent-id}
	 * 
	 * @param xmlNode
	 * @param nodeId
	 * @param parentId
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/node/{node-id}/parentof/{parent-id}")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response postChangeNodeParent(String xmlNode, @PathParam("node-id") String nodeId,
			@PathParam("parent-id") String parentId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(nodeId) || !isUUID(parentId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, null, null, null); // FIXME
		try {
			boolean returnValue = nodeManager.changeParentNode(ui.userId, nodeId, parentId);
			Response response;
			if (returnValue == false) {
				response = Response.status(409).entity("Cannot move").build();
			} else {
				response = Response.status(200).build();
			}

			return response;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Execute a macro command on a node, changing rights related. <br>
	 * POST /rest/api/nodes/node/{node-id}/action/{action-name} *
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param groupId
	 * @param nodeId
	 * @param macro
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@Path("/node/{node-id}/action/{action-name}")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postActionNode(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId,
			@PathParam("node-id") String nodeId, @PathParam("action-name") String macro, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId) {
		if (!isUUID(nodeId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null); // FIXME

		try {

			String returnValue = nodeManager.executeMacroOnNode(ui.userId, nodeId, macro);
			if (returnValue == "erreur") {
				throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'avez pas les droits d'acces");
			}

			return returnValue;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Delete a node<br>
	 * DELETE /rest/api/nodes/node/{node-uuid}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param nodeUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@Path("/node/{node-uuid}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteNode(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("node-uuid") String nodeUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			nodeManager.removeNode(nodeUuid, ui.userId, groupId);
			return "";
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch node content. <br>
	 * GET /rest/api/nodes/{node-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param nodeUuid
	 * @param xslFile
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @param lang
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	@Path("/{node-id}")
	@GET
	@Consumes(MediaType.APPLICATION_XML)
	public String getNodeWithXSL(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("node-id") String nodeUuid,
			@QueryParam("xsl-file") String xslFile, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId, @QueryParam("lang") String lang, @QueryParam("p1") String p1,
			@QueryParam("p2") String p2, @QueryParam("p3") String p3) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			// When we need more parameters, arrange this with format
			// "par1:par1val;par2:par2val;..."
			String parameters = "lang:" + lang;

			javax.servlet.http.HttpSession session = httpServletRequest.getSession(true);
			String ppath = session.getServletContext().getRealPath(File.separator);

			/// webapps...
			ppath = ppath.substring(0, ppath.lastIndexOf(File.separator, ppath.length() - 2) + 1);
			xslFile = ppath + xslFile;
			String returnValue = nodeManager
					.getNodeWithXSL(MimeTypeUtils.TEXT_XML, nodeUuid, xslFile, parameters, ui.userId, groupId)
					.toString();
			if (returnValue.length() != 0) {
				if (MediaType.APPLICATION_JSON.equals(accept))
					returnValue = XML.toJSONObject(returnValue).toString();
			}

			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND,
					"Node " + nodeUuid + " not found or xsl not found :" + ex.getMessage());
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (NullPointerException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND,
					"Node " + nodeUuid + " not found or xsl not found :" + ex.getMessage());
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
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param groupId
	 * @param nodeUuid
	 * @param semantictag
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@Path("/{node-id}/frommodelbysemantictag/{semantic-tag}")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postNodeFromModelBySemanticTag(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId,
			@PathParam("node-id") String nodeUuid, @PathParam("semantic-tag") String semantictag,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("user") Long userId) {
		if (!isUUID(nodeUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String returnValue = nodeManager
					.addNodeFromModelBySemanticTag(MimeTypeUtils.TEXT_XML, nodeUuid, semantictag, ui.userId, groupId)
					.toString();
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN,
					"Vous n'avez pas les droits d'acces " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
