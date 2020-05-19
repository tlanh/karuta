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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.GroupRightsRepository;
import eportfolium.com.karuta.consumer.repositories.NodeRepository;
import eportfolium.com.karuta.document.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.Node;

public abstract class BaseManager {

	@Autowired
	protected CredentialRepository credentialRepository;

	@Autowired
	protected NodeRepository nodeRepository;

	@Autowired
	protected GroupRightsRepository groupRightsRepository;

	private static final XmlMapper mapper = new XmlMapper();
	private static final ObjectWriter attributeWriter = mapper.writer().withRootName("");

	// Help reconstruct tree
	protected class Tree {
		NodeDocument node = null;
		String childString = "";
	};

	/**
	 * test pour l'affichage des différentes méthodes de Node
	 */
	public GroupRights getRights(Long userId, Long groupId, UUID nodeId) {

		GroupRights rights = null;

		if (credentialRepository.isAdmin(userId)) {
			rights = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true, true, true, true, false);
		} else if (credentialRepository.isDesigner(userId, nodeId)) /// Droits via le partage totale (obsolete) ou si c'est
		{
			rights = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true, true, true, true, false);
		} else {
			if (groupId == null || groupId == 0L) {
				rights = groupRightsRepository.getRightsByIdAndUser(nodeId, userId);
			}

			rights = groupRightsRepository.getRightsByUserAndGroup(nodeId, userId, groupId);
			rights = groupRightsRepository.getSpecificRightsForUser(nodeId, userId);
			rights = groupRightsRepository.getPublicRightsByGroupId(nodeId, groupId);

		}

		// Si null alors par défaut défaut accès à rien
		if (rights == null) {
			rights = new GroupRights();
			rights.setId(new GroupRightsId(new GroupRightInfo(), null));
		}

		// Dernière chance pour les droits avec les droits publics.
		if (nodeRepository.isPublic(nodeId)) {
			rights.setRead(true);
		}
		return rights;
	}

	protected boolean hasRight(Long userId, Long groupId, UUID nodeId, String right) {
		GroupRights rights = getRights(userId, groupId, nodeId);

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

	protected void processQuery(List<Pair<Node, GroupRights>> nodes,
								Map<UUID, Tree> entries,
								String role) {
		if (!CollectionUtils.isNotEmpty(nodes)) {
			return;
		}

		for (Pair<Node, GroupRights> pair : nodes) {
			Node node = pair.getLeft();
			GroupRights groupRights = pair.getRight();

			// In case we have rights on nodes that are not part of the portfolio
			if (node.getId() == null)
				continue;

			List<MetadataDocument> metadata = Arrays.asList(
					new MetadataWadDocument(node.getMetadataWad()),
					new MetadataEpmDocument(node.getMetadataEpm()),
					new MetadataDocument(node.getMetadata())
			);

			// TODO: Check whether data is always consistent regarding xsiType
			List<ResourceDocument> resources = Stream.of(
					node.getResResource(),
					node.getContextResource(),
					node.getResource())
				.filter(resource -> resource != null && resource.getContent() != null)
				.map(resource -> new ResourceDocument(resource, node))
				.collect(Collectors.toList());

			NodeDocument nodeDocument = new NodeDocument(node, groupRights, role);

			nodeDocument.setMetadata(metadata);
			nodeDocument.setResources(resources);

			/// Prepare data to reconstruct tree
			Tree entry = new Tree();
			entry.node = nodeDocument;

			if (StringUtils.isNotEmpty(node.getChildrenStr())) {
				entry.childString = node.getChildrenStr();
			}

			entries.put(node.getId(), entry);
		}
	}

	protected void reconstructTree(Tree node, Map<UUID, Tree> entries) {
		if (node.childString == null)
			return;

		Stream<String> childIds = Stream.of(node.childString.split(","));

		childIds
				.filter(id -> !id.equals(""))
				.forEach(id -> {
					Tree c = entries.remove(UUID.fromString(id));

					if (c != null) {
						reconstructTree(c, entries);
					}
				});
	}

	protected String xmlAttributes(MetadataDocument document) throws JsonProcessingException {
		final String attributesChain = attributeWriter.writeValueAsString(document);

		return attributesChain
				.substring(1, attributesChain.length() - 2);
	}

	protected String xmlAttributes(ResourceDocument document) throws JsonProcessingException {
		final String attributesChain = attributeWriter.writeValueAsString(document);

		return attributesChain
				.substring(1, attributesChain.length() - 2);
	}
}
