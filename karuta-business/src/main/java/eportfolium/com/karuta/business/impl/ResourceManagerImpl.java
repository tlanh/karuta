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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.consumer.repositories.ResourceTableRepository;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.util.JavaTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
@Transactional
public class ResourceManagerImpl extends BaseManager implements ResourceManager {

	@Autowired
	private PortfolioManager portfolioManager;

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private ResourceTableRepository resourceTableRepository;

	public String getResource(MimeType outMimeType, UUID parentNodeId, Long userId, Long groupId)
			throws BusinessException {

		if (!hasRight(userId, groupId, parentNodeId, GroupRights.READ)) {
			throw new GenericBusinessException("403 FORBIDDEN : No READ credential");
		}

		ResourceTable rt = resourceTableRepository.getResourceByParentNodeUuid(parentNodeId);

		return "<asmResource id=\"" + rt.getId().toString() + "\" contextid=\"" + parentNodeId.toString() + "\">"
				+ rt.getXsiType() + "</asmResource>";
	}

	/** Récupère le noeud, et assemble les ressources, s'il y en a */
	@Override
	public String getResource(UUID nodeId) {
		String result = "";

		Node resNode = nodeRepository.findById(nodeId).get();
		String m_epm = resNode.getMetadataEpm();
		if (m_epm == null)
			m_epm = "";
		result += "<" + resNode.getAsmType() + " id='" + resNode.getId().toString() + "'>";
		result += "<metadata " + resNode.getMetadata() + "/>";
		result += "<metadata-epm " + m_epm + "/>";
		result += "<metadata-wad " + resNode.getMetadataWad() + "/>";

		ResourceTable resResource = resNode.getResource();
		if (resResource != null && resResource.getId() != null) {
			result += "<asmResource id='" + resResource.getId().toString() + "' contextid='"
					+ resNode.getId().toString() + "' xsi_type='" + resResource.getXsiType() + "'>";
			result += resResource.getContent();
			result += "</asmResource>";
		}

		resResource = resNode.getResResource();
		if (resResource != null && resResource.getId() != null) {
			result += "<asmResource id='" + resResource.getId().toString() + "' contextid='"
					+ resNode.getId().toString() + "' xsi_type='" + resResource.getXsiType() + "'>";
			result += resResource.getContent();
			result += "</asmResource>";

		}

		resResource = resNode.getContextResource();
		if (resResource != null && resResource.getId() != null) {
			result += "<asmResource id='" + resResource.getId().toString() + "' contextid='"
					+ resNode.getId().toString() + "' xsi_type='" + resResource.getXsiType() + "'>";
			result += resResource.getContent();
			result += "</asmResource>";

		}
		result += "</" + resNode.getAsmType() + ">";

		return result;
	}

	@Override
	public String getResources(MimeType outMimeType, UUID portfolioId, Long userId, Long groupId) throws Exception {
		List<ResourceTable> res = resourceTableRepository.getResourcesByPortfolioUUID(portfolioId);

		String returnValue = "";

		if (outMimeType.getSubtype().equals("xml")) {
			returnValue += "<resources>";
			for (ResourceTable rt : res) {
				returnValue += "<resource " + DomUtils.getXmlAttributeOutput("id", rt.getNode().getId().toString())
						+ " />";
			}
			returnValue += "</resources>";
		} else {
			returnValue += "{";
			boolean firstNode = true;
			for (ResourceTable rt : res) {
				if (firstNode)
					firstNode = false;
				else
					returnValue += " , ";
				returnValue += "resource: { " + DomUtils.getJsonAttributeOutput("id", rt.getNode().getId().toString())
						+ " } ";
			}
			returnValue += "}";
		}
		return returnValue;
	}

	public Integer changeResource(MimeType inMimeType, UUID parentNodeId, String xmlResource, Long userId,
			Long groupId) throws BusinessException, Exception {

		int retVal = -1;

		xmlResource = DomUtils.filterXmlResource(xmlResource);

		ResourceTable rt = resourceTableRepository.getResourceByParentNodeUuid(parentNodeId);

		Document doc = DomUtils.xmlString2Document(xmlResource, new StringBuffer());
		// Puis on le recrée
		org.w3c.dom.Node node = (doc.getElementsByTagName("asmResource")).item(0);

		if (!hasRight(userId, groupId, parentNodeId, GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");

		portfolioManager.updateTimeByNode(parentNodeId);

		retVal = updateResource(rt.getId(), null, DomUtils.getInnerXml(node), userId);

		return retVal;

	}

	public String addResource(MimeType inMimeType, UUID parentNodeId, String in, Long userId, Long groupId)
			throws BusinessException, Exception {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No ADMIN right");

		in = DomUtils.filterXmlResource(in);

		if (!hasRight(userId, groupId, parentNodeId, GroupRights.WRITE)) {
			throw new GenericBusinessException("403 FORBIDDEN : No WRITE credential");
		} else
			nodeManager.addNode(inMimeType, parentNodeId, in, userId, groupId, true);

		return "";
	}

	public void removeResource(UUID resourceId, Long userId, Long groupId)
			throws DoesNotExistException, BusinessException {
		if (!credentialRepository.isAdmin(userId))
			throw new GenericBusinessException("403 FORBIDDEN : No admin right");

		if (hasRight(userId, groupId, resourceId, GroupRights.DELETE)) {
			resourceTableRepository.deleteById(resourceId);
		}
	}

	public void changeResourceByXsiType(UUID nodeId, String xsiType, String content, Long userId) throws Exception {

		if (StringUtils.equals(xsiType, "nodeRes")) {
			updateResResource(nodeId, content, userId);
			/// Interprétation du code XML.
			Document doc = DomUtils.xmlString2Document(
					"<?xml version='1.0' encoding='UTF-8' standalone='no'?><res>" + content + "</res>",
					new StringBuffer());
			NodeList nodes = doc.getElementsByTagName("code");
			org.w3c.dom.Node code = nodes.item(0);
			if (code != null) {
				org.w3c.dom.Node codeContent = code.getFirstChild();

				String codeVal;
				if (codeContent != null) {
					codeVal = codeContent.getNodeValue();
					// Vérifier si le code existe déjà.
					if (nodeRepository.isCodeExist(codeVal, nodeId)) {
						throw new GenericBusinessException("CONFLICT : code already exists.");
					} else {
						if (nodeManager.updateNodeCode(nodeId, codeVal) > 0) {
							throw new GenericBusinessException("Cannot update node code");
						}
					}
				}
			}
		} else if (StringUtils.equals(xsiType, "context")) {
			updateContextResource(nodeId, content, userId);
		} else {
			updateResource(nodeId, content, userId);
		}

	}

	@Override
	public int addResource(UUID id, UUID parentId, String xsiType, String content, UUID portfolioModelId,
						   boolean sharedNodeRes, boolean sharedRes, Long userId) {

		int status = 0;

		ResourceTable rt = null;
		if (((xsiType.equals("nodeRes") && sharedNodeRes)
				|| (!xsiType.equals("context") && !xsiType.equals("nodeRes") && sharedRes))
				&& portfolioModelId != null) {
			// On ne fait rien
		} else {
			Optional<ResourceTable> resourceTableOptional = resourceTableRepository.findById(id);

			if (resourceTableOptional.isPresent()) {
				rt = resourceTableOptional.get();
			} else {
				rt = new ResourceTable();
				rt.setId(id);
			}

			rt.setXsiType(xsiType);
			rt.setContent(content);
			rt.setCredential(new Credential(userId));
			rt.setModifUserId(userId);

			resourceTableRepository.save(rt);
		}


		try {
			final Node n = nodeRepository.findById(parentId).get();

			// Ensuite on met à jour les id ressource au niveau du noeud parent
			if (xsiType.equals("nodeRes")) {
				n.setResResource(rt);
				if (sharedNodeRes && portfolioModelId != null) {
					n.setSharedNodeResUuid(id);
				} else {
					n.setSharedNodeResUuid(null);
				}
			} else if (xsiType.equals("context")) {
				n.setContextResource(rt);
			} else {
				n.setResource(rt);
				if (sharedRes && portfolioModelId != null) {
					n.setSharedResUuid(id);
				} else {
					n.setSharedResUuid(null);
				}
			}

			nodeRepository.save(n);
		} catch (Exception ex) {
			ex.printStackTrace();
			status = 1;
		}
		return status;
	}

	@Override
	public int updateResource(UUID nodeUuid, String content, Long userId) {
		ResourceTable rt = resourceTableRepository.getResourceByParentNodeUuid(nodeUuid);

		rt.setContent(content);
		rt.setCredential(new Credential(userId));
		rt.setModifUserId(userId);

		resourceTableRepository.save(rt);

		return 0;
	}

	@Override
	public int updateResource(UUID id, String xsiType, String content, Long userId) {
		if (xsiType != null) {
			resourceTableRepository.deleteById(id);

			Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.date_default_timezone));

			ResourceTable rt = new ResourceTable(
				id,
				xsiType,
				content,
				new Credential(userId),
				userId,
				now
			);

			resourceTableRepository.save(rt);
		} else {
			Optional<ResourceTable> resourceTable = resourceTableRepository.findById(id);

			if (resourceTable.isPresent()) {
				ResourceTable rt = resourceTable.get();

				rt.setContent(content);
				rt.setCredential(new Credential(userId));
				rt.setModifUserId(userId);

				resourceTableRepository.save(rt);
			} else {
				return 1;
			}

		}

		return 0;
	}

	@Override
	public int updateContextResource(UUID nodeUuid, String content, Long userId) {
		ResourceTable rt = resourceTableRepository.getContextResourceByNodeUuid(nodeUuid);

		rt.setContent(content);
		rt.setCredential(new Credential(userId));
		rt.setModifUserId(userId);

		resourceTableRepository.save(rt);

		return 0;
	}

	private int updateResResource(UUID nodeUuid, String content, Long userId) {
		ResourceTable rt = resourceTableRepository.getResourceOfResourceByNodeUuid(nodeUuid);

		rt.setContent(content);
		rt.setCredential(new Credential(userId));
		rt.setModifUserId(userId);

		resourceTableRepository.save(rt);

		return 0;
	}
}
