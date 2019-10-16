package eportfolium.com.karuta.business.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimeTypeParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eportfolium.com.karuta.business.contract.FileManager;
import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.consumer.contract.dao.ConfigurationDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupMembersDao;
import eportfolium.com.karuta.consumer.contract.dao.ResourceTableDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembersId;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.util.JavaTimeUtil;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.util.Tools;
import eportfolium.com.karuta.util.ValidateUtil;

@Service
public class PortfolioManagerImpl extends BaseManager implements PortfolioManager {

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private PortfolioDao portfolioDao;

	@Autowired
	private PortfolioGroupDao portfolioGroupDao;

	@Autowired
	private PortfolioGroupMembersDao portfolioGroupMembersDao;

	@Autowired
	private NodeDao nodeDao;

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private GroupRightsDao groupRightsDao;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private GroupInfoDao groupInfoDao;

	@Autowired
	private ResourceTableDao resourceTableDao;

	@Autowired
	private GroupUserDao groupUserDao;

	@Autowired
	private ConfigurationDao configurationDao;

	@Autowired
	private FileManager fileManager;

	static private final Logger logger = LoggerFactory.getLogger(PortfolioManagerImpl.class);

	public boolean changePortfolioDate(String fromNodeuuid, String fromPortuuid) {
		boolean hasChanged = false;
		final Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.date_default_timezone));

		try {
			Portfolio portfolio = null;
			if (fromNodeuuid != null) {
				UUID portfolioUUID = nodeDao.getPortfolioIdFromNode(fromNodeuuid);
				portfolio = portfolioDao.findById(portfolioUUID);
				portfolio.setModifDate(now);
				hasChanged = true;
			} else if (fromPortuuid != null) {
				portfolio = portfolioDao.findById(UUID.fromString(fromPortuuid));
				portfolio.setModifDate(now);
				hasChanged = true;
			}
			portfolioDao.merge(portfolio);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return hasChanged;
	}

	public boolean deletePortfolioGroups(Long portfolioGroupId) {
		boolean res = false;
		try {
			PortfolioGroup pg = portfolioGroupDao.findById(portfolioGroupId);
			List<PortfolioGroupMembers> results = portfolioGroupMembersDao.getByPortfolioGroupID(portfolioGroupId);
			Iterator<PortfolioGroupMembers> it = results.iterator();
			while (it.hasNext()) {
				portfolioGroupMembersDao.remove(it.next());
			}
			portfolioGroupDao.remove(pg);
			res = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public boolean deletePortfolioFromPortfolioGroups(String portfolioUuid, Long portfolioGroupId) {
		boolean result = false;

		try {
			PortfolioGroupMembersId pgmID = new PortfolioGroupMembersId();
			pgmID.setPortfolio(portfolioDao.findById(UUID.fromString(portfolioUuid)));
			pgmID.setPortfolioGroup(portfolioGroupDao.findById(portfolioGroupId));
			PortfolioGroupMembers portfolioGroupMembers = portfolioGroupMembersDao.findById(pgmID);
			portfolioGroupMembersDao.remove(portfolioGroupMembers);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public String getPortfolioByPortfolioGroup(Long portfolioGroupId) {
		StringBuilder result = new StringBuilder();
		result.append("<group id=\"").append(portfolioGroupId).append("\">");
		List<Portfolio> portfolios = portfolioGroupDao.getPortfolioByPortfolioGroup(portfolioGroupId);
		Iterator<Portfolio> it = portfolios.iterator();
		while (it.hasNext()) {
			result.append("<portfolio");
			result.append(" id=\"");
			result.append(it.next().getId().toString());
			result.append("\"");
			result.append(">");
			result.append("</portfolio>");
		}
		result.append("</group>");
		return result.toString();
	}

	public int setPortfolioActive(String portfolioUuid, Boolean active) {
		int result = -1;
		try {
			Portfolio portfolio = portfolioDao.findById(UUID.fromString(portfolioUuid));
			portfolio.setActive(BooleanUtils.toInteger(active));
			portfolioDao.merge(portfolio);
			result = 0;
		} catch (DoesNotExistException e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getPortfolio(MimeType outMimeType, String portfolioUuid, Long userId, Long groupId, String label,
			String resource, String files, long substid, Integer cutoff)
			throws DoesNotExistException, BusinessException, Exception {

		Node rootNode = portfolioDao.getPortfolioRootNode(portfolioUuid);
		String header = "";
		String footer = "";

		GroupRights rights = getRightsOnPortfolio(userId, groupId, portfolioUuid);
		if (!rights.isRead()) {
			userId = credentialDao.getPublicUid();
			/// Vérifie les droits avec le compte publique (dernière chance)
			GroupRights publicRights = groupRightsDao.getPublicRightsByUserId(rootNode.getId(), userId);
			if (!publicRights.isRead()) {
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
			}
		}

		if (outMimeType.getSubtype().equals("xml")) {
			Long ownerId = portfolioDao.getOwner(portfolioUuid);
			boolean isOwner = false;
			if (ownerId == userId)
				isOwner = true;

			String headerXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><portfolio code=\"0\" id=\"" + portfolioUuid
					+ "\" owner=\"" + isOwner + "\"><version>4</version>";

			String data = getLinearXml(portfolioUuid, rootNode.getId().toString(), null, true, null, userId,
					rights.getGroupRightInfo().getId(), rights.getGroupRightInfo().getLabel(), cutoff);

			StringWriter stw = new StringWriter();
			stw.append(headerXML + data + "</portfolio>");

			if (resource != null && files != null) {
				if (resource.equals("true") && files.equals("true")) {
					String adressedufichier = System.getProperty("user.dir") + "/tmp_getPortfolio_" + new Date()
							+ ".xml";
					String adresseduzip = System.getProperty("user.dir") + "/tmp_getPortfolio_" + new Date() + ".zip";

					File file = null;
					PrintWriter ecrire;
					try {
						file = new File(adressedufichier);
						ecrire = new PrintWriter(new FileOutputStream(adressedufichier));
						ecrire.println(stw.toString());
						ecrire.flush();
						ecrire.close();
						System.out.print("fichier cree ");
					} catch (IOException ioe) {
						System.out.print("Erreur : ");
						ioe.printStackTrace();
					}

					try {
						ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(adresseduzip));
						zip.setMethod(ZipOutputStream.DEFLATED);
						zip.setLevel(Deflater.BEST_COMPRESSION);
						File dataDirectories = new File(file.getName());
						FileInputStream fis = new FileInputStream(dataDirectories);
						byte[] bytes = new byte[fis.available()];
						fis.read(bytes);

						ZipEntry entry = new ZipEntry(file.getName());
						entry.setTime(dataDirectories.lastModified());
						zip.putNextEntry(entry);
						zip.write(bytes);
						zip.closeEntry();
						fis.close();
						zip.close();
						file.delete();

						return adresseduzip;
					} catch (FileNotFoundException fileNotFound) {
						fileNotFound.printStackTrace();
					} catch (IOException io) {
						io.printStackTrace();
					}
				}
			}

			return stw.toString();
		} else if (outMimeType.getSubtype().equals("json")) {
			header = "{\"portfolio\": { \"-xmlns:xsi\": \"http://www.w3.org/2001/XMLSchema-instance\",\"-schemaVersion\": \"1.0\",";
			footer = "}}";
		}

		return header + nodeManager
				.getNode(outMimeType, rootNode.getId().toString(), true, userId, groupId, label, cutoff).toString()
				+ footer;
	}

	private String getLinearXml(String portfolioUuid, String rootuuid, Node portfolio, boolean withChildren,
			String withChildrenOfXsiType, Long userId, Long groupId, String role, Integer cutoff)
			throws SQLException, SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
		DocumentBuilder parse = newInstance.newDocumentBuilder();

		long time0 = 0;
		long time1 = 0;
		long time2 = 0;
		long time3 = 0;
		long time4 = 0;
		long time5 = 0;

		time0 = System.currentTimeMillis();

		List<Pair<Node, GroupRights>> portfolioStructure = getPortfolioStructure(portfolioUuid, userId, groupId,
				cutoff);

		time1 = System.currentTimeMillis();

		Map<String, Object[]> resolve = new HashMap<String, Object[]>();
		/// Node -> parent
		Map<String, t_tree> entries = new HashMap<String, t_tree>();

		processQuery(portfolioStructure, resolve, entries, null, parse, role);

		time2 = System.currentTimeMillis();

		portfolioStructure = getSharedStructure(portfolioUuid, userId, groupId, cutoff);

		time3 = System.currentTimeMillis();

		if (portfolioStructure != null) {
			processQuery(portfolioStructure, resolve, entries, null, parse, role);
		}

		time4 = System.currentTimeMillis();

		/// Reconstruct functional tree
		t_tree root = entries.get(rootuuid);
		StringBuilder out = new StringBuilder(256);
		if (root != null)
			reconstructTree(out, root, entries);

		time5 = System.currentTimeMillis();

		System.out.println("---- Portfolio ---");
		System.out.println("Query Main: " + (time1 - time0));
		System.out.println("Parsing Main: " + (time2 - time1));
		System.out.println("Query shared: " + (time3 - time2));
		System.out.println("Parsing shared: " + (time4 - time3));
		System.out.println("Reconstruction a: " + (time5 - time4));
		System.out.println("------------------"); //

		return out.toString();
	}

	private List<Pair<Node, GroupRights>> getPortfolioStructure(String portfolioUuid, Long userId, Long groupId,
			Integer cutoff) {

		long time0 = 0;
		long time1 = 0;
		long time2 = 0;
		long time3 = 0;
		long time4 = 0;
		long time5 = 0;
		long time6 = 0;

		List<Pair<Node, GroupRights>> portfolioStructure = new ArrayList<Pair<Node, GroupRights>>();

		time0 = System.currentTimeMillis();

		Node rootNode = portfolioDao.getPortfolioRootNode(portfolioUuid);
		time1 = System.currentTimeMillis();
		GroupRights rights = null;

		// Cas admin, designer, owner
		if (rootNode != null && (credentialDao.isAdmin(userId) || credentialDao.isDesigner(userId, rootNode.getId())
				|| userId == portfolioDao.getOwner(portfolioUuid))) {
			List<Node> nodes = nodeDao.getNodesWithResources(portfolioUuid);
			for (Node node : nodes) {
				rights = new GroupRights(true, true, true, true);
				portfolioStructure.add(Pair.of(node, rights));
			}
		}
		/// FIXME: Il faudrait peut-etre prendre une autre strategie pour selectionner
		/// les bonnes donnees : Cas proprietaire OU cas general (via les droits
		/// partagés)
		else if (hasRights(userId, portfolioUuid)) {
			time2 = System.currentTimeMillis();
			Map<UUID, GroupRights> t_rights_22 = new HashMap<UUID, GroupRights>();
			time3 = System.currentTimeMillis();

			String login = credentialDao.getLoginById(userId);
//				FIXME: Devrait peut-etre verifier si la personne a les droits d'y acceder?
			List<GroupRights> grList = groupRightsDao.getPortfolioAndUserRights(UUID.fromString(portfolioUuid), login,
					groupId);
			for (GroupRights gr : grList) {
				if (t_rights_22.containsKey(gr.getGroupRightsId())) {
					GroupRights original = t_rights_22.get(gr.getGroupRightsId());
					original.setRead(Boolean.logicalOr(gr.isRead(), original.isRead()));
					original.setWrite(Boolean.logicalOr(gr.isWrite(), original.isWrite()));
					original.setDelete(Boolean.logicalOr(gr.isDelete(), original.isDelete()));
					original.setSubmit(Boolean.logicalOr(gr.isSubmit(), original.isSubmit()));
					original.setAdd(Boolean.logicalOr(gr.isAdd(), original.isAdd()));
				} else {
					t_rights_22.put(gr.getGroupRightsId(), gr);
				}
			}

			time4 = System.currentTimeMillis();

			List<Node> nodes = nodeDao.getNodes(t_rights_22.keySet());

			// Selectionne les donnees selon la filtration
			for (Node node : nodes) {
				if (t_rights_22.containsKey(node.getId())) { // Verification des droits
					rights = t_rights_22.get(node.getId());
					if (rights.isRead()) { // On doit au moins avoir le droit de lecture
						portfolioStructure.add(Pair.of(node, rights));
					}
				}
			}
			time5 = System.currentTimeMillis();

		} else if (portfolioDao.isPublic(portfolioUuid)) // Public case, looks like previous query, but with
		{
			List<Node> nodes = nodeDao.getNodesWithResources(portfolioUuid);
			for (Node node : nodes) {
				rights = new GroupRights(true, false, false, false);
				portfolioStructure.add(Pair.of(node, rights));
			}
		}

		time6 = System.currentTimeMillis();

		System.out.println((time1 - time0) + "," + (time2 - time1) + "," + (time3 - time2) + "," + (time4 - time3) + ","
				+ (time5 - time4));
		System.out.println("---- Query Portfolio ----");
		System.out.println("Fetch root: " + (time1 - time0));
		System.out.println("Check rights: " + (time2 - time1));
		System.out.println("Create temp: " + (time3 - time2));
		System.out.println("Fetch rights all/group: " + (time4 - time3));
		System.out.println("Fetch user rights: " + (time5 - time4));
		System.out.println("Actual query: " + (time6 - time5)); //

		return portfolioStructure;
	}

	/**
	 * Recupere les noeuds partages d'un portfolio. C'est separe car les noeuds ne
	 * provenant pas d'un meme portfolio, on ne peut pas les selectionner rapidement
	 * Autre possibilite serait de garder ce meme type de fonctionnement pour une
	 * selection par niveau d'un portfolio.<br>
	 * TODO: A faire un 'benchmark' dessus
	 * 
	 * @param portfolioUuid
	 * @param userId
	 * @param groupId
	 * @param cutoff
	 * @return
	 */
	private List<Pair<Node, GroupRights>> getSharedStructure(String portfolioUuid, Long userId, Long groupId,
			Integer cutoff) {

		List<Pair<Node, GroupRights>> portfolioStructure = new ArrayList<Pair<Node, GroupRights>>();

		if (portfolioDao.hasSharedNodes(portfolioUuid)) {
			List<Node> t_nodes = nodeDao.getSharedNodes(portfolioUuid);
			Map<Integer, Set<UUID>> t_map_parentid = new HashMap<Integer, Set<UUID>>();
			Set<UUID> t_set_parentid = new HashSet<UUID>();

			for (Node t_node : t_nodes) {
				t_set_parentid.add(t_node.getSharedNodeUuid());
			}

			t_map_parentid.put(0, t_set_parentid);

			/// On boucle, sera toujours <= e "nombre de noeud du portfolio"
			int level = 0;
			boolean added = true;
			Set<UUID> t_struc_parentid_2 = null;
			while (added && (cutoff == null ? true : level < cutoff)) {
				t_struc_parentid_2 = new HashSet<UUID>();
				for (Node t_node : t_nodes) {
					for (UUID t_parent_node : t_map_parentid.get(level)) {
						if (t_node.getPortfolio().getId().equals(UUID.fromString(portfolioUuid))
								&& t_node.getParentNode().getId().equals(t_parent_node)) {
							t_struc_parentid_2.add(t_node.getId());
							break;
						}
					}
				}
				t_map_parentid.put(level + 1, t_struc_parentid_2);
				t_set_parentid.addAll(t_struc_parentid_2);
				added = CollectionUtils.isNotEmpty(t_struc_parentid_2); // On s'arrete quand rien n'a ete ajoute
				level = level + 1; // Prochaine etape
			}

			List<Node> nodes = nodeDao.getNodes(t_set_parentid);
			GroupRights rights = null;
			for (Node node : nodes) {
				rights = groupRightsDao.getRightsByIdAndUser(node.getId(), userId);
				if (rights != null && rights.isRead()) { // On doit au moins avoir le droit de lecture
					portfolioStructure.add(Pair.of(node, rights));
				}
			}
		}
		return portfolioStructure;
	}

	public String getPortfolioShared(Long userId) {
		StringBuilder out = new StringBuilder();

		List<Map<String, Object>> portfolios = portfolioDao.getPortfolioShared(userId);
		out.append("<portfolios>");
		Iterator<Map<String, Object>> it = portfolios.iterator();
		Map<String, Object> current = null;
		while (it.hasNext()) {
			current = it.next();
			Long gid = MapUtils.getLong(current, "gid");
			UUID portfolioUuid = (UUID) current.get("portfolio");
			out.append("<portfolio gid='" + gid + "' portfolio='" + portfolioUuid.toString() + "'/>");
		}
		out.append("</portfolios>");
		return out.toString();
	}

	public String getPortfolioByCode(MimeType mimeType, String portfolioCode, Long userId, Long groupId,
			String resources, long substid) throws DoesNotExistException, BusinessException, Exception {
		UUID pid = portfolioDao.getPortfolioUuidFromNodeCode(portfolioCode);
		boolean withResources = BooleanUtils.toBoolean(resources);
		String result = "";

		if (withResources) {
			try {
				return getPortfolio(mimeType, pid.toString(), userId, groupId, null, null, null, substid, null)
						.toString();
			} catch (MimeTypeParseException e) {
				e.printStackTrace();
			}
		} else {
			Portfolio p = portfolioDao.findById(pid);
			result += DomUtils.getXmlAttributeOutput("id", p.getId().toString()) + " ";
			result += DomUtils.getXmlAttributeOutput("root_node_id", p.getRootNode().getId().toString()) + " ";
			result += ">";
			result += nodeManager.getNodeXmlOutput(p.getRootNode().getId().toString(), false, "nodeRes", userId,
					groupId, null, false);
			result += "</portfolio>";

		}
		return result;
	}

	public GroupRights getRightsOnPortfolio(Long userId, Long groupId, String portfolioUuid) {
		GroupRights reponse = new GroupRights();

		try {
			/// modif_user_id => current owner
			Portfolio p = portfolioDao.getPortfolio(portfolioUuid);
			if (p != null) {
				if (p.getModifUserId() == userId)
					// Is the owner
					reponse = new GroupRights(new GroupRightsId(), true, true, true, true, true);
				else // General case
					reponse = nodeManager.getRights(userId, groupId, p.getRootNode().getId());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return reponse;
	}

	public GroupRights getRightsOnPortfolio(Long userId, Long groupId, UUID portfolioUuid) {
		GroupRights reponse = new GroupRights();

		try {
			/// modif_user_id => current owner
			Portfolio p = portfolioDao.getPortfolio(portfolioUuid);
			if (p != null) {
				if (p.getModifUserId() == userId)
					// Is the owner
					reponse = new GroupRights(new GroupRightsId(), true, true, true, true, true);
				else // General case
					reponse = nodeManager.getRights(userId, groupId, p.getRootNode().getId());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return reponse;
	}

	/**
	 * Has rights, whether ownership, or given by someone
	 * 
	 * @param userId
	 * @param portfolioUuid
	 * @return
	 */
	public boolean hasRights(Long userId, String portfolioUuid) {
		boolean hasRights = false;

		if (userId != null && portfolioUuid != null) {
			/// Evaluate ownership
			Long modif_user_id = portfolioDao.getOwner(portfolioUuid);
			if (Objects.equals(modif_user_id, userId)) {
				hasRights = true;
			} else // Check further for other shared rights
			{
				List<GroupUser> gu = groupUserDao.getByPortfolioAndUser(portfolioUuid, userId);
				if (CollectionUtils.isNotEmpty(gu)) {
					hasRights = true;
				}
			}
		}
		return hasRights;
	}

	public void deletePortfolio(String portfolioUuid, Long userId, Long groupId) throws Exception {
		boolean hasRights = false;

		GroupRights rights = getRightsOnPortfolio(userId, groupId, portfolioUuid);
		if (rights.isDelete() || credentialDao.isAdmin(userId)) {
			hasRights = true;
		}

		if (hasRights) {
			// S'il y a quelque chose de particulier, on s'assure que tout soit bien nettoyé
			// de façon separée
			List<GroupRightInfo> griList = groupRightInfoDao.getByPortfolioID(portfolioUuid);
			for (java.util.Iterator<GroupRightInfo> it = griList.iterator(); it.hasNext();) {
				groupRightInfoDao.remove(it.next());
			}

			/// Resources
			List<ResourceTable> rtList = resourceTableDao.getResourcesByPortfolioUUID(portfolioUuid);
			for (java.util.Iterator<ResourceTable> it = rtList.iterator(); it.hasNext();) {
				resourceTableDao.remove(it.next());
			}

			rtList = resourceTableDao.getContextResourcesByPortfolioUUID(portfolioUuid);
			for (java.util.Iterator<ResourceTable> it = rtList.iterator(); it.hasNext();) {
				resourceTableDao.remove(it.next());
			}

			rtList = resourceTableDao.getResourcesOfResourceByPortfolioUUID(portfolioUuid);
			for (java.util.Iterator<ResourceTable> it = rtList.iterator(); it.hasNext();) {
				resourceTableDao.remove(it.next());
			}

			/// Nodes
			List<Node> nodes = nodeDao.getNodes(portfolioUuid);
			for (java.util.Iterator<Node> it = nodes.iterator(); it.hasNext();) {
				nodeDao.remove(it.next());
			}

			/// Portfolio
			portfolioDao.removeById(portfolioUuid);

			/// delete portfolio from Group
			List<PortfolioGroupMembers> pgmList = portfolioGroupMembersDao.getByPortfolioID(portfolioUuid);
			for (java.util.Iterator<PortfolioGroupMembers> it = pgmList.iterator(); it.hasNext();) {
				portfolioGroupMembersDao.remove(it.next());
			}
		}
	}

	public boolean isOwner(Long id, String portfolioUuid) {
		return portfolioDao.isOwner(id, portfolioUuid);
	}

	public boolean changePortfolioOwner(String portfolioUuid, long newOwner) {
		return portfolioDao.changePortfolioOwner(portfolioUuid, newOwner);
	}

	public Portfolio changePortfolioConfiguration(String portfolioUuid, Boolean portfolioActive, Long userId)
			throws BusinessException {
		if (!credentialDao.isAdmin(userId)) {
			throw new GenericBusinessException("No admin right");
		}
		return portfolioDao.updatePortfolioConfiguration(portfolioUuid, portfolioActive);
	}

	public Object getPortfolios(MimeType outMimeType, long userId, long groupId, Boolean portfolioActive, long substid,
			Boolean portfolioProject, String projectId, Boolean countOnly, String search) {
		return portfolioDao.getPortfolios(userId, substid, portfolioActive);
	}

	public boolean putPortfolio(MimeType inMimeType, MimeType outMimeType, String in, String portfolioUuid, Long userId,
			Boolean portfolioActive, int groupId, UUID portfolioModelId) throws Exception {
		StringBuffer outTrace = new StringBuffer();

		Portfolio resPortfolio = portfolioDao.getPortfolio(portfolioUuid);
		if (resPortfolio != null) {
			// Le portfolio existe donc on regarde si modele ou pas
			portfolioModelId = resPortfolio.getModelId();
		} else {
			resPortfolio = new Portfolio();
		}

		if (PhpUtil.empty(userId) || !ValidateUtil.isUnsignedId(0)) {
			if (resPortfolio != null)
				userId = resPortfolio.getCredential().getId();
		}

		if (in.length() > 0) {
			Document doc = DomUtils.xmlString2Document(in, outTrace);

			org.w3c.dom.Node rootNode = (doc.getElementsByTagName("portfolio")).item(0);
			if (rootNode == null) {
				throw new GenericBusinessException("Root Node (portfolio) not found !");
			} else {
				rootNode = (doc.getElementsByTagName("asmRoot")).item(0);

				String rootNodeUuid = UUID.randomUUID().toString();
				org.w3c.dom.Node idAtt = rootNode.getAttributes().getNamedItem("id");
				if (idAtt != null) {
					String tempId = idAtt.getNodeValue();
					if (tempId.length() > 0) {
						rootNodeUuid = tempId;
					}
				}

				if (resPortfolio.getId() != null) {
					portfolioDao.merge(resPortfolio);
				} else {
					portfolioDao.add(rootNodeUuid, null, userId, resPortfolio);
				}

				nodeManager.writeNode(rootNode, portfolioUuid, portfolioModelId, userId, 0, null, null, false, false,
						true, null, false);
			}
		}

		portfolioDao.updatePortfolioConfiguration(portfolioUuid, portfolioActive);
		return true;
	}

	public String postPortfolioParserights(String portfolioUuid, Long userId) {

		if (!credentialDao.isAdmin(userId) && !credentialDao.isCreator(userId))
			return "no rights";

		boolean setPublic = false;

		try {
			/// temp class
			class right {
				int rd = 0;
				int wr = 0;
				int dl = 0;
				int sb = 0;
				int ad = 0;
				String types = "";
				String rules = "";
				String notify = "";
			}

			class groupright {
				right getGroup(String label) {
					right r = rights.get(label.trim());
					if (r == null) {
						r = new right();
						rights.put(label, r);
					}
					return r;
				}

				void setNotify(String roles) {
					Iterator<right> iter = rights.values().iterator();
					while (iter.hasNext()) {
						right r = iter.next();
						r.notify = roles.trim();
					}
				}

				HashMap<String, right> rights = new HashMap<String, right>();
			}

			class resolver {
				groupright getUuid(String uuid) {
					groupright gr = resolve.get(uuid);
					if (gr == null) {
						gr = new groupright();
						resolve.put(uuid, gr);
					}
					return gr;
				};

				HashMap<String, groupright> resolve = new HashMap<String, groupright>();
				HashMap<String, Long> groups = new HashMap<String, Long>();
			}

			resolver resolve = new resolver();

			// Selection des metadonnees
			List<Node> nodes = nodeDao.getNodes(portfolioUuid);
			Iterator<Node> it = nodes.iterator();

			DocumentBuilder documentBuilder;
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			while (it.hasNext()) {
				Node current = it.next();
				String uuid = current.getId().toString();
				String meta = current.getMetadataWad();
				// meta = meta.replaceAll("user", login);
				String nodeString = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><transfer " + meta + "/>";

				groupright role = resolve.getUuid(uuid);

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

							right r = role.getGroup(nodeRole);
							r.rd = 1;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("showtoroles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {
							nodeRole = tokens.nextElement().toString();

							right r = role.getGroup(nodeRole);
							r.rd = 0;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("delnoderoles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {

							nodeRole = tokens.nextElement().toString();
							right r = role.getGroup(nodeRole);
							r.dl = 1;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("editnoderoles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {
							nodeRole = tokens.nextElement().toString();
							right r = role.getGroup(nodeRole);
							r.wr = 1;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("submitnoderoles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {
							nodeRole = tokens.nextElement().toString();
							right r = role.getGroup(nodeRole);
							r.sb = 1;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("seeresroles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {
							nodeRole = tokens.nextElement().toString();
							right r = role.getGroup(nodeRole);
							r.rd = 1;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("delresroles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {
							nodeRole = tokens.nextElement().toString();
							right r = role.getGroup(nodeRole);
							r.dl = 1;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("editresroles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {
							nodeRole = tokens.nextElement().toString();
							right r = role.getGroup(nodeRole);
							r.wr = 1;

							resolve.groups.put(nodeRole, 0L);
						}
					}
					att = attribMap.getNamedItem("submitroles");
					if (att != null) {
						StringTokenizer tokens = new StringTokenizer(att.getNodeValue(), " ");
						while (tokens.hasMoreElements()) {
							nodeRole = tokens.nextElement().toString();
							right r = role.getGroup(nodeRole);
							r.sb = 1;

							resolve.groups.put(nodeRole, 0L);
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
							right r = role.getGroup(nrole);
							r.rules = actions;

							resolve.groups.put(nrole, 0L);
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
									resolve.groups.put(roles[i].trim(), 0L);
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
						role.setNotify(merge);
					}

					if (portfolioDao.isPublic(portfolioUuid))
						setPublic = true;
//						/*
					meta = current.getMetadata();
					nodeString = "<?xml version='1.0' encoding='UTF-8' standalone='no'?><transfer " + meta + "/>";
					is = new InputSource(new StringReader(nodeString));
					doc = documentBuilder.parse(is);
					attribNode = doc.getDocumentElement();
					attribMap = attribNode.getAttributes();
					org.w3c.dom.Node publicatt = attribMap.getNamedItem("public");
					if (publicatt != null && "Y".equals(publicatt.getNodeValue()))
						setPublic = true;
					// */
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			/// On insere les donnees pre-compile
			Iterator<String> entries = resolve.groups.keySet().iterator();

			// Cree les groupes, ils n'existent pas
			GroupInfo gi = new GroupInfo();
			GroupRightInfo gri = new GroupRightInfo();

			while (entries.hasNext()) {
				String label = entries.next();
				gri.setOwner(1L);
				gri.setLabel(label);
				gri.setChangeRights(false);
				gri.setPortfolio(new Portfolio(UUID.fromString(portfolioUuid)));

				groupRightInfoDao.persist(gri);
				Long grid = gri.getId();
				resolve.groups.put(label, grid);

				gi.setGroupRightInfo(gri);
				gi.setOwner(1L);
				gi.setLabel(label);
				groupInfoDao.persist(gi);
			}

			/// Ajout des droits des noeuds
			GroupRights groupRights = null;

			Iterator<Entry<String, groupright>> rights = resolve.resolve.entrySet().iterator();
			while (rights.hasNext()) {
				Entry<String, groupright> entry = rights.next();
				String uuid = entry.getKey();
				groupright gr = entry.getValue();

				Iterator<Entry<String, right>> rightiter = gr.rights.entrySet().iterator();
				while (rightiter.hasNext()) {
					Entry<String, right> rightelem = rightiter.next();
					String group = rightelem.getKey();
					long grid = resolve.groups.get(group);
					right rightval = rightelem.getValue();
					groupRights = new GroupRights();
					groupRights.setId(new GroupRightsId());
					groupRights.setGroupRightInfo(new GroupRightInfo(grid));
					groupRights.setGroupRightsId(UUID.fromString(uuid));
					groupRights.setRead(BooleanUtils.toBoolean(rightval.rd));
					groupRights.setWrite(BooleanUtils.toBoolean(rightval.wr));
					groupRights.setDelete(BooleanUtils.toBoolean(rightval.dl));
					groupRights.setSubmit(BooleanUtils.toBoolean(rightval.sb));
					groupRights.setAdd(BooleanUtils.toBoolean(rightval.ad));
					groupRights.setTypesId(rightval.types);
					groupRights.setRulesId(rightval.rules);
					groupRights.setNotifyRoles(rightval.notify);
					groupRightsDao.persist(groupRights);
				}
			}

			/// Create base group
			Long groupid = securityManager.createRole(portfolioUuid, "all", userId);

			/// Finalement on cree un role designer
			groupid = securityManager.createRole(portfolioUuid, "designer", userId);

			groupUserDao.addUserInGroup(userId, groupid);

			// Update time
			portfolioDao.updateTime(portfolioUuid);

			// Set portfolio public if needed
			if (setPublic)
				groupManager.setPublicState(userId, portfolioUuid, setPublic);
		} catch (Exception e) {
			logger.error("MESSAGE: " + e.getMessage() + " " + e.getLocalizedMessage());
		}

		return portfolioUuid;
	}

	public String addPortfolio(MimeType inMimeType, MimeType outMimeType, String in, long userId, long groupId,
			String portfolioModelId, long substid, boolean parseRights, String projectName)
			throws BusinessException, Exception {
		if (!credentialDao.isAdmin(userId) && !credentialDao.isCreator(userId))
			throw new GenericBusinessException("FORBIDDEN : No admin right");

		StringBuffer outTrace = new StringBuffer();

		// Si le modele est renseigne, on ignore le XML poste et on recupere le contenu
		// du modele a la place
		// FIXME Unused, we instanciate/copy a portfolio
		if (portfolioModelId != null)
			in = getPortfolio(inMimeType, portfolioModelId, userId, groupId, null, null, null, substid, null)
					.toString();

		Portfolio portfolio = new Portfolio();
		if (in.length() > 0) {
			Document doc = DomUtils.xmlString2Document(in, outTrace);

			/// Check if portfolio code is already used
			XPath xPath = XPathFactory.newInstance().newXPath();
			String filterRes = "//*[local-name()='asmRoot']/*[local-name()='asmResource']/*[local-name()='code']";
			NodeList nodelist = (NodeList) xPath.compile(filterRes).evaluate(doc, XPathConstants.NODESET);

			if (nodelist.getLength() > 0) {
				String code = nodelist.item(0).getTextContent();
				if (projectName != null) {
					// Find if it contains a project name
					int dot = code.indexOf(".");
					if (dot < 0) // Doesn't exist, add it
						code = projectName + "." + code;
					else // Replace
						code = projectName + code.substring(dot);
				}

				// Simple query
				if (nodeDao.isCodeExist(code, null))
					throw new GenericBusinessException("CONFLICT : Existing code.");

				nodelist.item(0).setTextContent(code);
			}

			org.w3c.dom.Node rootNode = (doc.getElementsByTagName("portfolio")).item(0);
			if (rootNode == null)
				throw new Exception("Root Node (portfolio) not found !");
			else {
				rootNode = (doc.getElementsByTagName("asmRoot")).item(0);

				String uuid = UUID.randomUUID().toString();

				portfolioDao.add(uuid, null, userId, portfolio);
				// On recupere le nouvel uuid généré
				nodeManager.writeNode(rootNode, portfolio.getId().toString(), UUID.fromString(portfolioModelId), userId,
						0, uuid, null, false, false, false, null, parseRights);
			}
		}

		/// If we instanciate, don't need the designer role
		long groupid = securityManager.createRole(portfolio.getId().toString(), "all", userId);
		/// Creer groupe 'designer', 'all' est mis avec ce qui est specifique dans le
		/// xml recu
		groupid = securityManager.createRole(portfolio.getId().toString(), "designer", userId);

		/// Ajoute la personne dans ce groupe
		groupUserDao.addUserInGroup(groupid, userId);

		portfolio.setActive(BooleanUtils.toInteger(true));
		if (StringUtils.isNotEmpty(portfolioModelId))
			portfolio.setModelId(UUID.fromString(portfolioModelId));

		portfolioDao.merge(portfolio);

		String result = "<portfolios>";
		result += "<portfolio ";
		result += DomUtils.getXmlAttributeOutput("id", portfolio.getId().toString()) + " ";
		result += "/>";
		result += "</portfolios>";
		return result;
	}

	public String postPortfolioZip(MimeType mimeType, MimeType mimeType2, String path, String userName,
			InputStream inputStream, Long userId, Long groupId, String modelId, Long credentialSubstitutionId,
			boolean parseRights, String projectName) throws BusinessException, FileNotFoundException, Exception {
		if (!credentialDao.isAdmin(userId) && !credentialDao.isCreator(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		if (projectName == null)
			projectName = "";
		else
			projectName = projectName.trim();

		DataInputStream inZip = new DataInputStream(inputStream);
		// Parse the request
		System.out.println(inputStream);

		String filename;
		String[] xmlFiles;
		String[] allFiles;
		byte[] buff = new byte[0x100000]; // 1MB buffer

		String outsideDir = path.substring(0, path.lastIndexOf(File.separator)) + "_files" + File.separator;
		File outsideDirectoryFile = new File(outsideDir);
		System.out.println(outsideDir);
		// if the directory does not exist, create it
		if (!outsideDirectoryFile.exists()) {
			outsideDirectoryFile.mkdir();
		}

		// Creation du zip
		String portfolioUuidPreliminaire = UUID.randomUUID().toString();
		filename = outsideDir + "xml_" + portfolioUuidPreliminaire + ".zip";
		FileOutputStream outZip = new FileOutputStream(filename);

		int len;

		while ((len = inZip.read(buff)) != -1) {
			outZip.write(buff, 0, len);
		}

		inZip.close();
		outZip.close();

		// -- unzip --
		fileManager.unzip(filename, outsideDir + portfolioUuidPreliminaire + File.separator);

		// Unzip just the next zip level. I hope there will be no zipped documents...
		String[] zipFiles = fileManager.findFiles(outsideDir + portfolioUuidPreliminaire + File.separator, "zip");
		for (int i = 0; i < zipFiles.length; ++i) {
			fileManager.unzip(zipFiles[i], outsideDir + portfolioUuidPreliminaire + File.separator);
		}

		xmlFiles = fileManager.findFiles(outsideDir + portfolioUuidPreliminaire + File.separator, "xml");
		allFiles = fileManager.findFiles(outsideDir + portfolioUuidPreliminaire + File.separator, null);

		////// Lecture du fichier de portfolio
		StringBuffer outTrace = new StringBuffer();
		//// Importation du portfolio
		// --- Read xml fileL ----
		///// Pour associer l'ancien uuid -> nouveau, pour les fichiers
		Map<String, String> resolve = new HashMap<String, String>();
		Portfolio portfolio = null;
		boolean hasLoaded = false;
		try {
			for (int i = 0; i < xmlFiles.length; i++) {
				String xmlFilepath = xmlFiles[i];
				String xmlFilename = xmlFilepath.substring(xmlFilepath.lastIndexOf(File.separator));
				if (xmlFilename.contains("_"))
					continue; // Case when we add an xml in the portfolio

				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFilepath), "UTF8"));
				String line;
				StringBuilder sb = new StringBuilder();

				while ((line = br.readLine()) != null) {
					sb.append(line.trim());
				}
				br.close();
				String xml = "?";
				xml = sb.toString();

				portfolio = new Portfolio();

				if (xml.contains("<portfolio")) // Le porfolio (peux mieux faire)
				{
					Document doc = DomUtils.xmlString2Document(xml, outTrace);

					// Find code
					/// Cherche si on a deje envoye quelque chose
					XPath xPath = XPathFactory.newInstance().newXPath();
					String filterRes = "//*[local-name()='asmRoot']/*[local-name()='asmResource']/*[local-name()='code']";
					NodeList nodelist = (NodeList) xPath.compile(filterRes).evaluate(doc, XPathConstants.NODESET);

					if (nodelist.getLength() > 0) {
						String code = nodelist.item(0).getTextContent();

						if (!"".equals(projectName)) // If a new name has been specified
						{
							// Find if it contains a project name
							int dot = code.indexOf(".");
							if (dot > 0)
								code = projectName + code.substring(dot);
							else // If no dot, it's a project, skip it
								continue;

							// Check if new code exists
							if (nodeDao.isCodeExist(code, null))
								throw new GenericBusinessException("409 Conflict : Existing code.");

							// Replace content
							nodelist.item(0).setTextContent(code);
						} else // Otherwise, check if it exists
						{
							// Simple query
							if (nodeDao.isCodeExist(code, null))
								throw new GenericBusinessException("409 Conflict : Existing code.");
						}
					}

					// Check if it needs replacing
					org.w3c.dom.Node rootNode = (doc.getElementsByTagName("portfolio")).item(0);
					if (rootNode == null)
						throw new Exception("Root Node (portfolio) not found !");
					else {
						rootNode = (doc.getElementsByTagName("asmRoot")).item(0);

						String uuid = UUID.randomUUID().toString();
						portfolioDao.add(uuid, null, userId, portfolio);
						nodeManager.writeNode(rootNode, portfolio.getId().toString(), null, userId, 0, uuid, null,
								false, false, false, resolve, parseRights);
					}

					portfolio.setActive(1);
					portfolioDao.merge(portfolio);

					/// Create base group
					Long groupid = securityManager.createRole(portfolio.getId().toString(), "all", userId);
					/// Finalement on cree un rele designer
					groupid = securityManager.createRole(portfolio.getId().toString(), "designer", userId);

					/// Ajoute la personne dans ce groupe
					groupUserDao.addUserInGroup(groupid, userId);

					hasLoaded = true;
				}
			}
		} catch (Exception e) {
			throw e;
		}

		if (hasLoaded)
			for (int i = 0; i < allFiles.length; i++) {
				String fullPath = allFiles[i];
				String tmpFileName = fullPath.substring(fullPath.lastIndexOf(File.separator) + 1);

				if (!tmpFileName.contains("_"))
					continue; // We want ressources now, they have '_' in their name
				int index = tmpFileName.indexOf("_");
				if (index == -1)
					index = tmpFileName.indexOf(".");
				int last = tmpFileName.lastIndexOf(File.separator);
				if (last == -1)
					last = 0;
				String uuid = tmpFileName.substring(last, index);

				String lang;
				try {
					lang = tmpFileName.substring(index + 1, index + 3);

					if ("un".equals(lang)) // Hack sort of fixing previous implementation
						lang = "en";
				} catch (Exception ex) {
					lang = "";
				}

				// Attention on initialise la ligne file
				// avec l'UUID d'origine de l'asmContext parent
				// Il sera mis e jour avec l'UUID asmContext final dans writeNode
				try {
					String resolved = resolve.get(uuid); /// New uuid
					String sessionval = Tools.passwdGen(24, "RANDOM");
					// session.getId()
					// FIX ... there is no session id in RESTFUL webServices so generate a mocked
					// one in place
					File file = new File(fullPath);
					String backend = configurationDao.get("backendserver");

					if (resolved != null) {
						/// Have to send it in FORM, compatibility with regular file posting
						fileManager.rewriteFile(sessionval, backend, userName, resolved, lang, file);
					}
				} catch (Exception ex) {
					// Le nom du fichier ne commence pas par un UUID,
					// ce n'est donc pas une ressource
					ex.printStackTrace();
				}
			}

		File zipfile = new File(filename);
		zipfile.delete();
		File zipdir = new File(outsideDir + portfolioUuidPreliminaire + File.separator);
		zipdir.delete();

		return portfolio.getId().toString();
	}

	public Object postInstanciatePortfolio(MimeType mimeType, String portfolioId, String srccode, String tgtcode,
			Long id, int groupId, boolean copyshared, String groupname, boolean setOwner) {
		// TODO Auto-generated method stub
		return null;
	}

	public int addPortfolioInGroup(String portfolioUuid, Long portfolioGroupId, String label, Long userId) {
		try {
			PortfolioGroup pg = portfolioGroupDao.findById(portfolioGroupId);
			Portfolio p = null;
			if (label != null) {
				pg.setLabel(label);
				pg = portfolioGroupDao.merge(pg);
			} else {
				if (!StringUtils.equalsIgnoreCase(pg.getType(), "PORTFOLIO"))
					return 1;

				p = portfolioDao.findById(UUID.fromString(portfolioUuid));

				PortfolioGroupMembers pgm = new PortfolioGroupMembers(new PortfolioGroupMembersId());
				pgm.setPortfolio(p);
				pgm.setPortfolioGroup(pg);
				portfolioGroupMembersDao.persist(pgm);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	public Long getPortfolioGroupIdFromLabel(String groupLabel, Long userId) {
		return portfolioGroupDao.getPortfolioGroupIdFromLabel(groupLabel);
	}

	public String getPortfolioGroupList() {
		class TreeNode {
			String nodeContent;
			int nodeId;
			List<TreeNode> childs = new ArrayList<TreeNode>();
		}
		class ProcessTree {
			public void reconstruct(StringBuilder data, TreeNode tree) {
				String nodeData = tree.nodeContent;
				data.append(nodeData); // Add current node content
				for (int i = 0; i < tree.childs.size(); ++i) {
					TreeNode child = tree.childs.get(i);
					reconstruct(data, child);
				}
				// Close node tag
				data.append("</group>");
			}
		}

		StringBuilder result = new StringBuilder();
		result.append("<groups>");
		try {
			List<PortfolioGroup> pgList = portfolioGroupDao.findAll();

			List<TreeNode> trees = new ArrayList<TreeNode>();
			Map<Long, TreeNode> resolve = new HashMap<Long, TreeNode>();
			ProcessTree pf = new ProcessTree();

			StringBuilder currNode = new StringBuilder();
			for (PortfolioGroup pg : pgList) {
				currNode.setLength(0);
				String pgStr = String.valueOf(pg.getId());
				String type = pg.getType();
				currNode.append("<group type='").append(type.toLowerCase()).append("' id=\"");
				currNode.append(pgStr);
				currNode.append("\"><label>");
				currNode.append(pg.getLabel());
				currNode.append("</label>");
				// group tag will be closed at reconstruction

				TreeNode currTreeNode = new TreeNode();
				currTreeNode.nodeContent = currNode.toString();
				currTreeNode.nodeId = Integer.parseInt(pgStr);
				PortfolioGroup parent = pg.getParent().getParent();
				resolve.put(Long.valueOf(currTreeNode.nodeId), currTreeNode);
				if (parent != null && !PhpUtil.empty(parent.getId())) {
					TreeNode parentTreeNode = resolve.get(parent.getId());
					parentTreeNode.childs.add(currTreeNode);
				} else // Top level groups
				{
					trees.add(currTreeNode);
				}
			}

			/// Go through top level parent and reconstruct each tree
			for (int i = 0; i < trees.size(); ++i) {
				TreeNode topNode = trees.get(i);
				pf.reconstruct(result, topNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		result.append("</groups>");

		return result.toString();
	}

	public String getPortfolioGroupListFromPortfolio(String portfolioUuid) {
		List<PortfolioGroupMembers> pgmList = portfolioGroupMembersDao.getByPortfolioID(portfolioUuid);
		final StringBuilder result = new StringBuilder();

		for (PortfolioGroupMembers pgm : pgmList) {
			if (pgm.getPortfolioGroup() != null && PhpUtil.empty(pgm.getPortfolioGroup().getId())) {
				result.append("<group");
				result.append(" id=\"");
				result.append(pgm.getPortfolioGroup().getId());
				result.append("\">");
				result.append(pgm.getPortfolioGroup().getLabel());
				result.append("</group>");
			}
		}
		result.append("</portfolio>");
		return result.toString();
	}

	public Long createPortfolioGroup(String groupname, String type, Long parentId, Long userId) {
		Long groupid = -1L;
		boolean isOK = true;
		try {
			// Check if parent exists
			if (parentId != null && !portfolioGroupDao.exists(parentId, "GROUP")) {
				isOK = false;
			}

			if (isOK) {
				PortfolioGroup pg = new PortfolioGroup();
				pg.setLabel(groupname);
				pg.setType(type);
				if (parentId != null)
					pg.setParent(new PortfolioGroup(parentId));

				portfolioGroupDao.persist(pg);
				groupid = pg.getId();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return groupid;

	}

	public String findRoleByPortfolio(MimeType mimeType, String role, String portfolioId, Long userId) {
		GroupRightInfo gri = groupRightInfoDao.getByPortfolioAndLabel(portfolioId, role);
		Long grid = null;
		if (gri != null) {
			grid = gri.getId();
		} else {
			return "Le grid n'existe pas";
		}
		return "grid = " + grid;
	}

	public String findRolesByPortfolio(String portfolioUuid, Long userId) {
		GroupRights rights = getRightsOnPortfolio(userId, 0L, portfolioUuid);
		if (!rights.isRead())
			return null;

		List<GroupInfo> giList = groupInfoDao.getByPortfolio(portfolioUuid);

		String result = "<groups>";
		for (GroupInfo gi : giList) {
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", gi.getId().toString()) + " ";
			result += DomUtils.getXmlAttributeOutput("templateId", gi.getGroupRightInfo().getId().toString()) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("groupid", gi.getId().toString()) + " ";
			result += DomUtils.getXmlElementOutput("groupname", gi.getLabel());
			result += DomUtils.getXmlElementOutput("roleid", gi.getGroupRightInfo().getId().toString()) + " ";
			result += DomUtils.getXmlElementOutput("rolename", gi.getGroupRightInfo().getLabel()) + " ";
			result += "</group>";
		}
		result += "</groups>";

		return result;
	}

	public String getGroupRightsInfos(Long userId, String portfolioId) throws BusinessException {
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : no admin right");

		List<GroupRightInfo> resList = groupRightInfoDao.getByPortfolioAndUser(UUID.fromString(portfolioId), userId);
		String result = "<groupRightsInfos>";
		for (GroupRightInfo res : resList) {
			result += "<groupRightInfo ";
			result += DomUtils.getXmlAttributeOutput("grid", res.getId().toString()) + " ";
			result += ">";
			result += "<label>" + res.getLabel() + "</label>";
			result += "<owner>" + String.valueOf(res.getOwner()) + "</owner>";
			result += "</groupRightInfo>";
		}

		result += "</groupRightsInfos>";
		return result;
	}

	public String addRoleInPortfolio(Long userId, String portfolio, String data) throws BusinessException {
		if (!credentialDao.isAdmin(userId) && !portfolioDao.isOwner(userId, portfolio))
			throw new GenericBusinessException("FORBIDDEN 403 : No admin right");

		String value = "erreur";
		/// Parse data
		DocumentBuilder documentBuilder;
		Document document = null;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(data));
			document = documentBuilder.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/// Probleme de parsage
		if (document == null)
			return value;

		try {
			Element labelNode = document.getDocumentElement();
			String label = null;

			if (labelNode != null) {
				org.w3c.dom.Node labelText = labelNode.getFirstChild();
				if (labelText != null)
					label = labelText.getNodeValue();
			}

			if (label == null)
				return value;
			/// Creation du groupe de droit

			Long grid = 0L;
			final GroupRightInfo gri = new GroupRightInfo();
			gri.setOwner(userId);
			gri.setLabel(label);
			gri.setPortfolio(new Portfolio(UUID.fromString(portfolio)));

			try {
				groupRightInfoDao.persist(gri);
				grid = gri.getId();
			} catch (Exception e) {
			}

			labelNode.setAttribute("id", Long.toString(grid));

			/// Recupere les donnees avec identifiant mis-a-jour
			StringWriter stw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult stream = new StreamResult(stw);
			serializer.transform(source, stream);
			value = stw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return value;
	}

	public boolean isOwner(Long userId, UUID pid) {
		return portfolioDao.isOwner(userId, pid);
	}

	public String copyPortfolio(MimeType inMimeType, String portfolioUuid, String srcCode, String newCode, Long userId,
			boolean setOwner) throws Exception {
		Portfolio originalPortfolio = null;
		String newPortfolioUuid = null;
		try {
			/// source code is OK ?
			if (srcCode != null) {
				/// Find back portfolio from source code
				originalPortfolio = portfolioDao.getPortfolioFromNodeCode(srcCode);
				if (originalPortfolio != null)
					// Portfolio uuid
					portfolioUuid = originalPortfolio.getId().toString();
			}

			if (portfolioUuid == null)
				return "Error: no portofolio selected";

			//////////////////////////////
			/// Copie de la structure ///
			/////////////////////////////
			final Map<Node, Node> nodes = new HashMap<Node, Node>();

			// Recupération des noeuds du portfolio a copier
			final List<Node> originalNodeList = nodeDao.getNodes(portfolioUuid);
			final List<Node> copiedNodeList = new ArrayList<Node>(originalNodeList.size());
			Node copy = null;
			Node rootNodeCopy = null;
			/// Copie des noeuds -- structure du portfolio
			for (Node original : originalNodeList) {
				copy = new Node(original);
				if (setOwner) {
					copy.setModifUserId(userId);
				} else {
					copy.setModifUserId(1L); // FIXME hard-coded root userid
				}

				//////////////////////////////
				/// Copie des ressources///
				/////////////////////////////
				if (copy.getResource() != null) {
					if (setOwner) {
						copy.getResource().setModifUserId(userId);
					} else {
						copy.getResource().setModifUserId(1L);
					}
					resourceTableDao.persist(copy.getResource());
				}
				if (copy.getResResource() != null) {
					// Mise a jour du code dans le contenu du noeud
					if (StringUtils.equalsIgnoreCase(copy.getAsmType(), "asmRoot")) {
						copy.getResResource().setContent(
								StringUtils.replace(copy.getResResource().getContent(), copy.getCode(), newCode));

					}
					if (setOwner) {
						copy.getResResource().setModifUserId(userId);
					} else {
						copy.getResResource().setModifUserId(1L);
					}
					resourceTableDao.persist(copy.getResResource());
				}
				if (copy.getContextResource() != null) {
					if (setOwner) {
						copy.getContextResource().setModifUserId(userId);
					} else {
						copy.getContextResource().setModifUserId(1L);
					}
					resourceTableDao.persist(copy.getContextResource());
				}

				// Mise a jour du code dans le code interne de la BD
				if (StringUtils.equalsIgnoreCase(copy.getAsmType(), "asmRoot")) {
					copy.setCode(newCode);
					rootNodeCopy = copy;
				}

				nodeDao.persist(copy);
				copiedNodeList.add(copy);
				nodes.put(original, copy);
			}

			/// Ajout du portfolio dans la table
			Portfolio portfolioCopy = new Portfolio(originalPortfolio);
			portfolioCopy.setRootNode(rootNodeCopy);
			portfolioDao.persist(portfolioCopy);
			newPortfolioUuid = portfolioCopy.getId().toString();

			Entry<Node, Node> entry = null;
			Node key = null;
			Node value = null;
			Node searchedNode = new Node();
			for (Iterator<Entry<Node, Node>> it = nodes.entrySet().iterator(); it.hasNext();) {
				entry = it.next();
				key = entry.getKey();
				value = entry.getValue();
				/// Assignation des nouveaux parents
				if (key.getParentNode() != null) {
					value.setParentNode(nodes.get(key.getParentNode()));
				}
				/// Mise a jour de la liste des enfants
				/// L'ordre determine le rendu visuel final du xml
				String[] children = StringUtils.split(key.getChildrenStr(), ",");
				String[] childrenCopies = new String[children.length];
				for (int i = 0; i < children.length; i++) {
					searchedNode.setId(UUID.fromString(children[i]));
					copy = nodes.get(searchedNode);
					childrenCopies[i] = copy.getId().toString();
				}
				value.setChildrenStr(StringUtils.join(childrenCopies, ","));
				/// Liaison des noeuds copiés au nv portfolio
				value.setPortfolio(portfolioCopy);
				nodeDao.merge(value);
			}

			/// Finalement on cree un role designer
			Long groupid = securityManager.createRole(newPortfolioUuid, "designer", userId);

			/// Ajoute la personne dans ce groupe
			groupUserDao.addUserInGroup(groupid, userId);

			/// Force 'all' role creation
			groupid = securityManager.createRole(newPortfolioUuid, "all", userId);

			/// Check base portfolio's public state and act accordingly
			if (portfolioDao.isPublic(portfolioUuid))
				groupManager.setPublicState(userId, newPortfolioUuid, true);

		} catch (Exception e) {
			newPortfolioUuid = "erreur: " + e.getMessage();
		}
		return newPortfolioUuid;
	}

}
