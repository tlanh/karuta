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

		updateResourceAttrs(res, xmlAttributes(resource), userId);

		return 0;
	}

	@Override
	public String addResource(UUID parentNodeId, ResourceDocument resource, Long userId, Long groupId)
			throws BusinessException {
		if (!credentialRepository.isAdmin(userId)
				&& !hasRight(userId, groupId, parentNodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No right to write");

		String xsiType = resource.getXsiType();

		Resource res = resourceRepository.findById(resource.getId())
				.orElseGet(() -> new Resource(resource.getId()));

		res.setXsiType(xsiType);
		updateResourceAttrs(res, resource.getContent(), userId);

		nodeRepository.findById(parentNodeId).ifPresent(node -> {
			if (xsiType.equals("nodeRes")) {
				node.setResResource(res);
				node.setSharedNodeResUuid(null);
			} else if (xsiType.equals("context")) {
				node.setContextResource(res);
			} else {
				node.setResource(res);
				node.setSharedResUuid(null);
			}

			nodeRepository.save(node);
		});

		return "";
	}

	@Override
	public void removeResource(UUID resourceId, Long userId, Long groupId) throws BusinessException {
		if (!credentialRepository.isAdmin(userId)
				&& !hasRight(userId, groupId, resourceId, GroupRights.DELETE))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		resourceRepository.deleteById(resourceId);
	}

	@Override
	public void changeResourceByXsiType(UUID nodeId,
										String xsiType,
										ResourceDocument resource,
										Long userId) throws BusinessException {

		Resource existing;

		if ("nodeRes".equals(xsiType)) {
			String code = resource.getCode();

			if (nodeRepository.isCodeExist(code, nodeId)) {
				throw new GenericBusinessException("CONFLICT : code already exists.");
			} else if (nodeManager.updateNodeCode(nodeId, code) > 0) {
				throw new GenericBusinessException("Cannot update node code");
			}

			existing = resourceRepository.getResourceOfResourceByNodeUuid(nodeId);
		} else if ("context".equals(xsiType)) {
			existing = resourceRepository.getContextResourceByNodeUuid(nodeId);
		} else {
			existing = resourceRepository.getResourceByParentNodeUuid(nodeId);
		}

		updateResourceAttrs(existing, resource.getContent(), userId);
	}

	@Override
	public void updateResource(UUID id, String xsiType, String content, Long userId) {
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
	}

	private void updateResourceAttrs(Resource resource, String content, Long userId) {
		resource.setContent(content);
		resource.setCredential(new Credential(userId));
		resource.setModifUserId(userId);

		resourceRepository.save(resource);
	}
}
