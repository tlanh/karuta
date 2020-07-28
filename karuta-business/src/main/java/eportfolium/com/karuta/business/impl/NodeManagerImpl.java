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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Hibernate;
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

	//// Portfolio -> parent id -> list of node (0: parent, 1+: child)
	private InMemoryCache<UUID, Map<UUID, List<Node>>> cachedNodes = new InMemoryCache<>(600, 1500, 6);

	@Override
	public NodeDocument getNode(UUID nodeId, boolean withChildren, Long userId, Integer cutoff)
			throws BusinessException, JsonProcessingException {
		final GroupRights rights = getRights(userId, nodeId);

		if (!rights.isRead()) {
			/// Vérifie les droits avec le compte publique (dernière chance)
			if (!nodeRepository.isPublic(nodeId))
				throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");

			userId = credentialRepository.getPublicId();
		}

		List<Pair<Node, GroupRights>> nodes = getNodePerLevel(nodeId, userId, rights.getGroupRightInfo().getId(),
				cutoff);

		/// Node -> parent
		Map<UUID, Tree> entries = new HashMap<>();
		processQuery(nodes, entries, rights.getGroupRightInfo().getLabel());

		StringBuilder sb = new StringBuilder();
		
		/// Reconstruct functional tree
		Tree root = entries.get(nodeId);
		reconstructTree(sb, root.node, root, entries);

		return root.node;
	}

	@Override
	public Node writeNode(NodeDocument node, UUID portfolioId, Long userId, int ordrer, UUID forcedId,
						  Node parentNode, boolean rewriteId, Map<UUID, UUID> resolve, boolean parseRights)
			throws BusinessException, JsonProcessingException {

		if (node == null)
			return null;

		String code = "";

		Optional<ResourceDocument> resource = node.getResources()
			.stream()
			.filter(n -> "nodeRes".equals(n.getXsiType())).findFirst();

		if (resource.isPresent())
			code = resource.get().getCode();

		String semanticTag = null;

		final UUID nodeId = rewriteId ? node.getId() : (forcedId != null ? forcedId : UUID.randomUUID());

		if (resolve != null) // Mapping old id -> new id
			resolve.put(node.getId(), nodeId);

		// If we are dealing with the root of the tree, we need to ensure
		// that the node's type is "asmRoot".
		if (nodeId != null && node.getParent() != null) {
			if (!node.getType().equals("asmRoot") && portfolioId == null)
				throw new GenericBusinessException("Missing node with 'asmRoot' type");

			List<ResourceDocument> resourceDocuments = node.getResources();

			if (!resourceDocuments.isEmpty()) {
				code = resourceDocuments.get(0).getCode();
			}
		}

		if (parseRights && node.getMetadataWad() != null) {
			MetadataWadDocument metadataWad = node.getMetadataWad();

			BiConsumer<String, String> processRoles = (roles, right) -> {
				if (roles != null) {
					for (String role : roles.split(" ")) {
						groupManager.addGroupRights(role, nodeId, right, portfolioId, userId);
					}
				}
			};

			processRoles.accept(metadataWad.getSeenoderoles(), GroupRights.READ);
			processRoles.accept(metadataWad.getDelnoderoles(), GroupRights.DELETE);
			processRoles.accept(metadataWad.getEditnoderoles(), GroupRights.WRITE);
			processRoles.accept(metadataWad.getEditresroles(), GroupRights.WRITE);
			processRoles.accept(metadataWad.getSubmitroles(), GroupRights.SUBMIT);
			processRoles.accept(metadataWad.getShowtoroles(), GroupRights.NONE);

			if (metadataWad.getNotifyroles() != null) {
				groupManager.changeNotifyRoles(portfolioId, nodeId,
						metadataWad.getNotifyroles().replace(" ", ","));
			}

			groupManager.setPublicState(userId, portfolioId, node.getMetadata().getPublic());

		}


		if (node.getMetadata() != null) {
			MetadataDocument metadata = node.getMetadata();

			semanticTag = metadata.getSemantictag();
		}

		Node nodeEntity = add(node, nodeId, parentNode, semanticTag, code, ordrer, userId, portfolioId);

		//// Insert resource associated with this node
		for (ResourceDocument resourceDocument : node.getResources()) {
			resourceManager.addResource(nodeEntity, resourceDocument, userId);
		}
		
		// Loop through children to go down in the tree.
		if (!node.getChildren().isEmpty()) {
			int k = 0;

			for (NodeDocument child : node.getChildren()) {
				UUID childId = null;

				if (!rewriteId)
					childId = UUID.randomUUID();

				writeNode(child, portfolioId, userId, k, childId, nodeEntity, rewriteId, resolve, parseRights);
				k++;
			}
		}

		if (parentNode != null)	// Update parent children list, if asmRoot, no parent
			updateNode(parentNode.getId());

		return nodeEntity;
	}

	@Override
	public NodeDocument getNode(UUID nodeId, boolean withChildren, String withChildrenOfXsiType, Long userId,
			String label, boolean checkSecurity) throws JsonProcessingException {
		if (checkSecurity) {
			GroupRights rights = getRights(userId, nodeId);

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
			nodeDocument = getNode(node.getSharedNodeUuid(), true, null, userId,
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
					Node child = nodeRepository.findById(uuid).orElse(new Node());

					if (withChildrenOfXsiType == null
							|| withChildrenOfXsiType.equals(child.getXsiType())) {
						children.add(getNode(uuid, true, null, userId, null, true));
					}
				}

				nodeDocument.setChildren(children);
			}
		}

		return nodeDocument;
	}

	@Override
	public NodeDocument getNodeBySemanticTag(UUID portfolioId, String semantictag, Long userId)
			throws BusinessException, JsonProcessingException {

		final List<Node> nodes = nodeRepository.getNodesBySemanticTag(portfolioId, semantictag);

		if (nodes.isEmpty()) {
			return null;
		}

		// On récupère d'abord l'uuid du premier noeud trouve correspondant au
		// semantictag
		UUID nodeId = nodes.get(0).getId();

		if (!hasRight(userId, nodeId, GroupRights.READ)) {
			throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
		}

		return getNode(nodeId, true, null, userId, null, true);
	}

	@Override
	public NodeList getNodesBySemanticTag(Long userId, UUID portfolioId, String semanticTag) throws BusinessException {
		List<Node> nodes = nodeRepository.getNodesBySemanticTag(portfolioId, semanticTag);

		if (nodes.stream()
				.anyMatch(n -> !hasRight(userId, n.getId(), GroupRights.READ))) {
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
		if (!credentialRepository.isAdmin(userId) && !hasRight(userId, nodeId, GroupRights.READ)) {
			throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
		}

		return nodeRepository.findById(nodeId)
				.map(n -> n.getPortfolio().getId())
				.orElse(null);
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
					document.setPrivate("hide".equals(macroName));
                }
            }

            final String metadataAttributes = xmlAttributes(document);

            nodeRepository.findById(nodeId)
				.ifPresent(n -> {
					n.setMetadataWad(metadataAttributes);
					nodeRepository.save(n);
				});
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

            final String metadataAttributes = xmlAttributes(document);

            nodeRepository.findById(nodeId)
					.ifPresent(n -> {
						n.setMetadataWad(metadataAttributes);
						nodeRepository.save(n);
					});

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

			resolve.put(uuid, rolesMap);

			MetadataWadDocument metadata = MetadataWadDocument.from(meta);

			BiConsumer<String, Consumer<GroupRights>> processRoles = (roleStr, consumer) -> {
				if (roleStr != null) {
					for (String role : roleStr.split(" ")) {
						if (!rolesMap.containsKey(role)) {
							GroupRights gr = new GroupRights();
							consumer.accept(gr);
							rolesMap.put(role, gr);
						}
					}
				}
			};

			processRoles.accept(metadata.getSeenoderoles(),  (gr) -> gr.setRead(true));
			processRoles.accept(metadata.getShowtoroles(),   (gr) -> gr.setRead(false));
			processRoles.accept(metadata.getDelnoderoles(),  (gr) -> gr.setDelete(true));
			processRoles.accept(metadata.getEditnoderoles(), (gr) -> gr.setWrite(true));
			processRoles.accept(metadata.getSubmitroles(),   (gr) -> gr.setSubmit(true));
			processRoles.accept(metadata.getEditresroles(),  (gr) -> gr.setWrite(true));

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
				String merge = String.join(",", metadata.getNotifyroles().split(" "));

				for (GroupRights value : rolesMap.values()) {
					value.setNotifyRoles(merge);
				}
			}

			/// Now remove mention to being submitted
			Node n = nodeRepository.findById(uuid).get();
			n.setMetadataWad(xmlAttributes(metadata));

			nodeRepository.save(n);

			GroupRightsId grId = new GroupRightsId();

			for (Entry<UUID, Map<String, GroupRights>> entry : resolve.entrySet()) {
				UUID id = entry.getKey();
				Map<String, GroupRights> gr = entry.getValue();

				for (Entry<String, GroupRights> rightElem : gr.entrySet()) {
					String group = rightElem.getKey();

					GroupRights gr2 = groupRightsRepository.getRightsByIdAndLabel(id, group);
					GroupRightInfo gri = gr2 != null ? gr2.getGroupRightInfo() : null;

					GroupRights rightValue = rightElem.getValue();
					grId.setGroupRightInfo(gri);
					grId.setId(id);
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
	public MetadataWadDocument getNodeMetadataWad(UUID nodeId, Long userId) throws BusinessException, JsonProcessingException {
		if (!hasRight(userId, nodeId, GroupRights.READ)) {
			throw new GenericBusinessException("Vous n'avez pas les droits nécessaires.");
		}

		Optional<Node> node = nodeRepository.findById(nodeId);

		if (!node.isPresent() || !node.get().getAsmType().equals("asmResource"))
			return null;

		return MetadataWadDocument.from(node.get().getMetadataWad());
	}

	@Override
	public Integer changeNode(UUID nodeId, NodeDocument node, Long userId) throws BusinessException, JsonProcessingException {
		if (!hasRight(userId, nodeId, GroupRights.WRITE))
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
	public String changeNodeMetadataWad(UUID nodeId, MetadataWadDocument metadata, Long userId)
			throws BusinessException, JsonProcessingException {

		if (!hasRight(userId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		final String metadataAttributes = xmlAttributes(metadata);

		nodeRepository.findById(nodeId)
				.ifPresent(node -> {
					node.setMetadataWad(metadataAttributes);
					nodeRepository.save(node);
					portfolioManager.updateTimeByNode(nodeId);
				});

		return "editer";
	}

	@Override
	public void removeNode(UUID nodeId, Long userId) throws BusinessException {
		if (!hasRight(userId, nodeId, GroupRights.DELETE))
			if (!credentialRepository.isAdmin(userId)
					&& !credentialRepository.isDesigner(userId, nodeId))
				throw new GenericBusinessException("403 FORBIDDEN, No admin right");

		/// Copy portfolio base info
		final Node nodeToRemove = nodeRepository.findById(nodeId).get();

		/// Portfolio id, nécessaire pour plus tard !
		final UUID portfolioId = nodeToRemove.getPortfolio().getId();
		final List<Node> nodes = nodeRepository.getNodesWithResources(portfolioId);

		final Set<Node> nodesToDelete = listHierarchy(nodeToRemove, nodes, null);
		final Set<Resource> resourcesToDelete = new LinkedHashSet<>();

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

		portfolioManager.updateTimeByNode(nodeId);
		nodeRepository.deleteAll(nodesToDelete);
		resourceRepository.deleteAll(resourcesToDelete);

		if (nodeToRemove.getParentNode() != null)
			updateNode(nodeToRemove.getParentNode().getId());
	}

	@Override
	public boolean changeParentNode(Long userid, UUID nodeId, UUID parentId) throws BusinessException {
		if (!credentialRepository.isAdmin(userid) && !credentialRepository.isDesigner(userid, nodeId))
			throw new GenericBusinessException("FORBIDDEN 403 : No admin right");

		// To avoid defining a node as its parent.
		if (nodeId.equals(parentId))
			return false;

		Optional<Node> nodeOptional = nodeRepository.findById(nodeId);

		if (!nodeOptional.isPresent())
			return false;

		Node node = nodeOptional.get();
		UUID puuid = node.getParentNode() != null ? node.getParentNode().getId() : null;

		Integer next = nodeRepository.getNodeNextOrderChildren(parentId);

		node.setParentNode(new Node(parentId));
		node.setNodeOrder(next);

		nodeRepository.save(node);

		// Mettre à jour la liste d'enfants pour le noeud d'origine et le noeud de
		// destination.
		updateNode(puuid);
		updateNode(parentId);

		portfolioManager.updateTimeByNode(nodeId);

		return true;
	}

	@Override
	public Long moveNodeUp(UUID nodeId) {
		Optional<Node> nodeOptional = nodeRepository.findById(nodeId);

		if (!nodeOptional.isPresent())
			return -1L;

		Node node = nodeOptional.get();

		int order = node.getNodeOrder();
		UUID puuid = node.getParentNode() != null ? node.getParentNode().getId() : null;

		if (order < 0)
			return -1L;
		if (order == 0)
			return -2L;

		final List<Node> nodes = nodeRepository.getNodesByOrder(puuid, order);

		/// Swap node order
		for (Node sibling : nodes) {
			if (sibling.getNodeOrder() == order) {
				sibling.setNodeOrder(order - 1);
			} else {
				sibling.setNodeOrder(order);
			}

			nodeRepository.save(sibling);
		}

		// Mettre à jour la liste des enfants
		updateNode(puuid);
		portfolioManager.updateTimeByNode(nodeId);

		return 0L;
	}

	@Override
	public String changeNodeMetadataEpm(UUID nodeId, MetadataEpmDocument metadata, Long userId)
			throws BusinessException, JsonProcessingException {
		if (!hasRight(userId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("FORBIDDEN 403 : No WRITE credential ");

		final String metadataAttributes = xmlAttributes(metadata);

		nodeRepository.findById(nodeId)
			.ifPresent(node -> {
				node.setMetadataEpm(metadataAttributes);
				nodeRepository.save(node);

				portfolioManager.updateTimeByNode(nodeId);
			});

		return "editer";
	}

	@Override
	public String changeNodeMetadata(UUID nodeId, MetadataDocument metadata, Long userId)
			throws BusinessException, JsonProcessingException {

		if (!hasRight(userId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN, no WRITE credential");

		UUID portfolioUuid = portfolioRepository.getPortfolioUuidFromNode(nodeId);

		if( metadata.getPublic() != null )
			// Public has to be managed via the group/user function
			groupManager.setPublicState(userId, portfolioUuid, metadata.getPublic());

		final String metadataAttributes = xmlAttributes(metadata);

		nodeRepository.findById(nodeId)
			.ifPresent(node -> {
				node.setMetadata(metadataAttributes);
				node.setSemantictag(metadata.getSemantictag());
				if( metadata.getSharedResource() != null )
					node.setSharedRes(metadata.getSharedResource());
				if( metadata.getSharedNode() != null )
					node.setSharedNode(metadata.getSharedNode());
				if( metadata.getSharedNodeResource() != null )
					node.setSharedNodeRes(metadata.getSharedNodeResource());

				nodeRepository.save(node);
				portfolioManager.updateTime(portfolioUuid);
			});

		return "editer";
	}

	@Override
	public String changeNodeContext(UUID nodeId, ResourceDocument resource, Long userId)
			throws BusinessException {
		if (!hasRight(userId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		resourceManager.changeResourceByXsiType(nodeId, "context", resource, userId);

		return "editer";
	}

	@Override
	public String changeNodeResource(UUID nodeId, ResourceDocument resource, Long userId)
			throws BusinessException {
		if (!hasRight(userId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		resourceManager.changeResourceByXsiType(nodeId, "nodeRes", resource, userId);

		return "editer";
	}

	@Override
	public NodeList addNode(UUID parentNodeId, NodeDocument node, Long userId, boolean forcedUuid)
			throws BusinessException, JsonProcessingException {

		Integer nodeOrder = nodeRepository.getNodeNextOrderChildren(parentNodeId);
		Portfolio portfolio = portfolioRepository.getPortfolioFromNode(parentNodeId);

		UUID portfolioId = portfolio != null ? portfolio.getId() : null;

		Node pNode = nodeRepository.getNodes(Collections.singletonList(parentNodeId)).get(0);

		// TODO getNodeRight postNode
		Node nodeId = writeNode(node, portfolioId, userId, nodeOrder, null, pNode, forcedUuid, null, true);

		portfolioManager.updateTimeByNode(portfolioId);

		return new NodeList(Collections.singletonList(new NodeDocument(nodeId)));
	}

	@Override
	public NodeDocument getNodeWithXSL(UUID nodeId, String xslFile, String parameters, Long userId)
			throws BusinessException, JsonProcessingException {
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

		return getNode(nodeId, true, userId, null);
	}

	@Override
	public NodeList addNodeFromModelBySemanticTag(UUID nodeId, String semanticTag, Long userId)
			throws BusinessException, JsonProcessingException {
		Portfolio portfolio = portfolioRepository.getPortfolioFromNode(nodeId);

		UUID portfolioModelId = null;

		if (portfolio != null) {
			portfolioModelId = portfolio.getModelId();
		}

		NodeDocument node = getNodeBySemanticTag(portfolioModelId, semanticTag, userId);

		// C'est le noeud obtenu dans le modèle indiqué par la table de correspondance.
		UUID otherParentNodeUuid = nodeRepository.getNodeUuidByPortfolioModelAndSemanticTag(portfolioModelId, semanticTag);

		return addNode(otherParentNodeUuid, node, userId, true);
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
	public NodeList getNodes(String rootNodeCode, String childSemtag, Long userId,
			String parentSemtag, String parentNodeCode, Integer cutoff) throws BusinessException {

		UUID pid = portfolioRepository.getPortfolioUuidFromNodeCode(rootNodeCode);
		final NodeList emptyList = new NodeList(Collections.emptyList());

		if (pid == null)
			return emptyList;

		GroupRights rights = portfolioManager.getRightsOnPortfolio(userId, pid);

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


			Stream<Node> nodeStream = listHierarchy(parentTagNode.orElse(null), new ArrayList<>(), cutoff)
					.stream()
					.filter(node -> StringUtils.indexOf(node.getSemantictag(), childSemtag) != -1)
					.sorted((o1, o2) -> {
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

		Optional<Node> nodeOptional = nodeRepository.findById(nodeId);

		if (!nodeOptional.isPresent())
			return Collections.emptyList();

		Node n = nodeOptional.get();

		UUID portfolioId = n.getPortfolio().getId();
		List<Node> nodes = nodeRepository.getNodes(portfolioId);

		Set<UUID> parentIds = listHierarchy(n, nodes, cutoff)
				.stream()
				.map(Node::getId)
				.collect(Collectors.toSet());

		Map<UUID, GroupRights> rights = new HashMap<>();

		if (credentialRepository.isDesigner(userId, nodeId) || credentialRepository.isAdmin(userId)) {
			for (UUID parentNode : parentIds) {
				GroupRights gr = new GroupRights();
				gr.setId(new GroupRightsId(null, parentNode));
				gr.setRead(true);
				gr.setWrite(true);
				gr.setDelete(true);
				gr.setSubmit(false);
				gr.setAdd(false);

				rights.put(parentNode, gr);
			}
		} else {
			if (nodeRepository.isPublic(nodeId)) {
				for (UUID parentNode : parentIds) {
					GroupRights gr = new GroupRights();
					gr.setId(new GroupRightsId(null, parentNode));
					gr.setRead(true);
					gr.setWrite(false);
					gr.setDelete(true);
					gr.setSubmit(false);
					gr.setAdd(false);

					rights.put(parentNode, gr);
				}
			}

			// Aggregation des droits avec 'all', l'appartenance du groupe de l'utilisateur,
			// et les droits propres a l'utilisateur
			GroupRightInfo gri1 = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, "all");
			GroupInfo gi = groupUserRepository.getUniqueByUser(userId)
					.getGroupInfo();

			Long grid3 = nodes.stream()
					.filter(node -> node.getId().equals(nodeId) &&
							node.getPortfolio().equals(gi.getGroupRightInfo().getPortfolio()))
					.findFirst()
					.map(node -> gi.getGroupRightInfo().getId())
					.orElse(0L);

			List<GroupRights> grList = groupRightsRepository.getByPortfolioAndGridList(portfolioId, gri1.getId(), rrgId,
					grid3);

			for (UUID ts : parentIds) {
				for (GroupRights item : grList) {
					if (item.getGroupRightsId().equals(ts)) {
						if (rights.containsKey(item.getGroupRightsId())) {
							GroupRights original = rights.get(item.getGroupRightsId());
							original.setRead(item.isRead() || original.isRead());
							original.setWrite(item.isWrite() || original.isWrite());
							original.setDelete(item.isDelete() || original.isDelete());
							original.setSubmit(item.isSubmit() || original.isSubmit());
							original.setAdd(item.isAdd() || original.isAdd());
						} else {
							rights.put(item.getGroupRightsId(), item);
						}
					}
				}
			}
		}

		List<Node> nodeList = nodeRepository.getNodes(new ArrayList<>(parentIds));

		return nodeList.stream()
				.filter(node -> rights.containsKey(node.getId()))
				.filter(node -> rights.get(node.getId()).isRead())
				.map(node -> Pair.of(node, rights.get(node.getId())))
				.collect(Collectors.toList());
	}

    /**
     * Même chose que postImportNode, mais on ne prend pas en compte le parsage des
     * droits
     */
    public UUID copyNode(UUID destId, String tag, String code, UUID sourceId, Long userId)
            throws BusinessException, JsonProcessingException {
        return importNode(destId, tag, code, sourceId, userId, false);
    }

	private UUID checkCache(UUID portfolioId) {

		boolean setCache = false;
		Node root = portfolioRepository.getPortfolioRootNode(portfolioId);
		Portfolio portfolio = root.getPortfolio();
		String code = null;

		// Le portfolio n'a pas été trouvé, pas besoin d'aller plus loin
		if (portfolio != null) {
			portfolioId = portfolio.getId();
			code = root.getCode();
			// Vérifier si nous n'avons pas déjà le portfolio en cache
			if (cachedNodes.get(portfolioId) != null) {
				final Map<UUID, List<Node>> nodes = cachedNodes.get(portfolioId);
				log.info("Portfolio présent dans le cache pour le code : " + code + " -> " + portfolioId);

				Iterator<List<Node>> iter = nodes.values().iterator();
				// Vérifier si le cache est toujours à jour.
				if (nodes.isEmpty() || portfolio.getModifDate() == null
						|| !portfolio.getModifDate().equals(iter.next().get(1).getModifDate())) {
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
				/// Since we'll have to loop for a bit with substree,
				/// convert list as hashmap
        HashMap<UUID, List<Node>> resolve = new HashMap<>();
        
				for (Node node : nodes) {
					node.setModifDate(portfolio.getModifDate());
					Node ppNode = node.getParentNode();	// Sometime get proxy object
					Node pNode = (Node) Hibernate.unproxy(ppNode);
					UUID parentId = null;
					/// FIXME: Some other node than root have parent as null (import?)
					if( pNode != null )
						parentId = pNode.getId();
//					else
//						continue;		/// current node is root
					
					List<Node> branch = resolve.get(parentId);
					if( branch == null )
					{
						branch = new ArrayList<Node>();
						branch.add(pNode);	/// Parent node as first eleemnt
						resolve.put(parentId, branch);
					}
					branch.add(node);
				}
				// Mettre tous les noeuds dans le cache.
				cachedNodes.put(portfolioId, resolve);
			}
		}

		return portfolioId;
	}

	@Override
	public UUID importNode(UUID destId, String tag, String code, UUID sourceId, Long userId)
            throws BusinessException, JsonProcessingException {
	    return importNode(destId, tag, code, sourceId, userId, true);
    }

    private UUID importNode(UUID destId, String tag, String code, UUID sourceId, Long userId, boolean parseRights)
            throws BusinessException, JsonProcessingException {
		if ((StringUtils.isEmpty(tag) || StringUtils.isEmpty(code)) && sourceId == null) {
            throw new IllegalArgumentException(
                    "importNode() a reçu des paramètres non valides (complétez le paramètre 'srcuuid' ou les paramètres 'tag' et 'code').");
		}

        if (sourceId != null && !hasRight(userId, sourceId, GroupRights.READ)) {
            throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
        }
        
        // Pour la copie de la structure
        UUID portfolioId = null;
        UUID baseUuid = null;

        Node baseNode = null;
        Map<UUID, List<Node>> nodes = null;

        // On évite la recherche de balises puisque nous connaissons l'uuid du noeud à
        // copier.
        if (sourceId != null) {
            // Puisque nous ne savons pas si ces noeuds doivent être mis en cache, on
            // recherche les informations dans la base.
            UUID portfolioUuid = portfolioRepository.getPortfolioUuidFromNode(sourceId);
            if( portfolioUuid == null )
          		throw new GenericBusinessException("Aucun noeud trouvé pour le UUID : " + sourceId);

            /// Check cache validity
            checkCache( portfolioUuid );
            
            // Keep copy for later
            nodes = cachedNodes.get(portfolioId);
            baseUuid = sourceId;

            /// Fetch base node from child list;
            List<Node> clist = nodes.get(baseUuid);
            baseNode = clist.get(0);	// First element is parent
        } else {
        	/// Code/Tag
        	Portfolio portfolio = portfolioRepository.getPortfolioFromNodeCode(code);
        	if ( portfolio == null) {
        		throw new GenericBusinessException("Aucun noeud trouvé pour le code : " + code);
        	}
        	portfolioId = checkCache(portfolio.getId());
            // Get nodes from portfolio we need to copy from cache
            nodes = cachedNodes.get(portfolioId);

            // Check whether we can find a node with the given tag
            Node nodeSearch = null;

            /// For all branches
            for (List<Node> lnode : nodes.values()) {
            	Iterator<Node> inode = lnode.iterator();
            	/// Skip first in list, it's the parent
            	inode.next();
            	// Directly check if it's either in code or semantictag
            	while( inode.hasNext() )
            	{
            		Node n = inode.next();
            		if (StringUtils.equalsIgnoreCase(n.getCode(), tag) || StringUtils.equalsIgnoreCase(n.getSemantictag(), tag)) {
            			nodeSearch = n;
            			break;
            		}
            		
            	}
            }

            if (nodeSearch != null) {
            		baseNode = nodeSearch;
                baseUuid = nodeSearch.getId();
            } else {
                throw new GenericBusinessException(
                        "Aucun noeud trouvé pour le code : " + code + " et le tag : " + tag);
            }
        }
        
        final Node destNode = nodeRepository.findById(destId).get();
        int nodeOrder = nodeRepository.getFirstLevelChildren(destId).size();
        Portfolio destPortfolio = destNode.getPortfolio();
        
        /// baseuuid is the starting node to copy from

        Node baseCopyNode = new Node(baseNode);
        baseCopyNode.setParentNode(destNode);
        baseCopyNode.setModifUserId(userId);
        baseCopyNode.setPortfolio(destPortfolio);
        baseCopyNode.setNodeOrder(nodeOrder+1);
        baseCopyNode = nodeRepository.save(baseCopyNode);
        
        /// Ajout de l'enfant dans le noeud de destination
        String destClist = destNode.getChildrenStr();
        destClist += ","+baseCopyNode.getId().toString();
        destNode.setChildrenStr(destClist);
        nodeRepository.save(destNode);

        /// Contient les noeuds à copier.
        final Set<Node> nodesToCopy = new LinkedHashSet<>();
        /// Contient les uuid des noeuds à copier.
        final Set<UUID> nodesUuidToCopy = new LinkedHashSet<>();

        final Map<Integer, Set<UUID>> parentIdMap = new HashMap<>();
        Queue<Node> resolveParent = new LinkedList<>();

        nodesToCopy.add(baseCopyNode);
        resolveParent.add(baseCopyNode);

        parentIdMap.put(0, nodesUuidToCopy);

        Node qnode = baseCopyNode;

        while (qnode != null ) {
            //// Retrieve current branch
          	List<Node> childs = nodes.get(qnode.getId());
          	if( childs == null )	// Leaf
        		{
              qnode = resolveParent.poll();
          		continue;
        		}
          	List<String> cList = new ArrayList<>();
          	/// For listed childs
              for (Node cNode : childs ) {
              	/// Copy and adjust values
              	Node ccopy = new Node(cNode);
              	ccopy.setParentNode(qnode);
              	ccopy.setModifUserId(userId);
              	ccopy.setPortfolio(destPortfolio);
              	
              	ccopy = nodeRepository.save(ccopy);
              	cList.add(ccopy.getId().toString());
              	
              	/// Copy list to submit (Or need to submit before)
              	nodesToCopy.add(ccopy);
              	/// Add to queue for traversing
              	resolveParent.add(cNode);
              }
              qnode.setChildrenStr(String.join(",", cList));
              nodeRepository.save(qnode);
              
            qnode = resolveParent.poll();
        }

        //////////////////////////////////////////
        /// Copie des noeuds et des ressources ///
        /////////////////////////////////////////

        // Contain a mapping between original elements and their copy.
        final Map<Node, Node> allNodes = new HashMap<>();
        final Map<Resource, Resource> resources = new HashMap<>();

        for (Node node : nodesToCopy) {

            if (node.getResource() != null) {
                Resource resourceCopy = node.getResource();
                resourceCopy.setModifUserId(userId);

                if (!node.isSharedRes() || !node.getSharedNode() || !node.isSharedNodeRes()) {
                    resourceRepository.save(resourceCopy);
                    resources.put(node.getResource(), resourceCopy);
                }
            }

            if (node.getResResource() != null) {
                Resource resourceCopy = node.getResResource();
                resourceCopy.setModifUserId(userId);

                if (!node.isSharedRes() || !node.getSharedNode() || !node.isSharedNodeRes()) {
                    resourceRepository.save(resourceCopy);
                    resources.put(node.getResource(), resourceCopy);
                }
            }

            if (node.getContextResource() != null) {
                Resource resourceCopy = node.getContextResource();
                resourceCopy.setModifUserId(userId);

                if (!node.isSharedRes() || !node.getSharedNode() || !node.isSharedNodeRes()) {
                    resourceRepository.save(resourceCopy);
                    resources.put(node.getResource(), resourceCopy);
                }
            }

//            nodeRepository.save(node);
            allNodes.put(node, node);	///// !
        }


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
                    groupManager.changeNotifyRoles(portfolioUuid, uuid,
							metadata.getNotifyroles().replace(" ", ","));
                }
            }

            /// Ajout des droits des noeuds
            for (GroupRights gr : t_group_rights.values()) {
                groupRightsRepository.save(gr);
            }
        }

		return baseCopyNode.getId();

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

	private void updateNode(UUID nodeId) {
		List<Node> nodes = nodeRepository.getFirstLevelChildren(nodeId);

		/// Re-numérote les noeuds (on commence à 0)
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).setNodeOrder(i);
		}

		nodeRepository.saveAll(nodes);

		String uuidsStr = nodeRepository.getParentNodeUUIDs(nodeId)
								.stream()
								.map(UUID::toString)
								.collect(Collectors.joining(","));

		nodeRepository.findById(nodeId)
				.ifPresent(node -> {
					node.setChildrenStr(uuidsStr);
					nodeRepository.save(node);
				});

	}

	private void updateNodeOrder(UUID nodeUuid, int order) {
		nodeRepository.findById(nodeUuid)
				.ifPresent(node -> {
					node.setNodeOrder(order);
					nodeRepository.save(node);
				});
	}

	private Node add(NodeDocument nodeDocument, UUID nodeId, Node nodeParent, String semanticTag, String code, int order, Long userId,
					 UUID portfolioId) throws JsonProcessingException {
		
		Node node = new Node();
		node.setId(nodeId);

		node.setParentNode(nodeParent);
		node.setChildrenStr("");

		node.setNodeOrder(order);
		node.setAsmType(nodeDocument.getType());
		node.setXsiType(nodeDocument.getXsiType());

		MetadataDocument metadata = nodeDocument.getMetadata();

		if (metadata != null) {
			node.setSharedRes(metadata.getSharedResource());
			node.setSharedNode(metadata.getSharedNode());
			node.setSharedNodeRes(metadata.getSharedNodeResource());
		}

		if (nodeDocument.getMetadata() != null)
		{
			String attrvalue = xmlAttributes(nodeDocument.getMetadata());
			node.setMetadata(attrvalue);
		}

		if (nodeDocument.getMetadataWad() != null)
		{
			String attrvalue = xmlAttributes(nodeDocument.getMetadataWad());
			node.setMetadataWad(attrvalue);
		}

		if (nodeDocument.getMetadataEpm() != null)
		{
			String attrvalue = xmlAttributes(nodeDocument.getMetadataEpm());
			node.setMetadataEpm(attrvalue);
		}

		node.setSemtag(nodeDocument.getSemtag());
		node.setSemantictag(semanticTag);
		node.setLabel(nodeDocument.getLabel());
		node.setCode(code);
		node.setDescr(nodeDocument.getDescription());
		node.setFormat(nodeDocument.getFormat());
		node.setModifUserId(userId);

		if (portfolioId != null) {
			portfolioRepository.findById(portfolioId)
				.ifPresent(node::setPortfolio);
		}

		return nodeRepository.save(node);
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
		node.setFormat(format);
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

	@Override
	public NodeRightsDocument getRights(UUID nodeId, Long userId) {
		GroupRights groupRights = getRights(userId, nodeId);

		return new NodeRightsDocument(nodeId, groupRights);
	}

	private Set<Node> listHierarchy(Node base, List<Node> nodes, Integer cutoff) {
		Map<Integer, Set<Node>> treeMap = new HashMap<>();
		Set<Node> allChildren = new LinkedHashSet<>();

		if (base != null)
			allChildren.add(base);

		treeMap.put(0, allChildren);

		int level = 0;
		boolean added = true;

		while (added && (cutoff == null || level < cutoff)) {
			Set<Node> parentNodes = treeMap.get(level);

			Set<Node> children = nodes.stream()
					.filter(node -> node.getParentNode() != null)
					.filter(node -> parentNodes.stream().anyMatch(n -> n.getId().equals(node.getParentNode().getId())))
					.collect(Collectors.toSet());

			treeMap.put(level + 1, children);
			allChildren.addAll(children);

			added = !children.isEmpty();
			level++;
		}

		return allChildren;
	}
}
