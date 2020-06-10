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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eportfolium.com.karuta.business.contract.*;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.util.JavaTimeUtil;

@Service
@Transactional
public class PortfolioManagerImpl extends BaseManager implements PortfolioManager {

	static private final Logger logger = LoggerFactory.getLogger(PortfolioManagerImpl.class);

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
			Iterator<Right> iter = rights.values().iterator();
			while (iter.hasNext()) {
				Right r = iter.next();
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

	@Override
	public PortfolioGroupDocument getPortfoliosByPortfolioGroup(Long portfolioGroupId) {
		List<Portfolio> portfolios = portfolioRepository.findByPortfolioGroup(portfolioGroupId);

		return new PortfolioGroupDocument(portfolioGroupId, portfolios.stream()
				.map(p -> new PortfolioDocument(p.getId()))
				.collect(Collectors.toList()));
	}

	@Override
	public PortfolioDocument getPortfolio(UUID portfolioId, Long userId, Long groupId, Integer cutoff)
			throws BusinessException, JsonProcessingException {

		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);

		GroupRights rights = getRightsOnPortfolio(userId, groupId, portfolioId);

		if (!rights.isRead()) {
			userId = credentialRepository.getPublicId();
			/// Vérifie les droits avec le compte publique (dernière chance)
			GroupRights publicRights = groupRightsRepository.getPublicRightsByUserId(rootNode.getId(), userId);
			if (!publicRights.isRead()) {
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
			}
		}

		Long ownerId = portfolioRepository.getOwner(portfolioId);
		boolean owner = ownerId.equals(userId);

        return getPortfolio(portfolioId, rootNode.getId(), userId,
                rights.getGroupRightInfo().getId(), owner, rights.getGroupRightInfo().getLabel(), cutoff);
	}

	@Override
    public String getZippedPortfolio(PortfolioDocument portfolio) throws IOException {
        String filePath = System.getProperty("user.dir") + "/tmp_getPortfolio_" + new Date()
                + ".xml";
        String zipPath = System.getProperty("user.dir") + "/tmp_getPortfolio_" + new Date() + ".zip";

        XmlMapper xmlMapper = new XmlMapper();

        try (PrintWriter ecrire = new PrintWriter(new FileOutputStream(filePath));
             ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipPath))) {
            File file = new File(filePath);

            ecrire.println(xmlMapper.writeValueAsString(portfolio));
            ecrire.flush();

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
            file.delete();

            return zipPath;
        }
    }

	private PortfolioDocument getPortfolio(UUID portfolioId,
										   UUID rootuuid,
										   Long userId,
										   Long groupId,
										   boolean owner,
										   String role,
										   Integer cutoff) throws JsonProcessingException {
		/// Node -> parent
		Map<UUID, Tree> entries = new HashMap<>();
		List<Pair<Node, GroupRights>> structure = getPortfolioStructure(portfolioId, userId, groupId);

		processQuery(structure, entries, role);

		structure = getSharedStructure(portfolioId, userId, cutoff);

		if (structure != null) {
			processQuery(structure, entries, role);
		}

		/// Reconstruct functional tree
		Tree root = entries.get(rootuuid);

		if (root != null)
			reconstructTree(root, entries);

		return new PortfolioDocument(portfolioId, owner, "", entries.values().stream()
				.map(t -> t.node)
				.collect(Collectors.toList()));
	}

	private List<Pair<Node, GroupRights>> getPortfolioStructure(UUID portfolioId, Long userId, Long groupId) {

		List<Pair<Node, GroupRights>> portfolioStructure = new ArrayList<>();

		Node rootNode = portfolioRepository.getPortfolioRootNode(portfolioId);

		// Cas admin, designer, owner
		if (rootNode != null
				&& (credentialRepository.isAdmin(userId)
						|| credentialRepository.isDesigner(userId, rootNode.getId())
						|| userId == portfolioRepository.getOwner(portfolioId))) {
			List<Node> nodes = nodeRepository.getNodesWithResources(portfolioId);
			for (Node node : nodes) {
				GroupRights rights = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true, true, true, true, true);
				portfolioStructure.add(Pair.of(node, rights));
			}
		}
		/// FIXME: Il faudrait peut-être prendre une autre stratégie pour sélectionner
		/// les bonnes données : Cas propriétaire OU cas general (via les droits
		/// partagés)
		else if (hasRights(userId, portfolioId)) {
			Map<UUID, GroupRights> rights = new HashMap<>();

			String login = credentialRepository.getLoginById(userId);
//				FIXME: Devrait peut-être verifier si la personne a les droits d'y accéder?
			List<GroupRights> grList = groupRightsRepository.getPortfolioAndUserRights(portfolioId, login,
					groupId);
			for (GroupRights gr : grList) {
				if (rights.containsKey(gr.getGroupRightsId())) {
					GroupRights original = rights.get(gr.getGroupRightsId());
					original.setRead(Boolean.logicalOr(gr.isRead(), original.isRead()));
					original.setWrite(Boolean.logicalOr(gr.isWrite(), original.isWrite()));
					original.setDelete(Boolean.logicalOr(gr.isDelete(), original.isDelete()));
					original.setSubmit(Boolean.logicalOr(gr.isSubmit(), original.isSubmit()));
					original.setAdd(Boolean.logicalOr(gr.isAdd(), original.isAdd()));
				} else {
					rights.put(gr.getGroupRightsId(), gr);
				}
			}

			List<Node> nodes = nodeRepository.getNodes(new ArrayList<>(rights.keySet()));

			// Sélectionne les données selon la filtration
			for (Node node : nodes) {
				if (rights.containsKey(node.getId())) { // Verification des droits
					GroupRights groupRights = rights.get(node.getId());
					if (groupRights.isRead()) { // On doit au moins avoir le droit de lecture
						portfolioStructure.add(Pair.of(node, groupRights));
					}
				}
			}

		} else if (portfolioRepository.isPublic(portfolioId)) // Public case, looks like previous query, but with
		{
			List<Node> nodes = nodeRepository.getNodesWithResources(portfolioId);
			for (Node node : nodes) {
				GroupRights groupRights = new GroupRights(true, false, false, false);
				portfolioStructure.add(Pair.of(node, groupRights));
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
	 * @param cutoff
	 * @return
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
	public PortfolioDocument getPortfolioByCode(String portfolioCode, Long userId, Long groupId,
			boolean resources) throws BusinessException, JsonProcessingException {
		Portfolio portfolio = portfolioRepository.getPortfolioFromNodeCode(portfolioCode);

		if (portfolio == null) {
			return null;
		}

		if (resources) {
			return getPortfolio(portfolio.getId(), userId, groupId, null);
		} else {
			PortfolioDocument document = new PortfolioDocument(portfolio.getId());
			document.setNodes(Collections.singletonList(nodeManager.getNode(portfolio.getRootNode().getId(), false, "nodeRes", userId,
					groupId, null, false)));
			return document;
		}
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
				if (!gu.isEmpty()) {
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
	public Portfolio changePortfolioConfiguration(UUID portfolioId, Boolean portfolioActive) {

		Optional<Portfolio> portfolio = portfolioRepository.findById(portfolioId);

		if (portfolio.isPresent()) {
			Portfolio p = portfolio.get();
			p.setActive(portfolioActive ? 1 : 0);

			portfolioRepository.save(p);

			return p;
		} else {
			return null;
		}
	}

	@Override
	public PortfolioList getPortfolios(long userId, Boolean active, long substid, Boolean project) {
		List<Portfolio> portfolios = getPortfolios(userId, substid, active, project);

		return new PortfolioList(portfolios.stream()
                .map(p -> new PortfolioDocument(p, p.getModifUserId().equals(userId)))
                .collect(Collectors.toList()));
	}

	@Override
	public boolean rewritePortfolioContent(PortfolioDocument portfolio, UUID portfolioId, Long userId,
										   Boolean portfolioActive) throws BusinessException, JsonProcessingException {

		NodeDocument rootNode = portfolio.getNodes().stream()
				.filter(n -> n.getType().equals("asmRoot"))
				.findFirst()
				.orElseThrow(() -> new GenericBusinessException("No root node found"));

		UUID rootNodeUuid = portfolio.getId() != null ? portfolio.getId() : UUID.randomUUID();

		Portfolio portfolioRecord = portfolioRepository.findById(portfolioId)
										.orElse(add(rootNodeUuid, null, userId, new Portfolio()));

		if (userId == null || userId == 0L) {
			userId = portfolioRecord.getCredential().getId();
		}

		nodeManager.writeNode(rootNode, portfolioRecord.getId(), portfolioRecord.getModelId(), userId, 0, null, null, false, false,
				true, null, false);

		// On récupère le noeud root généré précédemment et on l'affecte au portfolio.
		portfolioRecord.setRootNode(nodeRepository.getRootNodeByPortfolio(portfolioRecord.getId()));
		portfolioRecord.setActive(portfolioActive ? 1 : 0);

		portfolioRepository.save(portfolioRecord);

		return true;
	}

	class Right {
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
			GroupRights groupRights = null;

			for (Entry<String, GroupRight> entry : resolve.resolve.entrySet()) {
				GroupRight gr = entry.getValue();

				for (Entry<String, Right> rightelem : gr.rights.entrySet()) {
					String group = rightelem.getKey();
					long grid = resolve.groups.get(group);
					Right rightval = rightelem.getValue();
					groupRights = new GroupRights();
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

			// Rendre le portfolio public si nécessaire
			if (setPublic)
				groupManager.setPublicState(userId, portfolioId, true);
		}

		return portfolioId;
	}

	@Override
	public PortfolioList addPortfolio(PortfolioDocument portfolioDocument, long userId, long groupId, UUID portfolioModelId,
							   boolean parseRights, String projectName)
			throws BusinessException, JsonProcessingException {
		if (!credentialRepository.isAdmin(userId) && !credentialRepository.isCreator(userId))
			throw new GenericBusinessException("FORBIDDEN : No admin right");

		// Si le modèle est renseigné, on ignore le XML poste et on récupère le contenu
		// du modèle a la place
		// FIXME Inutilisé, nous instancions / copions un portfolio
		if (portfolioModelId != null)
			portfolioDocument = getPortfolio(portfolioModelId, userId, groupId, null);

		Optional<NodeDocument> nodeDocument = portfolioDocument.getNodes()
					.stream()
					.filter(n -> n.getType().equals("asmRoot")
									&& n.getResources() != null
									&& !n.getResources().isEmpty())
					.findFirst();

		if (!nodeDocument.isPresent()) {
			throw new GenericBusinessException("Exception handling node");
		}

		NodeDocument rootNode = nodeDocument.get();

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

		Portfolio portfolio = add(uuid, null, userId, new Portfolio());

		nodeManager.writeNode(rootNode, portfolio.getId(), portfolioModelId, userId, 0, uuid, null,
				false, false, false, null, parseRights);

		// On récupère le noeud root généré précédemment et on l'affecte au portfolio.
		portfolio.setRootNode(nodeRepository.getRootNodeByPortfolio(portfolio.getId()));

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

		return new PortfolioList(
				Collections.singletonList(new PortfolioDocument(portfolio.getId())));
	}

	@Override
	public String importZippedPortfolio(String path, String userName, InputStream inputStream, Long userId, Long groupId,
										String modelId, Long credentialSubstitutionId,
										boolean parseRights, String projectName) throws BusinessException, IOException {
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

            portfolio = new Portfolio();

            PortfolioDocument document = new XmlMapper()
                    .readerFor(PortfolioDocument.class)
                    .readValue(xml);

            Optional<NodeDocument> asmRoot = document.getNodes().stream()
                    .filter(n -> n.getType().equals("asmRoot"))
                    .findFirst();

            if (!asmRoot.isPresent())
                continue;

            Optional<ResourceDocument> resource = asmRoot.get()
                    .getResources()
                    .stream()
                    .findFirst();

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

            add(uuid, null, userId, portfolio);

            nodeManager.writeNode(asmRoot.get(), portfolio.getId(), null, userId, 0, uuid,
                    null, false, false, false, resolve, parseRights);

            // On récupère le noeud root généré précédemment et on l'affecte au portfolio.
            portfolio.setRootNode(nodeRepository.getRootNodeByPortfolio(portfolio.getId()));
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
        }

		File zipfile = new File(filename);
		zipfile.delete();
		File zipdir = new File(outsideDir + portfolioUuidPreliminaire + File.separator);
		zipdir.delete();

		return portfolio.getId().toString();
	}

	@Override
	public String instanciatePortfolio(String portfolioId, String srccode, String tgtcode, Long id,
			int groupId, boolean copyshared, String groupname, boolean setOwner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int addPortfolioInGroup(UUID portfolioId, Long portfolioGroupId, String label) {
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
	public PortfolioGroupList getPortfolioGroupListFromPortfolio(UUID portfolioId) {
		List<PortfolioGroupMembers> groupMembers = portfolioGroupMembersRepository.getByPortfolioID(portfolioId);

		return new PortfolioGroupList(groupMembers.stream()
				.map(PortfolioGroupMembers::getPortfolioGroup)
				.map(PortfolioGroupDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public Long addPortfolioGroup(String groupname, String type, Long parentId) {
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

	@Override
	public String getRoleByPortfolio(String role, UUID portfolioId) {
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
	public GroupInfoList getRolesByPortfolio(UUID portfolioId, Long userId) {
		GroupRights rights = getRightsOnPortfolio(userId, 0L, portfolioId);
		if (!rights.isRead())
			return null;

		List<GroupInfo> groupInfos = groupInfoRepository.getByPortfolio(portfolioId);

		return new GroupInfoList(groupInfos.stream()
				.map(GroupInfoDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	@PreAuthorize("hasRole('admin')")
	public GroupRightInfoList getGroupRightsInfos(UUID portfolioId) {
		List<GroupRightInfo> groups = groupRightInfoRepository.getByPortfolioID(portfolioId);

		return new GroupRightInfoList(groups.stream()
				.map(GroupRightInfoDocument::new)
				.collect(Collectors.toList()));
	}

	@Override
	public UUID copyPortfolio(UUID portfolioId, String srcCode, String newCode, Long userId,
							  boolean setOwner) {
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

	private String passwdGen(Integer length) {
		Random random = new Random();

		Double num_bytes = Math.ceil(length * 0.75);
		byte[] bytes = new byte[num_bytes.intValue()];
		random.nextBytes(bytes);
		return new String(Base64.encodeBase64(bytes)).replaceAll("\\s+$", "").substring(0, length);
	}

	private Portfolio add(UUID rootNodeId, UUID modelId, Long userId, Portfolio portfolio) {
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

		if (!rootNode.isPresent() || !credential.isPresent()) {
			throw new IllegalArgumentException();
		}

		Node node = rootNode.get();
		Credential cr = credential.get();

		portfolio.setRootNode(node);
		portfolio.setCredential(cr);

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
