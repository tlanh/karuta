package eportfolium.com.karuta.business.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.activation.MimeType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import eportfolium.com.karuta.business.contract.GroupManager;

//import com.portfolio.data.provider.MysqlDataProvider.t_tree;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioDao;
import eportfolium.com.karuta.consumer.contract.dao.ResourceTableDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.util.PhpUtil;

/**
 * @author mlengagne
 *
 */
@Service
public class NodeManagerImpl implements NodeManager {

	@Autowired
	private GroupManager groupManager;
	
	@Autowired
	private PortfolioDao portfolioDao;

	@Autowired
	private NodeDao nodeDao;

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private ResourceTableDao resourceTableDao;

	@Autowired
	private GroupRightsDao groupRightsDao;

	public String getNode(MimeType outMimeType, String nodeUuid, boolean withChildren, Long userId, Long groupId,
			String label, Integer cutoff) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * public String getNode(MimeType outMimeType, String nodeUuid, boolean
	 * withChildren, Long userId, Long groupId, String label, Integer cutoff) throws
	 * Exception { StringBuffer nodeXML = new StringBuffer();
	 * 
	 * long t_start = System.currentTimeMillis();
	 * 
	 * GroupRights rightsOnNode = getRights(userId, groupId, nodeUuid);
	 * 
	 * long t_nodeRight = System.currentTimeMillis();
	 * 
	 * if (!rightsOnNode.isRead()) { /// Verifie les droits avec le compte publique
	 * (derniere chance) userId = credentialDao.getPublicUid();
	 * 
	 * if (!nodeDao.isPublic(nodeUuid, null)) return nodeXML.toString(); }
	 * 
	 * if (outMimeType.getSubType().equals("xml")) { ResultSet result =
	 * getNodePerLevel(nodeUuid, userId, nodeRight.rrgId, cutoff); if (result ==
	 * null) // Node doesn't exist return null;
	 * 
	 * long t_nodePerLevel = System.currentTimeMillis();
	 * 
	 * /// Preparation du XML que l'on va renvoyer DocumentBuilderFactory
	 * documentBuilderFactory = DocumentBuilderFactory.newInstance();
	 * DocumentBuilder documentBuilder = null; Document document = null;
	 * documentBuilder = documentBuilderFactory.newDocumentBuilder(); document =
	 * documentBuilder.newDocument(); document.setXmlStandalone(true);
	 * 
	 * HashMap<String, Object[]> resolve = new HashMap<String, Object[]>(); /// Node
	 * -> parent HashMap<String, t_tree> entries = new HashMap<String, t_tree>();
	 * 
	 * long t_initContruction = System.currentTimeMillis();
	 * 
	 * processQuery(result, resolve, entries, document, documentBuilder,
	 * rightsOnNode.getGroupRightInfo().getLabel()); result.close();
	 * 
	 * long t_processQuery = System.currentTimeMillis();
	 * 
	 * /// Reconstruct functional tree t_tree root = entries.get(nodeUuid);
	 * StringBuilder out = new StringBuilder(256); reconstructTree(out, root,
	 * entries);
	 * 
	 * nodeXML.append(out.toString()); long t_buildXML = System.currentTimeMillis();
	 * 
	 * long t_convertString = System.currentTimeMillis();
	 * 
	 * long d_right = t_nodeRight - t_start; long d_queryNodes = t_nodePerLevel -
	 * t_nodeRight; long d_initConstruct = t_initContruction - t_nodePerLevel; long
	 * d_processQuery = t_processQuery - t_initContruction; long d_buildXML =
	 * t_buildXML - t_processQuery; long d_convertString = t_convertString -
	 * t_buildXML;
	 * 
	 * System.out.println("Query Rights: "+d_right);
	 * System.out.println("Query Nodes: "+d_queryNodes);
	 * System.out.println("Init build: "+d_initConstruct);
	 * System.out.println("Parse Query: "+d_processQuery);
	 * System.out.println("Build XML: "+d_buildXML);
	 * System.out.println("Convert XML: "+d_convertString); //
	 * 
	 * 
	 * return nodeXML.toString(); } else if
	 * (outMimeType.getSubType().equals("json")) return "{" +
	 * getNodeJsonOutput(nodeUuid, withChildren, null, userId, groupId, label, true)
	 * + "}"; else return null; }
	 */

	public String getNodesParent(String portfoliocode, String semtag, String semtag_parent, String code_parent)
			throws Exception {
		String result = "";
		try {
			Portfolio portfolio = portfolioDao.getPortfolioByPortfolioCode(portfoliocode);
			Node parentNode = nodeDao.getParentNode(portfolio.getId(), semtag_parent, portfoliocode);
			if (parentNode != null) {
				final List<Node> children = new ArrayList<Node>(parentNode.getChildren());
				for (Node child : children) {
					Node tmp = nodeDao.getNodeBySemanticTag(child.getId(), semtag);
					result += "<nodes>";
					if (tmp != null) {
						result += "<node ";
						result += DomUtils.getXmlAttributeOutput("id", tmp.getId().toString());
						result += ">";
						result += "</node>";
					}

					result += "</nodes>";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/** Récupère le noeud, et assemble les ressources, s'il y en a */
	public String getResource(String nodeUuid) {

		String result = "";
		Node resNode = nodeDao.getNode(nodeUuid);
		ResourceTable resResource = null;

		if (resNode != null) {
			String m_epm = resNode.getMetadataEpm();
			if (m_epm == null)
				m_epm = "";
			result += "<" + resNode.getAsmType() + " id='" + resNode.getId().toString() + "'>";
			result += "<metadata " + resNode.getMetadata() + "/>";
			result += "<metadata-epm " + m_epm + "/>";
			result += "<metadata-wad " + resNode.getMetadataWad() + "/>";

			resResource = resNode.getResource();
			if (resResource != null && resResource.getId() != null) {
				result += "<asmResource id='" + resResource.getId().toString() + "' contextid='"
						+ resNode.getId().toString() + "' xsi_type='" + resResource.getXsiType() + "'>";
				result += resResource.getContent();
				result += "</asmResource>";
			}
		}
		resResource = resNode.getResResource();
		if (resResource != null && resResource.getId() != null) {
			result += "<asmResource id='" + resResource.getId().toString() + "' contextid='"
					+ resNode.getId().toString() + "' xsi_type='" + resResource.getXsiType() + "'>";
			result += resResource.getContent();
			result += "</asmResource>";

		}

		resResource = resNode.getContextResource();
		if (resResource != null && resResource.getId() != null) {
			result += "<asmResource id='" + resResource.getId().toString() + "' contextid='"
					+ resNode.getId().toString() + "' xsi_type='" + resResource.getXsiType() + "'>";
			result += resResource.getContent();
			result += "</asmResource>";

		}

		result += "</" + resNode.getAsmType() + ">";

		return result;
	}

	public String writeNode(org.w3c.dom.Node node, String portfolioUuid, UUID portfolioModelId, Long userId, int ordrer,
			String forcedUuid, String forcedUuidParent, boolean sharedResParent, boolean sharedNodeResParent, boolean rewriteId,
			HashMap<String, String> resolve, boolean parseRights) throws BusinessException {

		String uuid = "";
		String originUuid = null;
		String parentUuid = null;
		String modelNodeUuid = null;
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

		String access = null;

		int returnValue = 0;

		if (node == null)
			return null;

		if (node.getNodeName().equals("portfolio")) {
			// On n'attribue pas d'uuid sur la balise portfolio
		} else {
		}

		String currentid = "";
		org.w3c.dom.Node idAtt = node.getAttributes().getNamedItem("id");
		if (idAtt != null) {
			String tempId = idAtt.getNodeValue();
			if (tempId.length() > 0)
				currentid = tempId;
		}

		// Si uuid force, alors on ne tient pas compte de l'uuid indique dans le xml
		if (rewriteId) // On garde les uuid par defaut
		{
			uuid = currentid;
		} else if (forcedUuid != null && !"".equals(forcedUuid)) {
			uuid = forcedUuid;
		} else
			uuid = UUID.randomUUID().toString();

		// Last state if nothing worked
//		if( uuid == null || "".equals(uuid) )
		// Force uuid rewrite

		if (resolve != null) // Mapping old id -> new id
			resolve.put(currentid, uuid);

		if (forcedUuidParent != null) {
			// Dans le cas d'un uuid parent force => POST => on genere un UUID
			parentUuid = forcedUuidParent;
		}

		/// Recuperation d'autre infos
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

		// Si id defini, alors on ecrit en base
		// TODO Transactionnel noeud+enfant
		NodeList children = null;
		try {
			children = node.getChildNodes();
			// On parcourt une premiere fois les enfants pour recuperer la liste e ecrire en
			// base
			for (int i = 0; i < children.getLength(); i++) {
				org.w3c.dom.Node child = children.item(i);

				if ("#text".equals(child.getNodeName()))
					continue;

				if (children.item(i).getNodeName().equals("metadata-wad")) {
					metadataWad = DomUtils.getNodeAttributesString(children.item(i));

					if (parseRights) {
						// Gestion de la securite integree
						//
						org.w3c.dom.Node metadataWadNode = children.item(i);
						try {
							if (metadataWadNode.getAttributes().getNamedItem("access") != null) {
								// if(access.equalsIgnoreCase("public") || access.contains("public"))
								// credential.postGroupRight("all",uuid,Credential.READ,portfolioUuid,userId);
							}
						} catch (Exception ex) {
						}

						try {
							if (metadataWadNode.getAttributes().getNamedItem("seenoderoles") != null) {
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("seenoderoles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {

									nodeRole = tokens.nextElement().toString();
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.READ, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.DELETE, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.WRITE, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.SUBMIT, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.READ, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.DELETE, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.WRITE, portfolioUuid,
											userId);
								}
							}
						} catch (Exception ex) {
						}

						try {
							if (metadataWadNode.getAttributes().getNamedItem("submitresroles") != null) // TODO
																										// submitresroles
																										// deprecated
																										// fro
																										// submitroles
							{
								StringTokenizer tokens = new StringTokenizer(
										metadataWadNode.getAttributes().getNamedItem("submitresroles").getNodeValue(),
										" ");
								while (tokens.hasMoreElements()) {
									nodeRole = tokens.nextElement().toString();
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.SUBMIT, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.SUBMIT, portfolioUuid,
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
									groupRightsDao.postGroupRight(nodeRole, uuid, GroupRights.NONE, portfolioUuid,
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
									groupRightsDao.postGroupRight(role, uuid, actions, portfolioUuid, userId);
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
								String merge = "";
								if (tokens.hasMoreElements())
									merge = tokens.nextElement().toString();
								while (tokens.hasMoreElements())
									merge += "," + tokens.nextElement().toString();

								groupManager.postNotifyRoles(userId, portfolioUuid, uuid, merge);
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
							setPublicState(userId, portfolioUuid, true);
						else if ("N".equals(publicatt))
							setPublicState(userId, portfolioUuid, false);
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
				// On verifie si l'enfant n'est pas un element de type code, label ou descr
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

		// Si on est au debut de l'arbre, on stocke la definition du portfolio
		// dans la table portfolio
		if (uuid != null && node.getParentNode() != null) {
			// On retrouve le code cache dans les ressources. blegh
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
				throw new Exception("Il manque la balise asmRoot !!");
		}

		// Si on instancie un portfolio e partir d'un modele
		// Alors on gere les share*
		if (portfolioModelId != null) {
			if (sharedNode) {
				sharedNodeUuid = originUuid;
			}
		} else
			modelNodeUuid = null;

		if (uuid != null && !node.getNodeName().equals("portfolio") && !node.getNodeName().equals("asmResource"))
			returnValue = nodeDao.create(uuid, parentUuid, "", asmType, xsiType, sharedRes, sharedNode, sharedNodeRes,
					sharedResUuid, sharedNodeUuid, sharedNodeResUuid, metadata, metadataWad, metadataEpm, semtag,
					semanticTag, label, code, descr, format, ordrer, userId, portfolioUuid);

		// Si le parent a ete force, cela veut dire qu'il faut mettre e jour les enfants
		// du parent
		// TODO
		// MODIF : On le met e jour tout le temps car dans le cas d'un POST les uuid ne
		// sont pas connus e l'avance
		// if(forcedUuidParent!=null)

		// Si le noeud est de type asmResource, on stocke le innerXML du noeud
		if (node.getNodeName().equals("asmResource")) {
			if (portfolioModelId != null) {
				if (xsiType.equals("nodeRes") && sharedNodeResParent) {
					sharedNodeResUuid = originUuid;
					insertMysqlResource(sharedNodeResUuid, parentUuid, xsiType, DomUtils.getInnerXml(node),
							portfolioModelId, sharedNodeResParent, sharedResParent, userId);
				} else if (!xsiType.equals("context") && !xsiType.equals("nodeRes") && sharedResParent) {

					sharedResUuid = originUuid;
					insertMysqlResource(sharedResUuid, parentUuid, xsiType, DomUtils.getInnerXml(node),
							portfolioModelId, sharedNodeResParent, sharedResParent, userId);
				} else {
					insertMysqlResource(uuid, parentUuid, xsiType, DomUtils.getInnerXml(node), portfolioModelId,
							sharedNodeResParent, sharedResParent, userId);
				}
			} else
				insertMysqlResource(uuid, parentUuid, xsiType, DomUtils.getInnerXml(node), portfolioModelId,
						sharedNodeResParent, sharedResParent, userId);

		}

		// On reparcourt ensuite les enfants pour continuer la recursivite
		// if(children!=null && sharedNode!=1)
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
						writeNode(child, portfolioUuid, portfolioModelId, userId, k, childId, uuid, sharedRes,
								sharedNodeRes, rewriteId, resolve, parseRights);
						k++;
					} else if ("asmResource".equals(nodeName)) // Les asmResource pose probleme dans l'ordre des noeuds
					{
						writeNode(child, portfolioUuid, portfolioModelId, userId, k, childId, uuid, sharedRes,
								sharedNodeRes, rewriteId, resolve, parseRights);
					}
				}
			}
		}

		updateMysqlNodeChildren(forcedUuidParent);

		return uuid;
	}

	public int deleteResource(String resourceUuid, Long userId, Long groupId) {
		int result = 0;
		if (getRights(userId, groupId, resourceUuid).isDelete()) {
			try {
				resourceTableDao.removeById(UUID.fromString(resourceUuid));
			} catch (Exception e) {
				result = 1;
			}
		}
		return result;
	}

	/**
	 * test pour l'affichage des différentes méthodes de Node
	 */
	public GroupRights getRights(Long userId, Long groupId, String nodeUuid) {
		return getRights(userId, groupId, UUID.fromString(nodeUuid));
	}

	/**
	 * test pour l'affichage des différentes méthodes de Node
	 */
	public GroupRights getRights(Long userId, Long groupId, UUID nodeUuid) {

		// Par defaut accès à rien
		GroupRights rights = new GroupRights();

		if (credentialDao.isAdmin(userId)) {
			rights = new GroupRights(true, true, true, true);
		} else if (credentialDao.isDesigner(userId, nodeUuid)) /// Droits via le partage totale (obsolete) ou si c'est
		{
			rights = new GroupRights(true, true, true, true);
		} else {

			if (PhpUtil.empty(groupId)) {
				rights = groupRightsDao.getRightsFromGroups(nodeUuid, userId);
			}

			rights = groupRightsDao.getRightsByGroupId(nodeUuid, userId, groupId);
			rights = groupRightsDao.getSpecificRightsForUser(nodeUuid, userId);
			rights = groupRightsDao.getPublicRightsByGroupId(nodeUuid, groupId);

		} // fin else

		/// Public rights (last chance for rights)
		if (nodeDao.isPublic(nodeUuid, null)) {
			rights.setRead(true);
		}

		return rights;
	}

	private StringBuffer getNodeJsonOutput(String nodeUuid, boolean withChildren, String withChildrenOfXsiType,
			Long userId, Long groupId, String label, boolean checkSecurity) {
		StringBuffer result = new StringBuffer();
		Node resNode = nodeDao.getNode(nodeUuid, userId, groupId);
		ResourceTable resResource;

		if (checkSecurity) {
			GroupRights nodeRight = getRights(userId, groupId, nodeUuid);
			//
			if (!nodeRight.isRead())
				return result;
		}

		if (resNode != null) {
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
				try {
					if (resNode.getChildrenStr().length() > 0) {
						result.append(", ");
						arrayChild = resNode.getChildrenStr().split(",");
						for (int i = 0; i < (arrayChild.length); i++) {
							Node childNode = nodeDao.getNode(arrayChild[i]);
							if (withChildrenOfXsiType == null
									|| StringUtils.equals(withChildrenOfXsiType, childNode.getXsiType()))
								result.append(
										getNodeJsonOutput(arrayChild[i], true, null, userId, groupId, label, true));

							if (withChildrenOfXsiType == null)
								if (arrayChild.length > 1)
									if (i < (arrayChild.length - 1))
										result.append(", ");
						}
					}
				} catch (Exception ex) {
					// Pas de children
				}
			}

			result.append(" } ");
		}

		return result;
	}

	public StringBuffer getNodeXmlOutput(String nodeUuid, boolean withChildren, String withChildrenOfXsiType,
			Long userId, Long groupId, String label, boolean checkSecurity) {
		StringBuffer result = new StringBuffer();
		// Vérification sécurité
		if (checkSecurity) {
			GroupRights rights = getRights(userId, groupId, nodeUuid);
			if (!rights.isRead()) {
				userId = credentialDao.getPublicUid();
				/// Vérifie les droits avec le compte publique (derniere chance)
				rights = groupRightsDao.getPublicRightsByUserId(userId, UUID.fromString(nodeUuid));
				if (!rights.isRead())
					return result;
			}
		}

		Node resNode = nodeDao.getNode(nodeUuid, userId, groupId);
		ResourceTable resResource;

		String indentation = " ";

		if (resNode != null) {
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
						Node resChildNode = nodeDao.getNode(arrayChild[i]);

						String tmpXsiType = "";
						if (resChildNode != null)
							tmpXsiType = resChildNode.getXsiType();

						if (withChildrenOfXsiType == null || withChildrenOfXsiType.equals(tmpXsiType))
							result.append(getNodeXmlOutput(arrayChild[i], true, null, userId, groupId, null, true));

					}
				}
			}

			result.append("</" + resNode.getAsmType() + ">");
		}

		return result;
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

	public Object getNodeBySemanticTag(MimeType outMimeType, String portfolioUuid, String semantictag, Long userId,
			Long groupId) {
		List<Node> node = nodeDao.getNodesBySemanticTag(portfolioUuid, semantictag);
		// On recupere d'abord l'uuid du premier noeud trouve correspondant au
		// semantictag
		String nodeUuidStr = node.get(0).getId().toString();

		if (!hasRight(userId, groupId, nodeUuidStr, GroupRights.READ))
			return null;

		if (outMimeType.getSubType().equals("xml"))
			return getNodeXmlOutput(nodeUuidStr, true, null, userId, groupId, null, true);
		else if (outMimeType.getSubType().equals("json"))
			return "{" + getNodeJsonOutput(nodeUuidStr, true, null, userId, groupId, null, true) + "}";
		else
			return null;
	}

	public boolean hasRight(Long userId, Long groupId, String nodeUuid, String right) {
		GroupRights rights = getRights(userId, groupId, nodeUuid);
		if (right.equals(GroupRights.READ))
			return rights.isRead();
		else if (right.equals(GroupRights.WRITE))
			return rights.isWrite();
		else if (right.equals(GroupRights.SUBMIT))
			return rights.isSubmit();
		else if (right.equals(GroupRights.DELETE))
			return rights.isDelete();
		else
			return false;
	}

	public Object getNodesBySemanticTag(MimeType outMimeType, Long userId, Long groupId, String portfolioUuid,
			String semanticTag) {
		List<Node> nodes = nodeDao.getNodesBySemanticTag(portfolioUuid, semanticTag);
		Iterator<Node> it = nodes.iterator();
		Node node = null;
		String result = "";
		if (outMimeType.getSubType().equals("xml")) {
			result = "<nodes>";
			while (it.hasNext()) {
				node = it.next();
				String nodeUuid = node.getId().toString();
				if (!hasRight(userId, groupId, nodeUuid, GroupRights.READ))
					return null;

				result += "<node ";
				result += DomUtils.getXmlAttributeOutput("id", nodeUuid) + " ";
				result += ">";
				result += "</node>";
			}
			result += "</nodes>";
		} else if (outMimeType.getSubType().equals("json")) {

			result = "{ \"nodes\": { \"node\": [";
			boolean firstPass = false;
			while (it.hasNext()) {
				node = it.next();
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

	public int updateResourceByXsiType(String nodeUuid, String xsiType, String content, Long userId) {

		try {
			resourceTableDao.updateResResource(nodeUuid, content, userId);

			/// Interpretation du code (vive le hack... Non)
			Document doc = DomUtils.xmlString2Document(
					"<?xml version='1.0' encoding='UTF-8' standalone='no'?><res>" + content + "</res>",
					new StringBuffer());
			NodeList nodes = doc.getElementsByTagName("code");
			org.w3c.dom.Node code = nodes.item(0);
			if (code != null) {
				org.w3c.dom.Node codeContent = code.getFirstChild();

				String codeVal;
				if (codeContent != null) {
					codeVal = codeContent.getNodeValue();
					// Check if code already exists
					if (nodeDao.isCodeExist(nodeUuid, codeVal)) {
						throw new GenericBusinessException("Status.CONFLICT : Existing code.");
					} else {
						if (nodeDao.updateNodeCode(nodeUuid, codeVal) > 0) {
							throw new GenericBusinessException("Cannot update node code");
						}
					}
				}
			} else if (xsiType.equals("context")) {
				resourceTableDao.updateContextResource(nodeUuid, content, userId);

			} else {
				resourceTableDao.updateResource(nodeUuid, content, userId);
			}

		} catch (Exception e) {
			return 1;
		}
		return 0;
	}

}
