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
import eportfolium.com.karuta.business.contract.BaseManager;
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.GroupRightsRepository;
import eportfolium.com.karuta.consumer.repositories.NodeRepository;
import eportfolium.com.karuta.document.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.Node;

public abstract class BaseManagerImpl implements BaseManager {

	@Autowired
	protected CredentialRepository credentialRepository;

	@Autowired
	protected NodeRepository nodeRepository;

	@Autowired
	protected GroupRightsRepository groupRightsRepository;

	private static final XmlMapper mapper = new XmlMapper();
	private static final ObjectWriter attributeWriter = mapper.writer().withRootName("");

	// Help reconstruct tree
	protected static class Tree {
		NodeDocument node = null;
		String childString = "";
	}

	@Override
	public GroupRights getRights(Long userId, Long groupId, UUID nodeId) {
		if (credentialRepository.isAdmin(userId)
				|| credentialRepository.isDesigner(userId, nodeId)) {
			return new GroupRights(new GroupRightsId(new GroupRightInfo(), null),
					true, true, true, true, false);
		} else {
			GroupRights rights;

			if (groupId == null || groupId == 0L) {
				rights = groupRightsRepository.getRightsByIdAndUser(nodeId, userId);
			} else {
				rights = groupRightsRepository.getRightsByUserAndGroup(nodeId, userId, groupId);
			}

			if (rights == null)
				rights = groupRightsRepository.getSpecificRightsForUser(nodeId, userId);

			if (rights == null)
				rights = groupRightsRepository.getPublicRightsByGroupId(nodeId, groupId);

			// If we couldn't find any associated group rights, we provide
			// an instance without any permission.
			if (rights == null) {
				rights = new GroupRights();
				rights.setId(new GroupRightsId(new GroupRightInfo(), null));
			}

			// If the node is public, we can give read access.
			if (nodeRepository.isPublic(nodeId)) {
				rights.setRead(true);
			}

			return rights;
		}
	}

	@Override
	public boolean hasRight(Long userId, Long groupId, UUID nodeId, String right) {
		GroupRights rights = getRights(userId, groupId, nodeId);

		switch (right) {
			case GroupRights.READ:
				return rights.isRead();
			case GroupRights.WRITE:
				return rights.isWrite();
			case GroupRights.SUBMIT:
				return rights.isSubmit();
			case GroupRights.DELETE:
				return rights.isDelete();
			default:
				return false;
		}
	}

	protected void processQuery(List<Pair<Node, GroupRights>> nodes,
								Map<UUID, Tree> entries,
								String role) throws JsonProcessingException {
		if (nodes.isEmpty()) {
			return;
		}

		for (Pair<Node, GroupRights> pair : nodes) {
			Node node = pair.getLeft();
			GroupRights groupRights = pair.getRight();

			// In case we have rights on nodes that are not part of the portfolio
			if (node.getId() == null)
				continue;

			// TODO: Check whether data is always consistent regarding xsiType
			List<ResourceDocument> resources = Stream.of(
					node.getResResource(),
					node.getContextResource(),
					node.getResource())
				.filter(resource -> resource != null && resource.getContent() != null)
				.map(resource -> new ResourceDocument(resource, node))
				.collect(Collectors.toList());

			NodeDocument nodeDocument = new NodeDocument(node, groupRights, role);

			nodeDocument.setMetadataWad(MetadataWadDocument.from(node.getMetadataWad()));
			nodeDocument.setMetadataEpm(MetadataEpmDocument.from(node.getMetadataEpm()));
			nodeDocument.setMetadata(MetadataDocument.from(node.getMetadata()));

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
