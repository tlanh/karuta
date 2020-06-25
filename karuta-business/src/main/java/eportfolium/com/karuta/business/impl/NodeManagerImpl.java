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

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.util.InMemoryCache;

@Service
@Transactional
public class NodeManagerImpl extends BaseManagerImpl implements NodeManager {

	static private final Logger log = LoggerFactory.getLogger(NodeManagerImpl.class);

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private PortfolioManager portfolioManager;

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private NodeRepository nodeRepository;

	@Autowired
	private CredentialRepository credentialRepository;

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private GroupRightInfoRepository groupRightInfoRepository;

	@Autowired
	private GroupInfoRepository groupInfoRepository;

	@Autowired
	private GroupUserRepository groupUserRepository;

	private InMemoryCache<UUID, List<Node>> cachedNodes = new InMemoryCache<>(600, 1500, 6);

	@Override
	public NodeDocument getNode(UUID nodeId, boolean withChildren, Long userId, Long groupId,
			String label, Integer cutoff) throws BusinessException, JsonProcessingException {
		final GroupRights rights = getRights(userId, groupId, nodeId);

		if (!rights.isRead()) {
			userId = credentialRepository.getPublicId();

			/// Vérifie les droits avec le compte publique (dernière chance)
			if (!nodeRepository.isPublic(nodeId))
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
		}

		List<Pair<Node, GroupRights>> nodes = getNodePerLevel(nodeId, userId, rights.getGroupRightInfo().getId(),
				cutoff);

		/// Node -> parent
		Map<UUID, Tree> entries = new HashMap<>();
		processQuery(nodes, entries, rights.getGroupRightInfo().getLabel());

		/// Reconstruct functional tree
		Tree root = entries.get(nodeId);
		reconstructTree(root, entries);

		// TODO: Check result ; not sure the output is expected
		return entries.get(nodeId).node;
	}

	@Override
	public UUID writeNode(NodeDocument node, UUID portfolioId, UUID portfolioModelId, Long userId,
						  int ordrer, UUID forcedId, UUID forcedParentId, boolean sharedResParent,
						  boolean sharedNodeResParent, boolean rewriteId, Map<UUID, UUID> resolve, boolean parseRights)
			throws BusinessException, JsonProcessingException {

		if (node == null)
			return null;

		boolean sharedRes = false;
		boolean sharedNode = false;
		boolean sharedNodeRes = false;

		final String asmType = node.getType();
		final String xsiType = node.getXsiType();
		final String semtag = node.getSemtag();
		final String format = node.getFormat();
		final String label = node.getLabel();
		final String descr = node.getDescription();

		String metadataStr = "";
		String metadataWadStr = "";
		String metadataEpmStr = "";

		String code = node.getCode();
		String semanticTag = null;

		final UUID nodeId = rewriteId ? node.getId() : (forcedId != null ? forcedId : UUID.randomUUID());

		if (resolve != null) // Mapping old id -> new id
			resolve.put(node.getId(), nodeId);

		// If we are dealing with the root of the tree, we need to ensure
		// that the node's type is "asmRoot".
		if (nodeId != null && node.getParent() != null) {
			if (!asmType.equals("asmRoot") && portfolioId == null)
				throw new GenericBusinessException("Missing node with 'asmRoot' type");

			List<ResourceDocument> resourceDocuments = node.getResources();

			if (!resourceDocuments.isEmpty()) {
				code = resourceDocuments.get(0).getCode();
			}
		}


		if (parseRights && node.getMetadataWad() != null) {
			MetadataWadDocument metadataWad = node.getMetadataWad();
			metadataWadStr = xmlAttributes(metadataWad);

			if (metadataWad.getSeenoderoles() != null) {
				for (String role : metadataWad.getSeenoderoles().split(" ")) {
					groupManager.addGroupRights(role, nodeId, GroupRights.READ, portfolioId,
							userId);
				}
			}

			if (metadataWad.getDelnoderoles() != null) {
				for (String role : metadataWad.getDelnoderoles().split(" ")) {
					groupManager.addGroupRights(role, nodeId, GroupRights.DELETE, portfolioId,
							userId);
				}
			}

			if (metadataWad.getEditnoderoles() != null) {
				for (String role : metadataWad.getEditnoderoles().split("")) {
					groupManager.addGroupRights(role, nodeId, GroupRights.WRITE, portfolioId,
							userId);
				}
			}

			if (metadataWad.getEditresroles() != null) {
				for (String role : metadataWad.getEditresroles().split(" ")) {
					groupManager.addGroupRights(role, nodeId, GroupRights.WRITE, portfolioId,
							userId);
				}
			}

			if (metadataWad.getSubmitroles() != null) {
				for (String role : metadataWad.getSubmitroles().split(" ")) {
					groupManager.addGroupRights(role, nodeId, GroupRights.SUBMIT, portfolioId,
							userId);
				}
			}

			if (metadataWad.getShowtoroles() != null) {
				for (String role : metadataWad.getShowtoroles().split(" ")) {
					groupManager.addGroupRights(role, nodeId, GroupRights.NONE, portfolioId,
							userId);
				}
			}

			if (metadataWad.getNotifyroles() != null) {
				groupManager.changeNotifyRoles(portfolioId, nodeId,
						metadataWad.getNotifyroles().replace(" ", ","));
			}

		}

		if (node.getMetadataEpm() != null) {
			metadataEpmStr = xmlAttributes(node.getMetadataEpm());
		}

		if (node.getMetadata() != null) {
			MetadataDocument metadata = node.getMetadata();

			groupManager.setPublicState(userId, portfolioId, metadata.getPublic());

			if (metadata.getSharedResource())
				sharedRes = true;
			if (metadata.getSharedNode())
				sharedNode = true;
			if (metadata.getSharedNodeResource())
				sharedNodeRes = true;

			semanticTag = metadata.getSemantictag();

			metadataStr = xmlAttributes(metadata);
		}

		UUID newNodeId = add(nodeId, "", asmType, xsiType, sharedRes, sharedNode, sharedNodeRes,
					metadataStr, metadataWadStr, metadataEpmStr, semtag,
					semanticTag, label, code, descr, format, ordrer, userId, portfolioId);

		// Loop through children to go down in the tree.
		if (!node.getChildren().isEmpty()) {
			int k = 0;

			for (NodeDocument child : node.getChildren()) {
				UUID childId = null;

				if (!rewriteId)
					childId = UUID.randomUUID();

				writeNode(child, portfolioId, portfolioModelId, userId, k, childId, newNodeId, sharedRes,
							sharedNodeRes, rewriteId, resolve, parseRights);
				k++;
			}
		}

		updateNode(forcedParentId);

		return newNodeId;
	}

	@Override
	public NodeDocument getNode(UUID nodeId, boolean withChildren, String withChildrenOfXsiType, Long userId,
			Long groupId, String label, boolean checkSecurity) throws JsonProcessingException {
		if (checkSecurity) {
			GroupRights rights = getRights(userId, groupId, nodeId);

			// If the user doesn't have the right to see this node, we
			// check whether the public account has such right.
			if (!rights.isRead()) {
				Long publicId = credentialRepository.getPublicId();

				rights = groupRightsRepository.getPublicRightsByUserId(nodeId, publicId);
				if (!rights.isRead())
					return null;
			}
		}

		Optional<Node> nodeOptional = nodeRepository.findById(nodeId);

		if (!nodeOptional.isPresent())
			return null;

		Node node = nodeOptional.get();
		NodeDocument nodeDocument;

		if (node.getSharedNodeUuid() != null) {
			nodeDocument = getNode(node.getSharedNodeUuid(), true, null, userId, groupId,
					null, true);
		} else {
			nodeDocument = new NodeDocument(node);

			MetadataWadDocument metadataWad = MetadataWadDocument.from(node.getMetadataWad());
			MetadataEpmDocument metadataEpm = MetadataEpmDocument.from(node.getMetadataEpm());
			MetadataDocument metadataNode = MetadataDocument.from(node.getMetadata());

			nodeDocument.setCode(node.getCode());
			nodeDocument.setLabel(node.getLabel());
			nodeDocument.setDescription(node.getDescr());
			nodeDocument.setSemtag(node.getSemtag());

			List<ResourceDocument> resources = new ArrayList<>();

			if (node.getResResource() != null) {
				resources.add(new ResourceDocument(node.getResResource(), node));
			}

			if (node.getContextResource() != null) {
				resources.add(new ResourceDocument(node.getContextResource(), node));
			}

			if (node.getResource() != null) {
				resources.add(new ResourceDocument(node.getResource(), node));
			}

			nodeDocument.setMetadata(metadataNode);
			nodeDocument.setMetadataEpm(metadataEpm);
			nodeDocument.setMetadataWad(metadataWad);
			nodeDocument.setResources(resources);
		}

		if (withChildren || withChildrenOfXsiType != null) {
			if (StringUtils.isNotEmpty(node.getChildrenStr())) {
				List<UUID> uuids = Arrays.stream(node.getChildrenStr().split(","))
									.map(UUID::fromString)
									.collect(Collectors.toList());

				List<NodeDocument> children = new ArrayList<>();

				for (UUID uuid : uuids) {
					Node child = nodeRepository.findById(uuid).get();

					if (withChildrenOfXsiType == null
							|| withChildrenOfXsiType.equals(child.getXsiType())) {
						children.add(getNode(uuid, true, null, userId, groupId, null, true));
					}
				}

				nodeDocument.setChildren(children);
			}
		}

		return nodeDocument;
	}

	@Override
	public NodeDocument getNodeBySemanticTag(UUID portfolioId, String semantictag, Long userId,
			Long groupId) throws BusinessException, JsonProcessingException {

		final List<Node> nodes = nodeRepository.getNodesBySemanticTag(portfolioId, semantictag);

		if (nodes.isEmpty()) {
			return null;
		}

		// On récupère d'abord l'uuid du premier noeud trouve correspondant au
		// semantictag
		UUID nodeId = nodes.get(0).getId();

		if (!hasRight(userId, groupId, nodeId, GroupRights.READ)) {
			throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
		}

		return getNode(nodeId, true, null, userId, groupId, null, true);
	}

	@Override
	public NodeList getNodesBySemanticTag(Long userId, Long groupId, UUID portfolioId,
																		  String semanticTag) throws BusinessException {
		List<Node> nodes = nodeRepository.getNodesBySemanticTag(portfolioId, semanticTag);

		if (nodes.stream()
				.anyMatch(n -> !hasRight(userId, groupId, n.getId(), GroupRights.READ))) {
			throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
		}

		return new NodeList(nodes.stream()
				.map(n -> new NodeDocument(n.getId()))
				.collect(Collectors.toList()));
	}

	@Override
	public boolean isCodeExist(String code) {
		return nodeRepository.isCodeExist(code);
	}

	@Override
	public UUID getPortfolioIdFromNode(Long userId, UUID nodeId) throws BusinessException {
		// Admin, or if user has a right to read can fetch this information
		if (!credentialRepository.isAdmin(userId) && !hasRight(userId, 0L, nodeId, GroupRights.READ)) {
			throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
		}

		Node n = nodeRepository.findById(nodeId).get();
		return n.getPortfolio().getId();
	}

	@Override
	public String executeMacroOnNode(long userId, UUID nodeId, String macroName)
            throws BusinessException, JsonProcessingException {

        /// Selection du grid de l'utilisateur
        GroupRights gr = groupRightsRepository.getPublicRightsByUserId(nodeId, userId);
        Long grid = null;
        String label = "";

        if (gr != null) {
            grid = gr.getGroupRightInfo().getId();
            label = gr.getGroupRightInfo().getLabel();
        }

        String tmp = nodeRepository.getMetadataWad(nodeId);

        // FIXME: Check if user has indeed the right to

        MetadataWadDocument document = MetadataWadDocument.from(tmp);

        long resetgroup = getRoleByNode(1L, nodeId, "resetter");

        // Admin or part of "resetter" group
        if ("reset".equals(macroName)
                && (credentialRepository.isAdmin(userId)
                    || securityManager.userHasRole(userId, resetgroup))) {

            resetRights(getChildren(nodeId));

        } else if ("show".equals(macroName) || "hide".equals(macroName)) {

            String roles = document.getShowroles();

            if (roles.contains(label)) {
                String showto = document.getShowtoroles();

                if (!"".equals(showto)) {
                    updateNodeRights(nodeId, Arrays.asList(showto.split(" ")), macroName);

                    if ("hide".equals(macroName))
                        document.setPrivate(true);
                    else
                        document.setPrivate(false);
                }
            }

            Node n = nodeRepository.findById(nodeId).get();
            n.setMetadataWad(xmlAttributes(document));
            nodeRepository.save(n);

        } else if ("submit".equals(macroName)) {
            List<Node> children = getChildren(nodeId);

            boolean updated = updateNodesRights(children, grid);

            if (!updated)
                return "unchanged";

            /// FIXME: This part might be deprecated in the near future
            String showto = "";

            if (document.getShowtoroles() != null)
                showto = document.getShowtoroles().replace(" ", "','");

            if (!"".equals(showto)) {
                updateNodeRights(nodeId, Collections.singletonList(showto), "show");
                document.setPrivate(false);
            }

            document.setSubmitted(true);
            document.setSubmitteddate(new Date());

            Node n = nodeRepository.findById(nodeId).get();
            n.setMetadataWad(xmlAttributes(document));
            nodeRepository.save(n);

        } else if ("submitall".equals(macroName)) {
            List<Node> children = getChildren(nodeId);

            log.info("ACTION: " + macroName + " grid: " + grid + " -> uuid: " + nodeId);

            /// Insert/replace existing editing related rights
            /// Same as submit, except we don't limit to user's own group right
            boolean hasChanges = updateAllNodesRights(children, grid);

            if (!hasChanges)
                return "unchanged";

            document.setSubmitted(true);
            document.setSubmitteddate(new Date());

            Node n = nodeRepository.findById(nodeId).get();
            n.setMetadataWad(xmlAttributes(document));
            nodeRepository.save(n);
        }

		return "OK";
	}

	@Override
	public void resetRights(List<Node> children) throws JsonProcessingException {
		Map<UUID, Map<String, GroupRights>> resolve = new HashMap<>();

		for (Node child : children) {
			UUID uuid = child.getId();
			String meta = child.getMetadataWad();

			final Map<String, GroupRights> existing = resolve.get(uuid);
			final Map<String, GroupRights> rolesMap = existing != null ? existing : new HashMap<>();

			if (rolesMap == null) {
				resolve.put(uuid, rolesMap);
			}

			// FIXME MARCHE PAS !
			MetadataWadDocument metadata = MetadataWadDocument.from(meta);

			if (metadata.getSeenoderoles() != null) {
				for (String role : metadata.getSeenoderoles().split(" ")) {
					if (!rolesMap.containsKey(role)) {
						GroupRights gr = new GroupRights();
						gr.setRead(true);
						rolesMap.put(role, gr);
					}
				}
			}

			if (metadata.getShowtoroles() != null) {
				for (String role : metadata.getShowtoroles().split(" ")) {
					if (!rolesMap.containsKey(role)) {
						GroupRights gr = new GroupRights();
						gr.setRead(false);
						rolesMap.put(role, gr);
					}
				}
			}

			if (metadata.getDelnoderoles() != null) {
				for (String role : metadata.getDelnoderoles().split(" ")) {
					if (!rolesMap.containsKey(role)) {
						GroupRights gr = new GroupRights();
						gr.setDelete(true);
						rolesMap.put(role, gr);
					}
				}
			}

			if (metadata.getEditnoderoles() != null) {
				for (String role : metadata.getEditnoderoles().split(" ")) {
					if (!rolesMap.containsKey(role)) {
						GroupRights gr = new GroupRights();
						gr.setWrite(true);
						rolesMap.put(role, gr);
					}
				}
			}

			if (metadata.getSubmitroles() != null) {
				for (String role : metadata.getSubmitroles().split(" ")) {
					if (!rolesMap.containsKey(role)) {
						GroupRights gr = new GroupRights();
						gr.setSubmit(true);
						rolesMap.put(role, gr);
					}
				}
			}

			if (metadata.getEditresroles() != null) {
				for (String role : metadata.getEditresroles().split(" ")) {
					if (!rolesMap.containsKey(role)) {
						GroupRights gr = new GroupRights();
						gr.setWrite(true);
						rolesMap.put(role, gr);
					}
				}
			}

			if (metadata.getMenuroles() != null) {
				for (String role : metadata.getMenuroles().split(";")) {
					/// Given format:
					/// code_portfolio,tag_semantique,label@en/libelle@fr,reles[;autre menu]
					String[] tokens = role.split(",");
					String menurolename = tokens[3];

					if (menurolename != null) {
						for (String s : menurolename.split(" "))
							rolesMap.put(s.trim(), new GroupRights());
					}
				}
			}

			if (metadata.getNotifyroles() != null) {
				/// Format pour l'instant: notifyroles="sender responsable"
				String merge = Arrays.stream(metadata.getNotifyroles().split(" "))
									.collect(Collectors.joining(","));

				for (GroupRights value : rolesMap.values()) {
					value.setNotifyRoles(merge);
				}
			}

			/// Now remove mention to being submitted
			Node n = nodeRepository.findById(uuid).get();
			n.setMetadataWad(xmlAttributes(metadata));

			nodeRepository.save(n);

			/// Ajout des droits des noeuds FIXME
			GroupRightsId grId = new GroupRightsId();
			GroupRightInfo gri = null;

			Iterator<Entry<UUID, Map<String, GroupRights>>> rights = resolve.entrySet().iterator();

			while (rights.hasNext()) {
				Entry<UUID, Map<String, GroupRights>> entry = rights.next();
				uuid = entry.getKey();
				Map<String, GroupRights> gr = entry.getValue();

				Iterator<Entry<String, GroupRights>> rightiter = gr.entrySet().iterator();
				while (rightiter.hasNext()) {
					Entry<String, GroupRights> rightElem = rightiter.next();
					String group = rightElem.getKey();

					GroupRights gr2 = groupRightsRepository.getRightsByIdAndLabel(uuid, group);
					if (gr2 != null)
						gri = gr2.getGroupRightInfo();

					GroupRights rightValue = rightElem.getValue();
					grId.setGroupRightInfo(gri);
					grId.setId(uuid);
					GroupRights toUpdate = groupRightsRepository.findById(grId).get();

					toUpdate.setRead(rightValue.isRead());
					toUpdate.setWrite(rightValue.isWrite());
					toUpdate.setDelete(rightValue.isDelete());
					toUpdate.setSubmit(rightValue.isSubmit());
					toUpdate.setAdd(rightValue.isAdd());
					toUpdate.setTypesId(rightValue.getTypesId());
					toUpdate.setRulesId(rightValue.getRulesId());
					toUpdate.setNotifyRoles(rightValue.getNotifyRoles());

					groupRightsRepository.save(toUpdate);
				}
			}
		}
	}

	@Override
	public long getRoleByNode(Long userId, UUID nodeId, String role) throws BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("FORBIDDEN : No admin right");

		// Check if role exists already
		Long groupId = groupRightInfoRepository.getIdByNodeAndLabel(nodeId, role);

		// If not, create it
		if (groupId == null) {
			Node n = nodeRepository.findById(nodeId).get();

			GroupRightInfo gri = new GroupRightInfo(n.getPortfolio(), role);
			groupRightInfoRepository.save(gri);

			GroupInfo groupInfo = new GroupInfo();

			groupInfo.setGroupRightInfo(new GroupRightInfo(gri.getId()));
			groupInfo.setLabel(role);
			groupInfo.setOwner(1L);

			groupInfoRepository.save(groupInfo);

			groupId = groupInfo.getId();
		}
		return groupId;
	}

	@Override
	public MetadataWadDocument getNodeMetadataWad(UUID nodeId, Long userId, Long groupId)
			throws BusinessException, JsonProcessingException {

		GroupRights rightsOnNode = getRights(userId, groupId, nodeId);

		if (!rightsOnNode.isRead()) {
			throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
		}

		Optional<Node> node = nodeRepository.findById(nodeId);

		if (!node.isPresent() || !node.get().getAsmType().equals("asmResource"))
			return null;

		return MetadataWadDocument.from(node.get().getMetadataWad());
	}

	@Override
	public Integer changeNode(UUID nodeId, NodeDocument node, Long userId, Long groupId) throws BusinessException, JsonProcessingException {
		if (!hasRight(userId, groupId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 Forbidden : no write credential ");

		if (node == null)
			return null;

		String asmType = node.getType();
		String xsiType = node.getXsiType();
		String semtag = node.getSemtag();
		String format = node.getFormat();
		String label = node.getLabel();
		String code = node.getCode();
		String descr = node.getDescription();

		String metadataStr = "";
		String metadataWadStr = "";
		String metadataEpmStr = "";

		boolean sharedRes = false;
		boolean sharedNode = false;
		boolean sharedNodeRes = false;

		for (ResourceDocument resourceDocument : node.getResources()) {
			resourceManager.updateResource(nodeId, resourceDocument.getXsiType(),
					resourceDocument.getContent(), userId);
		}

		if (node.getMetadataWad() != null) {
			metadataWadStr = xmlAttributes(node.getMetadataWad());
		}

		if (node.getMetadataEpm() != null) {
			metadataEpmStr = xmlAttributes(node.getMetadataEpm());
		}

		if (node.getMetadata() != null) {
			if (node.getMetadata().getSharedResource())
				sharedRes = true;
			if (node.getMetadata().getSharedNode())
				sharedNode = true;
			if (node.getMetadata().getSharedNodeResource())
				sharedNodeRes = true;

			metadataStr = xmlAttributes(node.getMetadata());
		}

		int order = 0;

		for (NodeDocument child : node.getChildren()) {
			updateNodeOrder(child.getId(), order);
			order++;
		}

		// TODO UpdateNode different selon creation de modèle ou instantiation copie
		if (!node.getChildren().isEmpty())
			updateNode(nodeId);

		portfolioManager.updateTimeByNode(nodeId);

		return update(nodeId, asmType, xsiType, semtag, label, code, descr, format, metadataStr,
				metadataWadStr, metadataEpmStr, sharedRes, sharedNode, sharedNodeRes, userId);
	}

	@Override
	public String changeNodeMetadataWad(UUID nodeId, MetadataWadDocument metadata, Long userId,
										Long groupId) throws BusinessException, JsonProcessingException {

		if (!hasRight(userId, groupId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		Node node = nodeRepository.findById(nodeId).get();
		node.setMetadataWad(xmlAttributes(metadata));
		nodeRepository.save(node);
		portfolioManager.updateTimeByNode(nodeId);

		return "editer";
	}

	@Override
	public void removeNode(UUID nodeId, Long userId, long groupId) throws BusinessException {
		final GroupRights rights = getRights(userId, groupId, nodeId);

		if (!rights.isDelete())
			if (!credentialRepository.isAdmin(userId)
					&& !credentialRepository.isDesigner(userId, nodeId))
				throw new GenericBusinessException("403 FORBIDDEN, No admin right");

		UUID parentid = null;

		/// Copy portfolio base info
		final Node baseNodeToRemove = nodeRepository.findById(nodeId).get();

		/// Portfolio id, nécessaire pour plus tard !
		final UUID portfolioId = baseNodeToRemove.getPortfolio().getId();
		final List<Node> t_nodes = nodeRepository.getNodesWithResources(portfolioId);

		/// Trouver un parent pour réorganiser les enfants restants.
		if (baseNodeToRemove.getParentNode() != null)
			parentid = baseNodeToRemove.getParentNode().getId();

		final Set<Node> nodesToDelete = new LinkedHashSet<Node>();
		final Set<Resource> resourcesToDelete = new LinkedHashSet<>();
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
			t_set_parentid = new LinkedHashSet<>();
			t_set_parent = new LinkedHashSet<>();

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
			added = !t_set_parentid.isEmpty(); // On s'arrete quand rien n'a été ajouté
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
		portfolioManager.updateTimeByNode(nodeId);

		// On efface les noeuds
		nodeRepository.deleteAll(nodesToDelete);

		// On efface les ressources
		resourceRepository.deleteAll(resourcesToDelete);

		if (parentid != null)
			updateNode(parentid);

		System.out.println("deleteNode :" + nodeId);
	}

	@Override
	public boolean changeParentNode(Long userid, UUID nodeId, UUID parentId) throws BusinessException {
		if (!credentialRepository.isAdmin(userid) && !credentialRepository.isDesigner(userid, nodeId))
			throw new GenericBusinessException("FORBIDDEN 403 : No admin right");

		// Pour qu'un noeud ne s'ajoute pas lui-même comme noeud parent
		if (nodeId.equals(parentId))
			return false;

		boolean status = false;
		try {
			Node n = nodeRepository.findById(nodeId).get();

			UUID puuid = null;
			if (n != null && n.getParentNode() != null) {
				puuid = n.getParentNode().getId();
			}

			Integer next = nodeRepository.getNodeNextOrderChildren(parentId);

			n.setParentNode(new Node(parentId));
			n.setNodeOrder(next);

			nodeRepository.save(n);

			// Mettre à jour la liste d'enfants pour le noeud d'origine et le noeud de
			// destination.
			updateNode(puuid);
			updateNode(parentId);

			portfolioManager.updateTimeByNode(nodeId);

			status = true;
		} catch (Exception e) {

		}
		return status;
	}

	@Override
	public Long moveNodeUp(UUID nodeId) {
		Long status = -1L;

		Node n = nodeRepository.findById(nodeId).get();
		int order = -1;
		UUID puuid = null;
		if (n != null) {
			order = n.getNodeOrder();
			puuid = n.getParentNode().getId();
		}

		if (order == 0) {
			status = -2L;
		} else if (order > 0) {
			final List<Node> nodes = nodeRepository.getNodesByOrder(puuid, order);
			/// Swap node order
			for (Node node : nodes) {
				if (node.getNodeOrder() == order) {
					node.setNodeOrder(order - 1);
				} else {
					node.setNodeOrder(order);
				}

				nodeRepository.save(node);
			}

			// Mettre à jour la liste des enfants
			updateNode(puuid);

			status = 0L;
			portfolioManager.updateTimeByNode(nodeId);
		}

		return status;
	}

	@Override
	public String changeNodeMetadataEpm(UUID nodeId, MetadataEpmDocument metadata, Long userId, long groupId)
			throws BusinessException, JsonProcessingException {
		if (!hasRight(userId, groupId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("FORBIDDEN 403 : No WRITE credential ");

		Node node = nodeRepository.findById(nodeId).get();
		node.setMetadataEpm(xmlAttributes(metadata));
		nodeRepository.save(node);

		portfolioManager.updateTimeByNode(nodeId);

		return "editer";
	}

	@Override
	public String changeNodeMetadata(UUID nodeId, MetadataDocument metadata, Long userId, long groupId)
			throws BusinessException, JsonProcessingException {

		if (!hasRight(userId, groupId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN, no WRITE credential");

		UUID portfolioUuid = portfolioRepository.getPortfolioUuidFromNode(nodeId);

		// Public has to be managed via the group/user function
		groupManager.setPublicState(userId, portfolioUuid, metadata.getPublic());

		Node n = nodeRepository.findById(nodeId).get();

		n.setMetadata(xmlAttributes(metadata));
		n.setSemantictag(metadata.getSemantictag());
		n.setSharedRes(metadata.getSharedResource());
		n.setSharedNode(metadata.getSharedNode());
		n.setSharedNodeRes(metadata.getSharedNodeResource());

		nodeRepository.save(n);
		portfolioManager.updateTime(portfolioUuid);

		return "editer";
	}

	@Override
	public String changeNodeContext(UUID nodeId, ResourceDocument resource, Long userId, Long groupId)
			throws BusinessException {
		if (!hasRight(userId, groupId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		resourceManager.changeResourceByXsiType(nodeId, "context", resource, userId);

		return "editer";
	}

	@Override
	public String changeNodeResource(UUID nodeId, ResourceDocument resource, Long userId, Long groupId)
			throws BusinessException {
		if (!hasRight(userId, groupId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		resourceManager.changeResourceByXsiType(nodeId, "nodeRes", resource, userId);

		return "editer";
	}

	@Override
	public NodeList addNode(UUID parentNodeId, NodeDocument node, Long userId, Long groupId,
			boolean forcedUuid) throws JsonProcessingException, BusinessException {

		Integer nodeOrder = nodeRepository.getNodeNextOrderChildren(parentNodeId);
		Portfolio portfolio = portfolioRepository.getPortfolioFromNode(parentNodeId);
		UUID portfolioId = null;
		UUID portfolioModelId = null;

		if (portfolio != null) {
			portfolioId = portfolio.getId();
			portfolioModelId = portfolio.getModelId();
		}

		// TODO getNodeRight postNode

		UUID nodeId = writeNode(node, portfolioId, portfolioModelId, userId, nodeOrder, null, parentNodeId,
				false, false, forcedUuid, null, true);

		portfolioManager.updateTimeByNode(portfolioId);

		return new NodeList(Collections.singletonList(new NodeDocument(nodeId)));
	}

	@Override
	public NodeDocument getNodeWithXSL(UUID nodeId, String xslFile, String parameters, Long userId,
			Long groupId) throws BusinessException, JsonProcessingException {
		String result = null;
		/// Préparation des paramètres pour les besoins futurs, format:
		/// "par1:par1val;par2:par2val;..."
		String[] table = parameters.split(";");
		int parSize = table.length;
		String[] param = new String[parSize];
		String[] paramVal = new String[parSize];
		for (int i = 0; i < parSize; ++i) {
			String line = table[i];
			int var = line.indexOf(":");
			param[i] = line.substring(0, var);
			paramVal[i] = line.substring(var + 1);
		}

		return getNode(nodeId, true, userId, groupId, null, null);
	}

	@Override
	public NodeList addNodeFromModelBySemanticTag(UUID nodeId, String semanticTag, Long userId,
			Long groupId) throws BusinessException, JsonProcessingException {
		Portfolio portfolio = portfolioRepository.getPortfolioFromNode(nodeId);

		UUID portfolioModelId = null;

		if (portfolio != null) {
			portfolioModelId = portfolio.getModelId();
		}

		NodeDocument node = getNodeBySemanticTag(portfolioModelId, semanticTag, userId, groupId);

		// C'est le noeud obtenu dans le modèle indiqué par la table de correspondance.
		UUID otherParentNodeUuid = nodeRepository.getNodeUuidByPortfolioModelAndSemanticTag(portfolioModelId, semanticTag);

		return addNode(otherParentNodeUuid, node, userId, groupId, true);
	}

	@Override
	public void changeRights(UUID nodeId, String label, GroupRights rights) {
		GroupRights gr = groupRightsRepository.getRightsByIdAndLabel(nodeId, label);

		if (gr != null) {
			gr.setRead(rights.isRead());
			gr.setWrite(rights.isWrite());
			gr.setDelete(rights.isDelete());
			gr.setSubmit(rights.isSubmit());

			groupRightsRepository.save(gr);
		}
	}

	@Override
	public NodeList getNodes(String rootNodeCode, String childSemtag, Long userId, Long groupId,
			String parentSemtag, String parentNodeCode, Integer cutoff) throws BusinessException {

		UUID pid = portfolioRepository.getPortfolioUuidFromNodeCode(rootNodeCode);
		final NodeList emptyList = new NodeList(Collections.emptyList());

		if (pid == null)
			return emptyList;

		GroupRights rights = portfolioManager.getRightsOnPortfolio(userId, groupId, pid);

		if (!rights.isRead()
				&& !credentialRepository.isAdmin(userId)
				&& !portfolioRepository.isPublic(pid)
				&& !portfolioManager.isOwner(userId, pid))
			throw new GenericBusinessException("403 FORBIDDEN : no admin right");

		// Not null, not empty
		// When we have a set, subset, and code of selected item
		/// Searching nodes subset where semtag is under semtag_parent. First filtering
		// is with parentNodeCode
		if (StringUtils.isNotEmpty(parentSemtag) && StringUtils.isNotEmpty(parentNodeCode)) {
			List<Node> nodes = nodeRepository.getNodes(pid);

			/// Init temp set and hashmap
			final Optional<Node> parentTagNode = nodes.stream()
				.filter(n -> StringUtils.equals(n.getCode(), parentNodeCode)
							&& StringUtils.indexOf(n.getSemantictag(), parentSemtag) != -1)
				.findFirst();

			final Map<Integer, Set<Node>> nodesByLevel = new HashMap<>();

			if (parentTagNode.isPresent()) {
				nodesByLevel.put(0, Collections.singleton(parentTagNode.get()));
			} else {
				nodesByLevel.put(0, new LinkedHashSet<>());
			}

			int level = 0;
			boolean added = true;

			while (added && (cutoff == null || level < cutoff)) {
				Set<Node> found = new HashSet<>();

				for (Node node : nodes) {
					for (Node t_node : nodesByLevel.get(level)) {
						if (node.getParentNode() != null
								&& t_node.getId().equals(node.getParentNode().getId())) {
							found.add(node);
							break;
						}
					}
				}

				nodesByLevel.put(level + 1, found);
				level = level + 1;

				// We stop once nothing is added anymore
				added = !found.isEmpty();
			}

			Set<Node> semtagSet = new HashSet<>();

			for (Set<Node> nodeSet : nodesByLevel.values()) {
				for (Node t_node : nodeSet) {
					if (StringUtils.indexOf(t_node.getSemantictag(), childSemtag) != -1) {
						semtagSet.add(t_node);
					}
				}
			}

			Stream<Node> nodeStream = semtagSet.stream().sorted((o1, o2) -> {
				int result = StringUtils.compare(o1.getCode(), o2.getCode());

				if (result == 0) {
					return NumberUtils.compare(o1.getNodeOrder(), o2.getNodeOrder());
				}

				return result;
			});

			return new NodeList(nodeStream
					.map(this::getNodeDocument)
					.collect(Collectors.toList()));
		} else {
			if (portfolioRepository.existsById(pid)) {
				List<Node> nodes = nodeRepository.getNodesBySemanticTag(pid, childSemtag);

				return new NodeList(nodes.stream()
						.map(this::getNodeDocument)
						.collect(Collectors.toList()));
			}
		}

		return emptyList;
	}

	private List<Pair<Node, GroupRights>> getNodePerLevel(UUID nodeId, Long userId, Long rrgId, Integer cutoff) {

		Node n = nodeRepository.findById(nodeId).get();

		/// Portfolio id, nécessaire plus tard !
		UUID portfolioId = n.getPortfolio().getId();
		List<Node> t_nodes = nodeRepository.getNodes(portfolioId);

		/// Initialise la descente des noeuds, si il y a un partage on partira de le
		/// sinon du noeud par défaut
		/// FIXME: There will be something with shared_node_uuid

		Map<Integer, Set<UUID>> t_map_parentid = new HashMap<>();
		Set<UUID> t_set_parentid = new LinkedHashSet<>();

		t_set_parentid.add(n.getId());
		t_map_parentid.put(0, t_set_parentid);

		/// On boucle, avec les shared_node si ils existent.
		/// FIXME: Possibilité de boucle infini
		int level = 0;
		boolean added = true;

		Set<UUID> t_struc_parentid_2 = null;

		while (added && (cutoff == null ? true : level < cutoff)) {
			t_struc_parentid_2 = new HashSet<>();

			for (Node t_node : t_nodes) {
				for (UUID t_parent_node : t_map_parentid.get(level)) {
					if (t_node.getParentNode() != null
							&& t_node.getParentNode().getId().equals(t_parent_node)) {
						t_struc_parentid_2.add(t_node.getId());
						break;
					}
				}
			}
			t_map_parentid.put(level + 1, t_struc_parentid_2);
			t_set_parentid.addAll(t_struc_parentid_2);
			added = !t_struc_parentid_2.isEmpty(); // On s'arrete quand rien n'a été ajouté
			level = level + 1; // Prochaine étape
		}

		Map<UUID, GroupRights> t_rights_22 = new HashMap<>();
		GroupRights gr = null;

		if (credentialRepository.isDesigner(userId, nodeId) || credentialRepository.isAdmin(userId)) {
			for (UUID t_node_parent : t_set_parentid) {
				gr = new GroupRights();
				gr.setId(new GroupRightsId(null, t_node_parent));
				gr.setRead(true);
				gr.setWrite(true);
				gr.setDelete(true);
				gr.setSubmit(false);
				gr.setAdd(false);
				t_rights_22.put(t_node_parent, gr);
			}
		} else {
			if (nodeRepository.isPublic(nodeId)) {
				for (UUID t_node_parent : t_set_parentid) {
					gr = new GroupRights();
					gr.setId(new GroupRightsId(null, t_node_parent));
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
			GroupRightInfo gri1 = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, "all");
			GroupUser gu = null;
			Long grid3 = 0L;
			gu = groupUserRepository.getUniqueByUser(userId);

			for (Node t_node : t_nodes) {
				if (t_node.getId().equals(nodeId)
						&& t_node.getPortfolio().equals(gu.getGroupInfo().getGroupRightInfo().getPortfolio())) {
					grid3 = gu.getGroupInfo().getGroupRightInfo().getId();
				}
			}

			List<GroupRights> grList = groupRightsRepository.getByPortfolioAndGridList(portfolioId, gri1.getId(), rrgId,
					grid3);
			for (UUID ts : t_set_parentid) {
				for (GroupRights item : grList) {
					if (item.getGroupRightsId().equals(ts)) {
						if (t_rights_22.containsKey(item.getGroupRightsId())) {
							GroupRights original = t_rights_22.get(item.getGroupRightsId());
							original.setRead(Boolean.logicalOr(item.isRead(), original.isRead()));
							original.setWrite(Boolean.logicalOr(item.isWrite(), original.isWrite()));
							original.setDelete(Boolean.logicalOr(item.isDelete(), original.isDelete()));
							original.setSubmit(Boolean.logicalOr(item.isSubmit(), original.isSubmit()));
							original.setAdd(Boolean.logicalOr(item.isAdd(), original.isAdd()));
						} else {
							t_rights_22.put(item.getGroupRightsId(), item);
						}
					}
				}
			}
		}

		List<Node> nodes = nodeRepository.getNodes(new ArrayList<>(t_set_parentid));
		List<Pair<Node, GroupRights>> finalResults = new ArrayList<>();

		// Sélectionne les données selon la filtration
		for (Node node : nodes) {
			if (t_rights_22.containsKey(node.getId())) { // Verification des droits
				GroupRights rights = t_rights_22.get(node.getId());
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
     * @param destId
     * @param tag
     * @param code
     * @param sourceId
     * @param userId
     * @param groupId
     * @return
     */
    public UUID copyNode(UUID destId, String tag, String code, UUID sourceId, Long userId, Long groupId)
            throws BusinessException, JsonProcessingException {
        return importNode(destId, tag, code, sourceId, userId, groupId, false);
    }

	private UUID checkCache(String code) {

		UUID portfolioId = null;
		boolean setCache = false;
		Portfolio portfolio = portfolioRepository.getPortfolioFromNodeCode(code);

		// Le portfolio n'a pas été trouvé, pas besoin d'aller plus loin
		if (portfolio != null) {
			portfolioId = portfolio.getId();
			// Vérifier si nous n'avons pas déjà le portfolio en cache
			if (cachedNodes.get(portfolioId) != null) {
				final List<Node> nodes = cachedNodes.get(portfolioId);
				log.info("Portfolio présent dans le cache pour le code : " + code + " -> " + portfolioId);

				// Vérifier si le cache est toujours à jour.
				if (nodes.isEmpty() || portfolio.getModifDate() == null
						|| !portfolio.getModifDate().equals(nodes.get(0).getModifDate())) {
					// le cache est obsolète
					log.info("Cache obsolète pour : " + code);
					cachedNodes.remove(portfolioId);
					log.info("Supprimé du cache pour : " + code + " -> " + portfolioId);
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
				final List<Node> nodes = nodeRepository.getNodes(portfolioId);
				for (Node node : nodes) {
					node.setModifDate(portfolio.getModifDate());
				}
				// Mettre tous les noeuds dans le cache.
				cachedNodes.put(portfolioId, nodes);
			}
		}

		return portfolioId;
	}

	@Override
	public UUID importNode(UUID destId, String tag, String code, UUID sourceId, Long userId, long groupId)
            throws BusinessException, JsonProcessingException {
	    return importNode(destId, tag, code, sourceId, userId, groupId, true);
    }

    private UUID importNode(UUID destId, String tag, String code, UUID sourceId, Long userId, Long groupId, boolean parseRights)
            throws BusinessException, JsonProcessingException {
		if ((StringUtils.isEmpty(tag) || StringUtils.isEmpty(code)) && sourceId == null) {
            throw new IllegalArgumentException(
                    "importNode() a reçu des paramètres non valides (complétez le paramètre 'srcuuid' ou les paramètres 'tag' et 'code').");
		}

        if (sourceId != null && !hasRight(userId, groupId, sourceId, GroupRights.READ)) {
            throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
        } else if (checkCache(code) == null) {
            throw new GenericBusinessException("Aucun noeud trouvé pour le code : " + code);
        }

        UUID portfolioId = null;

        // Pour la copie de la structure
        UUID baseUuid = null;

        List<Node> nodes = null;

        // On évite la recherche de balises puisque nous connaissons l'uuid du noeud à
        // copier.
        if (sourceId != null) {
            // Puisque nous ne savons pas si ces noeuds doivent être mis en cache, on
            // recherche les informations dans la base.
            UUID portfolioUuid = portfolioRepository.getPortfolioUuidFromNode(sourceId);

            // Récupération des noeuds du portfolio à copier depuis la base.
            nodes = nodeRepository.getNodes(portfolioUuid);

            baseUuid = sourceId;
        } else {
            // Get nodes from portfolio we need to copy from cache
            nodes = cachedNodes.get(portfolioId);

            // Check whether we can find a node with the given tag
            Node nodeByTag = null;
            Node nodeBySemanticTag = null;

            for (Node node : nodes) {
                if (StringUtils.equalsIgnoreCase(node.getCode(), tag)) {
                    nodeByTag = node;
                    break;
                }

                if (StringUtils.equalsIgnoreCase(node.getSemantictag(), tag) && nodeBySemanticTag == null) {
                    nodeBySemanticTag = node;
                }
            }

            if (nodeByTag != null) {
                baseUuid = nodeByTag.getId();

            } else if (nodeBySemanticTag != null) {
                baseUuid = nodeBySemanticTag.getId();

            } else {
                throw new GenericBusinessException(
                        "Aucun noeud trouvé pour le code : " + code + " et le tag : " + tag);
            }
        }

        final Node destNode = nodeRepository.findById(destId).get();

        /// Contient les noeuds à copier.
        final Set<Node> nodesToCopy = new LinkedHashSet<>();
        /// Contient les uuid des noeuds à copier.
        final Set<UUID> nodesUuidToCopy = new LinkedHashSet<>();

        final Map<Integer, Set<UUID>> parentIdMap = new HashMap<>();

        for (Node node : nodes) {
            if (node.getId().equals(baseUuid)) {
                node.setParentNode(destNode);
                nodesUuidToCopy.add(node.getId());
                nodesToCopy.add(node);
                break;
            }
        }

        parentIdMap.put(0, nodesUuidToCopy);

        int level = 0;
        boolean added = true;

        while (added) {
            Set<UUID> parentIds = new LinkedHashSet<>();
            Set<Node> parentNodes = new LinkedHashSet<>();

            for (Node node : nodes) {
                for (UUID t_parent_id : parentIdMap.get(level)) {
                    if (node.getParentNode() != null
                            && node.getParentNode().getId().equals(t_parent_id)) {
                        parentIds.add(node.getId());
                        parentNodes.add(node);
                        break;
                    }
                }
            }

            parentIdMap.put(level + 1, parentIds);
            nodesUuidToCopy.addAll(parentIds);
            nodesToCopy.addAll(parentNodes);
            added = !parentIds.isEmpty();
            level = level + 1;
        }

        //////////////////////////////////////////
        /// Copie des noeuds et des ressources ///
        /////////////////////////////////////////

        // Contain a mapping between original elements and their copy.
        final Map<Node, Node> allNodes = new HashMap<>();
        final Map<Resource, Resource> resources = new HashMap<>();

        for (Node node : nodesToCopy) {
            Node nodeCopy = new Node(node);
            nodeCopy.setModifUserId(userId);

            if (node.getResource() != null) {
                Resource resourceCopy = nodeCopy.getResource();
                resourceCopy.setModifUserId(userId);

                if (!node.isSharedRes() || !node.getSharedNode() || !node.isSharedNodeRes()) {
                    resourceRepository.save(resourceCopy);
                    resources.put(node.getResource(), resourceCopy);
                }
            }

            if (node.getResResource() != null) {
                Resource resourceCopy = nodeCopy.getResResource();
                resourceCopy.setModifUserId(userId);

                if (!node.isSharedRes() || !node.getSharedNode() || !node.isSharedNodeRes()) {
                    resourceRepository.save(resourceCopy);
                    resources.put(node.getResource(), resourceCopy);
                }
            }

            if (node.getContextResource() != null) {
                Resource resourceCopy = nodeCopy.getContextResource();
                resourceCopy.setModifUserId(userId);

                if (!node.isSharedRes() || !node.getSharedNode() || !node.isSharedNodeRes()) {
                    resourceRepository.save(resourceCopy);
                    resources.put(node.getResource(), resourceCopy);
                }
            }

            nodeRepository.save(nodeCopy);
            allNodes.put(node, nodeCopy);
        }

        final Node searchedNode = new Node();
        final Portfolio destPortfolio = new Portfolio(destNode.getPortfolio().getId());

        for (Entry<Node, Node> entry : allNodes.entrySet()) {
            Node originalNode = entry.getKey();
            Node copiedNode = entry.getValue();

            if (originalNode.getParentNode() != null) {
                originalNode.setParentNode(allNodes.get(originalNode.getParentNode()));
            }

            // Update children list ; the order will define the XML output
            if (originalNode.getChildrenStr() != null) {
                String[] children = StringUtils.split(originalNode.getChildrenStr(), ",");
                List<String> childrenCopies = new ArrayList<>();

                for (String child : children) {
                    searchedNode.setId(UUID.fromString(child));
                    Node nodeCopy = allNodes.get(searchedNode);
                    childrenCopies.add(nodeCopy.getId().toString());
                }

                copiedNode.setChildrenStr(StringUtils.join(childrenCopies, ","));
            }

            copiedNode.setPortfolio(destPortfolio);
            nodeRepository.save(copiedNode);
        }

        // Mise à jour de l'ordre et du noeud parent de la copie
        searchedNode.setId(baseUuid);

        Node nodeCopy = allNodes.get(searchedNode);
        int nodeOrder = nodeRepository.getFirstLevelChildren(destId).size();

        nodeCopy.setParentNode(destNode);
        nodeCopy.setNodeOrder(nodeOrder);
        nodeRepository.save(nodeCopy);

        /// Ajout de l'enfant dans le noeud de destination
        destNode.setChildrenStr((destNode.getChildrenStr() != null ? destNode.getChildrenStr() + "," : "")
                + nodeCopy.getId().toString());
        nodeRepository.save(destNode);

        //////////////////////////////////
        /// Copie des droits des noeuds ///
        /////////////////////////////////
        // Login
        final String login = credentialRepository.getLoginById(userId);

        /// Copier les rôles actuel @Override pour faciliter le référencement.
        final UUID tmpPortfolioUuid = portfolioRepository.getPortfolioUuidFromNode(destId);

        // Récupération des rôles dans la BDD.
        final List<GroupRightInfo> griList = groupRightInfoRepository.getByPortfolioID(tmpPortfolioUuid);

        //// Set temporaire roles
        final Set<GroupRightInfo> t_set_groupRightInfo = new HashSet<>(griList);
        final Map<GroupRightsId, GroupRights> t_group_rights = new HashMap<>();

        GroupRights t_gr = null;
        final boolean hasGroup = !griList.isEmpty();

        /// Gestion des droits
        if (parseRights && hasGroup) {
            String onlyuser = "(?<![-=+])(user)(?![-=+])";
            Pattern pattern = Pattern.compile(onlyuser);

            for (Entry<Node, Node> entry : allNodes.entrySet()) {
                boolean found = false;
                GroupRightsId grId = new GroupRightsId();

                Node original = entry.getKey();
                Node copy = entry.getValue();

                UUID uuid = copy.getId();
                UUID portfolioUuid = copy.getPortfolio().getId();

                // Process et remplacement de 'user' par la personne en cours
                String meta = original.getMetadataWad();

                Matcher matcher = pattern.matcher(meta);

                if (matcher.find()) {
                    meta = meta.replaceAll(onlyuser, login);

                    // Remplacer les métadonnées par le nom d'utilisateur actuel
                    copy.setMetadataWad(meta);
                    nodeRepository.save(copy);

                    // S'assurer qu'un groupe d'utilisateurs spécifique existe en base et y ajouter
                    // l'utilisateur.
                    long ngid = getRoleByNode(1L, destId, login);
                    securityManager.addUserToGroup(userId, ngid);

                    /// Ensure entry is there in temp table, just need a skeleton info
                    GroupRightInfo groupRightInfo = new GroupRightInfo();

                    groupRightInfo.setId(ngid);
                    groupRightInfo.setLabel(login);
                    groupRightInfo.setOwner(1L);
                    t_set_groupRightInfo.add(groupRightInfo);
                }

                MetadataWadDocument metadata = MetadataWadDocument.from(meta);

                // FIXME: à améliorer pour faciliter le changement des droits
                String nodeRole;

                if (metadata.getSeenoderoles() != null) {
                    StringTokenizer tokens = new StringTokenizer(metadata.getSeenoderoles(), " ");

                    while (tokens.hasMoreElements()) {
                        nodeRole = tokens.nextElement().toString();
                        for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
                            if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
                                grId.setGroupRightInfo(tmp_gri);
                                grId.setId(uuid);

                                if (t_group_rights.containsKey(grId)) {
                                    t_gr = t_group_rights.get(grId);
                                    t_gr.setRead(true);
                                } else {
                                    GroupRights gr = new GroupRights();
                                    gr.setId(grId);
                                    gr.setRead(true);
                                    t_group_rights.put(grId, t_gr);
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

                if (metadata.getShowtoroles() != null) {
                    StringTokenizer tokens = new StringTokenizer(metadata.getShowtoroles(), " ");

                    while (tokens.hasMoreElements()) {
                        nodeRole = tokens.nextElement().toString();
                        for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
                            if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
                                grId.setGroupRightInfo(tmp_gri);
                                grId.setId(uuid);
                                if (t_group_rights.containsKey(grId)) {
                                    t_gr = t_group_rights.get(grId);
                                    t_gr.setRead(false);
                                } else {
                                    t_gr = new GroupRights();
                                    t_gr.setId(grId);
                                    t_gr.setRead(false);
                                    t_group_rights.put(grId, t_gr);
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

                if (metadata.getDelnoderoles() != null) {
                    StringTokenizer tokens = new StringTokenizer(metadata.getDelnoderoles(), " ");

                    while (tokens.hasMoreElements()) {
                        nodeRole = tokens.nextElement().toString();
                        for (GroupRightInfo t_gri : t_set_groupRightInfo) {
                            if (StringUtils.equalsIgnoreCase(t_gri.getLabel(), nodeRole)) {
                                grId.setGroupRightInfo(t_gri);
                                grId.setId(uuid);

                                if (t_group_rights.containsKey(grId)) {
                                    t_gr = t_group_rights.get(grId);
                                    t_gr.setDelete(true);
                                } else {
                                    t_gr = new GroupRights();
                                    t_gr.setId(grId);
                                    t_gr.setDelete(true);
                                    t_gr.setRead(false);
                                    t_group_rights.put(grId, t_gr);
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

                if (metadata.getEditnoderoles() != null) {
                    StringTokenizer tokens = new StringTokenizer(metadata.getEditnoderoles(), " ");

                    while (tokens.hasMoreElements()) {
                        nodeRole = tokens.nextElement().toString();
                        for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
                            if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
                                grId.setGroupRightInfo(tmp_gri);
                                grId.setId(uuid);

                                if (t_group_rights.containsKey(grId)) {
                                    t_gr = t_group_rights.get(grId);
                                    t_gr.setWrite(true);
                                } else {
                                    t_gr = new GroupRights();
                                    t_gr.setId(grId);
                                    t_gr.setWrite(true);
                                    t_gr.setRead(false);
                                    t_group_rights.put(grId, t_gr);
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

                if (metadata.getSubmitroles() != null) {
                    StringTokenizer tokens = new StringTokenizer(metadata.getSubmitroles(), " ");

                    while (tokens.hasMoreElements()) {
                        nodeRole = tokens.nextElement().toString();
                        for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
                            if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
                                grId.setGroupRightInfo(tmp_gri);
                                grId.setId(uuid);

                                if (t_group_rights.containsKey(grId)) {
                                    t_gr = t_group_rights.get(grId);
                                    t_gr.setSubmit(true);
                                } else {
                                    t_gr = new GroupRights();
                                    t_gr.setId(grId);
                                    t_gr.setSubmit(true);
                                    t_gr.setRead(false);
                                    t_group_rights.put(grId, t_gr);
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

                if (metadata.getEditresroles() != null) {
                    StringTokenizer tokens = new StringTokenizer(metadata.getEditresroles(), " ");

                    while (tokens.hasMoreElements()) {
                        nodeRole = tokens.nextElement().toString();
                        for (GroupRightInfo tmp_gri : t_set_groupRightInfo) {
                            if (StringUtils.equalsIgnoreCase(tmp_gri.getLabel(), nodeRole)) {
                                grId.setGroupRightInfo(tmp_gri);
                                grId.setId(uuid);

                                if (t_group_rights.containsKey(grId)) {
                                    t_gr = t_group_rights.get(grId);
                                    t_gr.setWrite(true);
                                } else {
                                    t_gr = new GroupRights();
                                    t_gr.setId(grId);
                                    t_gr.setWrite(true);
                                    t_gr.setRead(false);
                                    t_group_rights.put(grId, t_gr);
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


                /// FIXME: Incomplete

                if (metadata.getMenuroles() != null) {
                    /// Pour les différents items du menu
                    StringTokenizer menuline = new StringTokenizer(metadata.getMenuroles(), ";");

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
                            for (String role : roles) {
                                // Ensure roles exists
                                securityManager.addRole(portfolioId, role, 1L);
                            }
                        }
                    }
                }

                if (metadata.getNotifyroles() != null) {
                    /// Format pour l'instant: actionroles="sender:1,2;responsable:4"
                    StringTokenizer tokens = new StringTokenizer(metadata.getNotifyroles(), " ");
                    String notifyRoles = "";

                    if (tokens.hasMoreElements())
                        notifyRoles = tokens.nextElement().toString();
                    while (tokens.hasMoreElements())
                        notifyRoles += "," + tokens.nextElement().toString();

                    groupManager.changeNotifyRoles(portfolioUuid, uuid, notifyRoles);
                }
            }

            /// Ajout des droits des noeuds
            for (GroupRights gr : t_group_rights.values()) {
                groupRightsRepository.save(gr);
            }
        }

        // On récupère le UUID crée
        searchedNode.setId(baseUuid);

		return allNodes.get(searchedNode).getId();

	}

	@Override
	public List<Node> getChildren(UUID nodeId) {
		final Map<Integer, List<Node>> tree = new HashMap<>();
		final Optional<Node> root = nodeRepository.findById(nodeId);

		if (root.isPresent()) {
			return getChildren(Collections.singletonList(root.get()), tree, 0);
		} else {
			return Collections.emptyList();
		}
	}

	private List<Node> getChildren(List<Node> nodes, Map<Integer, List<Node>> tree, int level) {
		List<Node> children = childrenFor(nodes);

		if (!children.isEmpty()) {
			tree.put(level, children);

			return getChildren(children, tree, level + 1);
		} else {
			return tree
					.values()
					.stream()
					.flatMap(List::stream)
					.collect(Collectors.toList());
		}
	}

	private List<Node> childrenFor(List<Node> nodes) {
		List<UUID> ids = nodes.stream().map(Node::getId).collect(Collectors.toList());
		return nodeRepository.getDirectChildren(ids);
	}

	public void updateNodeRights(UUID nodeUuid, List<String> labels, String macroName) {
		if (!labels.isEmpty()) {
			for (int i = 0; i < labels.size(); i++) {
				labels.set(i, StringUtils.prependIfMissing(labels.get(i), "'", "\""));
				labels.set(i, StringUtils.appendIfMissing(labels.get(i), "'", "\""));
			}
		}

		List<GroupRights> grList = groupRightsRepository.findByIdAndLabels(nodeUuid, labels);

		grList.forEach(gr -> {
			if ("hide".equals(macroName)) {
				gr.setRead(false);
			} else if ("show".equals(macroName)) {
				gr.setRead(true);
			}
		});

		groupRightsRepository.saveAll(grList);
	}

	public boolean updateNodesRights(List<Node> nodes, Long grid) {
		for (Node node : nodes) {
			GroupRights gr = groupRightsRepository.getRightsByGrid(node.getId(), grid);

			if (gr != null) {
				gr.setWrite(false);
				gr.setDelete(false);
				gr.setAdd(false);
				gr.setSubmit(false);
				gr.setTypesId(null);
				gr.setRulesId(null);

				groupRightsRepository.save(gr);
			} else {
				gr = new GroupRights();
				gr.setId(new GroupRightsId(new GroupRightInfo(grid), node.getId()));

				groupRightsRepository.save(gr);
			}
		}

		return !nodes.isEmpty();
	}

	public boolean updateAllNodesRights(List<Node> nodes, Long grid) {
		for (Node node : nodes) {
			List<GroupRights> grList = groupRightsRepository.getRightsById(node.getId());

			if (!grList.isEmpty()) {
				for (GroupRights tmpGr : grList) {
					tmpGr.setWrite(false);
					tmpGr.setDelete(false);
					tmpGr.setAdd(false);
					tmpGr.setSubmit(false);
					tmpGr.setTypesId(null);
					tmpGr.setRulesId(null);

					groupRightsRepository.save(tmpGr);
				}
			} else {
				GroupRights gr = new GroupRights();
				gr.setId(new GroupRightsId(new GroupRightInfo(grid), node.getId()));

				groupRightsRepository.save(gr);
			}
		}

		return !nodes.isEmpty();
	}

	private void updateNode(UUID nodeId) {
		List<Node> nodes = nodeRepository.getFirstLevelChildren(nodeId);

		/// Re-numérote les noeuds (on commence à 0)
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).setNodeOrder(i);
		}

		nodeRepository.saveAll(nodes);

		List<String> uuidsStr = nodeRepository.getParentNodeUUIDs(nodeId)
									.stream()
									.map(uuid -> uuid.toString())
									.collect(Collectors.toList());


		Node n = nodeRepository.findById(nodeId).get();
		n.setChildrenStr(String.join(",", uuidsStr));

		nodeRepository.save(n);
	}

	private void updateNodeOrder(UUID nodeUuid, int order) {
		Node n = nodeRepository.findById(nodeUuid).get();

		n.setNodeOrder(order);

		nodeRepository.save(n);
	}

	private UUID add(UUID nodeId, String nodeChildrenUuid, String asmType, String xsiType,
					 boolean sharedRes, boolean sharedNode, boolean sharedNodeRes, String metadata, String metadataWad, String metadataEpm, String semtag,
					 String semanticTag, String label, String code, String descr, String format, int order, Long modifUserId,
					 UUID portfolioId) {
		Optional<Node> nodeOptional = nodeRepository.findById(nodeId);
		Node node = nodeOptional.orElseGet(Node::new);

		if (nodeId != null) {
			node.setParentNode(new Node(nodeId));
		}
		if (nodeChildrenUuid != null) {
			node.setChildrenStr(nodeChildrenUuid);
		}

		node.setNodeOrder(order);
		node.setAsmType(asmType);
		node.setXsiType(xsiType);
		node.setSharedRes(sharedRes);
		node.setSharedNode(sharedNode);
		node.setSharedNodeRes(sharedNodeRes);

		node.setMetadata(metadata);
		node.setMetadataWad(metadataWad);
		node.setMetadataEpm(metadataEpm);
		node.setSemtag(semtag);
		node.setSemantictag(semanticTag);
		node.setLabel(label);
		node.setCode(code);
		node.setDescr(descr);
		node.setFormat(format);
		node.setModifUserId(modifUserId);

		if (portfolioId != null)
			node.setPortfolio(new Portfolio(portfolioId));

		nodeRepository.save(node);

		return node.getId();
	}

	private int update(UUID nodeId, String asmType, String xsiType, String semantictag, String label, String code,
					  String descr, String format, String metadata, String metadataWad, String metadataEpm, boolean sharedRes,
					  boolean sharedNode, boolean sharedNodeRes, Long modifUserId) {
		Optional<Node> nodeOptional = nodeRepository.findById(nodeId);

		if (!nodeOptional.isPresent()) {
			return -1;
		}

		Node node = nodeOptional.get();

		node.setAsmType(asmType);
		node.setXsiType(xsiType);
		node.setSemantictag(semantictag);
		node.setLabel(label);
		node.setCode(code);
		node.setDescr(descr);
		node.setMetadata(metadata);
		node.setMetadataWad(metadataWad);
		node.setMetadataEpm(metadataEpm);
		node.setSharedRes(sharedRes);
		node.setSharedNode(sharedNode);
		node.setSharedNodeRes(sharedNodeRes);
		node.setModifUserId(modifUserId);

		nodeRepository.save(node);

		return 0;
	}

	private NodeDocument getNodeDocument(Node node) {
		NodeDocument nodeDocument = new NodeDocument(node);

		List<ResourceDocument> resources = new ArrayList<>();

		if (node.getMetadata() != null) {
			try {
				nodeDocument.setMetadata(MetadataDocument.from(node.getMetadata()));
			} catch (JsonProcessingException ignored)  { }
		}

		if (node.getMetadataEpm() != null) {
			try {
				nodeDocument.setMetadataEpm(MetadataEpmDocument.from(node.getMetadataEpm()));
			} catch (JsonProcessingException ignored)  { }
		}

		if (node.getMetadataWad() != null) {
			try {
				nodeDocument.setMetadataWad(MetadataWadDocument.from(node.getMetadataWad()));
			} catch (JsonProcessingException ignored) { }
		}

		if (node.getResource() != null) {
			resources.add(new ResourceDocument(node.getResource(), node));
		}

		if (node.getResResource() != null) {
			resources.add(new ResourceDocument(node.getResResource(), node));
		}

		if (node.getContextResource() != null) {
			resources.add(new ResourceDocument(node.getContextResource(), node));
		}

		nodeDocument.setResources(resources);

		return nodeDocument;
	}
}
