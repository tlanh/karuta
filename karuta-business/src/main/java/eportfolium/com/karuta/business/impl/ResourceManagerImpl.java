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

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.business.contract.FileManager;
import eportfolium.com.karuta.consumer.repositories.PortfolioRepository;
import eportfolium.com.karuta.consumer.repositories.ResourceRepository;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.util.JavaTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
@Transactional
public class ResourceManagerImpl extends BaseManagerImpl implements ResourceManager {

	@Autowired
	private FileManager fileManager;

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;


	@Override
	public ResourceDocument getResource(UUID parentNodeId) {
		Resource resource = resourceRepository.getResourceByParentNodeUuid(parentNodeId);

		ResourceDocument document = new ResourceDocument(resource.getId());

		document.setNodeId(parentNodeId);
		document.setContent(resource.getXsiType());

		return document;
	}

	@Override
	public Integer changeResource(UUID parentNodeId, ResourceDocument resource, Long userId)
			throws BusinessException, JsonProcessingException {

		if (!hasRight(userId, parentNodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		Resource res = resourceRepository.getResourceOfResourceByNodeUuid(parentNodeId);

		// To update `modifDate`.
		portfolioRepository.findById(parentNodeId)
				.ifPresent(portfolio -> portfolioRepository.save(portfolio));

		res.setContent(xmlAttributes(resource));
		res.setModifUserId(userId);
		res.setModifDate(JavaTimeUtil.toJavaDate(LocalDateTime.now()));
		resourceRepository.save(res);

		return 0;
	}

	@Override
	public String addResource(UUID parentNodeId, ResourceDocument resource, Long userId)
			throws BusinessException {

		if (!credentialRepository.isAdmin(userId)
				&& !hasRight(userId, parentNodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No right to write");


		if( resource.getId() == null )
			return "";

		String xsiType = resource.getXsiType();

		Resource res = resourceRepository.findById(resource.getId())
				.orElseGet(() -> new Resource(resource.getId()));

		res.setXsiType(xsiType);
		updateResourceAttrs(res, resource.getContent(), userId);

		nodeRepository.findById(parentNodeId).ifPresent(node -> {
			if (xsiType.equals("nodeRes")) {
				node.setResource(res);
				node.setSharedNodeResUuid(null);
			} else if (xsiType.equals("context")) {
				node.setContextResource(res);
			} else {
				node.setResResource(res);
				node.setSharedResUuid(null);
			}

			nodeRepository.save(node);
		});

		return "";
	}

	@Override
	public void removeResource(UUID resourceId, Long userId) throws BusinessException {
		if (!credentialRepository.isAdmin(userId)
				&& !hasRight(userId, resourceId, GroupRights.DELETE))
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

			Node node = nodeRepository.findById(nodeId)
					.orElseThrow(() -> new GenericBusinessException("Cannot update node code"));

			if (nodeRepository.isCodeExist(code, nodeId))
				throw new GenericBusinessException("CONFLICT : code already exists.");

			node.setCode(code);
			nodeRepository.save(node);

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

	@Override
	public boolean updateContent(UUID nodeId,
								 Long userId,
								 InputStream content,
								 String lang,
								 boolean thumbnail) throws BusinessException {
		if (!hasRight(userId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("No rights.");

		Resource resource = resourceRepository.findByNodeId(nodeId);
		ResourceDocument document = new ResourceDocument(resource, resource.getNode());

		return fileManager.updateResource(document, content, lang, thumbnail);
	}

	@Override
	public ResourceDocument fetchResource(UUID nodeId, OutputStream output, String lang, boolean thumbnail) {
		Resource resource = resourceRepository.findByNodeId(nodeId);
		ResourceDocument document = new ResourceDocument(resource, resource.getNode());

		if (fileManager.fetchResource(document, output, lang, thumbnail)) {
			return document;
		} else {
			return null;
		}
	}

	private void updateResourceAttrs(Resource resource, String content, Long userId) {
		resource.setContent(content);
		resource.setCredential(new Credential(userId));
		resource.setModifUserId(userId);

		resourceRepository.save(resource);
	}
}
