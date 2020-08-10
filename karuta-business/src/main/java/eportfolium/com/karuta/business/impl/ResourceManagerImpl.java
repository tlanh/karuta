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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.FileManager;
import eportfolium.com.karuta.consumer.repositories.PortfolioRepository;
import eportfolium.com.karuta.consumer.repositories.ResourceRepository;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.util.JavaTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

		res.setContent(resource.getContent());
		res.setModifUserId(userId);
		res.setModifDate(JavaTimeUtil.toJavaDate(LocalDateTime.now()));
		resourceRepository.save(res);

		return 0;
	}

	@Override
	public String addResource(UUID parentNodeId, ResourceDocument resource, Long userId)
			throws BusinessException {
		
		String retval = "";
		Optional<Node> node = nodeRepository.findById(parentNodeId);
		if( node.isPresent() )
			retval = addResource( node.get(), resource, userId);
		return retval;
	}


	@Override
	public String addResource(Node parentNodeId, ResourceDocument resource, Long userId)
			throws BusinessException {

		if (!credentialRepository.isAdmin(userId)
				&& !hasRight(userId, parentNodeId.getId(), GroupRights.WRITE))
			throw new GenericBusinessException("403 FORBIDDEN : No right to write");


		if( resource.getId() == null )
			return "";

		String xsiType = resource.getXsiType();

		Resource res = resourceRepository.findById(resource.getId())
				.orElseGet(() -> new Resource(resource.getId()));

		res.setXsiType(xsiType);
		switch( xsiType )
		{
			case "nodeRes":
				res.setNode(parentNodeId);
				break;
			case "context":
				res.setContextNode(parentNodeId);
				break;
			default:
				res.setResNode(parentNodeId);
				break;
		}
		updateResourceAttrs(res, resource.getContent(), userId);

		res = resourceRepository.save(res);
		
		if (xsiType.equals("nodeRes")) {
			parentNodeId.setResource(res);
			parentNodeId.setSharedNodeResUuid(null);
		} else if (xsiType.equals("context")) {
			parentNodeId.setContextResource(res);
		} else {
			parentNodeId.setResResource(res);
			parentNodeId.setSharedResUuid(null);
		}

		nodeRepository.save(parentNodeId);

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
		Node node = nodeRepository.findById(nodeId)
				.orElseThrow(() -> new GenericBusinessException("Cannot update node code"));

		if ("nodeRes".equals(xsiType)) {
			String code = resource.getCode();

			if (nodeRepository.isCodeExist(code, nodeId))
				throw new GenericBusinessException("CONFLICT : code already exists.");

			node.setCode(code);
			nodeRepository.save(node);

			existing = node.getResource();
		} else if ("context".equals(xsiType)) {
			existing = node.getContextResource();
		} else {
			existing = node.getResResource();
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

	//// Only for changing file
	@Override
	public String updateContent(UUID nodeId,
								 Long userId,
								 InputStream uploadfile,
								 String lang,
								 boolean thumbnail,
								 String contextPath) throws BusinessException {
		if (!hasRight(userId, nodeId, GroupRights.WRITE))
			throw new GenericBusinessException("No rights.");

		Resource resource = resourceRepository.getResourceOfResourceByNodeUuid(nodeId);
		ResourceDocument document = new ResourceDocument(resource, resource.getResNode());

		return fileManager.updateResource(document, uploadfile, lang, thumbnail, contextPath);
	}

	@Override
	public ResourceDocument fetchResource(UUID nodeId, OutputStream output, String lang, boolean thumbnail, String contextPath) {
		Resource resource = resourceRepository.getResourceOfResourceByNodeUuid(nodeId);
		ResourceDocument document = new ResourceDocument();

		//// Since values don't get parsed correctly
		try
		{
			DocumentBuilderFactory documentBuilderFactory =DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader("<node>"+resource.getContent()+"</node>"));
			Document doc = documentBuilder.parse(is);
			DOMImplementationLS impl = (DOMImplementationLS)doc.getImplementation().getFeature("LS", "3.0");
			LSSerializer serial = impl.createLSSerializer();
			serial.getDomConfig().setParameter("xml-declaration", false);
			
			//// ID to search
			String resolve = findData(doc, "fileid", lang);
			document.setFileid(lang, resolve);

			/// Or just a single one shared
			if( "".equals(resolve) )
			{
				output.close();
				return document;
			}
			
			//// Filename
			String filename = findData(doc, "filename", lang);
			document.setFilename(lang, filename);

			String type = findData(doc, "type", lang);
			document.setType(lang, type);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		if (fileManager.fetchResource(document, output, lang, thumbnail, contextPath)) {
			return document;
		} else {
			return null;
		}
	}
	
	private String findData( Document doc, String name, String lang) throws XPathExpressionException
	{
		XPath xPath = XPathFactory.newInstance().newXPath();

		String retval = null;
		String filterType = "//*[local-name()='"+name+"' and @lang='"+lang+"']";
		NodeList textList = (NodeList) xPath.compile(filterType).evaluate(doc, XPathConstants.NODESET);
		String type = "";
		if( textList.getLength() != 0 )
		{
			Element fileNode = (Element) textList.item(0);
			retval = fileNode.getTextContent();
		}

		return retval;
	}

	private void updateResourceAttrs(Resource resource, String content, Long userId) {
		resource.setContent(content);
		resource.setCredential(new Credential(userId));
		resource.setModifUserId(userId);

		resourceRepository.save(resource);
	}
}
