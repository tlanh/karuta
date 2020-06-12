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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.consumer.repositories.PortfolioRepository;
import eportfolium.com.karuta.consumer.repositories.ResourceRepository;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.util.JavaTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
@Transactional
public class ResourceManagerImpl extends BaseManagerImpl implements ResourceManager {

	@Autowired
	private PortfolioManager portfolioManager;

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Override
	public ResourceDocument getResource(UUID parentNodeId, Long userId, Long groupId)
			throws BusinessException {

		if (!hasRight(userId, groupId, parentNodeId, GroupRights.READ)) {
			throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
		}

		Resource resource = resourceRepository.getResourceByParentNodeUuid(parentNodeId);

		ResourceDocument document = new ResourceDocument(resource.getId());

		document.setNodeId(parentNodeId);
		document.setContent(resource.getXsiType());

		return document;
	}

	@Override
	public ResourceList getResources(UUID portfolioId, Long userId, Long groupId) {
		List<Resource> resources = resourceRepository
				.getResourcesByPortfolioUUID(portfolioId);

		return new ResourceList(resources.stream()
				.map(r -> new ResourceDocument(r.getNode().getId()))
				.collect(Collectors.toList()));
	}

	@Override
	public Integer changeResource(UUID parentNodeId, ResourceDocument resource, Long userId,
			Long groupId) throws BusinessException, JsonProcessingException {

		if (!hasRight(userId, groupId, parentNodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		Resource res = resourceRepository.getResourceByParentNodeUuid(parentNodeId);

		portfolioManager.updateTimeByNode(parentNodeId);

		return updateResource(res.getId(), null, xmlAttributes(resource), userId);
	}

	@Override
	public String addResource(UUID parentNodeId, ResourceDocument resource, Long userId, Long groupId)
			throws BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No ADMIN right");


		if (!hasRight(userId, groupId, parentNodeId, GroupRights.WRITE)) {
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");
		}

		Portfolio portfolio = portfolioRepository.getPortfolioFromNode(parentNodeId);

		String xsiType = resource.getXsiType();

		UUID portfolioId = portfolio != null ? portfolio.getId()  : null;
		UUID modelId = portfolio != null ? portfolio.getModelId() : null;

		addResource(resource.getId(), portfolioId, xsiType, resource.getContent(),
				modelId, false, false, userId);

		return "";
	}

	@Override
	public void removeResource(UUID resourceId, Long userId, Long groupId) throws BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		if (hasRight(userId, groupId, resourceId, GroupRights.DELETE)) {
			resourceRepository.deleteById(resourceId);
		}
	}

	@Override
	public void changeResourceByXsiType(UUID nodeId,
										String xsiType,
										ResourceDocument resource,
										Long userId) throws BusinessException {

		if (StringUtils.equals(xsiType, "nodeRes")) {
			updateResResource(nodeId, resource.getContent(), userId);
			String code = resource.getCode();

			if (nodeRepository.isCodeExist(code, nodeId)) {
				throw new GenericBusinessException("CONFLICT : code already exists.");
			} else if (nodeManager.updateNodeCode(nodeId, code) > 0) {
				throw new GenericBusinessException("Cannot update node code");
			}
		} else if (StringUtils.equals(xsiType, "context")) {
			updateContextResource(nodeId, resource.getContent(), userId);
		} else {
			updateResource(nodeId, resource.getContent(), userId);
		}
	}

	@Override
	public int addResource(UUID id, UUID parentId, String xsiType, String content, UUID portfolioModelId,
						   boolean sharedNodeRes, boolean sharedRes, Long userId) {

		if (((xsiType.equals("nodeRes") && sharedNodeRes)
				|| (!xsiType.equals("context") && !xsiType.equals("nodeRes") && sharedRes))
				&& portfolioModelId != null) {
			return 0;
		}

		Resource resource = resourceRepository.findById(id)
								.orElse(new Resource(id));

		resource.setXsiType(xsiType);
		resource.setContent(content);
		resource.setCredential(new Credential(userId));
		resource.setModifUserId(userId);

		resourceRepository.save(resource);

		Optional<Node> node = nodeRepository.findById(parentId);

		if (!node.isPresent())
			return 1;

		Node n = node.get();

		// Ensuite on met à jour les id ressource au niveau du noeud parent
		if (xsiType.equals("nodeRes")) {
			n.setResResource(resource);
			if (sharedNodeRes) {
				n.setSharedNodeResUuid(id);
			} else {
				n.setSharedNodeResUuid(null);
			}
		} else if (xsiType.equals("context")) {
			n.setContextResource(resource);
		} else {
			n.setResource(resource);
			if (sharedRes) {
				n.setSharedResUuid(id);
			} else {
				n.setSharedResUuid(null);
			}
		}

		nodeRepository.save(n);

		return 0;
	}

	@Override
	public int updateResource(UUID nodeUuid, String content, Long userId) {
		Resource resource = resourceRepository.getResourceByParentNodeUuid(nodeUuid);

		resource.setContent(content);
		resource.setCredential(new Credential(userId));
		resource.setModifUserId(userId);

		resourceRepository.save(resource);

		return 0;
	}

	@Override
	public int updateResource(UUID id, String xsiType, String content, Long userId) {
		if (xsiType != null) {
			resourceRepository.deleteById(id);

			Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.defaultTimezone));

			Resource resource = new Resource(
				id,
				xsiType,
				content,
				new Credential(userId),
				userId,
				now
			);

			resourceRepository.save(resource);
		} else {
			Optional<Resource> resourceOptional = resourceRepository.findById(id);

			if (resourceOptional.isPresent()) {
				Resource resource = resourceOptional.get();

				resource.setContent(content);
				resource.setCredential(new Credential(userId));
				resource.setModifUserId(userId);

				resourceRepository.save(resource);
			} else {
				return 1;
			}
		}

		return 0;
	}

	@Override
	public int updateContextResource(UUID nodeUuid, String content, Long userId) {
		Resource resource = resourceRepository.getContextResourceByNodeUuid(nodeUuid);

		resource.setContent(content);
		resource.setCredential(new Credential(userId));
		resource.setModifUserId(userId);

		resourceRepository.save(resource);

		return 0;
	}

	private void updateResResource(UUID nodeUuid, String content, Long userId) {
		Resource resource = resourceRepository.getResourceOfResourceByNodeUuid(nodeUuid);

		resource.setContent(content);
		resource.setCredential(new Credential(userId));
		resource.setModifUserId(userId);

		resourceRepository.save(resource);
	}
}
