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
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimeTypeParseException;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
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

import eportfolium.com.karuta.business.contract.*;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.model.bean.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.util.JavaTimeUtil;

@Service
@Transactional
public class PortfolioManagerImpl extends BaseManager implements PortfolioManager {

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioGroupRepository portfolioGroupRepository;

	@Autowired
	private PortfolioGroupMembersRepository portfolioGroupMembersRepository;

	@Autowired
	private NodeRepository nodeRepository;

	@Autowired
	private CredentialRepository credentialRepository;

	@Autowired
	private GroupRightsRepository groupRightsRepository;

	@Autowired
	private GroupRightInfoRepository groupRightInfoRepository;

	@Autowired
	private GroupInfoRepository groupInfoRepository;

	@Autowired
	private ResourceTableRepository resourceTableRepository;

	@Autowired
	private GroupUserRepository groupUserRepository;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private FileManager fileManager;

	/// temp class

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

		Map<String, right> rights = new HashMap<String, right>();
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

		Map<String, groupright> resolve = new HashMap<String, groupright>();
		Map<String, Long> groups = new HashMap<String, Long>();
	}

	static private final Logger logger = LoggerFactory.getLogger(PortfolioManagerImpl.class);

	@Override
	public boolean changePortfolioDate(UUID nodeId, UUID portfolioId) {
		final Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.date_default_timezone));

		Portfolio portfolio = null;

		if (nodeId != null) {
			UUID portfolioUUID = nodeRepository.getPortfolioIdFromNode(nodeId);
			portfolio = portfolioRepository.findById(portfolioUUID).get();
			portfolio.setModifDate(now);
		} else if (portfolioId != null) {
			portfolio = portfolioRepository.findById(portfolioId).get();
			portfolio.setModifDate(now);
		}

		portfolioRepository.save(portfolio);

		return true;
	}

	public boolean removePortfolioGroups(Long portfolioGroupId) {
		Optional<PortfolioGroup> pg = portfolioGroupRepository.findById(portfolioGroupId);

		if (pg.isPresent()) {
			List<PortfolioGroupMembers> groupMembers = portfolioGroupMembersRepository.getByPortfolioGroupID(portfolioGroupId);
			portfolioGroupMembersRepository.deleteAll(groupMembers);

			portfolioGroupRepository.delete(pg.get());

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean removePortfolioFromPortfolioGroups(UUID portfolioId, Long portfolioGroupId) {
		boolean result = false;

		try {
			PortfolioGroupMembersId pgmID = new PortfolioGroupMembersId();

			pgmID.setPortfolio(new Portfolio(portfolioId));
			pgmID.setPortfolioGroup(new PortfolioGroup(portfolioGroupId));

			portfolioGroupMembersRepository.deleteById(pgmID);

			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public String getPortfoliosByPortfolioGroup(Long portfolioGroupId) {
		StringBuilder result = new StringBuilder();
		result.append("<group id=\"").append(portfolioGroupId).append("\">");

		List<Portfolio> portfolios = portfolioRepository.findByPortfolioGroup(portfolioGroupId);

		for (Portfolio portfolio : portfolios) {
			result.append("<portfolio");
			result.append(" id=\"");
			result.append(portfolio.getId().toString());
			result.append("\"");
			result.append(">");
			result.append("</portfolio>");
		}
		result.append("</group>");
		return result.toString();
	}

	@Override
	public int changePortfolioActive(UUID portfolioId, Boolean active) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId).get();
		portfolio.setActive(BooleanUtils.toInteger(active));
		portfolioRepository.save(portfolio);

		return 0;
	}

	@Override
	public String getPortfolio(MimeType outMimeType, UUID portfolioId, Long userId, Long groupId, String label,
			String resource, String files, long substid, Integer cutoff)
			throws DoesNotExistException, BusinessException, Exception {

		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);
		String header = "";
		String footer = "";

		GroupRights rights = getRightsOnPortfolio(userId, groupId, portfolioId);

		if (!rights.isRead()) {
			userId = credentialRepository.getPublicId();
			/// Vérifie les droits avec le compte publique (dernière chance)
			GroupRights publicRights = groupRightsRepository.getPublicRightsByUserId(rootNode.getId(), userId);
			if (!publicRights.isRead()) {
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
			}
		}

		if (outMimeType.getSubtype().equals("xml")) {
			Long ownerId = portfolioRepository.getOwner(portfolioId);
			boolean isOwner = ownerId == userId;

			String headerXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><portfolio code=\"0\" id=\"" + portfolioId
					+ "\" owner=\"" + isOwner + "\"><version>4</version>";

			String data = getLinearXml(portfolioId, rootNode.getId().toString(), null, true, null, userId,
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
				.getNode(outMimeType, rootNode.getId(), true, userId, groupId, label, cutoff)
				+ footer;
	}

	private String getLinearXml(UUID portfolioId, String rootuuid, Node portfolio, boolean withChildren,
			String withChildrenOfXsiType, Long userId, Long groupId, String role, Integer cutoff)
			throws SQLException, SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
		DocumentBuilder parse = newInstance.newDocumentBuilder();

		List<Pair<Node, GroupRights>> portfolioStructure = getPortfolioStructure(portfolioId, userId, groupId,
				cutoff);

		Map<String, Object[]> resolve = new HashMap<String, Object[]>();
		/// Node -> parent
		Map<String, t_tree> entries = new HashMap<String, t_tree>();

		processQuery(portfolioStructure, resolve, entries, null, parse, role);

		portfolioStructure = getSharedStructure(portfolioId, userId, groupId, cutoff);

		if (portfolioStructure != null) {
			processQuery(portfolioStructure, resolve, entries, null, parse, role);
		}

		/// Reconstruct functional tree
		t_tree root = entries.get(rootuuid);
		StringBuilder out = new StringBuilder(256);
		if (root != null)
			reconstructTree(out, root, entries);

		return out.toString();
	}

	private List<Pair<Node, GroupRights>> getPortfolioStructure(UUID portfolioId, Long userId, Long groupId,
			Integer cutoff) {

		List<Pair<Node, GroupRights>> portfolioStructure = new ArrayList<Pair<Node, GroupRights>>();

		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);
		GroupRights rights = null;

		// Cas admin, designer, owner
		if (rootNode != null
				&& (credentialRepository.isAdmin(userId)
						|| credentialRepository.isDesigner(userId, rootNode.getId())
						|| userId == portfolioRepository.getOwner(portfolioId))) {
			List<Node> nodes = nodeRepository.getNodesWithResources(portfolioId);
			for (Node node : nodes) {
				rights = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true, true, true, true, true);
				portfolioStructure.add(Pair.of(node, rights));
			}
		}
		/// FIXME: Il faudrait peut-être prendre une autre stratégie pour sélectionner
		/// les bonnes données : Cas propriétaire OU cas general (via les droits
		/// partagés)
		else if (hasRights(userId, portfolioId)) {
			Map<UUID, GroupRights> t_rights_22 = new HashMap<>();

			String login = credentialRepository.getLoginById(userId);
//				FIXME: Devrait peut-être verifier si la personne a les droits d'y accéder?
			List<GroupRights> grList = groupRightsRepository.getPortfolioAndUserRights(portfolioId, login,
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

			List<Node> nodes = nodeRepository.getNodes(new ArrayList<>(t_rights_22.keySet()));

			// Sélectionne les données selon la filtration
			for (Node node : nodes) {
				if (t_rights_22.containsKey(node.getId())) { // Verification des droits
					rights = t_rights_22.get(node.getId());
					if (rights.isRead()) { // On doit au moins avoir le droit de lecture
						portfolioStructure.add(Pair.of(node, rights));
					}
				}
			}

		} else if (portfolioRepository.isPublic(portfolioId)) // Public case, looks like previous query, but with
		{
			List<Node> nodes = nodeRepository.getNodesWithResources(portfolioId);
			for (Node node : nodes) {
				rights = new GroupRights(true, false, false, false);
				portfolioStructure.add(Pair.of(node, rights));
			}
		}

		return portfolioStructure;
	}

	/**
	 * Récupère les noeuds partages d'un portfolio. C'est séparé car les noeuds ne
	 * provenant pas d'un même portfolio, on ne peut pas les sélectionner rapidement
	 * Autre possibilité serait de garder ce même type de fonctionnement pour une
	 * selection par niveau d'un portfolio.<br>
	 * TODO: A faire un 'benchmark' dessus
	 * 
	 * @param portfolioId
	 * @param userId
	 * @param groupId
	 * @param cutoff
	 * @return
	 */
	private List<Pair<Node, GroupRights>> getSharedStructure(UUID portfolioId, Long userId, Long groupId,
			Integer cutoff) {

		List<Pair<Node, GroupRights>> portfolioStructure = new ArrayList<Pair<Node, GroupRights>>();

		if (portfolioRepository.hasSharedNodes(portfolioId)) {
			List<Node> t_nodes = nodeRepository.getSharedNodes(portfolioId);

			Map<Integer, Set<UUID>> t_map_parentid = new HashMap<>();
			Set<UUID> t_set_parentid = new HashSet<>();

			for (Node t_node : t_nodes) {
				t_set_parentid.add(t_node.getSharedNodeUuid());
			}

			t_map_parentid.put(0, t_set_parentid);

			/// Les tours de boucle seront toujours <= au nombre de noeud du portfolio.
			int level = 0;
			boolean added = true;
			Set<UUID> t_struc_parentid_2 = null;
			while (added && (cutoff == null ? true : level < cutoff)) {
				t_struc_parentid_2 = new HashSet<>();

				for (Node t_node : t_nodes) {
					for (UUID t_parent_node : t_map_parentid.get(level)) {
						if (t_node.getPortfolio().getId().equals(portfolioId)
								&& t_node.getParentNode().getId().equals(t_parent_node)) {
							t_struc_parentid_2.add(t_node.getId());
							break;
						}
					}
				}
				t_map_parentid.put(level + 1, t_struc_parentid_2);
				t_set_parentid.addAll(t_struc_parentid_2);
				added = CollectionUtils.isNotEmpty(t_struc_parentid_2); // On s'arrete quand rien n'a été ajouté
				level = level + 1; // Prochaine étape
			}

			List<Node> nodes = nodeRepository.getNodes(new ArrayList<>(t_set_parentid));
			GroupRights rights = null;
			for (Node node : nodes) {
				rights = groupRightsRepository.getRightsByIdAndUser(node.getId(), userId);

				if (rights != null && rights.isRead()) { // On doit au moins avoir le droit de lecture
					portfolioStructure.add(Pair.of(node, rights));
				}
			}
		}
		return portfolioStructure;
	}

	public String getPortfolioShared(Long userId) {
		StringBuilder out = new StringBuilder();

		List<Map<String, Object>> portfolios = portfolioRepository.getPortfolioShared(userId);
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
		Portfolio portfolio = portfolioRepository.getPortfolioFromNodeCode(portfolioCode);

		if (portfolio == null) {
			throw new DoesNotExistException(Portfolio.class, portfolioCode);
		}

		boolean withResources = BooleanUtils.toBoolean(resources);
		String result = "";

		if (withResources) {
			try {
				return getPortfolio(mimeType, portfolio.getId(), userId, groupId, null, null, null, substid,
						null);
			} catch (MimeTypeParseException e) {
				e.printStackTrace();
			}
		} else {
			result += "<portfolio ";
			result += DomUtils.getXmlAttributeOutput("id", portfolio.getId().toString()) + " ";
			result += DomUtils.getXmlAttributeOutput("root_node_id", portfolio.getRootNode().getId().toString()) + " ";
			result += ">";
			result += nodeManager.getNodeXmlOutput(portfolio.getRootNode().getId(), false, "nodeRes", userId,
					groupId, null, false);
			result += "</portfolio>";

		}
		return result;
	}

	public GroupRights getRightsOnPortfolio(Long userId, Long groupId, UUID portfolioId) {
		GroupRights reponse = new GroupRights();

		try {
			/// modif_user_id => current owner
			Optional<Portfolio> p = portfolioRepository.findById(portfolioId);

			if (p.isPresent()) {
				if (p.get().getModifUserId() == userId)
					// Is the owner
					reponse = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true, true, true, true,
							true);
				else // General case
					reponse = nodeManager.getRights(userId, groupId, p.get().getRootNode().getId());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return reponse;
	}

	@Override
	public boolean hasRights(Long userId, UUID portfolioId) {
		boolean hasRights = false;

		if (userId != null && portfolioId != null) {
			// Évaluer l'appartenance
			Long modif_user_id = portfolioRepository.getOwner(portfolioId);

			if (Objects.equals(modif_user_id, userId)) {
				hasRights = true;
			} else // Vérifier les autres droits partagés
			{
				List<GroupUser> gu = groupUserRepository.getByPortfolioAndUser(portfolioId, userId);
				if (CollectionUtils.isNotEmpty(gu)) {
					hasRights = true;
				}
			}
		}
		return hasRights;
	}

	@Override
	public void removePortfolio(UUID portfolioId, Long userId, Long groupId) throws Exception {
		boolean hasRights = false;

		GroupRights rights = getRightsOnPortfolio(userId, groupId, portfolioId);
		if (rights.isDelete() || credentialRepository.isAdmin(userId)) {
			hasRights = true;
		}

		if (hasRights) {

			// S'il y a quelque chose de particulier, on s'assure que tout soit bien nettoyé
			// de façon séparée
			groupRightInfoRepository.deleteAll(groupRightInfoRepository.getByPortfolioID(portfolioId));

			/// Resources
			List<ResourceTable> rtList = resourceTableRepository.getResourcesByPortfolioUUID(portfolioId);
			resourceTableRepository.deleteAll(rtList);

			rtList = resourceTableRepository.getContextResourcesByPortfolioUUID(portfolioId);
			resourceTableRepository.deleteAll(rtList);

			rtList = resourceTableRepository.getResourcesOfResourceByPortfolioUUID(portfolioId);
			resourceTableRepository.deleteAll(rtList);

			/// Nodes
			nodeRepository.deleteAll(nodeRepository.getNodes(portfolioId));

			/// Supprimer le portfolio du groupe.
			List<PortfolioGroupMembers> groupMembers = portfolioGroupMembersRepository.getByPortfolioID(portfolioId);
			portfolioGroupMembersRepository.deleteAll(groupMembers);

			// Portfolio
			portfolioRepository.deleteById(portfolioId);
		}
	}

	@Override
	public boolean isOwner(Long userId, UUID portfolioId) {
		return portfolioRepository.isOwner(portfolioId, userId);
	}

	@Override
	public boolean changePortfolioOwner(UUID portfolioId, long newOwner) {
		Optional<Portfolio> portfolio = portfolioRepository.findById(portfolioId);

		if (portfolio.isPresent()) {
			Portfolio p = portfolio.get();

			Node rootNode = nodeRepository.findById(p.getRootNode().getId()).get();

			p.setModifUserId(newOwner);
			rootNode.setModifUserId(newOwner);

			portfolioRepository.save(p);
			nodeRepository.save(rootNode);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public Portfolio changePortfolioConfiguration(UUID portfolioId, Boolean portfolioActive, Long userId)
			throws BusinessException {
		if (!credentialRepository.isAdmin(userId)) {
			throw new GenericBusinessException("No admin right");
		}

		Optional<Portfolio> portfolio = portfolioRepository.findById(portfolioId);

		if (portfolio.isPresent()) {
			Portfolio p = portfolio.get();
			p.setActive(BooleanUtils.toInteger(portfolioActive));

			portfolioRepository.save(p);

			return p;
		} else {
			return null;
		}
	}

	public String getPortfolios(MimeType outMimeType, long userId, long groupId, Boolean portfolioActive,
			long substid, Boolean portfolioProject, String projectId, Boolean countOnly, String search) {
		StringBuilder result = new StringBuilder();
		List<Portfolio> portfolios = getPortfolios(userId, substid, portfolioActive, portfolioProject);
		result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><portfolios count=\""+portfolios.size()+"\">");
		for( Portfolio p : portfolios ) {
			Node n = p.getRootNode();
			ResourceTable rctx = n.getContextResource();
			ResourceTable rnode = n.getResource();
			ResourceTable rcontent = n.getResResource();
			
			String isOwner = "N";
			String ownerId = n.getModifUserId().toString();
			if( Integer.parseInt(ownerId) == userId )
				isOwner = "Y";
			
			result.append("<portfolio id=\"").append(p.getId().toString());
			result.append("\" root_node_id=\"").append(p.getRootNode().getId().toString());
			result.append("\" owner=\"").append(isOwner);
			result.append("\" ownerid=\"").append(ownerId);
			result.append("\" modified=\"").append(p.getModifDate().toString()).append("\">");
			
				String nodetype = n.getAsmType().toString();
				result.append("<").append(nodetype).append(" id=\"").append(n.getId().toString()).append("\">");
				
				if(!"asmResource".equals(nodetype))
				{
					String metawad = n.getMetadataWad();
					if(metawad!=null && !"".equals(metawad) )
					{
						result.append("<metadata-wad ").append(metawad).append("/>");
					}
					else
						result.append("<metadata-wad/>");
					
					String metaepm = n.getMetadataEpm();
					if(metaepm!=null && !"".equals(metaepm) )
						result.append("<metadata-epm "+metaepm+"/>");
					else
						result.append("<metadata-epm/>");
					
					String meta = n.getMetadata();
					if(meta!=null && !"".equals(meta))
						result.append("<metadata "+meta+"/>");
					else
						result.append("<metadata/>");
					
					String code = n.getCode();
					if(meta!=null && !"".equals(meta))
						result.append("<code>").append(code).append("</code>");
					else
						result.append("<code/>");
					
					String label = n.getLabel();
					if(label!=null && !"".equals(label))
						result.append("<label>").append(label).append("</label>");
					else
						result.append("<label/>");
						
					String descr = n.getDescr();
					if(descr!=null && !"".equals(descr))
						result.append("<description>").append(descr).append("</description>");
					else
						result.append("<description/>");
					
					String semantic = n.getSemantictag();
					if(semantic!=null && !"".equals(semantic))
						result.append("<semanticTag>").append(semantic).append("</semanticTag>");
					else
						result.append("<semanticTag/>");
				
				String nodeUuid = n.getId().toString();
				if( rcontent != null )
				{
					String resresuuid = rcontent.getId().toString();
					if( rcontent != null && resresuuid != null && !"".equals(resresuuid) )
					{
						String xsitype =  rcontent.getXsiType();
						result.append("<asmResource id='").append(resresuuid).append("' contextid='").append(nodeUuid).append("' xsi_type='").append(xsitype).append("'>");
						String resrescont = rcontent.getContent();
						if( resrescont != null && !"".equals(resrescont) )
							result.append(resrescont);
						result.append("</asmResource>");
					}
				}
				
				if( rctx != null )
				{
					String rescontuuid =rctx.getId().toString();
					if( rescontuuid != null && !"".equals(rescontuuid) )
					{
						String xsitype = rctx.getXsiType();
						result.append("<asmResource id='").append(rescontuuid).append("' contextid='").append(nodeUuid).append("' xsi_type='").append(xsitype).append("'>");
						String resrescont = rctx.getContent();
						if( resrescont != null && !"".equals(resrescont) )
							result.append(resrescont);
						result.append("</asmResource>");
					}
				}
				
				if( rnode != null )
				{
					String resnodeuuid = rnode.getId().toString();
					if( resnodeuuid != null && !"".equals(resnodeuuid) )
					{
						String xsitype = rnode.getXsiType().toString();
						result.append("<asmResource id='").append(resnodeuuid).append("' contextid='").append(nodeUuid).append("' xsi_type='").append(xsitype).append("'>");
						String resrescont = rnode.getContent();
						if( resrescont != null && !"".equals(resrescont) )
							result.append(resrescont);
						result.append("</asmResource>");
					}
				}
				result.append("</"+nodetype+">");
				result.append("</portfolio>");
			}

		}
		result.append("</portfolios>");
		return result.toString();
	}

	@Override
	public boolean rewritePortfolioContent(MimeType inMimeType, MimeType outMimeType, String xmlPortfolio,
			UUID portfolioId, Long userId, Boolean portfolioActive) throws Exception {
		StringBuffer outTrace = new StringBuffer();
		UUID portfolioModelId = null;

		Portfolio resPortfolio = portfolioRepository.findById(portfolioId).get();

		if (resPortfolio != null) {
			// Le portfolio existe donc on regarde si modèle ou pas
			if (resPortfolio.getModelId() != null) {
				portfolioModelId = resPortfolio.getModelId();
			}
		}

		if (userId == null || userId == 0L) {
			if (resPortfolio != null)
				userId = resPortfolio.getCredential().getId();
		}

		if (xmlPortfolio.length() > 0) {
			Document doc = DomUtils.xmlString2Document(xmlPortfolio, outTrace);

			org.w3c.dom.Node rootNode = (doc.getElementsByTagName("portfolio")).item(0);
			if (rootNode == null) {
				throw new GenericBusinessException("Root Node (portfolio) not found !");
			} else {
				rootNode = (doc.getElementsByTagName("asmRoot")).item(0);

				UUID rootNodeUuid = UUID.randomUUID();
				org.w3c.dom.Node idAtt = rootNode.getAttributes().getNamedItem("id");
				if (idAtt != null) {
					String tempId = idAtt.getNodeValue();
					if (tempId.length() > 0) {
						rootNodeUuid = UUID.fromString(tempId);
					}
				}

				if (resPortfolio != null) {
					resPortfolio = portfolioRepository.save(resPortfolio);
				} else {
					resPortfolio = add(rootNodeUuid, null, userId, new Portfolio());
				}

				nodeManager.writeNode(rootNode, resPortfolio.getId(), portfolioModelId, userId, 0, null, null, false, false,
						true, null, false);

				// On récupère le noeud root généré précédemment et on l'affecte au portfolio.
				resPortfolio.setRootNode(nodeRepository.getRootNodeByPortfolio(resPortfolio.getId()));
				resPortfolio = portfolioRepository.save(resPortfolio);
			}
		}
		if (resPortfolio != null) {
			resPortfolio.setActive(BooleanUtils.toInteger(portfolioActive));
			portfolioRepository.save(resPortfolio);
		}
		return true;
	}

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

	@Override
	public UUID postPortfolioParserights(UUID portfolioId, Long userId) {

		if (!credentialRepository.isAdmin(userId) && !credentialRepository.isCreator(userId))
			return null;

		boolean setPublic = false;

		try {

			resolver resolve = new resolver();

			// Sélection des méta-données
			List<Node> nodes = nodeRepository.getNodes(portfolioId);
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

					if (portfolioRepository.isPublic(portfolioId))
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

			/// On insère les données pré-compile
			Iterator<String> entries = resolve.groups.keySet().iterator();

			// Crée les groupes, ils n'existent pas

			while (entries.hasNext()) {
				GroupRightInfo gri = new GroupRightInfo();
				String label = entries.next();
				gri.setOwner(1L);
				gri.setLabel(label);
				gri.setChangeRights(false);
				gri.setPortfolio(new Portfolio(portfolioId));

				groupRightInfoRepository.save(gri);
				Long grid = gri.getId();
				resolve.groups.put(label, grid);

				groupInfoRepository.save(new GroupInfo(grid, 1L, label));
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

					groupRightsRepository.save(groupRights);
				}
			}

			// Créer le groupe de base
			securityManager.addRole(portfolioId, "all", userId);

			/// Finalement on crée un role designer
			Long groupid = securityManager.addRole(portfolioId, "designer", userId);

			GroupUser gu = new GroupUser(
					new GroupUserId(new GroupInfo(groupid), new Credential(userId)));

			groupUserRepository.save(gu);

			updateTime(portfolioId);

			// Rendre le portfolio public si nécessaire
			if (setPublic)
				groupManager.setPublicState(userId, portfolioId, setPublic);
		} catch (Exception e) {
			logger.error("MESSAGE: " + e.getMessage() + " " + e.getLocalizedMessage());
		}

		return portfolioId;
	}

	@Override
	public String addPortfolio(MimeType inMimeType, MimeType outMimeType, String xmlPortfolio, long userId,
			long groupId, UUID portfolioModelId, long substid, boolean parseRights, String projectName)
			throws BusinessException, Exception {
		if (!credentialRepository.isAdmin(userId) && !credentialRepository.isCreator(userId))
			throw new GenericBusinessException("FORBIDDEN : No admin right");

		StringBuffer outTrace = new StringBuffer();

		// Si le modèle est renseigné, on ignore le XML poste et on récupère le contenu
		// du modèle a la place
		// FIXME Inutilisé, nous instancions / copions un portfolio
		if (portfolioModelId != null)
			xmlPortfolio = getPortfolio(inMimeType, portfolioModelId, userId, groupId, null, null, null, substid, null);

		Portfolio portfolio = null;
		if (xmlPortfolio.length() > 0) {
			Document doc = DomUtils.xmlString2Document(xmlPortfolio, outTrace);

			// Vérifier si le code est déjà utilisé par un portfolio.
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
				if (nodeRepository.isCodeExist(code)) {
					throw new GenericBusinessException("CONFLICT : Existing code.");
				}
				nodelist.item(0).setTextContent(code);
			}

			org.w3c.dom.Node rootNode = (doc.getElementsByTagName("portfolio")).item(0);
			if (rootNode == null) {
				throw new Exception("Root Node (portfolio) not found !");
			} else {
				rootNode = (doc.getElementsByTagName("asmRoot")).item(0);

				UUID uuid = UUID.randomUUID();

				portfolio = add(uuid, null, userId, new Portfolio());
				nodeManager.writeNode(rootNode, portfolio.getId(), portfolioModelId, userId, 0, uuid, null,
						false, false, false, null, parseRights);
				// On récupère le noeud root généré précédemment et on l'affecte au portfolio.
				portfolio.setRootNode(nodeRepository.getRootNodeByPortfolio(portfolio.getId()));
			}
		}

		portfolio.setActive(1);
		portfolio.setModelId(portfolioModelId);

		portfolioRepository.save(portfolio);

		/// Si nous instancions, nous n'avons pas besoin du rôle de concepteur
		securityManager.addRole(portfolio.getId(), "all", userId);

		/// Créer groupe 'designer', 'all' est mis avec ce qui est spécifique dans le
		/// XML reçu.
		long roleId = securityManager.addRole(portfolio.getId(), "designer", userId);

		/// Ajoute la personne dans ce groupe
		groupUserRepository.save(new GroupUser(roleId, userId));

		String result = "<portfolios>";
		result += "<portfolio ";
		result += DomUtils.getXmlAttributeOutput("id", portfolio.getId().toString()) + " ";
		result += "/>";
		result += "</portfolios>";
		return result;
	}

	public String importZippedPortfolio(MimeType mimeType, MimeType mimeType2, String path, String userName,
			InputStream inputStream, Long userId, Long groupId, String modelId, Long credentialSubstitutionId,
			boolean parseRights, String projectName) throws BusinessException, FileNotFoundException, Exception {
		if (!credentialRepository.isAdmin(userId) && !credentialRepository.isCreator(userId))
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

		// Création de l'archive au format zip.
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
		// --- Lecture fichier XML ----
		///// Pour associer l'ancien uuid -> nouveau, pour les fichiers
		Map<UUID, UUID> resolve = new HashMap<>();
		Portfolio portfolio = null;
		boolean hasLoaded = false;

		try {
			for (int i = 0; i < xmlFiles.length; i++) {
				String xmlFilepath = xmlFiles[i];
				String xmlFilename = xmlFilepath.substring(xmlFilepath.lastIndexOf(File.separator));
				if (xmlFilename.contains("_"))
					continue; // Case when we add an XML in the portfolio

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
							if (nodeRepository.isCodeExist(code))
								throw new GenericBusinessException("409 Conflict : Existing code.");

							// Replace content
							nodelist.item(0).setTextContent(code);
						} else // Otherwise, check if it exists
						{
							// Simple query
							if (nodeRepository.isCodeExist(code))
								throw new GenericBusinessException("409 Conflict : Existing code.");
						}
					}

					// Check if it needs replacing
					org.w3c.dom.Node rootNode = (doc.getElementsByTagName("portfolio")).item(0);
					if (rootNode == null)
						throw new Exception("Root Node (portfolio) not found !");
					else {
						rootNode = (doc.getElementsByTagName("asmRoot")).item(0);

						UUID uuid = UUID.randomUUID();

						add(uuid, null, userId, portfolio);
						nodeManager.writeNode(rootNode, portfolio.getId(), null, userId, 0, uuid,
								null, false, false, false, resolve, parseRights);
						// On récupère le noeud root généré précédemment et on l'affecte au portfolio.
						portfolio.setRootNode(nodeRepository.getRootNodeByPortfolio(portfolio.getId()));
					}

					portfolio.setActive(1);
					portfolioRepository.save(portfolio);

					/// Create base group
					securityManager.addRole(portfolio.getId(), "all", userId);

					/// Finalement on crée un rôle de designer
					Long groupid = securityManager.addRole(portfolio.getId(), "designer", userId);

					/// Ajoute la personne dans ce groupe
					groupUserRepository.save(new GroupUser(groupid, userId));

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
					UUID resolved = resolve.get(uuid); /// New uuid
					String sessionval = passwdGen(24);
					// session.getId()
					// FIX ... there is no session id in RESTFUL webServices so generate a mocked
					// one in place
					File file = new File(fullPath);
					String backend = configurationManager.get("backendserver");

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

	public String instanciatePortfolio(MimeType mimeType, String portfolioId, String srccode, String tgtcode, Long id,
			int groupId, boolean copyshared, String groupname, boolean setOwner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addPortfolioInGroup(UUID portfolioId, Long portfolioGroupId, String label, Long userId) {
		try {
			PortfolioGroup pg = portfolioGroupRepository.findById(portfolioGroupId).get();
			Portfolio p = null;

			if (label != null) {
				pg.setLabel(label);

				portfolioGroupRepository.save(pg);
			} else {
				if (!StringUtils.equalsIgnoreCase(pg.getType(), "PORTFOLIO"))
					return 1;

				p = portfolioRepository.findById(portfolioId).get();

				PortfolioGroupMembers pgm = new PortfolioGroupMembers(new PortfolioGroupMembersId());
				pgm.setPortfolio(p);
				pgm.setPortfolioGroup(pg);
				portfolioGroupMembersRepository.save(pgm);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	public Long getPortfolioGroupIdFromLabel(String groupLabel, Long userId) {
		Optional<PortfolioGroup> portfolioGroup = portfolioGroupRepository.findByLabel(groupLabel);

		if (portfolioGroup.isPresent()) {
			return portfolioGroup.get().getId();
		} else {
			return -1L;
		}
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
			Iterable<PortfolioGroup> pgList = portfolioGroupRepository.findAll();

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
				PortfolioGroup parent = null;
				if (pg.getParent() != null) {
					parent = pg.getParent().getParent();
				}
				resolve.put(Long.valueOf(currTreeNode.nodeId), currTreeNode);

				if (parent != null && !(parent.getId() == null || parent.getId() == 0)) {
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

	@Override
	public String getPortfolioGroupListFromPortfolio(UUID portfolioId) {
		List<PortfolioGroupMembers> pgmList = portfolioGroupMembersRepository.getByPortfolioID(portfolioId);

		final StringBuilder result = new StringBuilder();
		result.append("<portfolio>");

		for (PortfolioGroupMembers pgm : pgmList) {
			Long portfolioGid = pgm.getPortfolioGroup().getId();

			if (pgm.getPortfolioGroup() != null && !(portfolioGid == null || portfolioGid == 0L)) {
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

	public Long addPortfolioGroup(String groupname, String type, Long parentId, Long userId) {
		Long groupid = -1L;
		boolean isOK = true;
		try {
			// Vérifier si le parent existe.
			if (parentId != null && !portfolioGroupRepository.existsByIdAndType(parentId, "GROUP")) {
				isOK = false;
			}

			if (isOK) {
				PortfolioGroup pg = new PortfolioGroup();
				pg.setLabel(groupname);
				pg.setType(type);
				if (parentId != null)
					pg.setParent(new PortfolioGroup(parentId));

				portfolioGroupRepository.save(pg);
				groupid = pg.getId();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return groupid;

	}

	public String getRoleByPortfolio(MimeType mimeType, String role, UUID portfolioId, Long userId) {
		GroupRightInfo gri = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, role);
		Long grid = null;
		if (gri != null) {
			grid = gri.getId();
		} else {
			return "Le grid n'existe pas";
		}
		return "grid = " + grid;
	}

	@Override
	public String getRolesByPortfolio(UUID portfolioId, Long userId) {
		GroupRights rights = getRightsOnPortfolio(userId, 0L, portfolioId);
		if (!rights.isRead())
			return null;

		List<GroupInfo> giList = groupInfoRepository.getByPortfolio(portfolioId);

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

	@Override
	public String getGroupRightsInfos(Long userId, UUID portfolioId) throws BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : no admin right");

		List<GroupRightInfo> resList = groupRightInfoRepository.getByPortfolioAndUser(portfolioId, userId);
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

	@Override
	public String addRoleInPortfolio(Long userId, UUID portfolioId, String data) throws BusinessException {
		if (!credentialRepository.isAdmin(userId) && !portfolioRepository.isOwner(portfolioId, userId))
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

		/// Problème de parsage
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
			/// Création du groupe de droit

			Long grid = 0L;
			final GroupRightInfo gri = new GroupRightInfo();
			gri.setOwner(userId);
			gri.setLabel(label);
			gri.setPortfolio(new Portfolio(portfolioId));

			try {
				groupRightInfoRepository.save(gri);
				grid = gri.getId();
			} catch (Exception e) {
			}

			labelNode.setAttribute("id", Long.toString(grid));

			/// Récupère les données avec identifiant mis à jour
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

	@Override
	public UUID copyPortfolio(MimeType inMimeType, UUID portfolioId, String srcCode, String newCode, Long userId,
			boolean setOwner) throws Exception {
		Portfolio originalPortfolio = null;

		try {
			/// le code source est OK ?
			if (srcCode != null) {
				// Retrouver le portfolio à partir du code source
				originalPortfolio = portfolioRepository.getPortfolioFromNodeCode(srcCode);
				if (originalPortfolio != null)
					// Portfolio uuid
					portfolioId = originalPortfolio.getId();
			}

			if (portfolioId == null)
				return null;

			//////////////////////////////
			/// Copie de la structure ///
			/////////////////////////////
			final Map<Node, Node> nodes = new HashMap<Node, Node>();

			// Récupération des noeuds du portfolio à copier.
			final List<Node> originalNodeList = nodeRepository.getNodes(portfolioId);
			final List<Node> copiedNodeList = new ArrayList<Node>(originalNodeList.size());
			Node copy = null, original = null, rootNodeCopy = null;
			/// Copie des noeuds -- structure du portfolio
			for (Iterator<Node> it = originalNodeList.iterator(); it.hasNext();) {
				original = it.next();
				copy = new Node(original);
				if (setOwner) {
					copy.setModifUserId(userId);
				} else {
					copy.setModifUserId(1L); // FIXME hard-coded root userid
				}

				////////////////////////////
				/// Copie des ressources///
				///////////////////////////
				if (copy.getResource() != null) {
					if (setOwner) {
						copy.getResource().setModifUserId(userId);
					} else {
						copy.getResource().setModifUserId(1L);
					}

					resourceTableRepository.save(copy.getResource());
				}
				if (copy.getResResource() != null) {
					// Mise a jour du code dans le contenu du noeud.
					if (StringUtils.equalsIgnoreCase(copy.getAsmType(), "asmRoot")) {
						copy.getResResource().setContent(
								StringUtils.replace(copy.getResResource().getContent(), copy.getCode(), newCode));

					}
					if (setOwner) {
						copy.getResResource().setModifUserId(userId);
					} else {
						copy.getResResource().setModifUserId(1L);
					}
					resourceTableRepository.save(copy.getResResource());
				}
				if (copy.getContextResource() != null) {
					if (setOwner) {
						copy.getContextResource().setModifUserId(userId);
					} else {
						copy.getContextResource().setModifUserId(1L);
					}
					resourceTableRepository.save(copy.getContextResource());
				}

				// Mise à jour du code dans le code interne de la BD.
				if (StringUtils.equalsIgnoreCase(copy.getAsmType(), "asmRoot")) {
					copy.setCode(newCode);
					rootNodeCopy = copy;
				}

				nodeRepository.save(copy);
				copiedNodeList.add(copy);
				nodes.put(original, copy);
			}

			/// Ajout du portfolio en base.
			Portfolio portfolioCopy = new Portfolio(originalPortfolio);
			portfolioCopy.setRootNode(rootNodeCopy);
			portfolioRepository.save(portfolioCopy);
			UUID newPortfolioUuid = portfolioCopy.getId();

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
				/// Mise à jour de la liste des enfants
				/// L'ordre determine le rendu visuel final du XML.
				if (key.getChildrenStr() != null) {
					String[] children = StringUtils.split(key.getChildrenStr(), ",");
					String[] childrenCopies = new String[children.length];
					for (int i = 0; i < children.length; i++) {
						searchedNode.setId(UUID.fromString(children[i]));
						copy = nodes.get(searchedNode);
						childrenCopies[i] = copy.getId().toString();
					}
					value.setChildrenStr(StringUtils.join(childrenCopies, ","));
				}
				/// Liaison des noeuds copiés au nouveau portfolio.
				value.setPortfolio(portfolioCopy);
				nodeRepository.save(value);
			}

			/// Finalement on crée un role designer
			Long groupid = securityManager.addRole(newPortfolioUuid, "designer", userId);

			/// Ajoute la personne dans ce groupe
			groupUserRepository.save(new GroupUser(groupid, userId));

			/// Force 'all' role creation
			groupid = securityManager.addRole(newPortfolioUuid, "all", userId);

			/// Check base portfolio's public state and act accordingly
			if (portfolioRepository.isPublic(portfolioId))
				groupManager.setPublicState(userId, newPortfolioUuid, true);

			return newPortfolioUuid;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public UUID getPortfolioUuidFromNode(UUID nodeId) {
		return portfolioRepository.getPortfolioUuidFromNode(nodeId);
	}

	private String passwdGen(Integer length) {
		Random random = new Random();

		Double num_bytes = Math.ceil(length * 0.75);
		byte[] bytes = new byte[num_bytes.intValue()];
		random.nextBytes(bytes);
		return new String(Base64.encodeBase64(bytes)).replaceAll("\\s+$", "").substring(0, length);
	}

	private Portfolio add(UUID rootNodeId, UUID modelId, Long userId, Portfolio portfolio) throws DoesNotExistException {
		if (portfolio.getRootNode() != null) {
			throw new IllegalArgumentException();
		}

		if (portfolio.getCredential() != null) {
			throw new IllegalArgumentException();
		}

		if (modelId != null) {
			portfolio.setModelId(modelId);
		}

		Optional<Node> rootNode = nodeRepository.findById(rootNodeId);
		Optional<Credential> credential = credentialRepository.findById(userId);

		if (!rootNode.isPresent()) {
			throw new DoesNotExistException(Node.class, null);
		}

		if (!credential.isPresent()) {
			throw new DoesNotExistException(Credential.class, null);
		}

		Node node = rootNode.get();
		Credential cr = credential.get();

		node.addPortfolio(portfolio);
		cr.addPortfolio(portfolio);

		portfolio.setModifUserId(cr.getId());

		nodeRepository.save(node);
		credentialRepository.save(cr);
		portfolioRepository.save(portfolio);

		return portfolio;
	}

	@Override
	public void updateTime(UUID portfolioId) {
		Portfolio portfolio = portfolioRepository.findById(portfolioId).get();
		portfolio.setModifDate(JavaTimeUtil.toJavaDate(LocalDateTime.now()));

		portfolioRepository.save(portfolio);
	}

	@Override
	public boolean updateTimeByNode(UUID nodeId) {
		Node n = nodeRepository.findById(nodeId).get();
		Portfolio p = n.getPortfolio();

		p.setModifDate(JavaTimeUtil.toJavaDate(LocalDateTime.now()));

		portfolioRepository.save(p);

		return true;
	}

	@Override
	public List<Portfolio> getPortfolios(Long userId,
								  Long substId,
								  Boolean portfolioActive,
								  Boolean portfolioProject) {

		// INNER JOIN p.rootNode
		// INNET JOIN rootNode.resResource
		// WHERE p.active = :active
		Specification<Portfolio> active = Specification.where((root, query, cb) -> {
			Join<Portfolio, Node> nodeJoin = root.join("rootNode");
			nodeJoin.join("resResource");

			return cb.equal(root.get("active"), portfolioActive);
		});

		// AND p.modifUserId = :modifUserId
		Specification<Portfolio> modifUser = Specification.where((root, query, cb) -> {
			return cb.equal(root.get("modifUserId"), userId);
		});

		// AND p.rootNode.semantictag LIKE '%karuta-project%'
		Specification<Portfolio> portfolioFilter = Specification.where((root, query, cb) -> {
			Join<Portfolio, Node> rootNode = root.join("rootNode");

			return cb.like(rootNode.get("semantictag"), "%karuta-project%");
		});

		Sort sort = Sort.by("rootNode.resResource");
		Specification<Portfolio> spec = active;

		if (portfolioProject)
			spec = spec.and(portfolioFilter);

		if (credentialRepository.isAdmin(userId)) {
			return portfolioRepository.findAll(spec, sort);
		} else {
			Specification<Portfolio> userIds = spec.and((root, query, cb) -> {
				List<Long> ids = Arrays.asList(userId, substId);

				Join<Portfolio, Credential> credentialJoin = root.join("credential");
				ListJoin<Credential, GroupUser> groupJoin = credentialJoin.joinList("groups");

				query.groupBy(root.get("portfolio.id"));

				return groupJoin.get("groups.id.credential.id").in(ids);
			});

			List<Portfolio> owned = portfolioRepository.findAll(spec.and(modifUser), sort);
			List<Portfolio> editable = portfolioRepository.findAll(spec.and(userIds), sort);

			return Stream.concat(owned.stream(), editable.stream())
					.collect(Collectors.toList());
		}
	}
}
