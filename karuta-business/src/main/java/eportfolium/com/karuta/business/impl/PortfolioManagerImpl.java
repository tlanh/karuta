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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eportfolium.com.karuta.business.contract.*;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.security.IsAdmin;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
@Transactional
public class PortfolioManagerImpl extends BaseManagerImpl implements PortfolioManager {

	static private final Logger logger = LoggerFactory.getLogger(PortfolioManagerImpl.class);

	@Autowired
	@Lazy
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
	private ResourceRepository resourceRepository;

	@Autowired
	private GroupUserRepository groupUserRepository;

	@Autowired
	private FileManager fileManager;

	@Autowired
	private ResourceManager resourceManager;

	/// temp class

	class GroupRight {
		Right getGroup(String label) {
			Right r = rights.get(label.trim());
			if (r == null) {
				r = new Right();
				rights.put(label, r);
			}
			return r;
		}

		void setNotify(String roles) {
			for (Right r : rights.values()) {
				r.notify = roles.trim();
			}
		}

		Map<String, Right> rights = new HashMap<>();
	}

	class Resolver {
		GroupRight getUuid(String uuid) {
			GroupRight gr = resolve.get(uuid);
			if (gr == null) {
				gr = new GroupRight();
				resolve.put(uuid, gr);
			}
			return gr;
		};

		Map<String, GroupRight> resolve = new HashMap<>();
		Map<String, Long> groups = new HashMap<>();
	}

	@Override
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
		PortfolioGroupMembersId pgmID = new PortfolioGroupMembersId();

		pgmID.setPortfolio(new Portfolio(portfolioId));
		pgmID.setPortfolioGroup(new PortfolioGroup(portfolioGroupId));

		portfolioGroupMembersRepository.deleteById(pgmID);

		return true;
	}

	@Override
	public PortfolioGroupDocument getPortfoliosByPortfolioGroup(Long portfolioGroupId) {
		List<Portfolio> portfolios = portfolioRepository.findByPortfolioGroup(portfolioGroupId);

		return new PortfolioGroupDocument(portfolioGroupId, portfolios.stream()
				.map(p -> new PortfolioDocument(p.getId()))
				.collect(Collectors.toList()));
	}

	@Override
	public String getPortfolio(UUID portfolioId, Long userId, Integer cutoff)
			throws BusinessException, JsonProcessingException {

		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);

		GroupRights rights = getRightsOnPortfolio(userId, portfolioId);

		if (!rights.isRead()) {
			Long publicId = credentialRepository.getPublicId();

			GroupRights publicRights = groupRightsRepository.getPublicRightsByUserId(rootNode.getId(), publicId);

			if (!publicRights.isRead()) {
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
			}
		}

		boolean owner = portfolioRepository.isOwner(portfolioId, userId);

		return getPortfolio(rootNode, userId, rights.getGroupRightInfo(), owner, cutoff);
	}

	@Override
	public File getZippedPortfolio(PortfolioDocument portfolio, String lang) throws IOException {
		File zipFile = File.createTempFile(portfolio.getId().toString(), ".zip");

		XmlMapper xmlMapper = new XmlMapper();

		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
			zip.setMethod(ZipOutputStream.DEFLATED);
			zip.setLevel(Deflater.BEST_COMPRESSION);

			// The file containing the portfolio as XML
			ZipEntry entry = new ZipEntry(portfolio.getId().toString() + ".xml");

			zip.putNextEntry(entry);
			zip.write(xmlMapper.writeValueAsString(portfolio).getBytes());
			zip.closeEntry();

			Predicate<ResourceDocument> filter;

			if (lang == null)
				filter = (r) -> r.getFileid() != null && !r.getFileid().isEmpty();
			else
				filter = (r) -> !r.getFileid(lang).isEmpty();

			// Nodes with file resources inside the portfolio.
			List<ResourceDocument> fileResources = portfolio
					.getRoot()
					.getResources()
					.stream()
					.filter(filter)
					.collect(Collectors.toList());

			for (ResourceDocument resource : fileResources) {
				String filename = resource.getFilename(lang);

				if (filename.equals(""))
					continue;

				String fullname = String.format("%s_%s.%s",
						resource.getNodeId().toString(),
						lang,
						filename.substring(filename.lastIndexOf(".") + 1));

				ZipEntry fileEntry = new ZipEntry(fullname);

				zip.putNextEntry(fileEntry);
				fileManager.fetchResource(resource, zip, lang, false);
				zip.closeEntry();
			}

			return zipFile;
		}
	}

	private String getPortfolio(Node rootNode, Long userId, GroupRightInfo groupRightInfo, boolean owner, Integer cutoff)
			throws JsonProcessingException {
		/// Node -> parent
		Map<UUID, Tree> entries = new HashMap<>();

		Portfolio portfolio = rootNode.getPortfolio();
		List<Pair<Node, GroupRights>> structure = getPortfolioStructure(portfolio.getId(), userId, groupRightInfo.getId());

		processQuery(structure, entries, groupRightInfo.getLabel());

		structure = getSharedStructure(portfolio.getId(), userId, cutoff);

		processQuery(structure, entries, groupRightInfo.getLabel());

		Tree root = entries.get(rootNode.getId());
		PortfolioDocument portfolioDocument = new PortfolioDocument(portfolio, owner);

		StringBuilder sb = new StringBuilder();
		String headerXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><portfolio code=\"0\" id=\""+portfolio.getId()+"\" owner=\""+owner+"\"><version>4</version>";
		sb.append(headerXML);
		
		if (root != null) {
			reconstructTree(sb, root.node, root, entries);
			portfolioDocument.setRoot(root.node);
		}

		sb.append("</portfolio>");
		
		return sb.toString();
	}

	private List<Pair<Node, GroupRights>> getPortfolioStructure(UUID portfolioId, Long userId, Long groupId) {

		List<Pair<Node, GroupRights>> portfolioStructure = new ArrayList<>();

		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);

		// Cas admin, designer, owner
		if (rootNode != null
				&& (credentialRepository.isAdmin(userId)
						|| credentialRepository.isDesigner(userId, rootNode.getId())
						|| portfolioRepository.isOwner(portfolioId, userId))) {

			List<Node> nodes = nodeRepository.getNodesWithResources(portfolioId);

			nodes.forEach(node -> {
				GroupRights rights = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true);
				portfolioStructure.add(Pair.of(node, rights));
			});

		} else if (hasRights(userId, portfolioId)) {
			Map<UUID, GroupRights> rights = new HashMap<>();

			String login = credentialRepository.getLoginById(userId);

			List<GroupRights> grList = groupRightsRepository.getPortfolioAndUserRights(portfolioId, login,
					groupId);

			for (GroupRights gr : grList) {
				if (rights.containsKey(gr.getGroupRightsId())) {
					GroupRights original = rights.get(gr.getGroupRightsId());
					original.setRead(gr.isRead() || original.isRead());
					original.setWrite(gr.isWrite() || original.isWrite());
					original.setDelete(gr.isDelete() || original.isDelete());
					original.setSubmit(gr.isSubmit() || original.isSubmit());
					original.setAdd(gr.isAdd() || original.isAdd());
				} else {
					rights.put(gr.getGroupRightsId(), gr);
				}
			}

			List<Node> nodes = nodeRepository.getNodes(new ArrayList<>(rights.keySet()));

			nodes.stream()
					.filter(node -> rights.containsKey(node.getId()))
					.filter(node -> rights.get(node.getId()).isRead())
					.forEach(node -> portfolioStructure.add(Pair.of(node, rights.get(node.getId()))));

		} else if (portfolioRepository.isPublic(portfolioId)) {

			List<Node> nodes = nodeRepository.getNodesWithResources(portfolioId);

			nodes.forEach(node -> {
				GroupRights groupRights = new GroupRights(true, false, false, false);
				portfolioStructure.add(Pair.of(node, groupRights));
			});
		}

		return portfolioStructure;
	}

	/**
	 * Récupère les noeuds partages d'un portfolio. C'est séparé car les noeuds ne
	 * provenant pas d'un même portfolio, on ne peut pas les sélectionner rapidement
	 * Autre possibilité serait de garder ce même type de fonctionnement pour une
	 * selection par niveau d'un portfolio.
	 */
	private List<Pair<Node, GroupRights>> getSharedStructure(UUID portfolioId, Long userId,
			Integer cutoff) {

		List<Pair<Node, GroupRights>> portfolioStructure = new ArrayList<>();

		if (portfolioRepository.hasSharedNodes(portfolioId)) {
			List<Node> nodes = nodeRepository.getSharedNodes(portfolioId);

			Map<Integer, Set<UUID>> parentIdMap = new HashMap<>();
			Set<UUID> parentIdSet = new HashSet<>();

			for (Node t_node : nodes) {
				parentIdSet.add(t_node.getSharedNodeUuid());
			}

			parentIdMap.put(0, parentIdSet);

			/// Les tours de boucle seront toujours <= au nombre de noeud du portfolio.
			int level = 0;
			boolean added = true;
			while (added && (cutoff == null ? true : level < cutoff)) {
				Set<UUID> parentIdSet2 = new HashSet<>();

				for (Node node : nodes) {
					for (UUID t_parent_node : parentIdMap.get(level)) {
						if (node.getPortfolio().getId().equals(portfolioId)
								&& node.getParentNode().getId().equals(t_parent_node)) {
							parentIdSet2.add(node.getId());
							break;
						}
					}
				}
				parentIdMap.put(level + 1, parentIdSet2);
				parentIdSet.addAll(parentIdSet2);
				added = !parentIdSet2.isEmpty(); // On s'arrete quand rien n'a été ajouté
				level = level + 1; // Prochaine étape
			}

			nodes = nodeRepository.getNodes(new ArrayList<>(parentIdSet));

			for (Node node : nodes) {
				GroupRights rights = groupRightsRepository.getRightsByIdAndUser(node.getId(), userId);

				if (rights != null && rights.isRead()) { // On doit au moins avoir le droit de lecture
					portfolioStructure.add(Pair.of(node, rights));
				}
			}
		}
		return portfolioStructure;
	}

	@Override
	public PortfolioList getPortfolioShared(Long userId) {
		List<Map<String, Object>> portfolios = portfolioRepository.getPortfolioShared(userId);

		return new PortfolioList(portfolios.stream()
				.map(current -> new PortfolioDocument(
						(UUID)current.get("portfolio"),
						(Long)current.get("gid")))
				.collect(Collectors.toList()));
	}

	@Override
	public String getPortfolioByCode(String portfolioCode, Long userId,
			boolean resources) throws BusinessException, JsonProcessingException {
		Portfolio portfolio = portfolioRepository.getPortfolioFromNodeCode(portfolioCode);

		if (portfolio == null) {
			return null;
		}

		if (resources) {
			return getPortfolio(portfolio.getId(), userId, null);

		} else {
			PortfolioDocument document = new PortfolioDocument(portfolio.getId());
			document.setRoot(nodeManager.getNode(portfolio.getRootNode().getId(), false, "nodeRes", userId,
					null, false));

			XmlMapper xmlMapper = new XmlMapper();

			return xmlMapper.writeValueAsString(document);
		}
	}

	@Override
	public GroupRights getRightsOnPortfolio(Long userId, UUID portfolioId) {
		return portfolioRepository.findById(portfolioId)
				.map(portfolio -> {
					if (portfolio.getModifUserId().equals(userId)) // Is the owner
						return new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true);
					else
						return nodeManager.getRights(userId, portfolio.getRootNode().getId());
				}).orElse(new GroupRights());
	}

	@Override
	public boolean hasRights(Long userId, UUID portfolioId) {
		if (userId != null && portfolioId != null) {
			Long modifUserId = portfolioRepository.getOwner(portfolioId);

			if (Objects.equals(modifUserId, userId)) {
				return true;
			} else {
				// Vérifier les autres droits partagés
				List<GroupUser> gu = groupUserRepository.getByPortfolioAndUser(portfolioId, userId);

				return !gu.isEmpty();
			}
		}

		return false;
	}

	@Override
	public void removePortfolio(UUID portfolioId, Long userId) {
		GroupRights rights = getRightsOnPortfolio(userId, portfolioId);

		if (!rights.isDelete() && !credentialRepository.isAdmin(userId)) {
			return;
		}

		// S'il y a quelque chose de particulier, on s'assure que tout soit bien nettoyé
		// de façon séparée
		groupRightInfoRepository.deleteAll(groupRightInfoRepository.getByPortfolioID(portfolioId));

		/// Resources
		resourceRepository.deleteAll(
				resourceRepository.getResourcesByPortfolioUUID(portfolioId));

		resourceRepository.deleteAll(
				resourceRepository.getContextResourcesByPortfolioUUID(portfolioId));

		resourceRepository.deleteAll(
				resourceRepository.getResourcesOfResourceByPortfolioUUID(portfolioId));

		/// Nodes
		nodeRepository.deleteAll(nodeRepository.getNodes(portfolioId));

		/// Remove portfolio from group.
		portfolioGroupMembersRepository.deleteAll(
				portfolioGroupMembersRepository.getByPortfolioID(portfolioId));

		// Portfolio
		portfolioRepository.deleteById(portfolioId);
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
			Node rootNode = p.getRootNode();

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
	public void changePortfolioConfiguration(UUID portfolioId, Boolean portfolioActive) {
		portfolioRepository.findById(portfolioId)
				.ifPresent(p -> {
					p.setActive(portfolioActive ? 1 : 0);
					portfolioRepository.save(p);
				});
	}

	@Override
	public String getPortfolios(long userId, Boolean active, long substid, Boolean specialProject, String portfolioCode) {
		List<Portfolio> portfolios = getPortfolios(userId, substid, active, specialProject, portfolioCode);

		String psformat = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><portfolios count=\"%d\">%s</portfolios>";
		String pformat = "<portfolio id=\"%s\" root_node_id=\"%s\" owner=\"%s\" ownerid=\"%s\" modified=\"%s\">%s</portfolio>";
		String arformat = "<asmRoot id=\"%s\"><metadata-wad %s/><metadata-epm %s/><metadata %s/><code>%s</code><label>%s</label><description>%s</description>%s</asmRoot>";
		String rformat = "<asmResource id=\"%s\" contextid=\"%s\" xsi_type=\"%s\">%s</asmResource>";
		
		StringBuilder sb = new StringBuilder();
		for( Portfolio p : portfolios )
		{
			Node rn = p.getRootNode();
			Resource nr = rn.getResource();
			Resource cr = rn.getContextResource();
			
			StringBuilder res = new StringBuilder();
			res.append(String.format(rformat, nr.getId(), rn.getId(), nr.getXsiType(), nr.getContent()));
			res.append(String.format(rformat, cr.getId(), rn.getId(), cr.getXsiType(), cr.getContent()));
			
			String rootdata = String.format(arformat, rn.getId(), rn.getMetadataWad(), rn.getMetadataEpm(), rn.getMetadata(), rn.getCode(), rn.getLabel(), rn.getDescr(), res.toString());
			
			boolean isowner = userId == p.getModifUserId() ? true : false;
			sb.append(String.format(pformat, p.getId(), rn.getId(), isowner, rn.getModifUserId(), p.getModifDate(), rootdata));
		}

		return String.format(psformat, portfolios.size(), sb.toString());

		/*
		return new PortfolioList(portfolios.stream()
                .map(p -> new PortfolioDocument(p, p.getModifUserId().equals(userId)))
                .collect(Collectors.toList()));
		//*/
	}

	@Override
	public boolean rewritePortfolioContent(PortfolioDocument portfolio, UUID portfolioId, Long userId,
										   Boolean portfolioActive) throws BusinessException, JsonProcessingException {

		NodeDocument rootNode = portfolio.getRoot();

		if (rootNode == null)
			throw new GenericBusinessException("No root node found");

		UUID rootNodeUuid = portfolio.getId() != null ? portfolio.getId() : UUID.randomUUID();

		Portfolio portfolioRecord = portfolioRepository.findById(portfolioId)
										.orElse(add(rootNodeUuid, userId, null));

		if (userId == null || userId == 0L) {
			userId = portfolioRecord.getCredential().getId();
		}

		nodeManager.writeNode(rootNode, portfolioRecord.getId(), userId, 0, null, null,
				true, null, false);

		// On récupère le noeud root généré précédemment et on l'affecte au portfolio.
		portfolioRecord.setRootNode(nodeRepository.getRootNodeByPortfolio(portfolioRecord.getId()));
		portfolioRecord.setActive(portfolioActive ? 1 : 0);

		portfolioRepository.save(portfolioRecord);

		return true;
	}

	static class Right {
		boolean rd = false;
		boolean wr = false;
		boolean dl = false;
		boolean sb = false;
		boolean ad = false;
		String types = "";
		String rules = "";
		String notify = "";
	}

	@Override
	public UUID postPortfolioParserights(UUID portfolioId, Long userId) throws JsonProcessingException, BusinessException {

		boolean setPublic = false;

		Resolver resolve = new Resolver();

		// Sélection des méta-données
		List<Node> nodes = nodeRepository.getNodes(portfolioId);

		for (Node current : nodes) {
			String uuid = current.getId().toString();
			String meta = current.getMetadataWad();

			XmlMapper mapper = new XmlMapper();

			MetadataWadDocument metadataWad = mapper
												.readerFor(MetadataWadDocument.class)
												.readValue("<metadata-ward " + meta + "></metadata-wad>");

			GroupRight role = resolve.getUuid(uuid);

			if (metadataWad.getSeenoderoles() != null) {
				for (String nodeRole : metadataWad.getSeenoderoles().split(" ")) {
					role.getGroup(nodeRole).rd = true;

					resolve.groups.put(nodeRole, 0L);
				}
			}

			if (metadataWad.getShowtoroles() != null) {
				for (String nodeRole : metadataWad.getShowtoroles().split(" ")) {
					role.getGroup(nodeRole).rd = false;

					resolve.groups.put(nodeRole, 0L);
				}
			}

			if (metadataWad.getDelnoderoles() != null) {
				for (String nodeRole : metadataWad.getDelnoderoles().split(" ")) {
					role.getGroup(nodeRole).dl = true;

					resolve.groups.put(nodeRole, 0L);
				}
			}

			if (metadataWad.getEditnoderoles() != null) {
				for (String nodeRole : metadataWad.getEditnoderoles().split(" ")) {
					role.getGroup(nodeRole).wr = true;

					resolve.groups.put(nodeRole, 0L);
				}
			}

			if (metadataWad.getEditresroles() != null) {
				for (String nodeRole : metadataWad.getEditresroles().split(" ")) {
					role.getGroup(nodeRole).wr = true;

					resolve.groups.put(nodeRole, 0L);
				}
			}

			if (metadataWad.getSubmitroles() != null) {
				for (String nodeRole : metadataWad.getSubmitroles().split(" ")) {
					role.getGroup(nodeRole).sb = true;

					resolve.groups.put(nodeRole, 0L);
				}
			}


			if (metadataWad.getMenuroles() != null) {
				for (String line : metadataWad.getMenuroles().split(";")) {
					/// Format pour l'instant:
					/// code_portfolio,tag_semantique,label@en/libelle@fr,reles[;autre menu]
					String[] tokens = line.split(",");
					String menurolename = tokens[3];

					if (menurolename != null) {
						// Break down list of roles
						for (String s : menurolename.split(" "))
							resolve.groups.put(s.trim(), 0L);
					}
				}
			}

			if (metadataWad.getNotifyroles() != null) {
				role.setNotify(metadataWad.getNotifyroles().replace(" ", ","));
			}

			if (portfolioRepository.isPublic(portfolioId))
				setPublic = true;

			MetadataDocument metadata = mapper
											.readerFor(MetadataDocument.class)
											.readValue("<metadata>" + current.getMetadata() + "</metadata>");

			if (metadata.getPublic())
				setPublic = true;

			for (String s : resolve.groups.keySet()) {
				GroupRightInfo gri = new GroupRightInfo();
				gri.setOwner(1L);
				gri.setLabel(s);
				gri.setChangeRights(false);
				gri.setPortfolio(new Portfolio(portfolioId));

				groupRightInfoRepository.save(gri);
				Long grid = gri.getId();
				resolve.groups.put(s, grid);

				groupInfoRepository.save(new GroupInfo(grid, 1L, s));
			}

			/// Ajout des droits des noeuds

			for (Entry<String, GroupRight> entry : resolve.resolve.entrySet()) {
				GroupRight gr = entry.getValue();

				for (Entry<String, Right> rightelem : gr.rights.entrySet()) {
					String group = rightelem.getKey();
					long grid = resolve.groups.get(group);
					Right rightval = rightelem.getValue();
					GroupRights groupRights = new GroupRights();
					groupRights.setId(new GroupRightsId());
					groupRights.setGroupRightInfo(new GroupRightInfo(grid));
					groupRights.setGroupRightsId(UUID.fromString(entry.getKey()));
					groupRights.setRead(rightval.rd);
					groupRights.setWrite(rightval.wr);
					groupRights.setDelete(rightval.dl);
					groupRights.setSubmit(rightval.sb);
					groupRights.setAdd(rightval.ad);
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

			// Define portfolio as public if necessary.
			if (setPublic)
				groupManager.setPublicState(userId, portfolioId, true);
		}

		return portfolioId;
	}

	@Override
	public PortfolioList addPortfolio(PortfolioDocument portfolioDocument, long userId, UUID portfolioModelId,
							   boolean parseRights, String projectName)
			throws BusinessException, JsonProcessingException {
		if (!credentialRepository.isAdmin(userId) && !credentialRepository.isCreator(userId))
			throw new GenericBusinessException("FORBIDDEN : No admin right");

		// Si le modèle est renseigné, on ignore le XML poste et on récupère le contenu
		// du modèle a la place
		// FIXME Inutilisé, nous instancions / copions un portfolio

		if (portfolioModelId != null) {
      ObjectMapper mapper = new XmlMapper();
      String xml = getPortfolio(portfolioModelId, userId, null);
    
      portfolioDocument = mapper
	            .readerFor(PortfolioDocument.class)
	            .readValue(xml);
		}

		NodeDocument rootNode = portfolioDocument.getRoot();

		if (rootNode == null) {
			throw new GenericBusinessException("Exception handling node");
		}

		Optional<ResourceDocument> resourceDocument = rootNode
				.getResources()
				.stream()
				.filter(r -> r.getCode() != null)
				.findFirst();

		if (resourceDocument.isPresent()) {
			String code = resourceDocument.get().getCode();

			if (projectName != null) {
				int dot = code.indexOf(".");

				if (dot < 0) // Doesn't exist, add it
					code = projectName + "." + code;
				else // Replace
					code = projectName + code.substring(dot);
			}

			if (nodeRepository.isCodeExist(code)) {
				throw new GenericBusinessException("CONFLICT : Existing code.");
			}

			resourceDocument.get().setCode(code);
		}

		UUID uuid = UUID.randomUUID();

		Portfolio portfolio = add(uuid, userId, portfolioDocument);

		/// Si nous instancions, nous n'avons pas besoin du rôle de concepteur
		securityManager.addRole(portfolio.getId(), "all", userId);

		/// Créer groupe 'designer', 'all' est mis avec ce qui est spécifique dans le
		/// XML reçu.
		long roleId = securityManager.addRole(portfolio.getId(), "designer", userId);

		/// Ajoute la personne dans ce groupe
		groupUserRepository.save(new GroupUser(roleId, userId));

		return new PortfolioList(
				Collections.singletonList(new PortfolioDocument(portfolio.getId())));
	}

	@Override
	public String importPortfolio(String path, InputStream inputStream, Long userId, boolean parseRights,
										String projectName) throws BusinessException, IOException {
        if (!credentialRepository.isAdmin(userId) && !credentialRepository.isCreator(userId))
            throw new GenericBusinessException("403 FORBIDDEN : No admin right");

        if (projectName == null)
            projectName = "";
        else
            projectName = projectName.trim();

        DataInputStream inZip = new DataInputStream(inputStream);

        String filename;
        String[] xmlFiles;
        String[] allFiles;
        byte[] buff = new byte[0x100000]; // 1MB buffer

        String outsideDir = path.substring(0, path.lastIndexOf(File.separator)) + "_files" + File.separator;
        File outsideDirectoryFile = new File(outsideDir);

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

        final String destination = outsideDir + portfolioUuidPreliminaire + File.separator;

        // -- unzip --
        fileManager.unzip(filename, destination);

        // Unzip just the next zip level. I hope there will be no zipped documents...
        String[] zipFiles = fileManager.findFiles(destination, "zip");

        for (String zipFile : zipFiles) {
            fileManager.unzip(zipFile, destination);
        }

        xmlFiles = fileManager.findFiles(destination, "xml");
        allFiles = fileManager.findFiles(destination, null);

        ////// Lecture du fichier de portfolio
        //// Importation du portfolio
        // --- Lecture fichier XML ----
        ///// Pour associer l'ancien uuid -> nouveau, pour les fichiers
        Map<UUID, UUID> resolve = new HashMap<>();
        Portfolio portfolio = null;
        PortfolioDocument document = null;
        boolean hasLoaded = false;

        for (String xmlFilepath : xmlFiles) {
            String xmlFilename = xmlFilepath.substring(xmlFilepath.lastIndexOf(File.separator));
            if (xmlFilename.contains("_"))
                continue; // Case when we add an XML in the portfolio

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(xmlFilepath), StandardCharsets.UTF_8));

            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }

            br.close();
            String xml = sb.toString();

            ObjectMapper mapper = new XmlMapper();
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            document = mapper
                    .readerFor(PortfolioDocument.class)
                    .readValue(xml);

            NodeDocument asmRoot = document.getRoot();

            if (asmRoot == null)
                continue;

            Optional<ResourceDocument> resource = asmRoot
                    .getResources()
                    .stream()
                    .filter(n -> "nodeRes".equals(n.getXsiType())).findFirst();

            if (resource.isPresent()) {
                String code = resource.get().getCode();

                if (!"".equals(projectName)) { // If a new name has been specified
                    // Find if it contains a project name
                    int dot = code.indexOf(".");

                    if (dot > 0)
                        code = projectName + code.substring(dot);
                    else // If no dot, it's a project, skip it
                        continue;
                }

                if (nodeRepository.isCodeExist(code))
                    throw new GenericBusinessException("409 Conflict : Existing code.");
            }

            UUID uuid = UUID.randomUUID();

            portfolio = add(uuid, userId, document);

            /// Create base group
            securityManager.addRole(document.getId(), "all", userId);

            /// Finalement on crée un rôle de designer
            Long groupid = securityManager.addRole(document.getId(), "designer", userId);

            /// Ajoute la personne dans ce groupe
            groupUserRepository.save(new GroupUser(groupid, userId));

            hasLoaded = true;
        }

        if (hasLoaded) {
            for (String fullPath : allFiles) {
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

                lang = tmpFileName.substring(index + 1, index + 3);

                if ("un".equals(lang)) // Hack sort of fixing previous implementation
                    lang = "en";

                // Attention on initialise la ligne file
                // avec l'UUID d'origine de l'asmContext parent
                // Il sera mis e jour avec l'UUID asmContext final dans writeNode
                try {
                    UUID resolved = resolve.get(UUID.fromString(uuid)); /// New uuid
                    FileInputStream input = new FileInputStream(new File(fullPath));

                    if (resolved != null) {
                        /// Have to send it in FORM, compatibility with regular file posting
						resourceManager.updateContent(resolved, userId, input, lang, false);
                    }
                } catch (Exception ex) {
                    // Le nom du fichier ne commence pas par un UUID,
                    // ce n'est donc pas une ressource
                    ex.printStackTrace();
                }
            }
        }

		File zipfile = new File(filename);
		zipfile.delete();
		File zipdir = new File(outsideDir + portfolioUuidPreliminaire + File.separator);
		zipdir.delete();

		return portfolio.getId().toString();
	}

	@Override
	public String instanciatePortfolio(UUID portfolioId, String srccode, String tgtcode, Long id,
			boolean copyshared, String groupname, boolean setOwner) {

		//// Fetch nodes to be instanciated
		Portfolio portfolio = null;
		Long owner = null;
		List<Node> nodelist = null;

		if (srccode != null) { /// Find by source code
			portfolio = portfolioRepository.getPortfolioFromNodeCode(srccode);
			owner = portfolio.getCredential().getId();
			nodelist = new ArrayList<>(portfolio.getNodes());
		} else {
			nodelist = nodeRepository.getNodes(portfolioId);
			owner = nodelist.get(0).getModifUserId();
		}

		Credential credential = credentialRepository.getUserInfos(owner);

		class Helper
		{
			/// uuid -> available roles
			GroupRights getRights( UUID uuid, String role )
			{
				/// Fetch (role, uuid) rights
				Pair<UUID, String> k = new ImmutablePair<UUID, String>(uuid, role);
				GroupRights gr = rights.get(k);
				
				// if doesn't exist, new line in DB
				if( gr == null )
				{
					/// Check if role exist
					GroupRightInfo gri = resolve.get(role);
					if( gri == null )	// New group appeared
					{
						GroupInfo gi = new GroupInfo();
						gi.setLabel(role);
						gri = new GroupRightInfo();
						gri.setLabel(role);
						
						// Association with GroupInfo
						gri.setGroupInfo(gi);
						gri.setPortfolio(portfolio);
						gri = groupRightInfoRepository.save(gri);	// Need to have update value
						gi.setGroupRightInfo(gri);
						gi = groupInfoRepository.save(gi);
						
						/// Keep for future use
						resolve.put(role, gri);
					}
					
					gr = new GroupRights();
					GroupRightsId transit = new GroupRightsId();
					gr.setId(transit);
					gr.setGroupRightInfo(gri);
					gr.setGroupRightsId(uuid);
					
					rights.put(k, gr);
				}
				return gr;
			};
			
			void setNotify( UUID uuid, String notify )
			{
				/// Set notification for all groups associated with this uuid
				Set<String> keys = resolve.keySet();
				for( String k : keys )
				{
					Pair<UUID, String> p = new ImmutablePair<UUID, String>(uuid, k);
					GroupRights gr = rights.get(p);
					if( gr != null )
						gr.setNotifyRoles(notify);
				}
			}
			
			void save()
			{
				/// Send all GroupRights
				for( GroupRights gr : rights.values() )
					groupRightsRepository.save(gr);
			}

			HashMap<String, GroupRightInfo> resolve = new HashMap<String, GroupRightInfo>();
			HashMap<Pair<UUID, String>, GroupRights> rights = new HashMap<Pair<UUID, String>, GroupRights>();
			Portfolio portfolio = null;
		};
		
		/// Make new portfolio
		Portfolio copyPortfolio = new Portfolio();
		copyPortfolio.setCredential(credential);
		copyPortfolio.setModifUserId(owner);
		copyPortfolio.setActive(1);
		
		// Sent for update
		copyPortfolio = portfolioRepository.save(copyPortfolio);
		
		// Group creation helper and configuration
		Helper helper = new Helper();
		helper.portfolio = copyPortfolio;
		
		Node asmroot = null;
		
		//// To resolve new parent uuid
		HashMap<UUID, UUID> resolve = new HashMap<UUID, UUID>();
		HashMap<UUID, Node> copyList = new HashMap<UUID, Node>(nodelist.size());
		/// Bundle of branches from base instance for children resolution
		HashMap<UUID, String> oldfaggot = new HashMap<UUID, String>();
		
		/// For rights resolution, group name -> DB object
		for( Node n : nodelist )
		{
			/// Duplicate node (this also duplicate resources)
			Node cn = new Node(n);
			
			/// Set new portfolio ownership
			cn.setPortfolio(copyPortfolio);
			
			/// Need to send so some values get updated
			if( cn.getResource() != null )
				resourceRepository.save(cn.getResource());
			if(cn.getResResource() != null)
				resourceRepository.save(cn.getResResource());
			if( cn.getContextResource() != null )
				resourceRepository.save(cn.getContextResource());
			cn = nodeRepository.save(cn);

			// Need to set parent and children list after the fact since list is unordered
			oldfaggot.put(n.getId(), n.getChildrenStr());
			if( asmroot == null )
			{
				Node p = cn.getParentNode();
				if( p == null )	// root node
				{
					/// Update code
					cn.setCode(tgtcode);
					Resource res = cn.getResource();
					String rescontent = res.getContent();
					rescontent = rescontent.replaceAll("<code>.*</code>", String.format("<code>%s</code>", tgtcode));
					res.setContent(rescontent);
					/// Update/keep track of new objects
					resourceRepository.save(res);
					cn = asmroot = nodeRepository.save(cn);
				}
			}

			/// Keep copy for child list update and resolution
			copyList.put(cn.getId(), cn);
			/// New UUID resolution
			resolve.put(n.getId(), cn.getId());
			
			/// Parse metadata for rights
			String metadatawad = cn.getMetadataWad();
			MetadataWadDocument mwd = null;
			try { mwd = MetadataWadDocument.from(metadatawad); }
			catch( JsonProcessingException e ){ e.printStackTrace(); }
			
			/// Read rights
			String snr = mwd.getSeenoderoles();
			if( snr != null )
			{
				StringTokenizer tokens = new StringTokenizer(snr, " ");
				while (tokens.hasMoreElements())
				{
					String nodeRole = tokens.nextElement().toString();
					GroupRights right = helper.getRights(cn.getId(), nodeRole);
					right.setRead(true);
				}
			}
			/// Read rights
			String str = mwd.getShowtoroles();
			if( str != null )
			{
				StringTokenizer tokens = new StringTokenizer(str, " ");
				while (tokens.hasMoreElements())
				{
					String nodeRole = tokens.nextElement().toString();
					GroupRights right = helper.getRights(cn.getId(), nodeRole);
					/// User shown to can't see by default
					right.setRead(false);
				}
			}
			/// Delete
			String dnr = mwd.getDelnoderoles();
			if( dnr != null )
			{
				StringTokenizer tokens = new StringTokenizer(dnr, " ");
				while (tokens.hasMoreElements())
				{
					String nodeRole = tokens.nextElement().toString();
					GroupRights right = helper.getRights(cn.getId(), nodeRole);
					right.setDelete(true);
				}
			}
			/// Write
			String enr = mwd.getEditnoderoles();
			if( enr != null )
			{
				StringTokenizer tokens = new StringTokenizer(enr, " ");
				while (tokens.hasMoreElements())
				{
					String nodeRole = tokens.nextElement().toString();
					GroupRights right = helper.getRights(cn.getId(), nodeRole);
					right.setWrite(true);
				}
			}
			/// Submit
			String sr = mwd.getSubmitroles();
			if( sr != null )
			{
				StringTokenizer tokens = new StringTokenizer(sr, " ");
				while (tokens.hasMoreElements())
				{
					String nodeRole = tokens.nextElement().toString();
					GroupRights right = helper.getRights(cn.getId(), nodeRole);
					right.setSubmit(true);
				}
			}
			/// Actions
			String ar = null;
//			String ar = mwd.getAttributes().get("actionroles");
			if( ar != null )
			{
				/// Format pour l'instant: actionroles="sender:1,2;responsable:4"
				StringTokenizer tokens = new StringTokenizer(ar, ";");
				while (tokens.hasMoreElements())
				{
					String nodeRole = tokens.nextElement().toString();
					StringTokenizer data = new StringTokenizer(nodeRole, ":");
					String nrole = data.nextElement().toString();
					String actions = data.nextElement().toString().trim();
					
					GroupRights right = helper.getRights(cn.getId(), nrole);
					right.setRulesId(actions);
				}
			}
			/// Menus
			String mr = mwd.getMenuroles();
			if( mr != null )
			{
				/// Format pour l'instant: code_portfolio,tag_semantique,label@en/libelle@fr,roles[;autre menu]
				StringTokenizer tokens = new StringTokenizer(mr, ";");
				while (tokens.hasMoreElements())
				{
					String menuline = tokens.nextElement().toString();
					String[] data = menuline.split(",");
					String menurolename = data[3];
	
					if( menurolename != null )
					{
						// Break down list of roles
						String[] roles = menurolename.split(" ");
						// Only ensure that group exists, for logical use
						for( int i=0; i<roles.length; ++i )
							helper.getRights(cn.getId(), roles[i]);
					}
				}
			}
			/// Notification
			String nr = mwd.getNotifyroles();
			if( nr != null )
			{
				///// FIXME: Should be done in UI
				/// Format pour l'instant: notifyroles="sender responsable[ responsable]"
				StringTokenizer tokens = new StringTokenizer(nr, " ");
				String merge = "";
				if( tokens.hasMoreElements() )
					merge = tokens.nextElement().toString().trim();
				while (tokens.hasMoreElements())
					merge += ","+tokens.nextElement().toString().trim();
				helper.setNotify( cn.getId(), merge);	/// Add person to alert in all roles in this uuid
			}
		}
		
		copyPortfolio.setRootNode(asmroot);
		copyPortfolio = portfolioRepository.save(copyPortfolio);
		
		/// Save all rights
		helper.save();
		
		/// Update children list
		for( Entry<UUID, String> e : oldfaggot.entrySet() )
		{
			UUID nk = e.getKey();
			ArrayList<String> cl = new ArrayList<String>();
			String ocl = e.getValue();
			if( "".equals(ocl) ) continue;
			String token[] = ocl.split(",");
			Node n = copyList.get(resolve.get(nk));
			for( String t : token )
			{
				Node c = copyList.get(resolve.get(UUID.fromString(t)));
				c.setParentNode(n);
				cl.add(c.getId().toString());
			}
			n.setChildrenStr(String.join(",", cl));
			nodeRepository.save(n);
		}
		
		return copyPortfolio.getId().toString();
	}

	@Override
	public int addPortfolioInGroup(UUID portfolioId, Long portfolioGroupId, String label) {
		Optional<PortfolioGroup> pgOptional = portfolioGroupRepository.findById(portfolioGroupId);

		if (!pgOptional.isPresent())
			return -1;

		PortfolioGroup pg = pgOptional.get();

		if (label != null) {
			pg.setLabel(label);

			portfolioGroupRepository.save(pg);
		} else {
			if (!StringUtils.equalsIgnoreCase(pg.getType(), "portfolio"))
				return 1;

			Portfolio p = portfolioRepository.findById(portfolioId).get();

			PortfolioGroupMembers pgm = new PortfolioGroupMembers(new PortfolioGroupMembersId());
			pgm.setPortfolio(p);
			pgm.setPortfolioGroup(pg);
			portfolioGroupMembersRepository.save(pgm);
		}

		return 0;
	}

	@Override
	public Long getPortfolioGroupIdFromLabel(String groupLabel) {
		Optional<PortfolioGroup> portfolioGroup = portfolioGroupRepository.findByLabel(groupLabel);

		if (portfolioGroup.isPresent()) {
			return portfolioGroup.get().getId();
		} else {
			return -1L;
		}
	}

	@Override
	public String getPortfolioGroupList() {
		class TreeNode {
			String nodeContent;
			int nodeId;
			final List<TreeNode> childs = new ArrayList<>();
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

		Iterable<PortfolioGroup> pgList = portfolioGroupRepository.findAll();

		List<TreeNode> trees = new ArrayList<>();
		Map<Long, TreeNode> resolve = new HashMap<>();
		ProcessTree pf = new ProcessTree();

		StringBuilder currNode = new StringBuilder();
		for (PortfolioGroup pg : pgList) {
			currNode.setLength(0);
			String pgStr = String.valueOf(pg.getId());
			String type = pg.getType();
			currNode.append("<group type=\"").append(type.toLowerCase()).append("\" id=\"");
			currNode.append(pgStr);
			currNode.append("\"><label>").append(pg.getLabel()).append("</label>");

			// group tag will be closed at reconstruction

			TreeNode currTreeNode = new TreeNode();
			currTreeNode.nodeContent = currNode.toString();
			currTreeNode.nodeId = Integer.parseInt(pgStr);
			PortfolioGroup parent = null;

			if (pg.getParent() != null) {
				parent = pg.getParent();
			}

			resolve.put((long) currTreeNode.nodeId, currTreeNode);

			if (parent != null && !(parent.getId() == null || parent.getId() == 0)) {
				TreeNode parentTreeNode = resolve.get(parent.getId());
				parentTreeNode.childs.add(currTreeNode);
			} else { // Top level groups
				trees.add(currTreeNode);
			}
		}

		/// Go through top level parent and reconstruct each tree
		for (TreeNode topNode : trees) {
			pf.reconstruct(result, topNode);
		}

		result.append("</groups>");

		return result.toString();
	}

	@Override
	public PortfolioGroupList getPortfolioGroupListFromPortfolio(UUID portfolioId) {
		List<PortfolioGroupMembers> groupMembers = portfolioGroupMembersRepository.getByPortfolioID(portfolioId);

		return new PortfolioGroupList(groupMembers.stream()
				.map(PortfolioGroupMembers::getPortfolioGroup)
				.map(PortfolioGroupDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public Long addPortfolioGroup(String groupname, String type, Long parentId) {
		// Vérifier si le parent existe.
		if (parentId != null && !portfolioGroupRepository.existsByIdAndType(parentId, "GROUP")) {
			return -1L;
		}

		PortfolioGroup pg = new PortfolioGroup();
		pg.setLabel(groupname);
		pg.setType(type);

		if (parentId != null)
			pg.setParent(new PortfolioGroup(parentId));

		portfolioGroupRepository.save(pg);

		return pg.getId();
	}

	@Override
	public String getRoleByPortfolio(String role, UUID portfolioId) {
		GroupRightInfo gri = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, role);

		if (gri != null) {
			return "grid = " + gri.getId();
		} else {
			return "Le grid n'existe pas";
		}
	}

	@Override
	public GroupInfoList getRolesByPortfolio(UUID portfolioId, Long userId) {
		GroupRights rights = getRightsOnPortfolio(userId, portfolioId);
		if (!rights.isRead())
			return null;

		List<GroupInfo> groupInfos = groupInfoRepository.getByPortfolio(portfolioId);

		return new GroupInfoList(groupInfos.stream()
				.map(GroupInfoDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	@IsAdmin
	public GroupRightInfoList getGroupRightsInfos(UUID portfolioId) {
		List<GroupRightInfo> groups = groupRightInfoRepository.getByPortfolioID(portfolioId);

		return new GroupRightInfoList(groups.stream()
				.map(GroupRightInfoDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public UUID copyPortfolio(UUID portfolioId, String srcCode, String newCode, Long userId, boolean setOwner)
			throws BusinessException {
		Portfolio originalPortfolio;

		/// le code source est OK ?
		if (srcCode != null) {
			// Retrouver le portfolio à partir du code source
			originalPortfolio = portfolioRepository.getPortfolioFromNodeCode(srcCode);
		} else {
			originalPortfolio = portfolioRepository.findById(portfolioId).orElse(null);
		}

		if (originalPortfolio != null)
			portfolioId = originalPortfolio.getId();

		if (portfolioId == null || originalPortfolio == null)
			return null;

		//////////////////////////////
		/// Copie de la structure ///
		/////////////////////////////
		final Map<Node, Node> nodes = new HashMap<>();

		// Récupération des noeuds du portfolio à copier.
		final List<Node> originalNodeList = nodeRepository.getNodes(portfolioId);
		Node rootNodeCopy = null;

		/// Copie des noeuds -- structure du portfolio
		for (Node node : originalNodeList) {
			Node copy = new Node(node);

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

				resourceRepository.save(copy.getResource());
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
				resourceRepository.save(copy.getResResource());
			}

			if (copy.getContextResource() != null) {
				if (setOwner) {
					copy.getContextResource().setModifUserId(userId);
				} else {
					copy.getContextResource().setModifUserId(1L);
				}
				resourceRepository.save(copy.getContextResource());
			}

			// Mise à jour du code dans le code interne de la BD.
			if (StringUtils.equalsIgnoreCase(copy.getAsmType(), "asmRoot")) {
				copy.setCode(newCode);
				rootNodeCopy = copy;
			}

			nodeRepository.save(copy);
			nodes.put(node, copy);
		}

		/// Ajout du portfolio en base.
		Portfolio portfolioCopy = new Portfolio(originalPortfolio);
		portfolioCopy.setRootNode(rootNodeCopy);
		portfolioRepository.save(portfolioCopy);
		UUID newPortfolioUuid = portfolioCopy.getId();

		for (Entry<Node, Node> entry : nodes.entrySet()) {
			Node key = entry.getKey();
			Node value = entry.getValue();

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
					Node searchedNode = new Node();
					searchedNode.setId(UUID.fromString(children[i]));
					Node copy = nodes.get(searchedNode);
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
		securityManager.addRole(newPortfolioUuid, "all", userId);

		/// Check base portfolio's public state and act accordingly
		if (portfolioRepository.isPublic(portfolioId))
			groupManager.setPublicState(userId, newPortfolioUuid, true);

		return newPortfolioUuid;
	}

	private Portfolio add(UUID rootNodeId, Long userId, PortfolioDocument portfolio) throws JsonProcessingException, BusinessException {

		Optional<Node> rootNode = nodeRepository.findById(rootNodeId);
		Optional<Credential> credential = credentialRepository.findById(userId);

		if (rootNode.isPresent() || !credential.isPresent()) {
			throw new IllegalArgumentException();
		}

		Credential cr = credential.get();
		
		NodeDocument asmRoot = portfolio.getRoot();

		Map<UUID, UUID> resolve = new HashMap<>();
		
		Portfolio port = new Portfolio();
		port.setCredential(cr);
		port.setModifUserId(cr.getId());
		port.setActive(1);
		port = portfolioRepository.save(port);

		Node node = nodeManager.writeNode(asmRoot, port.getId(), userId, 0, null,
				null, true, resolve, false);

		port.setRootNode(node);
		port = portfolioRepository.save(port);

		credentialRepository.save(cr);

		return port;
	}

	@Override
	public void updateTime(UUID portfolioId) {
		portfolioRepository.findById(portfolioId)
				.ifPresent(portfolio -> portfolioRepository.save(portfolio));
	}

	@Override
	public void updateTimeByNode(UUID nodeId) {
		nodeRepository.findById(nodeId)
				.ifPresent(node -> portfolioRepository.save(node.getPortfolio()));
	}

	@Override
	public List<Portfolio> getPortfolios(Long userId,
								  Long substId,
								  Boolean portfolioActive,
								  Boolean specialProject,
								  String portfolioCode) {

		// INNER JOIN p.rootNode
		// INNET JOIN rootNode.resResource
		// WHERE p.active = :active
		Specification<Portfolio> active = Specification.where((root, query, cb) -> {
			Join<Portfolio, Node> nodeJoin = root.join("rootNode");
			nodeJoin.join("resource");
			
			int pactive = 0;
			if( portfolioActive )
				pactive =1;

			return cb.equal(root.get("active"), pactive);
		});

		// AND p.modifUserId = :modifUserId
		Specification<Portfolio> modifUser = Specification.where((root, query, cb) -> {
			return cb.equal(root.get("modifUserId"), userId);
		});

		Sort sort = Sort.by("rootNode.resResource");
		Specification<Portfolio> spec = active;

		if ( specialProject != null )
		{
			// AND p.rootNode.semantictag LIKE '%karuta-project%'
			Specification<Portfolio> portfolioFilter = Specification.where((root, query, cb) -> {
				Join<Portfolio, Node> rootNode = root.join("rootNode");

				if( specialProject )
					return cb.like(rootNode.get("semantictag"), "%karuta-project%");
				else
					return cb.notLike(rootNode.get("semantictag"), "%karuta-project%");
			});

			spec = spec.and(portfolioFilter);
		}
		
		if ( portfolioCode != null )
		{
			Specification<Portfolio> portfolioFilter = Specification.where((root, query, cb) -> {
				Join<Portfolio, Node> rootNode = root.join("rootNode");

				String condition = String.format("%%%s%%", portfolioCode);
				return cb.like(rootNode.get("code"), condition);
			});

			spec = spec.and(portfolioFilter);
		}

		if (credentialRepository.isAdmin(userId)) {
			return portfolioRepository.findAll(spec, sort);
		} else {
			
			List<Portfolio> owned = portfolioRepository.findAll(spec.and(modifUser), sort);
			List<Portfolio> shared = portfolioRepository.getPortfolioSharedWithRights(userId);

			return Stream.concat(owned.stream(), shared.stream())
					.collect(Collectors.toList());
		}
	}
}
