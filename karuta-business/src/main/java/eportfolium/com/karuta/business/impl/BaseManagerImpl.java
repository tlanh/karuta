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
import eportfolium.com.karuta.business.UserInfo;
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
import eportfolium.com.karuta.model.bean.Resource;
import org.springframework.security.core.userdetails.UserDetails;

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
		String type = "";
		String nodeContent = "";
	}

	@Override
	public GroupRights getRights(Long userId, UUID nodeId) {
		if (credentialRepository.isAdmin(userId)
				|| credentialRepository.isDesigner(userId, nodeId)) {
			return new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true);
		} else {
			GroupRights rights = groupRightsRepository.getRightsByIdAndUser(nodeId, userId);

			if (rights == null)
				rights = groupRightsRepository.getSpecificRightsForUser(nodeId, userId);

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
	public boolean hasRight(Long userId, UUID nodeId, String right) {
		GroupRights rights = getRights(userId, nodeId);

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

	@Override
	public boolean canRead(UserDetails userDetails, UUID nodeId) {
		return hasRight(userDetails, nodeId, GroupRights.READ);
	}

	@Override
	public boolean canDelete(UserDetails userDetails, UUID nodeId) {
		return hasRight(userDetails, nodeId, GroupRights.DELETE);
	}

	@Override
	public boolean canWrite(UserDetails userDetails, UUID nodeId) {
		return hasRight(userDetails, nodeId, GroupRights.WRITE);
	}

	private boolean hasRight(UserDetails userDetails, UUID nodeId, String right) {
		Long userId = (userDetails instanceof UserInfo) ? ((UserInfo)userDetails).getId() : 0L;

		return hasRight(userId, nodeId, right);
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

			StringBuilder data = new StringBuilder(256);
			String nodeFormat = "<%s delete=\"%s\" id=\"%s\" read=\"%s\" role=\"%s\" submit=\"%s\" write=\"%s\" last_modif=\"%s\" xsi_type=\"%s\">" +
					"<metadata-wad %s/>" +
					"<metadata-epm %s/>" +
					"<metadata %s/>";
			
			String nodeData = String.format(nodeFormat, node.getAsmType(), groupRights.isDelete(), node.getId(), groupRights.isRead(), "", groupRights.isSubmit(), groupRights.isWrite(), node.getModifDate(), node.getXsiType(),
					node.getMetadataWad(), node.getMetadataEpm(), node.getMetadata());
			data.append(nodeData);
			
			String resFormat = "<asmResource contextid=\"%s\" id=\"%s\" last_modif=\"%s\" xsi_type=\"%s\">%s</asmResource>";
			Resource noderes = node.getResource();
			if( noderes != null )
			{
				String resData = String.format(resFormat, node.getId(), noderes.getId(), noderes.getModifDate(), noderes.getXsiType(), noderes.getContent());
				data.append(resData);
			}
			
			Resource nodectx = node.getContextResource();
			if( nodectx != null )
			{
				String resData = String.format(resFormat, nodectx.getId(), nodectx.getId(), nodectx.getModifDate(), nodectx.getXsiType(), nodectx.getContent());
				data.append(resData);
			}
			
			Resource noderesres = node.getResResource();
			if( noderesres != null )
			{
				String resData = String.format(resFormat, node.getId(), noderesres.getId(), noderesres.getModifDate(), noderesres.getXsiType(), noderesres.getContent());
				data.append(resData);
			}

			NodeDocument nodeDocument = new NodeDocument(node, groupRights, role);

			nodeDocument.setMetadataWad(MetadataWadDocument.from(node.getMetadataWad()));
			nodeDocument.setMetadataEpm(MetadataEpmDocument.from(node.getMetadataEpm()));
			nodeDocument.setMetadata(MetadataDocument.from(node.getMetadata()));

			nodeDocument.getResources().addAll(Stream.of(
						node.getResource(),
						node.getResResource(),
						node.getContextResource())
					.filter(Objects::nonNull)
					.map(resource -> new ResourceDocument(resource, node))
					.collect(Collectors.toList()));

			/// Prepare data to reconstruct tree
			Tree entry = new Tree();
			entry.node = nodeDocument;
			entry.type = node.getAsmType();
			entry.nodeContent = data.toString();

			if (StringUtils.isNotEmpty(node.getChildrenStr())) {
				entry.childString = node.getChildrenStr();
			}

			entries.put(node.getId(), entry);
		}
	}

	protected void reconstructTree(StringBuilder sb, NodeDocument root, Tree node, Map<UUID, Tree> entries) {
		if (node.childString == null)
			return;

		Stream<String> childIds = Stream.of(node.childString.split(","));

		sb.append(node.nodeContent);
		
		childIds
				.filter(id -> !id.equals(""))
				.forEach(id -> {
					Tree child = entries.remove(UUID.fromString(id));

					if (child != null) {
						root.getChildren().add(child.node);
						reconstructTree(sb, child.node, child, entries);
					}
				});
		sb.append("</").append(node.type).append(">");
	}

	protected String xmlAttributes(MetadataDocument document) throws JsonProcessingException {
		final String attributesChain = attributeWriter.writeValueAsString(document);

		return attributesChain
				.substring(1, attributesChain.length() - 2).trim();
	}

	protected String xmlAttributes(ResourceDocument document) throws JsonProcessingException {
		final String attributesChain = attributeWriter.writeValueAsString(document);

		return attributesChain
				.substring(2, attributesChain.length() - 3);
	}
}
