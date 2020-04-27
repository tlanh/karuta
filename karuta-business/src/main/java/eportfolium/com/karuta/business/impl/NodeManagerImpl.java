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

package eportfolium.com.karuta.business.impl;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.consumer.contract.dao.AnnotationDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioDao;
import eportfolium.com.karuta.consumer.contract.dao.ResourceTableDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.Annotation;
import eportfolium.com.karuta.model.bean.AnnotationId;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.util.InMemoryCache;

/**
 * @author mlengagne
 */
@Service
@Transactional
public class NodeManagerImpl extends BaseManager implements NodeManager {

	static private final Logger log = LoggerFactory.getLogger(NodeManagerImpl.class);

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private PortfolioManager portfolioManager;

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private PortfolioDao portfolioDao;

	@Autowired
	private NodeDao nodeDao;

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private ResourceTableDao resourceTableDao;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private GroupInfoDao groupInfoDao;

	@Autowired
	private GroupUserDao groupUserDao;

	@Autowired
	private AnnotationDao annotationDao;

	private InMemoryCache<String, List<Node>> cachedNodes = new InMemoryCache<String, List<Node>>(600, 1500, 6);

	public String getNode(MimeType outMimeType, String nodeUuid, boolean withChildren, Long userId, Long groupId,
			String label, Integer cutoff) throws DoesNotExistException, BusinessException, Exception {
		final GroupRights rights = getRights(userId, groupId, nodeUuid);

		if (!rights.isRead()) {
			userId = credentialDao.getPublicUid();
			/// Vérifie les droits avec le compte publique (dernière chance)
			if (!nodeDao.isPublic(nodeUuid))
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
		}

		if (outMimeType.getSubtype().equals("xml")) {
			List<Pair<Node, GroupRights>> nodes = getNodePerLevel(nodeUuid, userId, rights.getGroupRightInfo().getId(),
					cutoff);

			/// Préparation du XML que l'on va renvoyer
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = null;
			Document document = null;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.newDocument();
			document.setXmlStandalone(true);

			Map<String, Object[]> resolve = new HashMap<String, Object[]>();
			/// Node -> parent
			Map<String, t_tree> entries = new HashMap<String, t_tree>();
			processQuery(nodes, resolve, entries, document, documentBuilder, rights.getGroupRightInfo().getLabel());

			/// Reconstruct functional tree
			t_tree root = entries.get(nodeUuid);
			StringBuilder out = new StringBuilder(256);
			reconstructTree(out, root, entries);

			String nodexml = out.toString();

			return nodexml;
		} else if (outMimeType.getSubtype().equals("json"))
			return "{" + getNodeJsonOutput(nodeUuid, withChildren, null, userId, groupId, label, true) + "}";
		else {
			return null;
		}
	}

	public String getChildNodes(String parentNodeCode, String parentSemtag, String childSemtag) throws Exception {
		String result = "";
		try {
			Portfolio portfolio = portfolioDao.getPortfolioFromNodeCode(parentNodeCode);
			Node parentNode = nodeDao.getNodeBySemtagAndCode(portfolio.getId().toString(), parentSemtag,
					parentNodeCode);
			if (parentNode != null) {
				final List<Node> children = nodeDao.getFirstLevelChildren(parentNode.getId().toString());
				if (CollectionUtils.isNotEmpty(children)) {
					result += "<nodes>";
					for (Node child : children) {
						Node tmp = nodeDao.getNodeBySemanticTag(child.getId(), childSemtag);
						if (tmp != null) {
							result += "<node ";
							result += DomUtils.getXmlAttributeOutput("id", tmp.getId().toString());
							result += ">";
							result += "</node>";
						}
					}
					result += "</nodes>";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String writeNode(org.w3c.dom.Node node, String portfolioUuid, String portfolioModelId, Long userId,
			int ordrer, String forcedUuid, String forcedUuidParent, boolean sharedResParent,
			boolean sharedNodeResParent, boolean rewriteId, Map<String, String> resolve, boolean parseRights)
			throws BusinessException {

		String nodeUuid = "";
		String originUuid = null;
		String parentUuid = null;
		boolean sharedRes = false;
		boolean sharedNode = false;
		boolean sharedNodeRes = false;

		String sharedResUuid = null;
		String sharedNodeUuid = null;
		String sharedNodeResUuid = null;

		String metadata = "";
		String metadataWad = "";
		String metadataEpm = "";
		String asmType = null;
		String xsiType = null;
		String semtag = null;
		String format = null;
		String label = null;
		String code = null;
		String descr = null;
		String semanticTag = null;

		String nodeRole = null;

		if (node == null)
			return null;

		if (node.getNodeName().equals("portfolio")) {
			// On n'attribue pas d'uuid sur la balise portfolio
		} else {
		}

		String currentNodeid = "";
		org.w3c.dom.Node idAtt = node.getAttributes().getNamedItem("id");
		if (idAtt != null) {
			String tempId = idAtt.getNodeValue();
			if (tempId.length() > 0)
				currentNodeid = tempId;
		}

		// Si uuid force, alors on ne tient pas compte de l'uuid indique dans le XML.
		if (rewriteId) // On garde les uuid par défaut
		{
			nodeUuid = currentNodeid;
		} else if (forcedUuid != null && !"".equals(forcedUuid)) {
			nodeUuid = forcedUuid;
		} else
			nodeUuid = UUID.randomUUID().toString();

		if (resolve != null) // Mapping old id -> new id
			resolve.put(currentNodeid, nodeUuid);

		if (forcedUuidParent != null) {
			// Dans le cas d'un uuid parent force => POST => on génère un UUID
			parentUuid = forcedUuidParent;
		}

		/// Récupération d'autres informations
		try {
			if (node.getNodeName() != null)
				asmType = node.getNodeName();
		} catch (Exception ex) {
		}
		try {
			if (node.getAttributes().getNamedItem("xsi_type") != null)
				xsiType = node.getAttributes().getNamedItem("xsi_type").getNodeValue().trim();
		} catch (Exception ex) {
		}
		try {
			if (node.getAttributes().getNamedItem("semtag") != null)
				semtag = node.getAttributes().getNamedItem("semtag").getNodeValue().trim();
		} catch (Exception ex) {

		}
		try {
			if (node.getAttributes().getNamedItem("format") != null)
				format = node.getAttributes().getNamedItem("format").getNodeValue().trim();
		} catch (Exception ex) {
		}

		// Si id défini, alors on écrit en base
		// TODO Transactionnel noeud+enfant
		NodeList children = null;
		try {
			children = node.getChildNodes();
			// On parcourt une premiere fois les enfants pour récupérer la liste à écrire en
			// base.
			for (int i = 0; i < children.getLength(); i++) {
				org.w3c.dom.Node child = children.item(i);

				if ("#text".equals(child.getNodeName()))
					continue;

				if (children.item(i).getNodeName().equals("metadata-wad")) {
					metadataWad = DomUtils.getNodeAttributesString(children.item(i));

					if (parseRights) {
						// Gestion de la sécurité intégrée
						//
						org.w3c.dom.Node metadataWadNode = children.item(i);

						try {
							if (metadataWadNode.getAttributes().getNamedItem("seenoderoles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("seenoderoles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {

									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.READ, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}
						try {
							if (metadataWadNode.getAttributes().getNamedItem("delnoderoles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("delnoderoles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {

									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.DELETE, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}
						try {
							if (metadataWadNode.getAttributes().getNamedItem("editnoderoles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("editnoderoles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {

									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.WRITE, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}
						try {
							if (metadataWadNode.getAttributes().getNamedItem("submitnoderoles") != null) // TODO
																											// submitnoderoles
																											// deprecated
																											// fro
																											// submitroles
							{
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("submitnoderoles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.SUBMIT, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}
						//
						try {
							if (metadataWadNode.getAttributes().getNamedItem("seeresroles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("seeresroles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.READ, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}

						try {
							if (metadataWadNode.getAttributes().getNamedItem("delresroles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("delresroles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.DELETE, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}

						try {
							if (metadataWadNode.getAttributes().getNamedItem("editresroles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("editresroles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.WRITE, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}

						try {
							if (metadataWadNode.getAttributes().getNamedItem("submitresroles") != null)
							// TODO submitresroles deprecated fro submitroles
							{
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("submitresroles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.SUBMIT, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}

						try {
							if (metadataWadNode.getAttributes().getNamedItem("submitroles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("submitroles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.SUBMIT, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}

						try {
							if (metadataWadNode.getAttributes().getNamedItem("showtoroles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("showtoroles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupManager.addGroupRights(nodeRole, nodeUuid, GroupRights.NONE, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}

						try {
							org.w3c.dom.Node actionroles = metadataWadNode.getAttributes().getNamedItem("actionroles");
							if (actionroles != null) {
								/// Format pour l'instant: actionroles="sender:1,2;responsable:4"
								StringTokenizer tokens = new StringTokenizer(actionroles.getNodeValue(), ";");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									StringTokenizer data = new StringTokenizer(nodeRole, ":");
									String role = data.nextElement().toString();
									String actions = data.nextElement().toString();
									groupManager.addGroupRights(role, nodeUuid, actions, portfolioUuid, userId);
								}
							}
						} catch (Exception ex) {
						}

						try /// TODO: e l'integration avec sakai/LTI
						{
							org.w3c.dom.Node notifyroles = metadataWadNode.getAttributes().getNamedItem("notifyroles");
							if (notifyroles != null) {
								/// Format pour l'instant: actionroles="sender:1,2;responsable:4"
								StringTokenizer tokens = new StringTokenizer(notifyroles.getNodeValue(), " ");
								String notifyRoles = "";
								if (tokens.hasMoreElements())
									notifyRoles = tokens.nextElement().toString();
								while (tokens.hasMoreElements())
									notifyRoles += "," + tokens.nextElement().toString();

								groupManager.changeNotifyRoles(userId, portfolioUuid, nodeUuid, notifyRoles);
							}
						} catch (Exception ex) {
						}

					}

				} else if (children.item(i).getNodeName().equals("metadata-epm")) {
					metadataEpm = DomUtils.getNodeAttributesString(children.item(i));
				} else if (children.item(i).getNodeName().equals("metadata")) {
//					/*
					try {
						String publicatt = children.item(i).getAttributes().getNamedItem("public").getNodeValue();
						if ("Y".equals(publicatt))
							groupManager.setPublicState(userId, portfolioUuid, true);
						else if ("N".equals(publicatt))
							groupManager.setPublicState(userId, portfolioUuid, false);
					} catch (Exception ex) {
					}
					// */

					String tmpSharedRes = "";
					try {
						tmpSharedRes = children.item(i).getAttributes().getNamedItem("sharedResource").getNodeValue();
					} catch (Exception ex) {
					}

					String tmpSharedNode = "";
					try {
						tmpSharedNode = children.item(i).getAttributes().getNamedItem("sharedNode").getNodeValue();
					} catch (Exception ex) {
					}

					String tmpSharedNodeRes = "";
					try {
						tmpSharedNodeRes = children.item(i).getAttributes().getNamedItem("sharedNodeResource")
								.getNodeValue();
					} catch (Exception ex) {
					}

					try {
						semanticTag = children.item(i).getAttributes().getNamedItem("semantictag").getNodeValue();
					} catch (Exception ex) {
					}

					if (tmpSharedRes.equalsIgnoreCase("y"))
						sharedRes = true;
					if (tmpSharedNode.equalsIgnoreCase("y"))
						sharedNode = true;
					if (tmpSharedNodeRes.equalsIgnoreCase("y"))
						sharedNodeRes = true;

					metadata = DomUtils.getNodeAttributesString(children.item(i));
				}
				// On vérifie si l'enfant n'est pas un element de type code, label ou descr
				else if (children.item(i).getNodeName().equals("label")) {
					label = DomUtils.getInnerXml(children.item(i));
				} else if (children.item(i).getNodeName().equals("code")) {
					code = DomUtils.getInnerXml(children.item(i));
				} else if (children.item(i).getNodeName().equals("description")) {
					descr = DomUtils.getInnerXml(children.item(i));
				} else if (children.item(i).getAttributes() != null) {
				}
			}
		} catch (Exception ex) {
			// Pas d'enfants
			ex.printStackTrace();
		}

		// Si on est au début de l'arbre, on stocke la definition du portfolio
		// dans la table portfolio
		if (nodeUuid != null && node.getParentNode() != null) {
			// On retrouve le code caché dans les ressources.
			NodeList childs = node.getChildNodes();
			for (int k = 0; k < childs.getLength(); ++k) {
				org.w3c.dom.Node child = childs.item(k);
				if ("asmResource".equals(child.getNodeName())) {
					NodeList grandchilds = child.getChildNodes();
					for (int l = 0; l < grandchilds.getLength(); ++l) {
						org.w3c.dom.Node gc = grandchilds.item(l);
						if ("code".equals(gc.getNodeName())) {
							code = DomUtils.getInnerXml(gc);
							break;
						}
					}
				}
				if (code != null)
					break;
			}

			if (node.getNodeName().equals("asmRoot")) {
			} else if (portfolioUuid == null)
				throw new GenericBusinessException("Il manque la balise asmRoot !!");
		}

		// Si on instancie un portfolio à partir d'un modèle
		// Alors on gère les share*
		if (portfolioModelId != null) {
			if (sharedNode) {
				sharedNodeUuid = originUuid;
			}
		}

		if (nodeUuid != null && !node.getNodeName().equals("portfolio") && !node.getNodeName().equals("asmResource"))
			nodeUuid = nodeDao.add(nodeUuid, parentUuid, "", asmType, xsiType, sharedRes, sharedNode, sharedNodeRes,
					sharedResUuid, sharedNodeUuid, sharedNodeResUuid, metadata, metadataWad, metadataEpm, semtag,
					semanticTag, label, code, descr, format, ordrer, userId, portfolioUuid);

		// Si le parent a été force, cela veut dire qu'il faut mettre e jour les enfants
		// du parent TODO
		// MODIF : On le met à jour tout le temps car dans le cas d'un POST les uuid ne
		// sont pas connus à l'avance
		// if(forcedUuidParent!=null)
		// Si le noeud est de type asmResource, on stocke le innerXML du noeud
		if (node.getNodeName().equals("asmResource")) {
			if (portfolioModelId != null) {
				if (xsiType.equals("nodeRes") && sharedNodeResParent) {
					sharedNodeResUuid = originUuid;
					resourceTableDao.addResource(sharedNodeResUuid, parentUuid, xsiType, DomUtils.getInnerXml(node),
							portfolioModelId.toString(), sharedNodeResParent, sharedResParent, userId);
				} else if (!xsiType.equals("context") && !xsiType.equals("nodeRes") && sharedResParent) {
					sharedResUuid = originUuid;
					resourceTableDao.addResource(sharedResUuid, parentUuid, xsiType, DomUtils.getInnerXml(node),
							portfolioModelId.toString(), sharedNodeResParent, sharedResParent, userId);
				} else {
					resourceTableDao.addResource(nodeUuid, parentUuid, xsiType, DomUtils.getInnerXml(node),
							portfolioModelId.toString(), sharedNodeResParent, sharedResParent, userId);
				}
			} else
				resourceTableDao.addResource(nodeUuid, parentUuid, xsiType, DomUtils.getInnerXml(node), null,
						sharedNodeResParent, sharedResParent, userId);
		}

		// Ensuite, on parcourt à nouveau les enfants pour continuer la récursivité.
		if (children != null) {
			int k = 0;
			for (int i = 0; i < children.getLength(); i++) {
				org.w3c.dom.Node child = children.item(i);
				String childId = null;
				if (!rewriteId)
					childId = UUID.randomUUID().toString();

				if (child.getAttributes() != null) {
					String nodeName = child.getNodeName();
					if ("asmRoot".equals(nodeName) || "asmStructure".equals(nodeName) || "asmUnit".equals(nodeName)
							|| "asmUnitStructure".equals(nodeName) || "asmUnitContent".equals(nodeName)
							|| "asmContext".equals(nodeName)) {
						writeNode(child, portfolioUuid, portfolioModelId, userId, k, childId, nodeUuid, sharedRes,
								sharedNodeRes, rewriteId, resolve, parseRights);
						k++;
					} else if ("asmResource".equals(nodeName)) // Les asmResource pose problème dans l'ordre des noeuds
					{
						writeNode(child, portfolioUuid, portfolioModelId, userId, k, childId, nodeUuid, sharedRes,
								sharedNodeRes, rewriteId, resolve, parseRights);
					}
				}
			}
		}
		nodeDao.updateNode(forcedUuidParent);
		return nodeUuid;
	}

	private StringBuffer getNodeJsonOutput(String nodeUuid, boolean withChildren, String withChildrenOfXsiType,
			Long userId, Long groupId, String label, boolean checkSecurity) {
		StringBuffer result = new StringBuffer();
		ResourceTable resResource = null;

		if (checkSecurity) {
			GroupRights nodeRight = getRights(userId, groupId, nodeUuid);
			if (!nodeRight.isRead())
				return result;
		}

		try {
			Node resNode = nodeDao.findById(UUID.fromString(nodeUuid));

			result.append("\"" + resNode.getAsmType() + "\": { "
					+ DomUtils.getJsonAttributeOutput("id", resNode.getId() + ", "));
			result.append(DomUtils.getJsonAttributeOutput("semantictag", resNode.getSemtag()) + ", ");

			if (resNode.getXsiType() != null)
				if (resNode.getXsiType().length() > 0)
					result.append(DomUtils.getJsonAttributeOutput("xsi_type", resNode.getXsiType()) + ", ");

			result.append(DomUtils.getJsonAttributeOutput("format", resNode.getFormat()) + ", ");
			result.append(DomUtils.getJsonAttributeOutput("modified", resNode.getModifDate().toGMTString()) + ", ");

			// si asmResource
			if (resNode.getAsmType().equals("asmResource")) {
				try {
					resResource = resourceTableDao.getResource(nodeUuid);
					if (resResource != null)
						result.append("\"#cdata-section\": \"" + JSONObject.escape(resResource.getContent()) + "\"");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			if (withChildren || withChildrenOfXsiType != null) {
				String[] arrayChild;

				if (resNode.getChildrenStr().length() > 0) {
					result.append(", ");
					arrayChild = resNode.getChildrenStr().split(",");
					for (int i = 0; i < (arrayChild.length); i++) {
						try {
							Node childNode = nodeDao.findById(UUID.fromString(arrayChild[i]));
							if (withChildrenOfXsiType == null
									|| StringUtils.equals(withChildrenOfXsiType, childNode.getXsiType()))
								result.append(
										getNodeJsonOutput(arrayChild[i], true, null, userId, groupId, label, true));

							if (withChildrenOfXsiType == null)
								if (arrayChild.length > 1)
									if (i < (arrayChild.length - 1))
										result.append(", ");
						} catch (DoesNotExistException ex) {
							// Noeud enfant non trouvé en base
						}
					}
				}
			}
			result.append(" } ");
		} catch (DoesNotExistException e) {
		}

		return result;
	}

	public String getNodeXmlOutput(String nodeUuid, boolean withChildren, String withChildrenOfXsiType, Long userId,
			Long groupId, String label, boolean checkSecurity) {
		StringBuffer result = new StringBuffer();

		// Vérification de sécurité
		if (checkSecurity) {
			GroupRights rights = getRights(userId, groupId, nodeUuid);
			if (!rights.isRead()) {
				userId = credentialDao.getPublicUid();
				/// Vérifie les droits avec le compte publique (dernière chance)
				rights = groupRightsDao.getPublicRightsByUserId(nodeUuid, userId);
				if (!rights.isRead())
					return result.toString();
			}
		}

		ResourceTable resResource = null;

		String indentation = " ";

		try {
			Node resNode = nodeDao.findById(UUID.fromString(nodeUuid));
			if (resNode.getSharedNodeUuid() != null) {
				result.append(getNodeXmlOutput(resNode.getSharedNodeUuid().toString(), true, null, userId, groupId,
						null, true));
			} else {
				result.append(indentation + "<" + resNode.getAsmType() + " "
						+ DomUtils.getXmlAttributeOutput("id", resNode.getId().toString()) + " ");
				result.append(">");

				if (!resNode.getAsmType().equals("asmResource")) {
					DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder;
					Document document = null;
					try {
						builder = newInstance.newDocumentBuilder();
						document = builder.newDocument();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					}

					if (resNode.getMetadataWad() != null && !resNode.getMetadataWad().equals("")) {
						Element meta = document.createElement("metadata-wad");
						convertAttr(meta, resNode.getMetadataWad());

						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer;
						try {
							transformer = transFactory.newTransformer();
							StringWriter buffer = new StringWriter();
							transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
							transformer.transform(new DOMSource(meta), new StreamResult(buffer));
							result.append(buffer.toString());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else
						result.append("<metadata-wad/>");

					if (resNode.getMetadataEpm() != null && !resNode.getMetadataEpm().equals(""))
						result.append("<metadata-epm " + resNode.getMetadataEpm() + "/>");
					else
						result.append("<metadata-epm/>");

					if (resNode.getMetadata() != null && !resNode.getMetadata().equals(""))
						result.append("<metadata " + resNode.getMetadata() + "/>");
					else
						result.append("<metadata/>");

					//
					result.append(DomUtils.getXmlElementOutput("code", resNode.getCode()));
					result.append(DomUtils.getXmlElementOutput("label", resNode.getLabel()));
					result.append(DomUtils.getXmlElementOutput("description", resNode.getDescr()));
					try {
						result.append(DomUtils.getXmlElementOutput("semanticTag", resNode.getSemantictag()));
					} catch (Exception ex) {
						result.append(DomUtils.getXmlElementOutput("semanticTag", ""));
					}
				} else {
				}

				if (resNode.getResResource() != null) {
					resResource = resNode.getResResource();
					if (resResource.getId().toString().length() > 0) {
						result.append("<asmResource id='" + resResource.getId().toString() + "'  contextid='" + nodeUuid
								+ "' xsi_type='nodeRes'>");
						result.append(resResource.getContent());
						result.append("</asmResource>");
					}
				}
				if (resNode.getContextResource() != null) {
					resResource = resNode.getContextResource();
					if (resResource.getId().toString().length() > 0) {
						result.append("<asmResource id='" + resResource.getId().toString() + "' contextid='" + nodeUuid
								+ "' xsi_type='context'>");
						result.append(resResource.getContent());
						result.append("</asmResource>");
					}
				}
				if (resNode.getResource() != null) {
					resResource = resNode.getResource();
					if (resNode.getId().toString().length() > 0) {
						result.append("<asmResource id='" + resNode.getId().toString() + "' contextid='" + nodeUuid
								+ "' xsi_type='" + resResource.getXsiType() + "'>");

						result.append(resResource.getContent());
						result.append("</asmResource>");
					}
				}
			}

			if (withChildren || withChildrenOfXsiType != null) {
				String[] arrayChild;
				if (StringUtils.isNotEmpty(resNode.getChildrenStr())) {
					arrayChild = resNode.getChildrenStr().split(",");
					for (int i = 0; i < (arrayChild.length); i++) {
						try {
							Node resChildNode = nodeDao.findById(UUID.fromString(arrayChild[i]));
							String tmpXsiType = resChildNode.getXsiType();

							if (withChildrenOfXsiType == null || withChildrenOfXsiType.equals(tmpXsiType))
								result.append(getNodeXmlOutput(arrayChild[i], true, null, userId, groupId, null, true));
						} catch (DoesNotExistException e) {
						}
					}
				}
			}
			result.append("</" + resNode.getAsmType() + ">");
		} catch (DoesNotExistException e) {
		}
		return result.toString();
	}

	//// Pourquoi on a converti les "en' en premier lieu?
	//// Avec de l'espoir on en aura plus besoin (meilleur performance)
	private void convertAttr(Element attributes, String att) {
		String nodeString = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><transfer " + att + "/>";

		try {
			/// Ensure we can parse it correctly
			DocumentBuilder documentBuilder;
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(nodeString));
			Document doc = documentBuilder.parse(is);

			/// Transfer attributes
			Element attribNode = doc.getDocumentElement();
			NamedNodeMap attribMap = attribNode.getAttributes();

			for (int i = 0; i < attribMap.getLength(); ++i) {
				org.w3c.dom.Node singleatt = attribMap.item(i);
				String name = singleatt.getNodeName();
				String value = singleatt.getNodeValue();
				attributes.setAttribute(name, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNodeBySemanticTag(MimeType outMimeType, String portfolioUuid, String semantictag, Long userId,
			Long groupId) throws DoesNotExistException, BusinessException {

		final List<Node> nodes = nodeDao.getNodesBySemanticTag(portfolioUuid, semantictag);

		try {
			// On récupère d'abord l'uuid du premier noeud trouve correspondant au
			// semantictag
			String nodeUuidStr = nodes.get(0).getId().toString();

			if (!hasRight(userId, groupId, nodeUuidStr, GroupRights.READ)) {
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
			}

			if (outMimeType.getSubtype().equals("xml")) {
				return getNodeXmlOutput(nodeUuidStr, true, null, userId, groupId, null, true).toString();
			} else if (outMimeType.getSubtype().equals("json")) {
				return "{" + getNodeJsonOutput(nodeUuidStr, true, null, userId, groupId, null, true) + "}";
			} else {
				return null;
			}
		} catch (IndexOutOfBoundsException e) {
			throw new DoesNotExistException(Node.class, semantictag);
		}
	}

	public String getNodesBySemanticTag(MimeType outMimeType, Long userId, Long groupId, String portfolioUuid,
			String semanticTag) throws BusinessException {
		List<Node> nodes = nodeDao.getNodesBySemanticTag(portfolioUuid, semanticTag);
		String result = "";
		if (outMimeType.getSubtype().equals("xml")) {
			result = "<nodes>";
			for (Node node : nodes) {
				String nodeUuid = node.getId().toString();
				if (!hasRight(userId, groupId, nodeUuid, GroupRights.READ)) {
					throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
				}

				result += "<node ";
				result += DomUtils.getXmlAttributeOutput("id", nodeUuid) + " ";
				result += ">";
				result += "</node>";
			}
			result += "</nodes>";
		} else if (outMimeType.getSubtype().equals("json")) {

			result = "{ \"nodes\": { \"node\": [";
			boolean firstPass = false;
			for (Node node : nodes) {
				if (firstPass)
					result += ",";
				result += "{ ";
				result += DomUtils.getJsonAttributeOutput("id", node.getId().toString()) + ", ";

				result += "} ";
				firstPass = true;
			}
			result += "] } }";
		}
		return result;
	}

	public boolean isCodeExist(String code, String uuid) {
		return nodeDao.isCodeExist(code, uuid);
	}

	public String getPortfolioIdFromNode(Long userId, String nodeUuid) throws DoesNotExistException, BusinessException {
		// Admin, or if user has a right to read can fetch this information
		if (!credentialDao.isAdmin(userId) && !hasRight(userId, 0L, nodeUuid, GroupRights.READ)) {
			throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
		}
		Node n = nodeDao.findById(UUID.fromString(nodeUuid));
		return n.getPortfolio().getId().toString();
	}

	public String executeMacroOnNode(long userId, String nodeUuid, String macroName) throws BusinessException {
		String val = "erreur";

		try {
			/// Selection du grid de l'utilisateur
			GroupRights gr = groupRightsDao.getPublicRightsByUserId(nodeUuid, userId);
			Long grid = null;
			String label = null;
			if (gr != null) {
				grid = gr.getGroupRightInfo().getId();
			}

			String meta = "";
			String tmp = nodeDao.getMetadataWad(nodeUuid);
			if (tmp != null)
				meta = tmp;

			/// FIXME: Check if user has indeed the right to

			// Parse it, for the amount of manipulation we do, it will be simpler than
			// find/replace
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			meta = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><metadata-wad " + meta + "></metadata-wad>";
			System.out.println("ACTION OUT: " + meta);
			InputSource is = new InputSource(new StringReader(meta));
			Document doc = documentBuilder.parse(is);
			Element rootMeta = doc.getDocumentElement();
			boolean doUpdate = true;

			NamedNodeMap metaAttr = rootMeta.getAttributes();
			long resetgroup = getRoleByNode(1L, nodeUuid, "resetter"); // Check for the possibility of resetter group
			if ("reset".equals(macroName)
					&& (securityManager.isAdmin(userId) || securityManager.userHasRole(userId, resetgroup))) // Admin,
																												// or
																												// part
																												// of
			// resetter group
			{
				/// if reset and admin
				// Call specific function to process current temporary table
				List<Node> children = nodeDao.getChildren(nodeUuid);
				resetRights(children);
			} else if ("show".equals(macroName) || "hide".equals(macroName)) {
				// Check if current group can show stuff
				org.w3c.dom.Node roleitem = metaAttr.getNamedItem("showroles");
				String roles = roleitem.getNodeValue();
				if (roles.contains(label)) // Can activate it
				{
					String showto = metaAttr.getNamedItem("showtoroles").getNodeValue();
					String vallist = "?";
					String[] valarray = showto.split(" ");
					for (int i = 0; i < valarray.length - 1; ++i) {
						vallist += ",?";
					}

					//// Il faut qu'il y a un showtorole
					if (!"".equals(showto)) {

						groupRightsDao.updateNodeRights(nodeUuid, Arrays.asList(valarray), macroName);

						org.w3c.dom.Node isPriv = metaAttr.getNamedItem("private");
						if (isPriv == null) {
							isPriv = doc.createAttribute("private");
							metaAttr.setNamedItem(isPriv);
						}
						// Update local string
						if ("hide".equals(macroName))
							isPriv.setNodeValue("Y");
						else if ("show".equals(macroName))
							isPriv.setNodeValue("N");
					}
				}

				// Update DB
				if (doUpdate) {
					meta = DomUtils.getNodeAttributesString(rootMeta);
					System.out.println("META: " + meta);
					Node n = nodeDao.findById(nodeUuid);
					n.setMetadataWad(meta);
					nodeDao.merge(n);
				}

			} else if ("submit".equals(macroName)) {
				List<Node> children = nodeDao.getChildren(nodeUuid);

				boolean updated = groupRightsDao.updateNodesRights(children, grid);
				/// Apply changes
				System.out.println("ACTION: " + macroName + " grid: " + grid + " -> uuid: " + nodeUuid);

				if (!updated)
					return "unchanged";

				/// FIXME: This part might be deprecated in the near future
				/// Vérifie le showtoroles
				org.w3c.dom.Node showtonode = metaAttr.getNamedItem("showtoroles");
				String showto = "";
				if (showtonode != null)
					showto = showtonode.getNodeValue();
				showto = showto.replace(" ", "','");
//				showto = "('" + showto +"')";

				//// Il faut qu'il y a un showtorole
				log.info("SHOWTO: " + showto);
				if (!"".equals(showto)) {
					log.info("SHOWING TO: " + showto);

					// Update rights
					groupRightsDao.updateNodeRights(nodeUuid, Arrays.asList(showto));
					metaAttr.removeNamedItem("private");
				}

				/// We then update the metadata notifying it was submitted
				rootMeta.setAttribute("submitted", "Y");
				/// Submitted date
				Date time = new Date();
				SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String timeFormat = dt.format(time);
				rootMeta.setAttribute("submitteddate", timeFormat);
				String updatedMeta = DomUtils.getNodeAttributesString(rootMeta);
				Node n = nodeDao.findById(nodeUuid);
				n.setMetadataWad(updatedMeta);
				nodeDao.merge(n);

			} else if ("submitall".equals(macroName)) {
				// Fill temp table 't_struc_nodeid' with node ids
				List<Node> children = nodeDao.getChildren(nodeUuid);

				/// Apply changes
				log.info("ACTION: " + macroName + " grid: " + grid + " -> uuid: " + nodeUuid);
				/// Insert/replace existing editing related rights
				/// Same as submit, except we don't limit to user's own group right
				boolean hasChanges = groupRightsDao.updateAllNodesRights(children, grid);

				if (!hasChanges)
					return "unchanged";

				/// We then update the metadata notifying it was submitted
				rootMeta.setAttribute("submitted", "Y");
				/// Submitted date
				Date time = new Date();
				SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String timeFormat = dt.format(time);
				rootMeta.setAttribute("submitteddate", timeFormat);
				String updatedMeta = DomUtils.getNodeAttributesString(rootMeta);
				Node n = nodeDao.findById(nodeUuid);
				n.setMetadataWad(meta);
				nodeDao.merge(n);
			} else if ("submitQuizz".equals(macroName)) {

				// Comparaison entre les réponses
				// node 1
				Node n1 = nodeDao.getParentNode(nodeUuid, "quizz");
				String uuidREP = n1.getId().toString();

				// node 2
				Node n2 = nodeDao.getParentNode(nodeUuid, "proxy-quizz");
				ResourceTable rt = resourceTableDao.getResourceByXsiType(n2.getId(), "Proxy");
				String ContentUuid2 = rt.getContent();
				String uuidSOL = ContentUuid2.substring(6, 42);

				String uuids = uuidREP + uuidSOL + nodeUuid;

				CloseableHttpClient client = HttpClientBuilder.create().build();
				String backend = configurationManager.get("backendserver");
				HttpGet method = new HttpGet(backend + "/compare/" + uuids);
				CloseableHttpResponse response = client.execute(method);
				String bodyAsString = EntityUtils.toString(response.getEntity());
				int prctElv = Integer.parseInt(bodyAsString);

				// Recherche noeud pourcentage mini
				String nodePrct = nodeDao.getNodeUuidBySemtag("level", nodeUuid); // Récupération noeud avec semantictag
																					// "mini"
				// parse le noeud
				String lbl = null;
				String ndSol = getNode(MimeTypeUtils.TEXT_XML, nodePrct, true, 1L, 0L, lbl, null);
				if (ndSol == null)
					return null;

				DocumentBuilderFactory documentBuilderFactory2 = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder2 = documentBuilderFactory2.newDocumentBuilder();
				ByteArrayInputStream is2 = new ByteArrayInputStream(ndSol.getBytes("UTF-8"));
				Document doc2 = documentBuilder2.parse(is2);

				DOMImplementationLS impl = (DOMImplementationLS) doc2.getImplementation().getFeature("LS", "3.0");
				LSSerializer serial = impl.createLSSerializer();
				serial.getDomConfig().setParameter("xml-declaration", true);

				// Récupération valeur seuil
				Element root = doc2.getDocumentElement();

				// root.getElementsByTagName("semantictag");
				org.w3c.dom.Node ndValeur = root.getFirstChild().getNextSibling().getNextSibling().getNextSibling()
						.getNextSibling().getNextSibling();
				org.w3c.dom.Node essai = ndValeur.getFirstChild().getNextSibling().getNextSibling();
				String seuil = ndValeur.getFirstChild().getNextSibling().getNextSibling().getTextContent().trim();
				int prctMini = Integer.parseInt(seuil);

				// Récupération asmContext contenant l'action
				final Node n3 = nodeDao.getParentNode(nodeUuid, "action");
				final String[] children = StringUtils.split(n3.getChildrenStr(), ",");
				final Set<String> childrenSet = new LinkedHashSet<String>(children.length);
				for (String child : children) {
					childrenSet.add(child);
				}

				final List<Node> contextActionNode = nodeDao.getNodes(new ArrayList<String>(childrenSet));
				String contextActionNodeUuid = contextActionNode.isEmpty() ? null
						: contextActionNode.get(0).getId().toString();

				// Récupération uuidNoeud sur lequel effectuer l'action, role et action
				String lbl2 = null;
				String nd = getNode(MimeTypeUtils.TEXT_XML, contextActionNodeUuid, true, 1L, 0L, lbl2, null);
				if (nd == null)
					return null;

				DocumentBuilderFactory documentBuilderFactory3 = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder3 = documentBuilderFactory3.newDocumentBuilder();
				ByteArrayInputStream is3 = new ByteArrayInputStream(nd.getBytes("UTF-8"));
				Document doc3 = documentBuilder3.parse(is3);

				DOMImplementationLS imple = (DOMImplementationLS) doc3.getImplementation().getFeature("LS", "3.0");
				LSSerializer seriale = imple.createLSSerializer();
				seriale.getDomConfig().setParameter("xml-declaration", true);

				Element racine = doc3.getDocumentElement();

				String action = null;
				String nodeAction = null;
				String role = null;

				NodeList valueList = racine.getElementsByTagName("value");
				nodeAction = valueList.item(0).getFirstChild().getNodeValue();

				NodeList actionList = racine.getElementsByTagName("action");
				action = actionList.item(0).getFirstChild().getNodeValue();

				NodeList roleList = racine.getElementsByTagName("role");
				role = roleList.item(0).getFirstChild().getNodeValue();

				userId = groupRightsDao.getUserIdFromNode(nodeAction);

				// comparaison
				if (prctElv >= prctMini) {
					executeAction(1L, nodeAction, action, role);

					Node n = nodeDao.findById(nodeAction);
					String metaA = "";
					if (n != null)
						metaA = n.getMetadataWad();

					// Parsage meta
					DocumentBuilderFactory documentBuilderFactorys = DocumentBuilderFactory.newInstance();
					DocumentBuilder documentBuilders = documentBuilderFactorys.newDocumentBuilder();
					metaA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><metadata-wad " + metaA + "></metadata-wad>";
					System.out.println("ACTION OUT: " + metaA);
					InputSource iss = new InputSource(new StringReader(metaA));
					Document docs = documentBuilders.parse(iss);
					Element rootMetaA = docs.getDocumentElement();
					boolean doUpdatee = true;

					NamedNodeMap metaAttrs = rootMetaA.getAttributes();
					org.w3c.dom.Node isPriv = metaAttrs.removeNamedItem("private");

					String updatedMeta = DomUtils.getNodeAttributesString(rootMetaA);
					n.setMetadataWad(updatedMeta);
					nodeDao.merge(n);

					executeMacroOnNode(userId, nodeUuid, "submit");
				} else {
					executeMacroOnNode(userId, uuidREP, "submit");
				}
			}
			val = "OK";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}

	public void resetRights(List<Node> children) throws ParserConfigurationException {
		Map<String, Map<String, GroupRights>> resolve = new HashMap<String, Map<String, GroupRights>>();

		DocumentBuilder documentBuilder;
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		for (Node child : children) {
			UUID uuid = child.getId();
			String meta = child.getMetadataWad();
			String nodeString = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><transfer " + meta + "/>";

			Map<String, GroupRights> rolesMap = resolve.get(uuid.toString());
			if (rolesMap == null) {
				rolesMap = new HashMap<String, GroupRights>();
				resolve.put(uuid.toString(), rolesMap);
			}

			try {
				/// parse meta
				InputSource is = new InputSource(new StringReader(nodeString));
				Document doc = documentBuilder.parse(is);

				/// Process attributes
				Element attribNode = doc.getDocumentElement();
				NamedNodeMap attribMap = attribNode.getAttributes();

				String nodeRole;
				org.w3c.dom.Node att = attribMap.getNamedItem("access");
				if (att != null) {
					// if(access.equalsIgnoreCase("public") || access.contains("public"))
					// credential.postGroupRight("all",uuid,Credential.READ,portfolioUuid,userId);
				}
				att = attribMap.getNamedItem("seenoderoles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setRead(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("showtoroles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setRead(false);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("delnoderoles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {

						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setDelete(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("editnoderoles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setWrite(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("submitroles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setSubmit(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("seeresroles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setRead(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("delresroles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setDelete(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("editresroles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setWrite(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				att = attribMap.getNamedItem("submitresroles");
				if (att != null) {
					StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						if (!rolesMap.containsKey(nodeRole)) {
							GroupRights gr = new GroupRights();
							gr.setSubmit(true);
							rolesMap.put(nodeRole, gr);
						}
					}
				}
				org.w3c.dom.Node actionroles = attribMap.getNamedItem("actionroles");
				if (actionroles != null) {
					/// Format pour l'instant: actionroles="sender:1,2;responsable:4"
					StringTokenizer tokens = new StringTokenizer(actionroles.getNodeValue(), ";");
					while (tokens.hasMoreElements()) {
						nodeRole = tokens.nextElement().toString();
						StringTokenizer data = new StringTokenizer(nodeRole, ":");
						String nrole = data.nextElement().toString();
						String actions = data.nextElement().toString().trim();
						if (!rolesMap.containsKey(nrole)) {
							GroupRights gr = new GroupRights();
							gr.setDelete(true);
							gr.setRulesId(actions);
							rolesMap.put(nrole, gr);
						}
					}
				}
				org.w3c.dom.Node menuroles = attribMap.getNamedItem("menuroles");
				if (menuroles != null) {
					/// Pour les differents items du menu
					StringTokenizer menuline = new StringTokenizer(menuroles.getNodeValue(), ";");

					while (menuline.hasMoreTokens()) {
						String line = menuline.nextToken();
						/// Format pour l'instant:
						/// code_portfolio,tag_semantique,label@en/libelle@fr,reles[;autre menu]
						String[] tokens = line.split(",");
						String menurolename = null;
						for (int t = 0; t < 4; ++t)
							menurolename = tokens[3];

						if (menurolename != null) {
							// Break down list of roles
							String[] roles = menurolename.split(" ");
							for (int i = 0; i < roles.length; ++i)
								rolesMap.put(roles[i].trim(), new GroupRights());
						}
					}
				}
				org.w3c.dom.Node notifyroles = attribMap.getNamedItem("notifyroles");
				if (notifyroles != null) {
					/// Format pour l'instant: notifyroles="sender responsable"
					StringTokenizer tokens = new StringTokenizer(notifyroles.getNodeValue(), " ");
					String merge = "";
					if (tokens.hasMoreElements())
						merge = tokens.nextElement().toString().trim();
					while (tokens.hasMoreElements())
						merge += "," + tokens.nextElement().toString().trim();

					for (GroupRights value : rolesMap.values()) {
						value.setNotifyRoles(merge);
					}

				}

				/// Now remove mention to being submitted
				attribNode.removeAttribute("submitted");
				attribNode.removeAttribute("submitteddate");
				String resetMeta = DomUtils.getNodeAttributesString(attribNode);
				Node n = nodeDao.findById(uuid);
				n.setMetadataWad(resetMeta);
				n = nodeDao.merge(n);

				/// Ajout des droits des noeuds FIXME
				GroupRightsId grId = new GroupRightsId();
				GroupRightInfo gri = null;

				Iterator<Entry<String, Map<String, GroupRights>>> rights = resolve.entrySet().iterator();

				while (rights.hasNext()) {
					Entry<String, Map<String, GroupRights>> entry = rights.next();
					uuid = UUID.fromString(entry.getKey());
					Map<String, GroupRights> gr = entry.getValue();

					Iterator<Entry<String, GroupRights>> rightiter = gr.entrySet().iterator();
					while (rightiter.hasNext()) {
						Entry<String, GroupRights> rightElem = rightiter.next();
						String group = rightElem.getKey();

						GroupRights gr2 = groupRightsDao.getRightsByIdAndLabel(uuid.toString(), group);
						if (gr2 != null)
							gri = gr2.getGroupRightInfo();

						GroupRights rightValue = rightElem.getValue();
						grId.setGroupRightInfo(gri);
						grId.setId(uuid);
						GroupRights toUpdate = groupRightsDao.findById(grId);

						toUpdate.setRead(rightValue.isRead());
						toUpdate.setWrite(rightValue.isWrite());
						toUpdate.setDelete(rightValue.isDelete());
						toUpdate.setSubmit(rightValue.isSubmit());
						toUpdate.setAdd(rightValue.isAdd());
						toUpdate.setTypesId(rightValue.getTypesId());
						toUpdate.setRulesId(rightValue.getRulesId());
						toUpdate.setNotifyRoles(rightValue.getNotifyRoles());
						groupRightsDao.merge(toUpdate);
					}
				}
			} catch (Exception e) {
			}
		}
	}

	public long getRoleByNode(Long userId, String nodeUuid, String role) throws BusinessException {
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("FORBIDDEN : No admin right");

		// Check if role exists already
		Long groupId = groupRightInfoDao.getIdByNodeAndLabel(nodeUuid, role);

		// If not, create it
		if (groupId == null) {
			Node n = nodeDao.findById(UUID.fromString(nodeUuid));
			Long grid = groupRightInfoDao.add(n.getPortfolio(), role);
			groupId = groupInfoDao.add(grid, 1L, role);
		}
		return groupId;
	}

	public String getNodeMetadataWad(MimeType mimeType, String nodeUuid, Long userId, Long groupId)
			throws DoesNotExistException, BusinessException {
		StringBuffer result = new StringBuffer();

		// Vérification de sécurité
		GroupRights rightsOnNode = getRights(userId, groupId, nodeUuid);
		if (!rightsOnNode.isRead()) {
			throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
		}

		Node node = nodeDao.findById(UUID.fromString(nodeUuid));

		if (!StringUtils.equals(node.getAsmType(), "asmResource")) {
			if (StringUtils.isNotEmpty(node.getMetadataWad()))
				result.append("<metadata-wad " + node.getMetadataWad() + "/>");
			else
				result.append("<metadata-wad/>");

		}

		return result.toString();
	}

	public Integer changeNode(MimeType inMimeType, String nodeUuid, String xmlNode, Long userId, Long groupId)
			throws Exception {
		String asmType = null;
		String xsiType = null;
		String semtag = null;
		String format = null;
		String label = null;
		String code = null;
		String descr = null;
		String metadata = "";
		String metadataWad = "";
		String metadataEpm = "";
		String nodeChildrenUuid = null;

		boolean sharedRes = false;
		boolean sharedNode = false;
		boolean sharedNodeRes = false;

		if (!hasRight(userId, groupId, nodeUuid, GroupRights.WRITE))
			throw new GenericBusinessException("403 Forbidden : no write credential ");

		String inPars = DomUtils.cleanXMLData(xmlNode);
		Document doc = DomUtils.xmlString2Document(inPars, new StringBuffer());
		// Puis on le recrée
		org.w3c.dom.Node node = doc.getDocumentElement();

		if (node == null)
			return null;

		try {
			if (node.getNodeName() != null)
				asmType = node.getNodeName();
		} catch (Exception ex) {
		}
		try {
			if (node.getAttributes().getNamedItem("xsi_type") != null)
				xsiType = node.getAttributes().getNamedItem("xsi_type").getNodeValue();
		} catch (Exception ex) {
		}
		try {
			if (node.getAttributes().getNamedItem("semtag") != null)
				semtag = node.getAttributes().getNamedItem("semtag").getNodeValue();
		} catch (Exception ex) {

		}
		try {
			if (node.getAttributes().getNamedItem("format") != null)
				format = node.getAttributes().getNamedItem("format").getNodeValue();
		} catch (Exception ex) {
		}

		// Si id défini, alors on écrit en base
		NodeList children = node.getChildNodes();
		// On parcourt une premiere fois les enfants pour récupérer la liste à écrire en
		// base
		int j = 0;
		for (int i = 0; i < children.getLength(); i++) {
			if (!children.item(i).getNodeName().equals("#text")) {
				// On vérifie si l'enfant n'est pas un element de type code, label ou descr.
				if (children.item(i).getNodeName().equals("label")) {
					label = DomUtils.getInnerXml(children.item(i));
				} else if (children.item(i).getNodeName().equals("code")) {
					code = DomUtils.getInnerXml(children.item(i));
				} else if (children.item(i).getNodeName().equals("description")) {
					descr = DomUtils.getInnerXml(children.item(i));
				} else if (children.item(i).getNodeName().equals("semanticTag")) {
					semtag = DomUtils.getInnerXml(children.item(i));
				} else if (children.item(i).getNodeName().equals("asmResource")) {
					// Si le noeud est de type asmResource, on stocke le innerXML du noeud
					resourceTableDao.updateResource(nodeUuid,
							children.item(i).getAttributes().getNamedItem("xsi_type").getNodeValue().toString(),
							DomUtils.getInnerXml(children.item(i)), userId);
				} else if (children.item(i).getNodeName().equals("metadata-wad")) {
					metadataWad = DomUtils.getNodeAttributesString(children.item(i));// " attr1=\"wad1\" attr2=\"wad2\"
																						// ";
				} else if (children.item(i).getNodeName().equals("metadata-epm")) {
					metadataEpm = DomUtils.getNodeAttributesString(children.item(i));
				} else if (children.item(i).getNodeName().equals("metadata")) {
					String tmpSharedRes = "";
					try {
						tmpSharedRes = children.item(i).getAttributes().getNamedItem("sharedRes").getNodeValue();
					} catch (Exception ex) {
					}

					String tmpSharedNode = "";
					try {
						tmpSharedNode = children.item(i).getAttributes().getNamedItem("sharedNode").getNodeValue();
					} catch (Exception ex) {
					}
					String tmpSharedNodeResource = "";
					try {
						tmpSharedNodeResource = children.item(i).getAttributes().getNamedItem("sharedNodeResource")
								.getNodeValue();
					} catch (Exception ex) {
					}

					if (tmpSharedRes.equalsIgnoreCase("y"))
						sharedRes = true;
					if (tmpSharedNode.equalsIgnoreCase("y"))
						sharedNode = true;
					if (tmpSharedNodeResource.equalsIgnoreCase("y"))
						sharedNodeRes = true;

					metadata = DomUtils.getNodeAttributesString(children.item(i));
				} else if (children.item(i).getAttributes() != null) {
					if (children.item(i).getAttributes().getNamedItem("id") != null) {
						if (nodeChildrenUuid == null)
							nodeChildrenUuid = "";
						if (j > 0)
							nodeChildrenUuid += ",";
						nodeChildrenUuid += children.item(i).getAttributes().getNamedItem("id").getNodeValue()
								.toString();
						nodeDao.updateNodeOrder(
								children.item(i).getAttributes().getNamedItem("id").getNodeValue().toString(), j);
						System.out.println("UPDATE NODE ORDER");
						j++;
					}
				}
			}
		}

		// Si le noeud est de type asmResource, on stocke le innerXML du noeud
		if (node.getNodeName().equals("asmResource")) {
			resourceManager.changeResourceByXsiType(nodeUuid, xsiType, DomUtils.getInnerXml(node), userId);
		}

		// TODO UpdateNode different selon creation de modèle ou instantiation copie
		if (nodeChildrenUuid != null)
			nodeDao.updateNode(nodeUuid);

		portfolioDao.updateTimeByNode(nodeUuid);

		int retval = nodeDao.update(nodeUuid, asmType, xsiType, semtag, label, code, descr, format, metadata,
				metadataWad, metadataEpm, sharedRes, sharedNode, sharedNodeRes, userId);

		return retval;
	}

	public String changeNodeMetadataWad(MimeType mimeType, String nodeUuid, String xmlMetawad, Long userId,
			Long groupId) throws Exception {
		String metadatawad = "";
		String result = null;

		if (!hasRight(userId, groupId, nodeUuid, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		// D'abord, on supprime les noeuds existants
		xmlMetawad = DomUtils.cleanXMLData(xmlMetawad);
		Document doc = DomUtils.xmlString2Document(xmlMetawad, new StringBuffer());
		// Puis on le recrée
		org.w3c.dom.Node node = doc.getDocumentElement();

		if (node.getNodeName().equals("metadata-wad")) {
			metadatawad = DomUtils.getNodeAttributesString(node);// " attr1=\"wad1\" attr2=\"wad2\" ";
		}

		Node n = nodeDao.findById(UUID.fromString(nodeUuid));
		n.setMetadataWad(metadatawad);
		nodeDao.merge(n);
		portfolioDao.updateTimeByNode(nodeUuid);
		result = "editer";
		return result;
	}

	public void removeNode(String nodeUuid, Long userId, long groupId) throws BusinessException {
		final GroupRights rights = getRights(userId, groupId, nodeUuid);

		if (!rights.isDelete())
			if (!credentialDao.isAdmin(userId) && !credentialDao.isDesigner(userId, nodeUuid))
				throw new GenericBusinessException("403 FORBIDDEN, No admin right");

		UUID parentid = null;

		/// Copy portfolio base info
		final Node baseNodeToRemove = nodeDao.findById(UUID.fromString(nodeUuid));

		/// Portfolio id, nécessaire pour plus tard !
		final UUID portfolioId = baseNodeToRemove.getPortfolio().getId();
		final List<Node> t_nodes = nodeDao.getNodesWithResources(portfolioId.toString());

		/// Trouver un parent pour réorganiser les enfants restants.
		if (baseNodeToRemove.getParentNode() != null)
			parentid = baseNodeToRemove.getParentNode().getId();

		final Set<Node> nodesToDelete = new LinkedHashSet<Node>();
		final Set<ResourceTable> resourcesToDelete = new LinkedHashSet<ResourceTable>();
		final Map<Integer, Set<String>> t_map_parentid = new HashMap<Integer, Set<String>>();

		Set<Node> t_set_parent = new LinkedHashSet<Node>();
		Set<String> t_set_parentid = new LinkedHashSet<String>();

		// Initialisation
		t_set_parentid.add(baseNodeToRemove.getId().toString());
		t_map_parentid.put(0, t_set_parentid);

		// On descend les noeuds
		int level = 0;
		boolean added = true;
		while (added) {
			t_set_parentid = new LinkedHashSet<String>();
			t_set_parent = new LinkedHashSet<Node>();

			for (Node t_node : t_nodes) {
				for (String t_parent_id : t_map_parentid.get(level)) {
					if (t_node.getParentNode() != null
							&& t_node.getParentNode().getId().toString().equals(t_parent_id)) {
						t_set_parentid.add(t_node.getId().toString());
						t_set_parent.add(t_node);
						break;
					}
				}
			}
			t_map_parentid.put(level + 1, t_set_parentid);
			nodesToDelete.addAll(t_set_parent);
			added = CollectionUtils.isNotEmpty(t_set_parentid); // On s'arrete quand rien n'a été ajouté
			level = level + 1; // Prochaine étape
		}

		// On ajoute le noeud de base dans les noeuds à effacer
		nodesToDelete.add(baseNodeToRemove);

		// On liste les ressources à effacer
		for (Node nodeToDelete : nodesToDelete) {
			if (nodeToDelete.getResource() != null) {
				resourcesToDelete.add(nodeToDelete.getResource());
			}

			if (nodeToDelete.getResResource() != null) {
				resourcesToDelete.add(nodeToDelete.getResResource());

			}
			if (nodeToDelete.getContextResource() != null) {
				resourcesToDelete.add(nodeToDelete.getContextResource());
			}
		}

		/// Met à jour la dernière date de modification, commencez par le faire puisque
		/// le noeud n'existera plus après.
		portfolioDao.updateTimeByNode(nodeUuid);

		// On efface les noeuds
		for (Node node : nodesToDelete) {
			nodeDao.remove(node);
		}

		// On efface les ressources
		for (ResourceTable resourceTable : resourcesToDelete) {
			resourceTableDao.remove(resourceTable);
		}

		if (parentid != null)
			nodeDao.updateNode(parentid.toString());

		System.out.println("deleteNode :" + nodeUuid);
	}

	public boolean changeParentNode(Long userid, String uuid, String uuidParent) throws BusinessException {
		if (!credentialDao.isAdmin(userid) && !credentialDao.isDesigner(userid, uuid))
			throw new GenericBusinessException("FORBIDDEN 403 : No admin right");

		// Pour qu'un noeud ne s'ajoute pas lui-même comme noeud parent
		if (uuid.equals(uuidParent))
			return false;

		boolean status = false;
		try {
			Node n = nodeDao.findById(UUID.fromString(uuid));

			UUID puuid = null;
			if (n != null && n.getParentNode() != null) {
				puuid = n.getParentNode().getId();
			}
			Integer next = nodeDao.getNodeNextOrderChildren(uuidParent);
			n.setParentNode(new Node(UUID.fromString(uuidParent)));
			n.setNodeOrder(next);
			nodeDao.merge(n);

			// Mettre à jour la liste d'enfants pour le noeud d'origine et le noeud de
			// destination.
			nodeDao.updateNode(puuid.toString());
			nodeDao.updateNode(uuidParent);
			portfolioDao.updateTimeByNode(uuid);
			status = true;
		} catch (Exception e) {

		}
		return status;
	}

	public Long moveNodeUp(String nodeUuid) throws DoesNotExistException {
		Long status = -1L;

		Node n = nodeDao.findById(UUID.fromString(nodeUuid));
		int order = -1;
		UUID puuid = null;
		if (n != null) {
			order = n.getNodeOrder();
			puuid = n.getParentNode().getId();
		}

		if (order == 0) {
			status = -2L;
		} else if (order > 0) {
			final List<Node> nodes = nodeDao.getNodesByOrder(puuid.toString(), order);
			/// Swap node order
			for (Node node : nodes) {
				if (node.getNodeOrder() == order) {
					node.setNodeOrder(order - 1);
				} else {
					node.setNodeOrder(order);
				}
				nodeDao.merge(node);
			}

			// Mettre à jour la liste des enfants
			nodeDao.updateNode(puuid.toString());

			status = 0L;
			portfolioDao.updateTimeByNode(nodeUuid);
		}

		return status;
	}

	public String changeNodeMetadataEpm(MimeType mimeType, String nodeUuid, String xmlMetadataEpm, Long userId,
			long groupId) throws Exception, BusinessException, DoesNotExistException {
		if (!hasRight(userId, groupId, nodeUuid, GroupRights.WRITE))
			throw new GenericBusinessException("FORBIDDEN 403 : No WRITE credential ");

		xmlMetadataEpm = DomUtils.cleanXMLData(xmlMetadataEpm);
		Document doc = DomUtils.xmlString2Document(xmlMetadataEpm, new StringBuffer());
		org.w3c.dom.Node node = doc.getDocumentElement();

		String metadataepm = "";
		if (node.getNodeName().equals("metadata-epm")) {
			metadataepm = DomUtils.getNodeAttributesString(node);
		}

		Node n = nodeDao.findById(UUID.fromString(nodeUuid));
		n.setMetadataEpm(metadataepm);
		nodeDao.merge(n);
		portfolioDao.updateTimeByNode(nodeUuid);
		return "editer";
	}

	public String changeNodeMetadata(MimeType mimeType, String nodeUuid, String xmlNode, Long userId, long groupId)
			throws DoesNotExistException, BusinessException, Exception {
		String metadata = "";

		boolean sharedRes = false;
		boolean sharedNode = false;
		boolean sharedNodeRes = false;

		if (!hasRight(userId, groupId, nodeUuid, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN, no WRITE credential");

		String status = "erreur";

		UUID portfolioUuid = portfolioDao.getPortfolioUuidFromNode(nodeUuid);

		// D'abord, on supprime les noeuds existants.
		xmlNode = DomUtils.cleanXMLData(xmlNode);
		Document doc = DomUtils.xmlString2Document(xmlNode, new StringBuffer());

		// Puis on le recrée
		org.w3c.dom.Node node = doc.getDocumentElement();

		if (node.getNodeName().equals("metadata")) {

			String tag = "";
			NamedNodeMap attr = node.getAttributes();

			/// Public has to be managed via the group/user function
			try {
				String publicatt = attr.getNamedItem("public").getNodeValue();
				if ("Y".equals(publicatt))
					groupManager.setPublicState(userId, portfolioUuid != null ? portfolioUuid.toString() : null, true);
				else if ("N".equals(publicatt))
					groupManager.setPublicState(userId, portfolioUuid != null ? portfolioUuid.toString() : null, false);
			} catch (Exception ex) {
			}

			try {
				tag = attr.getNamedItem("semantictag").getNodeValue();
			} catch (Exception ex) {
			}

			String tmpSharedRes = "";
			try {
				tmpSharedRes = attr.getNamedItem("sharedResource").getNodeValue();
				if (tmpSharedRes.equalsIgnoreCase("y"))
					sharedRes = true;
			} catch (Exception ex) {
			}

			String tmpSharedNode = "";
			try {
				tmpSharedNode = attr.getNamedItem("sharedNode").getNodeValue();
				if (tmpSharedNode.equalsIgnoreCase("y"))
					sharedNode = true;
			} catch (Exception ex) {
			}

			String tmpSharedNodeResource = "";
			try {
				tmpSharedNodeResource = attr.getNamedItem("sharedNodeResource").getNodeValue();
				if (tmpSharedNodeResource.equalsIgnoreCase("y"))
					sharedNodeRes = true;
			} catch (Exception ex) {
			}
			metadata = DomUtils.getNodeAttributesString(node);
			/// Mettre à jour les flags et données du champ
			Node n = nodeDao.findById(UUID.fromString(nodeUuid));
			n.setMetadata(metadata);
			n.setSemantictag(tag);
			n.setSharedRes(sharedRes);
			n.setSharedNode(sharedNode);
			n.setSharedNodeRes(sharedNodeRes);
			nodeDao.merge(n);
			status = "editer";
			portfolioDao.updateTime(portfolioUuid.toString());
		}
		return status;
	}

	public String changeNodeContext(MimeType mimeType, String nodeUuid, String xmlNode, Long userId, Long groupId)
			throws BusinessException, Exception {
		if (!hasRight(userId, groupId, nodeUuid, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		xmlNode = DomUtils.cleanXMLData(xmlNode);
		Document doc = DomUtils.xmlString2Document(xmlNode, new StringBuffer());
		// Puis on le recrée
		org.w3c.dom.Node node = doc.getDocumentElement();

		if (node.getNodeName().equals("asmResource")) {
			// Si le noeud est de type asmResource, on stocke le innerXML du noeud
			resourceManager.changeResourceByXsiType(nodeUuid, "context", DomUtils.getInnerXml(node), userId);
			return "editer";
		}
		return "erreur";
	}

	public String changeNodeResource(MimeType mimeType, String nodeUuid, String xmlNode, Long userId, Long groupId)
			throws BusinessException, Exception {
		if (!hasRight(userId, groupId, nodeUuid, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		xmlNode = DomUtils.cleanXMLData(xmlNode);
		Document doc = DomUtils.xmlString2Document(xmlNode, new StringBuffer());
		// Puis on le recrée
		org.w3c.dom.Node node;
		node = doc.getDocumentElement();

		if (node.getNodeName().equals("asmResource")) {
			// Si le noeud est de type asmResource, on stocke le innerXML du noeud
			resourceManager.changeResourceByXsiType(nodeUuid, "nodeRes", DomUtils.getInnerXml(node), userId);
			return "editer";
		}
		return "erreur";
	}

	public String addNode(MimeType inMimeType, String parentNodeUuid, String xmlNode, Long userId, Long groupId,
			boolean forcedUuid) throws Exception {

		Integer nodeOrder = nodeDao.getNodeNextOrderChildren(parentNodeUuid);
		Portfolio portfolio = portfolioDao.getPortfolioFromNode(parentNodeUuid);
		String portfolioUuid = null;
		String portfolioModelId = null;

		if (portfolio != null) {
			portfolioUuid = portfolio.getId().toString();
			portfolioModelId = portfolio.getModelId().toString();
		}

		// TODO getNodeRight postNode
		String inPars = DomUtils.cleanXMLData(xmlNode);
		Document doc = DomUtils.xmlString2Document(inPars, new StringBuffer());
		// Puis on le recrée
		org.w3c.dom.Node rootNode = doc.getDocumentElement();
		String nodeType = rootNode.getNodeName();

		String nodeUuid = writeNode(rootNode, portfolioUuid, portfolioModelId, userId, nodeOrder, null, parentNodeUuid,
				false, false, forcedUuid, null, true);

		String result = "<nodes>";
		result += "<" + nodeType + " ";
		result += DomUtils.getXmlAttributeOutput("id", nodeUuid) + " ";
		result += "/>";
		result += "</nodes>";

		portfolioDao.updateTimeByNode(parentNodeUuid);
		return result;
	}

	public String getNodeWithXSL(MimeType mimeType, String nodeUuid, String xslFile, String parameters, Long userId,
			Long groupId) throws DoesNotExistException, BusinessException, Exception {
		String result = null;
		/// Préparation des paramètres pour les besoins futurs, format:
		/// "par1:par1val;par2:par2val;..."
		String[] table = parameters.split(";");
		int parSize = table.length;
		String param[] = new String[parSize];
		String paramVal[] = new String[parSize];
		for (int i = 0; i < parSize; ++i) {
			String line = table[i];
			int var = line.indexOf(":");
			param[i] = line.substring(0, var);
			paramVal[i] = line.substring(var + 1);
		}

		String nodeInXml = getNode(mimeType, nodeUuid, true, userId, groupId, null, null).toString();
		if (nodeInXml != null) {
			result = DomUtils.processXSLTfile2String(DomUtils.xmlString2Document(nodeInXml, new StringBuffer()),
					xslFile, param, paramVal, new StringBuffer());
		}

		return result;
	}

	public String addNodeFromModelBySemanticTag(MimeType inMimeType, String nodeUuid, String semanticTag, Long userId,
			Long groupId) throws Exception {
		Portfolio portfolio = portfolioDao.getPortfolioFromNode(nodeUuid);

		String portfolioModelUuid = null;
		if (portfolio != null && portfolio.getModelId() != null) {
			portfolioModelUuid = portfolio.getModelId().toString();
		}
		String xml = getNodeBySemanticTag(inMimeType, portfolioModelUuid != null ? portfolioModelUuid.toString() : null,
				semanticTag, userId, groupId).toString();

		// C'est le noeud obtenu dans le modèle indiqué par la table de correspondance.
		UUID otherParentNodeUuid = nodeDao.getNodeUuidByPortfolioModelAndSemanticTag(portfolioModelUuid, semanticTag);

		return addNode(inMimeType, otherParentNodeUuid.toString(), xml, userId, groupId, true);
	}

	public void changeRights(String xmlNode, Long userId, Long subId, String label)
			throws BusinessException, Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document doc = documentBuilder.parse(new ByteArrayInputStream(xmlNode.getBytes("UTF-8")));

		XPath xPath = XPathFactory.newInstance().newXPath();
		List<String> portfolio = new ArrayList<String>();
		String xpathRole = "//*[local-name()='role']";
		XPathExpression findRole = xPath.compile(xpathRole);
		String xpathNodeFilter = "//*[local-name()='xpath']";
		XPathExpression findXpath = xPath.compile(xpathNodeFilter);
		String nodefilter = "";
		NodeList roles = null;

		/// Chercher le(s) portfolio(s)
		String portfolioNode = "//*[local-name()='portfoliogroup']";
		XPathExpression xpathFilter = null;
		org.w3c.dom.Node portgroupnode = (org.w3c.dom.Node) xPath.compile(portfolioNode).evaluate(doc,
				XPathConstants.NODE);
		if (portgroupnode != null) {
			org.w3c.dom.Node xpathNode = (org.w3c.dom.Node) findXpath.evaluate(portgroupnode, XPathConstants.NODE);
			nodefilter = xpathNode.getNodeValue();
			xpathFilter = xPath.compile(nodefilter);
			roles = (NodeList) findRole.evaluate(portgroupnode, XPathConstants.NODESET);
		} else {
			// Ou ajoutez l'unique portfolio.
			portfolioNode = "//*[local-name()='portfolio' and @*[local-name()='uuid']";
			org.w3c.dom.Node portnode = (org.w3c.dom.Node) xPath.compile(portfolioNode).evaluate(doc,
					XPathConstants.NODE);
			if (portnode != null) {
				portfolio.add(portnode.getNodeValue());

				org.w3c.dom.Node xpathNode = (org.w3c.dom.Node) findXpath.evaluate(portnode, XPathConstants.NODE);
				nodefilter = xpathNode.getNodeValue();
				xpathFilter = xPath.compile(nodefilter);
				roles = (NodeList) findRole.evaluate(portnode, XPathConstants.NODESET);
			}
		}

		List<String> nodesIds = new ArrayList<String>();
		for (int i = 0; i < portfolio.size(); ++i) // pour tous les portfolios.
		{
			String portfolioUuid = portfolio.get(i);
			String portfolioStr = portfolioManager
					.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioUuid, userId, 0L, label, null, null, subId, null)
					.toString();
			Document docPort = documentBuilder.parse(new ByteArrayInputStream(portfolioStr.getBytes("UTF-8")));

			// Récupérer des noeuds à l'intérieur de ces portfolios.
			NodeList portNodes = (NodeList) xpathFilter.evaluate(docPort, XPathConstants.NODESET);
			for (int j = 0; j < portNodes.getLength(); ++j) {
				org.w3c.dom.Node node = portNodes.item(j);
				String nodeuuid = node.getAttributes().getNamedItem("id").getNodeValue();

				nodesIds.add(nodeuuid); // Gardons ceux que nous devons changer de droits
			}
		}

		// Récupération d'un seul noeud
		if (nodesIds.isEmpty()) {
//				String singleNode = "//node";
			String singleNode = "//*[local-name()='node']";
			org.w3c.dom.Node sNode = (org.w3c.dom.Node) xPath.compile(singleNode).evaluate(doc, XPathConstants.NODE);
			String uuid = sNode.getAttributes().getNamedItem("uuid").getNodeValue();
			nodesIds.add(uuid);
			roles = (NodeList) findRole.evaluate(sNode, XPathConstants.NODESET);
		}

		/// Pour tous les rôles, nous devons changer
		for (int i = 0; i < roles.getLength(); ++i) {
			org.w3c.dom.Node rolenode = roles.item(i);
			String roleName = rolenode.getAttributes().getNamedItem("name").getNodeValue();
			org.w3c.dom.Node right = rolenode.getFirstChild();

			//
			if ("user".equals(roleName)) {
				/// username as role
			}

			if ("#text".equals(right.getNodeName()))
				right = right.getNextSibling();

			if ("right".equals(right.getNodeName())) // Modification des droits de noeud
			{
				NamedNodeMap rights = right.getAttributes();
				GroupRights rightsOnNode = new GroupRights();

				String val = rights.getNamedItem("RD").getNodeValue();
				if (val != null)
					rightsOnNode.setRead(Boolean.parseBoolean(val));
				val = rights.getNamedItem("WR").getNodeValue();
				if (val != null)
					rightsOnNode.setWrite(Boolean.parseBoolean(val));
				val = rights.getNamedItem("DL").getNodeValue();
				if (val != null)
					rightsOnNode.setDelete(Boolean.parseBoolean(val));
				val = rights.getNamedItem("SB").getNodeValue();
				if (val != null)
					rightsOnNode.setSubmit(Boolean.parseBoolean(val));

				/// Appliquer la modification pour tous les noeuds.
				for (int j = 0; j < nodesIds.size(); ++j) {
					changeRights(userId, nodesIds.get(j), roleName, rightsOnNode);
				}
			} else if ("action".equals(right.getNodeName())) // Using an action on node
			{
				/// Appliquer la modification pour tous les noeuds.
				for (int j = 0; j < nodesIds.size(); ++j) {

					// TODO: check for reset keyword

					// réinitialiser les droits.
					executeMacroOnNode(userId, nodesIds.get(j), "reset");
				}
			}
		}
	}

	/**
	 * change rights
	 */
	public String changeRights(Long userId, String nodeUuid, String role, GroupRights rights) throws BusinessException {
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		GroupRights gr = groupRightsDao.getRightsByIdAndLabel(nodeUuid, role);
		if (gr != null) {
			gr.setRead(rights.isRead());
			gr.setWrite(rights.isWrite());
			gr.setDelete(rights.isDelete());
			gr.setSubmit(rights.isSubmit());
			gr = groupRightsDao.merge(gr);
		}
		return "ok";
	}

	public String getNodes(MimeType mimeType, String rootNodeCode, String childSemtag, Long userId, Long groupId,
			String parentSemtag, String parentNodeCode, Integer cutoff) throws BusinessException {

		String pid = portfolioDao.getPortfolioUuidFromNodeCode(rootNodeCode);

		if (StringUtils.isEmpty(pid))
			throw new DoesNotExistException(Portfolio.class, "Not found with node code " + rootNodeCode);

		GroupRights rights = portfolioManager.getRightsOnPortfolio(userId, groupId, pid);
		if (!rights.isRead() && !credentialDao.isAdmin(userId) && !portfolioDao.isPublic(pid)
				&& !portfolioManager.isOwner(userId, pid))
			throw new GenericBusinessException("403 FORBIDDEN : no admin right");

		String result = "";

		try {
			// Not null, not empty
			// When we have a set, subset, and code of selected item
			/// Searching nodes subset where semtag is under semtag_parent. First filtering
			// is with parentNodeCode
			if (StringUtils.isNotEmpty(parentSemtag) && StringUtils.isNotEmpty(parentNodeCode)) {
				List<Node> nodes = nodeDao.getNodes(pid);

				/// Init temp set and hashmap
				final Map<Integer, Set<Node>> t_nodesByLevel = new HashMap<Integer, Set<Node>>();
				Set<Node> t_nodes = new LinkedHashSet<Node>();

				int found;
				/// Find parent tag
				for (Node node : nodes) {
					if (StringUtils.equals(node.getCode(), parentNodeCode)
							&& (found = StringUtils.indexOf(node.getSemantictag(), parentSemtag)) != -1) {
						t_nodes.add(node);
						break;
					}
				}
				t_nodesByLevel.put(0, t_nodes);

				try {
					int level = 0;
					boolean added = true;

					while (added && (cutoff == null ? true : level < cutoff)) {
						t_nodes = new HashSet<Node>();
						for (Node node : nodes) {
							for (Node t_node : t_nodesByLevel.get(level)) {
								if (node.getParentNode() != null
										&& t_node.getId().equals(node.getParentNode().getId())) {
									t_nodes.add(node);
									break;
								}
							}
						}
						t_nodesByLevel.put(level + 1, t_nodes);
						added = CollectionUtils.isNotEmpty(t_nodes); // On s'arrete quand rien n'a été ajouté.
						level = level + 1; // Prochaine étape
					}

					Set<Node> semtagSet = new HashSet<Node>();
					for (Set<Node> nodeSet : t_nodesByLevel.values()) {
						for (Node t_node : nodeSet) {
							if ((found = StringUtils.indexOf(t_node.getSemantictag(), childSemtag)) != -1) {
								semtagSet.add(t_node);
							}
						}
					}

					List<Node> nodeListBySemtag = new ArrayList<Node>(semtagSet);
					Collections.sort(nodeListBySemtag, new Comparator<Node>() {
						public int compare(Node o1, Node o2) {
							int result = StringUtils.compare(o1.getCode(), o2.getCode());
							if (result == 0) {
								result = NumberUtils.compare(o1.getNodeOrder(), o2.getNodeOrder());
							}
							return result;
						}
					});

					result += "<nodes>";
					for (Node nodeBySemtag : nodeListBySemtag) /// FIXME Could be done in a better way
					{
						result += "<node ";
						result += DomUtils.getXmlAttributeOutput("id", nodeBySemtag.getId().toString());
						result += ">";
						result += resourceManager.getResource(nodeBySemtag.getId());
						result += "</node>";
					}
					result += "</nodes>";
				} catch (Exception ex) {
					ex.printStackTrace();
					return null;
				}

				return result;
			} else {
				Portfolio portfolio = portfolioDao.getPortfolio(pid);
				if (portfolio != null) {
					List<Node> nodes = null;
					nodes = nodeDao.getNodesBySemanticTag(pid, childSemtag);
					result += "<nodes>";
					for (Node node : nodes) {
						result += "<node ";
						result += DomUtils.getXmlAttributeOutput("id", node.getId().toString());
						result += ">";
						result += resourceManager.getResource(node.getId());
						result += "</node>";
					}
					result += "</nodes>";
				}
			}
		} catch (Exception e) {

		}

		return result;
	}

	/******************************/
	/**
	 * Macro commandes et cie
	 *
	 * ## ## ### ##### ##### ##### ####### ## ## ## ## ## ## ## ## ## # ## ## ## ##
	 * ## ## ## ## ## ## ## ####### ## ###### ## ## ## ## ## ## ## ## ## ## ## ## ##
	 * ## ## ## ## ## ## ## ## ## ## ## ## ## ##### ## ## #####
	 **/
	/*****************************/
	public String executeAction(Long userId, String nodeUuid, String action, String role) {
		String val = "erreur";

		if ("showto".equals(action)) {

			if (credentialDao.isAdmin(userId)) // Can activate it
			{
				String[] showto = role.split(" ");

				//// Il faut qu'il y a un showtorole
				if (ArrayUtils.isNotEmpty(showto)) {
					// Update rights
					groupRightsDao.updateNodeRights(nodeUuid, Arrays.asList(showto));
				}
			}

			val = "OK";
		}

		return val;
	}

	private List<Pair<Node, GroupRights>> getNodePerLevel(String nodeUuid, Long userId, Long rrgId, Integer cutoff)
			throws DoesNotExistException, Exception {

		Node n = nodeDao.findById(UUID.fromString(nodeUuid));

		/// Portfolio id, nécessaire plus tard !
		String portfolioId = n.getPortfolio().getId().toString();
		List<Node> t_nodes = nodeDao.getNodes(portfolioId);

		/// Initialise la descente des noeuds, si il y a un partage on partira de le
		/// sinon du noeud par défaut
		/// FIXME: There will be something with shared_node_uuid

		Map<Integer, Set<String>> t_map_parentid = new HashMap<Integer, Set<String>>();
		Set<String> t_set_parentid = new LinkedHashSet<String>();

		t_set_parentid.add(n.getId().toString());
		t_map_parentid.put(0, t_set_parentid);

		/// On boucle, avec les shared_node si ils existent.
		/// FIXME: Possibilité de boucle infini
		int level = 0;
		boolean added = true;

		Set<String> t_struc_parentid_2 = null;
		while (added && (cutoff == null ? true : level < cutoff)) {
			t_struc_parentid_2 = new HashSet<String>();
			for (Node t_node : t_nodes) {
				for (String t_parent_node : t_map_parentid.get(level)) {
					if (t_node.getParentNode() != null
							&& t_node.getParentNode().getId().toString().equals(t_parent_node)) {
						t_struc_parentid_2.add(t_node.getId().toString());
						break;
					}
				}
			}
			t_map_parentid.put(level + 1, t_struc_parentid_2);
			t_set_parentid.addAll(t_struc_parentid_2);
			added = CollectionUtils.isNotEmpty(t_struc_parentid_2); // On s'arrete quand rien n'a été ajouté
			level = level + 1; // Prochaine étape
		}

		Map<String, GroupRights> t_rights_22 = new HashMap<String, GroupRights>();
		GroupRights gr = null;
		if (credentialDao.isDesigner(userId, nodeUuid) || credentialDao.isAdmin(userId)) {
			for (String t_node_parent : t_set_parentid) {
				gr = new GroupRights();
				gr.setId(new GroupRightsId(null, UUID.fromString(t_node_parent)));
				gr.setRead(true);
				gr.setWrite(true);
				gr.setDelete(true);
				gr.setSubmit(false);
				gr.setAdd(false);
				t_rights_22.put(t_node_parent, gr);
			}
		} else {
			if (nodeDao.isPublic(nodeUuid)) {
				for (String t_node_parent : t_set_parentid) {
					gr = new GroupRights();
					gr.setId(new GroupRightsId(null, UUID.fromString(t_node_parent)));
					gr.setRead(true);
					gr.setWrite(false);
					gr.setDelete(true);
					gr.setSubmit(false);
					gr.setAdd(false);
					t_rights_22.put(t_node_parent, gr);
				}
			}

			// Aggregation des droits avec 'all', l'appartenance du groupe de l'utilisateur,
			// et les droits propres a l'utilisateur
			GroupRightInfo gri1 = groupRightInfoDao.getByPortfolioAndLabel(portfolioId, "all");
			GroupUser gu = null;
			Long grid3 = 0L;
			gu = groupUserDao.getUniqueByUser(userId);
			for (Node t_node : t_nodes) {
				if (t_node.getId().toString().equals(nodeUuid)
						&& t_node.getPortfolio().equals(gu.getGroupInfo().getGroupRightInfo().getPortfolio())) {
					grid3 = gu.getGroupInfo().getGroupRightInfo().getId();
				}
			}

			List<GroupRights> grList = groupRightsDao.getByPortfolioAndGridList(portfolioId, gri1.getId(), rrgId,
					grid3);
			for (String ts : t_set_parentid) {
				for (GroupRights item : grList) {
					if (item.getGroupRightsId().toString().equals(ts)) {
						if (t_rights_22.containsKey(item.getGroupRightsId().toString())) {
							GroupRights original = t_rights_22.get(item.getGroupRightsId().toString());
							original.setRead(Boolean.logicalOr(item.isRead(), original.isRead()));
							original.setWrite(Boolean.logicalOr(item.isWrite(), original.isWrite()));
							original.setDelete(Boolean.logicalOr(item.isDelete(), original.isDelete()));
							original.setSubmit(Boolean.logicalOr(item.isSubmit(), original.isSubmit()));
							original.setAdd(Boolean.logicalOr(item.isAdd(), original.isAdd()));
						} else {
							t_rights_22.put(item.getGroupRightsId().toString(), item);
						}
					}
				}
			}
		}

		List<Node> nodes = nodeDao.getNodes(new ArrayList<>(t_set_parentid));
		List<Pair<Node, GroupRights>> finalResults = new ArrayList<Pair<Node, GroupRights>>();

		// Sélectionne les données selon la filtration
		for (Node node : nodes) {
			if (t_rights_22.containsKey(node.getId().toString())) { // Verification des droits
				GroupRights rights = t_rights_22.get(node.getId().toString());
				if (rights.isRead()) { // On doit au moins avoir le droit de lecture
					finalResults.add(Pair.of(node, rights));
				}
			}
		}

		return finalResults;

	}

	/**
	 * Même chose que postImportNode, mais on ne prend pas en compte le parsage des
	 * droits
	 * 
	 * @param mimeType
	 * @param parentId
	 * @param semtag
	 * @param code
	 * @param srcuuid
	 * @param id
	 * @return
	 */
	public String copyNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid, Long userId,
			Long groupId) throws Exception {
		if (StringUtils.isEmpty(tag) || StringUtils.isEmpty(code)) {
			if (StringUtils.isEmpty(srcuuid)) {
				throw new IllegalArgumentException(
						"copyNode() a reçu des paramètres non valides (complétez le paramètre 'srcuuid' ou les paramètres 'tag' et 'code').");
			}
		}

		String createdUuid = "erreur";

		try {
			String portfolioUUID = null;

			if (srcuuid != null) {
				// Vérifier si l'utilisateur a le droit en lecture
				if (!hasRight(userId, groupId, srcuuid, GroupRights.READ)) {
					throw new GenericBusinessException("403 FORBIDDEN : no READ credential");
				}
			} else {
				/// Check/update cache
				portfolioUUID = checkCache(code);

				if (portfolioUUID == null) {
					throw new GenericBusinessException("Aucun noeud trouvé pour le code : " + code);
				}
			}

			// Pour la copie de la structure
			String baseUuid = "";

			List<Node> t_nodes = null;

			// On évite la recherche de balises puisque nous connaissons l'uuid du noeud à
			// copier.
			if (srcuuid != null) {
				// Puisque nous ne savons pas si ces noeuds doivent être mis en cache, on
				// recherche les informations dans la base.
				UUID portfolioUuid = nodeDao.getPortfolioIdFromNode(srcuuid);

				// Récupération des noeuds du portfolio à copier depuis la base.
				t_nodes = nodeDao.getNodes(portfolioUuid.toString());

				baseUuid = srcuuid;
			} else {
				/// // Récupération des noeuds du portfolio à copier depuis le cache.
				t_nodes = cachedNodes.get(portfolioUUID);

				/// Vérifier si on peut trouver un noeud avec le tag envoyé
				Node nodeByTag = null;
				Node nodeBySemanticTag = null;
				for (Node t_node : t_nodes) {
					if (StringUtils.equalsIgnoreCase(t_node.getCode(), tag)) {
						nodeByTag = t_node;
						break; // on arrête lorsqu'on a trouvé le premier
					}
					// Si rien, continuer avec semantictag
					if (StringUtils.equalsIgnoreCase(t_node.getSemantictag(), tag) && nodeBySemanticTag == null) {
						nodeBySemanticTag = t_node; // Prendre le premier trouvé
					}
				}

				if (nodeByTag != null) {
					baseUuid = nodeByTag.getId().toString();
				} else {
					if (nodeBySemanticTag != null) {
						baseUuid = nodeBySemanticTag.getId().toString();
					} else {
						throw new GenericBusinessException(
								"Aucun noeud trouvé pour le code : " + code + " et le tag : " + tag);
					}
				}
			}

			final Node destNode = nodeDao.findById(UUID.fromString(destUuid));

			/// Contient les noeuds à copier.
			final Set<Node> nodesToCopy = new LinkedHashSet<Node>();
			/// Contient les uuid des noeuds à copier.
			final Set<String> nodesUuidToCopy = new LinkedHashSet<String>();

			final Map<Integer, Set<String>> t_map_parentid = new HashMap<Integer, Set<String>>();

			Set<String> t_set_parentid_2 = new LinkedHashSet<String>();
			Set<Node> t_set_parent_2 = null;

			for (Node t_node : t_nodes) {
				if (t_node.getId().toString().equals(baseUuid)) {
					t_node.setParentNode(destNode);
					nodesUuidToCopy.add(t_node.getId().toString());
					nodesToCopy.add(t_node);
					break;
				}
			}

			/// Initialisation du dictionnaire.
			t_map_parentid.put(0, nodesUuidToCopy);

			int level = 0;
			boolean added = true;

			/// les tours de boucle sont toujours <= au nombre de noeud du portfolio.
			while (added) {
				t_set_parentid_2 = new LinkedHashSet<String>();
				t_set_parent_2 = new LinkedHashSet<Node>();

				for (Node t_node : t_nodes) {
					for (String t_parent_id : t_map_parentid.get(level)) {
						if (t_node.getParentNode() != null
								&& t_node.getParentNode().getId().toString().equals(t_parent_id)) {
							t_set_parentid_2.add(t_node.getId().toString());
							t_set_parent_2.add(t_node);
							break;
						}
					}
				}
				t_map_parentid.put(level + 1, t_set_parentid_2);
				nodesUuidToCopy.addAll(t_set_parentid_2);
				nodesToCopy.addAll(t_set_parent_2);
				added = CollectionUtils.isNotEmpty(t_set_parentid_2); // On s'arrete quand rien n'a été ajouté
				level = level + 1; // Prochaine étape
			}

			//////////////////////////////////////////
			/// Copie des noeuds et des ressources ///
			/////////////////////////////////////////

			// Contient les noeuds d'origine et les copies.
			final Map<Node, Node> nodes = new HashMap<Node, Node>();
			// Contient les ressources d'origine et les copies.
			final Map<ResourceTable, ResourceTable> resources = new HashMap<ResourceTable, ResourceTable>();

			Node nodeCopy = null;
			ResourceTable resourceCopy = null;
			for (Node t_node : nodesToCopy) {
				/// Copie du noeud.
				nodeCopy = new Node(t_node);
				nodeCopy.setModifUserId(userId);

				//////////////////////////////
				/// Copie des ressources/////
				/////////////////////////////
				if (t_node.getResource() != null) {
					resourceCopy = nodeCopy.getResource();
					resourceCopy.setModifUserId(userId);
					if (!t_node.isSharedRes() || !t_node.getSharedNode() || !t_node.isSharedNodeRes()) {
						resourceTableDao.persist(resourceCopy);
						resources.put(t_node.getResource(), resourceCopy);
					}
				}

				if (t_node.getResResource() != null) {
					resourceCopy = nodeCopy.getResResource();
					resourceCopy.setModifUserId(userId);
					if (!t_node.isSharedRes() || !t_node.getSharedNode() || !t_node.isSharedNodeRes()) {
						resourceTableDao.persist(resourceCopy);
						resources.put(t_node.getResource(), resourceCopy);
					}
				}
				if (t_node.getContextResource() != null) {
					resourceCopy = nodeCopy.getContextResource();
					resourceCopy.setModifUserId(userId);
					if (!t_node.isSharedRes() || !t_node.getSharedNode() || !t_node.isSharedNodeRes()) {
						resourceTableDao.persist(resourceCopy);
						resources.put(t_node.getResource(), resourceCopy);
					}
				}

				nodeDao.persist(nodeCopy);
				nodes.put(t_node, nodeCopy);
			}

			final Node searchedNode = new Node();
			// Récupère les groupes de destination via le noeud de destination
			final List<GroupRightInfo> destGroups = groupRightInfoDao.getByNode(destUuid);
			final Portfolio destPortfolio = new Portfolio(destNode.getPortfolio().getId());

			Entry<Node, Node> tmp_entry = null;
			Node tmp_original_node = null;
			Node tmp_copied_node = null;
			GroupRights tmp_groupRights = null;

			// Contient la liste de droits des noeuds d'origine
			List<GroupRights> tmp_rights_list = null;

			for (Iterator<Entry<Node, Node>> it = nodes.entrySet().iterator(); it.hasNext();) {
				tmp_entry = it.next();
				tmp_original_node = tmp_entry.getKey();
				tmp_copied_node = tmp_entry.getValue();
				// Assigner le nouveau parent
				if (tmp_original_node.getParentNode() != null) {
					tmp_copied_node.setParentNode(nodes.get(tmp_original_node.getParentNode()));
				}
				if (tmp_original_node.getChildrenStr() != null) {
					// Mise à jour de la liste des enfants
					// L'ordre détermine le rendu visuel final du XML
					String[] children = StringUtils.split(tmp_original_node.getChildrenStr(), ",");
					String[] childrenCopies = new String[children.length];
					for (int i = 0; i < children.length; i++) {
						searchedNode.setId(UUID.fromString(children[i]));
						nodeCopy = nodes.get(searchedNode);
						childrenCopies[i] = nodeCopy.getId().toString();
					}
					tmp_copied_node.setChildrenStr(StringUtils.join(childrenCopies, ","));
				}
				/// Lier le noeud copié au portfolio de destination
				tmp_copied_node.setPortfolio(destPortfolio);
				nodeDao.merge(tmp_copied_node);

				//////////////////////////////////
				/// Copie des droits du noeud ///
				/////////////////////////////////

				// Récupère la liste des droits du noeud d'origine pour l'appliquer à la copie
				// sur les groupes de destination.
				tmp_rights_list = groupRightsDao.getRightsById(tmp_original_node.getId());
				for (GroupRights rights : tmp_rights_list) {
					for (GroupRightInfo destGroup : destGroups) {
						if (destGroup.getLabel().equals(rights.getGroupRightInfo().getLabel())) {
							tmp_groupRights = new GroupRights(rights);
							tmp_groupRights.setGroupRightInfo(destGroup);
							tmp_groupRights.setGroupRightsId(tmp_copied_node.getId());
							groupRightsDao.persist(tmp_groupRights);
							break;
						}
					}
				}
			}

			// Mise à jour de l'ordre et du noeud parent de la copie
			searchedNode.setId(UUID.fromString(baseUuid));
			nodeCopy = nodes.get(searchedNode);
			nodeCopy.setParentNode(destNode);
			int nodeOrder = nodeDao.getFirstLevelChildren(destUuid).size();
			nodeCopy.setNodeOrder(nodeOrder);
			nodeDao.merge(nodeCopy);

			/// Ajout de l'enfant dans le noeud de destination
			destNode.setChildrenStr((destNode.getChildrenStr() != null ? destNode.getChildrenStr() + "," : "")
					+ nodeCopy.getId().toString());
			nodeDao.merge(destNode);

			Entry<ResourceTable, ResourceTable> tmp_res_entry = null;
			ResourceTable tmp_original_resource = null;
			ResourceTable tmp_copied_resource = null;
			/// Ajout des droits des resources
			// Apparemment inutile si l'on s'en occupe qu'au niveau du contexte...
			for (Iterator<Entry<ResourceTable, ResourceTable>> it = resources.entrySet().iterator(); it.hasNext();) {
				tmp_res_entry = it.next();
				tmp_original_resource = tmp_res_entry.getKey();
				tmp_copied_resource = tmp_res_entry.getValue();
				tmp_rights_list = groupRightsDao.getRightsByIdAndGroup(tmp_original_resource.getId().toString(),
						groupId);
				for (GroupRights tmp_rights : tmp_rights_list) {
					tmp_groupRights = new GroupRights(tmp_rights);
					tmp_groupRights.setGroupRightInfo(tmp_rights.getGroupRightInfo());
					tmp_groupRights.setGroupRightsId(tmp_copied_resource.getId());
					groupRightsDao.persist(tmp_groupRights);
				}
			}

			// On récupère le uuid crée
			searchedNode.setId(UUID.fromString(baseUuid));
			createdUuid = nodes.get(searchedNode).getId().toString();
		} catch (Exception e) {
			createdUuid = "erreur: " + e.getMessage();
		}

		return createdUuid;
	}

	private String checkCache(String code) throws Exception {

		String portfolioUuid = null;
		boolean setCache = false;
		Portfolio portfolio = portfolioDao.getPortfolioFromNodeCode(code);

		// Le portfolio n'a pas été trouvé, pas besoin d'aller plus loin
		if (portfolio != null) {
			portfolioUuid = portfolio.getId().toString();
			// Vérifier si nous n'avons pas déjà le portfolio en cache
			if (cachedNodes.get(portfolioUuid) != null) {
				final List<Node> nodes = cachedNodes.get(portfolioUuid);
				log.info("Portfolio présent dans le cache pour le code : " + code + " -> " + portfolioUuid);

				// Vérifier si le cache est toujours à jour.
				if (CollectionUtils.isEmpty(nodes) || portfolio.getModifDate() == null
						|| !portfolio.getModifDate().equals(nodes.get(0).getModifDate())) {
					// le cache est obsolète
					log.info("Cache obsolète pour : " + code);
					cachedNodes.remove(portfolioUuid);
					log.info("Supprimé du cache pour : " + code + " -> " + portfolioUuid);
					setCache = true;
				}
			} else {
				setCache = true;
			}

			if (setCache) // Le portfolio n'est pas/plus présent dans le cache, chargez-le
			{
				log.info("Entrée manquante dans le cache pour le code: " + code);

				// Assignez la date du portfolio pour les noeuds en cache .. Utile pour
				// vérifier la validité du cache.
				final List<Node> nodes = nodeDao.getNodes(portfolioUuid);
				for (Node node : nodes) {
					node.setModifDate(portfolio.getModifDate());
				}
				// Mettre tous les noeuds dans le cache.
				cachedNodes.put(portfolioUuid, nodes);
			}
		}
		return portfolioUuid;
	}

	public String importNode(MimeType inMimeType, String destUuid, String tag, String code, String srcuuid, Long userId,
			long groupId) throws BusinessException, Exception {

		if (StringUtils.isEmpty(tag) || StringUtils.isEmpty(code)) {
			if (StringUtils.isEmpty(srcuuid)) {
				throw new IllegalArgumentException(
						"importNode() a reçu des paramètres non valides (complétez le paramètre 'srcuuid' ou les paramètres 'tag' et 'code').");
			}
		}

		String createdUuid = "erreur";

		try {
			String portfolioUUID = null;

			if (srcuuid != null) {
				// Vérifie si l'utilisateur a le droit d'y accéder.
				if (!hasRight(userId, groupId, srcuuid, GroupRights.READ)) {
					throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
				}
			} else {
				/// Vérification / mise à jour du cache
				portfolioUUID = checkCache(code);

				if (portfolioUUID == null) {
					throw new GenericBusinessException("Aucun noeud trouvé pour le code : " + code);
				}
			}

			// Pour la copie de la structure
			String baseUuid = "";

			List<Node> t_nodes = null;

			// On évite la recherche de balises puisque nous connaissons l'uuid du noeud à
			// copier.
			if (srcuuid != null) {
				// Puisque nous ne savons pas si ces noeuds doivent être mis en cache, on
				// recherche les informations dans la base.
				UUID portfolioUuid = nodeDao.getPortfolioIdFromNode(srcuuid);

				// Récupération des noeuds du portfolio à copier depuis la base.
				t_nodes = nodeDao.getNodes(portfolioUuid.toString());

				baseUuid = srcuuid;
			} else {
				/// Récupération des noeuds du portfolio à copier depuis le cache.
				t_nodes = cachedNodes.get(portfolioUUID);

				/// Vérifier si on peut trouver un noeud avec le tag envoyé
				Node nodeByTag = null;
				Node nodeBySemanticTag = null;
				for (Node t_node : t_nodes) {
					if (StringUtils.equalsIgnoreCase(t_node.getCode(), tag)) {
						nodeByTag = t_node;
						break; // on arrête lorsqu'on a trouvé le premier
					}
					// Si rien, continuer avec semantictag
					if (StringUtils.equalsIgnoreCase(t_node.getSemantictag(), tag) && nodeBySemanticTag == null) {
						nodeBySemanticTag = t_node; // Prendre le premier trouvé
					}
				}

				if (nodeByTag != null) {
					baseUuid = nodeByTag.getId().toString();
				} else {
					if (nodeBySemanticTag != null) {
						baseUuid = nodeBySemanticTag.getId().toString();
					} else {
						throw new GenericBusinessException(
								"Aucun noeud trouvé pour le code : " + code + " et le tag : " + tag);
					}
				}
			}

			final Node destNode = nodeDao.findById(UUID.fromString(destUuid));

			/// Contient les noeuds à copier.
			final Set<Node> nodesToCopy = new LinkedHashSet<Node>();
			/// Contient les uuid des noeuds à copier.
			final Set<String> nodesUuidToCopy = new LinkedHashSet<String>();

			final Map<Integer, Set<String>> t_map_parentid = new HashMap<Integer, Set<String>>();

			Set<String> t_set_parentid_2 = new LinkedHashSet<String>();
			Set<Node> t_set_parent_2 = null;

			for (Node t_node : t_nodes) {
				if (t_node.getId().toString().equals(baseUuid)) {
					t_node.setParentNode(destNode);
					nodesUuidToCopy.add(t_node.getId().toString());
					nodesToCopy.add(t_node);
					break;
				}
			}

			/// Initialisation du dictionnaire.
			t_map_parentid.put(0, nodesUuidToCopy);

			int level = 0;
			boolean added = true;

			/// les tours de boucle sont toujours <= au nombre de noeud du portfolio.
			while (added) {
				t_set_parentid_2 = new LinkedHashSet<String>();
				t_set_parent_2 = new LinkedHashSet<Node>();

				for (Node t_node : t_nodes) {
					for (String t_parent_id : t_map_parentid.get(level)) {
						if (t_node.getParentNode() != null
								&& t_node.getParentNode().getId().toString().equals(t_parent_id)) {
							t_set_parentid_2.add(t_node.getId().toString());
							t_set_parent_2.add(t_node);
							break;
						}
					}
				}
				t_map_parentid.put(level + 1, t_set_parentid_2);
				nodesUuidToCopy.addAll(t_set_parentid_2);
				nodesToCopy.addAll(t_set_parent_2);
				added = CollectionUtils.isNotEmpty(t_set_parentid_2); // On s'arrete quand rien n'a été ajouté
				level = level + 1; // Prochaine étape
			}

			//////////////////////////////////////////
			/// Copie des noeuds et des ressources ///
			/////////////////////////////////////////
			// Contient les noeuds d'origine et les copies.
			final Map<Node, Node> nodes = new HashMap<Node, Node>();
			// Contient les ressources d'origine et les copies.
			final Map<ResourceTable, ResourceTable> resources = new HashMap<ResourceTable, ResourceTable>();

			Node nodeCopy = null;
			ResourceTable resourceCopy = null;
			for (Node t_node : nodesToCopy) {
				/// Copie du noeud.
				nodeCopy = new Node(t_node);
				nodeCopy.setModifUserId(userId);

				//////////////////////////////
				/// Copie des ressources/////
				/////////////////////////////
				if (t_node.getResource() != null) {
					resourceCopy = nodeCopy.getResource();
					resourceCopy.setModifUserId(userId);
					if (!t_node.isSharedRes() || !t_node.getSharedNode() || !t_node.isSharedNodeRes()) {
						resourceTableDao.persist(resourceCopy);
						resources.put(t_node.getResource(), resourceCopy);
					}
				}

				if (t_node.getResResource() != null) {
					resourceCopy = nodeCopy.getResResource();
					resourceCopy.setModifUserId(userId);
					if (!t_node.isSharedRes() || !t_node.getSharedNode() || !t_node.isSharedNodeRes()) {
						resourceTableDao.persist(resourceCopy);
						resources.put(t_node.getResource(), resourceCopy);
					}
				}
				if (t_node.getContextResource() != null) {
					resourceCopy = nodeCopy.getContextResource();
					resourceCopy.setModifUserId(userId);
					if (!t_node.isSharedRes() || !t_node.getSharedNode() || !t_node.isSharedNodeRes()) {
						resourceTableDao.persist(resourceCopy);
						resources.put(t_node.getResource(), resourceCopy);
					}
				}

				nodeDao.persist(nodeCopy);
				nodes.put(t_node, nodeCopy);
			}

			final Node searchedNode = new Node();
			final Portfolio destPortfolio = new Portfolio(destNode.getPortfolio().getId());

			Entry<Node, Node> tmp_entry = null;
			Node tmp_original_node = null;
			Node tmp_copied_node = null;

			for (Iterator<Entry<Node, Node>> it = nodes.entrySet().iterator(); it.hasNext();) {
				tmp_entry = it.next();
				tmp_original_node = tmp_entry.getKey();
				tmp_copied_node = tmp_entry.getValue();
				/// Assigner le nouveau parent
				if (tmp_original_node.getParentNode() != null) {
					tmp_copied_node.setParentNode(nodes.get(tmp_original_node.getParentNode()));
				}
				// Mise à jour de la liste des enfants
				// L'ordre détermine le rendu visuel final du XML
				if (tmp_original_node.getChildrenStr() != null) {
					String[] children = StringUtils.split(tmp_original_node.getChildrenStr(), ",");
					String[] childrenCopies = new String[children.length];
					for (int i = 0; i < children.length; i++) {
						searchedNode.setId(UUID.fromString(children[i]));
						nodeCopy = nodes.get(searchedNode);
						childrenCopies[i] = nodeCopy.getId().toString();
					}
					tmp_copied_node.setChildrenStr(StringUtils.join(childrenCopies, ","));
				}
				// Lier le noeud copié au portfolio de destination
				tmp_copied_node.setPortfolio(destPortfolio);
				nodeDao.merge(tmp_copied_node);

			}

			// Mise à jour de l'ordre et du noeud parent de la copie
			searchedNode.setId(UUID.fromString(baseUuid));
			nodeCopy = nodes.get(searchedNode);
			nodeCopy.setParentNode(destNode);
			int nodeOrder = nodeDao.getFirstLevelChildren(destUuid).size();
			nodeCopy.setNodeOrder(nodeOrder);
			nodeDao.merge(nodeCopy);

			/// Ajout de l'enfant dans le noeud de destination
			destNode.setChildrenStr((destNode.getChildrenStr() != null ? destNode.getChildrenStr() + "," : "")
					+ nodeCopy.getId().toString());
			nodeDao.merge(destNode);

			//////////////////////////////////
			/// Copie des droits des noeuds ///
			/////////////////////////////////
			// Login
			final String login = credentialDao.getLoginById(userId);

			/// Copier les rôles actuel @Override pour faciliter le référencement.
			final UUID tmpPortfolioUuid = nodeDao.getPortfolioIdFromNode(destUuid);

			// Récupération des rôles dans la BDD.
			final List<GroupRightInfo> griList = groupRightInfoDao.getByPortfolioID(tmpPortfolioUuid);

			//// Set temporaire roles
			final Set<GroupRightInfo> t_set_groupRightInfo = new HashSet<GroupRightInfo>(griList);
			final Map<GroupRightsId, GroupRights> t_group_rights = new HashMap<GroupRightsId, GroupRights>();

			GroupRights t_gr = null;
			GroupRightsId t_grId = null;
			Entry<Node, Node> entry = null;
			Node original = null, copy = null;
			GroupRightInfo t_groupRightInfo = null;
			boolean found = false;
			final boolean hasGroup = !griList.isEmpty();

			/// Gestion des droits
			if (hasGroup) {
				String onlyuser = "(?<![-=+])(user)(?![-=+])";
				Pattern pattern = Pattern.compile(onlyuser);

				for (Iterator<Entry<Node, Node>> it = nodes.entrySet().iterator(); it.hasNext();) {
					found = false;
					t_grId = new GroupRightsId();

					entry = it.next();
					original = entry.getKey();
					copy = entry.getValue();

					UUID uuid = copy.getId();
					String portfolioUuid = copy.getPortfolio().getId().toString();
					// Process et remplacement de 'user' par la personne en cours
					String meta = original.getMetadataWad();

					Matcher matcher = pattern.matcher(meta);
					if (matcher.find()) {
						meta = meta.replaceAll(onlyuser, login);

						// Remplacer les métadonnées par le nom d'utilisateur actuel
						copy.setMetadataWad(meta);
						nodeDao.merge(copy);

						// S'assurer qu'un groupe d'utilisateurs spécifique existe en base et y ajouter
						// l'utilisateur.
						long ngid = getRoleByNode(1L, destUuid, login);
						securityManager.addUserToGroup(1L, userId, ngid);

						/// Ensure entry is there in temp table, just need a skeleton info
						t_groupRightInfo = new GroupRightInfo();
						t_groupRightInfo.setId(ngid);
						t_groupRightInfo.setLabel(login);
						t_groupRightInfo.setOwner(1L);
						t_set_groupRightInfo.add(t_groupRightInfo);
					}

					String nodeString = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><transfer " + meta
							+ "></transfer>";
					try {
						// S'assurer que nous pouvons le parser correctement
						DocumentBuilder documentBuilder;
						DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
						documentBuilder = documentBuilderFactory.newDocumentBuilder();
						InputSource is = new InputSource(new StringReader(nodeString));
						Document doc = documentBuilder.parse(is); // Transformer en un autre fichier.

						// Process attributes
						Element attribNode = doc.getDocumentElement();
						NamedNodeMap attribMap = attribNode.getAttributes();

						// FIXME: à améliorer pour faciliter le changement des droits
						String nodeRole;
						org.w3c.dom.Node att = attribMap.getNamedItem("access");
						att = attribMap.getNamedItem("seenoderoles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
									if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
										t_grId.setGroupRightInfo(tmp_gri);
										t_grId.setId(uuid);
										if (t_group_rights.containsKey(t_grId)) {
											t_gr = t_group_rights.get(t_grId);
											t_gr.setRead(true);
										} else {
											t_gr = new GroupRights();
											t_gr.setId(t_grId);
											t_gr.setRead(true);
											t_group_rights.put(t_grId, t_gr);
										}
										found = true;
										break;
									}
								}
								if (!found) {
									log.error("Role '" + nodeRole
											+ "' might not exist in destination portfolio. (seenoderoles)");
								}
							}
						}
						att = attribMap.getNamedItem("showtoroles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
									if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
										t_grId.setGroupRightInfo(tmp_gri);
										t_grId.setId(uuid);
										if (t_group_rights.containsKey(t_grId)) {
											t_gr = t_group_rights.get(t_grId);
											t_gr.setRead(false);
										} else {
											t_gr = new GroupRights();
											t_gr.setId(t_grId);
											t_gr.setRead(false);
											t_group_rights.put(t_grId, t_gr);
										}
										found = true;
										break;
									}
								}
								if (!found) {
									log.error("Role '" + nodeRole
											+ "' might not exist in destination portfolio. (showtoroles)");
								}
							}
						}
						att = attribMap.getNamedItem("delnoderoles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								for (GroupRightInfo t_gri : t_set_groupRightInfo) {
									if (StringUtils.equalsIgnoreCase(t_gri.getLabel(), nodeRole)) {
										t_grId.setGroupRightInfo(t_gri);
										t_grId.setId(uuid);
										if (t_group_rights.containsKey(t_grId)) {
											t_gr = t_group_rights.get(t_grId);
											t_gr.setDelete(true);
										} else {
											t_gr = new GroupRights();
											t_gr.setId(t_grId);
											t_gr.setDelete(true);
											t_gr.setRead(false);
											t_group_rights.put(t_grId, t_gr);
										}
										found = true;
										break;
									}
								}
								if (!found) {
									log.error("Role '" + nodeRole
											+ "' might not exist in destination portfolio. (delroles)");
								}
							}
						}
						att = attribMap.getNamedItem("editnoderoles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
									if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
										t_grId.setGroupRightInfo(tmp_gri);
										t_grId.setId(uuid);
										if (t_group_rights.containsKey(t_grId)) {
											t_gr = t_group_rights.get(t_grId);
											t_gr.setWrite(true);
										} else {
											t_gr = new GroupRights();
											t_gr.setId(t_grId);
											t_gr.setWrite(true);
											t_gr.setRead(false);
											t_group_rights.put(t_grId, t_gr);
										}
										found = true;
										break;
									}
								}
								if (!found) {
									log.error("Role '" + nodeRole
											+ "' might not exist in destination portfolio. (editnoderoles)");
								}
							}
						}
						att = attribMap.getNamedItem("submitroles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
									if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
										t_grId.setGroupRightInfo(tmp_gri);
										t_grId.setId(uuid);
										if (t_group_rights.containsKey(t_grId)) {
											t_gr = t_group_rights.get(t_grId);
											t_gr.setSubmit(true);
										} else {
											t_gr = new GroupRights();
											t_gr.setId(t_grId);
											t_gr.setSubmit(true);
											t_gr.setRead(false);
											t_group_rights.put(t_grId, t_gr);
										}
										found = true;
										break;
									}
								}
								if (!found) {
									log.error("Role '" + nodeRole
											+ "' might not exist in destination portfolio. (submitroles)");
								}
							}
						}

						att = attribMap.getNamedItem("seeresroles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								groupManager.addGroupRights(nodeRole, uuid.toString(), GroupRights.READ, portfolioUuid,
										userId);
							}
						}
						att = attribMap.getNamedItem("delresroles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								groupManager.addGroupRights(nodeRole, uuid.toString(), GroupRights.DELETE,
										portfolioUuid, userId);
							}
						}
						att = attribMap.getNamedItem("editresroles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
									if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
										t_grId.setGroupRightInfo(tmp_gri);
										t_grId.setId(uuid);
										if (t_group_rights.containsKey(t_grId)) {
											t_gr = t_group_rights.get(t_grId);
											t_gr.setWrite(true);
										} else {
											t_gr = new GroupRights();
											t_gr.setId(t_grId);
											t_gr.setWrite(true);
											t_gr.setRead(false);
											t_group_rights.put(t_grId, t_gr);
										}
										found = true;
										break;
									}
								}
								if (!found) {
									log.error("Role '" + nodeRole
											+ "' might not exist in destination portfolio. (editresroles)");
								}
							}
						}
						att = attribMap.getNamedItem("submitresroles");
						if (att != null) {
							StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								groupManager.addGroupRights(nodeRole, uuid.toString(), GroupRights.SUBMIT,
										portfolioUuid, userId);
							}
						}
						// */
						/// FIXME: Incomplete
						/// FIXME: Incomplete
						org.w3c.dom.Node menuroles = attribMap.getNamedItem("menuroles");
						if (menuroles != null) {
							/// Pour les différents items du menu
							StringTokenizer menuline = new StringTokenizer(menuroles.getNodeValue(), ";");

							while (menuline.hasMoreTokens()) {
								String line = menuline.nextToken();
								/// Format pour l'instant:
								/// code_portfolio,tag_semantique,label@en/libelle@fr,reles[;autre menu]
								String[] tokens = line.split(",");
								String menurolename = null;
								for (int t = 0; t < 4; ++t)
									menurolename = tokens[3];

								if (menurolename != null) {
									// Break down list of roles
									String[] roles = menurolename.split(" ");
									for (int i = 0; i < roles.length; ++i) {
										// Ensure roles exists
										securityManager.addRole(portfolioUUID, roles[i], 1L);
									}
								}
							}
						}
						org.w3c.dom.Node actionroles = attribMap.getNamedItem("actionroles");
						if (actionroles != null) {
							/// Format pour l'instant: actionroles="sender:1,2;responsable:4"
							StringTokenizer tokens = new StringTokenizer(actionroles.getNodeValue(), ";");
							while (tokens.hasMoreElements()) {
								nodeRole = tokens.nextElement().toString();
								StringTokenizer data = new StringTokenizer(nodeRole, ":");
								String role = data.nextElement().toString();
								String actions = data.nextElement().toString();
								groupManager.addGroupRights(role, uuid.toString(), actions, portfolioUuid, userId);
							}
						}

						org.w3c.dom.Node notifyroles = attribMap.getNamedItem("notifyroles");
						if (notifyroles != null) {
							/// Format pour l'instant: actionroles="sender:1,2;responsable:4"
							StringTokenizer tokens = new StringTokenizer(notifyroles.getNodeValue(), " ");
							String notifyRoles = "";
							if (tokens.hasMoreElements())
								notifyRoles = tokens.nextElement().toString();
							while (tokens.hasMoreElements())
								notifyRoles += "," + tokens.nextElement().toString();
							groupManager.changeNotifyRoles(userId, portfolioUuid, uuid.toString(), notifyRoles);
						}

					} catch (Exception e) {
						log.error("Erreur lors du traitement sur les droits : " + e.getMessage());
						e.printStackTrace();
					}
				}
				/// Ajout des droits des noeuds
				for (GroupRights gr : t_group_rights.values()) {
					groupRightsDao.persist(gr);
				}
			} /// Fin de la gestion des droits

			// On récupère le UUID crée
			searchedNode.setId(UUID.fromString(baseUuid));
			createdUuid = nodes.get(searchedNode).getId().toString();
		} catch (Exception e) {
			createdUuid = "erreur: " + e.getMessage();
		}
		return createdUuid;

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferAnnotationTable(Connection con, Map<String, String> nodesIds) throws SQLException {
		Annotation annotation = null;
		ResultSet res = annotationDao.findAll("annotation", con);
		while (res.next()) {
			annotation = new Annotation();
			annotation.setId(new AnnotationId(nodesIds.get(res.getString("nodeid")), res.getInt("rank")));
			annotation.setText(res.getString("text"));
			annotation.setCDate(res.getDate("c_date"));
			annotation.setAUser(res.getString("a_user"));
			annotation.setWadIdentifier(res.getString("wad_identifier"));
			annotationDao.merge(annotation);
		}
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void removeAnnotations() {
		annotationDao.removeAll();
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void removeNodes() {
		nodeDao.removeAll();

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public Map<String, String> transferNodeTable(Connection con, Map<String, String> rtIds, Map<String, String> portIds,
			Map<Long, Long> userIds) throws SQLException {
		ResultSet res = nodeDao.getMysqlNodes(con);
		Map<String, String> nodeIds = new HashMap<>();
		Node node = null;
		while (res.next()) {
			try {
				node = new Node();
				node.setChildrenStr(res.getString("node_children_uuid"));
				node.setNodeOrder(res.getInt("node_order"));
				if (StringUtils.isNotEmpty(res.getString("node_parent_uuid"))) {
					node.setParentNode(new Node(UUID.fromString(res.getString("node_parent_uuid"))));
				}
				node.setMetadata(res.getString("metadata"));
				node.setMetadataWad(res.getString("metadata_wad"));
				node.setMetadataWad(res.getString("metadata_epm"));
				if (StringUtils.isNotEmpty(res.getString("res_node_uuid")))
					node.setResource(resourceTableDao.findById(rtIds.get(res.getString("res_node_uuid"))));
				if (StringUtils.isNotEmpty(res.getString("res_res_node_uuid")))
					node.setResResource(resourceTableDao.findById(rtIds.get(res.getString("res_res_node_uuid"))));
				if (StringUtils.isNotEmpty(res.getString("res_context_node_uuid")))
					node.setContextResource(
							resourceTableDao.findById(rtIds.get(res.getString("res_context_node_uuid"))));
				node.setSharedRes(res.getBoolean("shared_res"));
				node.setSharedNode(res.getBoolean("shared_node"));
				node.setAsmType(res.getString("asm_type"));
				node.setXsiType(res.getString("xsi_type"));
				node.setSemtag(res.getString("semtag"));
				node.setLabel(res.getString("label"));
				node.setCode(res.getString("code"));
				node.setDescr(res.getString("descr"));
				node.setFormat(res.getString("format"));
				node.setModifUserId(userIds.get(res.getLong("modif_user_id")));
				node.setModifDate(res.getDate("modif_date"));
				if (StringUtils.isNotEmpty(res.getString("portfolio_id"))) {
					node.setPortfolio(portfolioDao.findById(portIds.get(res.getString("portfolio_id"))));
				}
				node = nodeDao.merge(node);
				nodeIds.put(res.getString("node_uuid"), node.getId().toString());
			} catch (DoesNotExistException e) {
				System.err.println("NodeManagerImpl.transferNodeTable()");
			}
		}
		return nodeIds;
	}

}
