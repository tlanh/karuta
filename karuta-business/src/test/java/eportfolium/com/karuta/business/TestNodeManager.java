package eportfolium.com.karuta.business;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MimeTypeUtils;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.model.bean.Node;

/**
 * @author mlengagne
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
public class TestNodeManager {

	/**
	 * Pretty-prints XML, supplied as a string.
	 * <p/>
	 * eg. <code>
	 * String formattedXml = new XmlFormatter().format("<tag><nested>hello</nested></tag>");
	 * </code>
	 */
	class XmlFormatter {

		public String format(String xml) {

			try {
				final InputSource src = new InputSource(new StringReader(xml));
				final org.w3c.dom.Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src)
						.getDocumentElement();
				final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

				// May need this:
				// System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");

				final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
				final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
				final LSSerializer writer = impl.createLSSerializer();

				writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the
																							// output needs to be
																							// beautified.
				writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the
																						// declaration is needed to be
																						// outputted.

				return writer.writeToString(document);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private NodeDao nodeDao;

//	@Test
//	public void testGetNodeBySemanticTag() throws BusinessException {
//		System.out.println("TestNodeManager.getNodeBySemanticTag()");
//		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
//		String semantictag = "asmStructure";
//		String node = nodeManager.getNodeBySemanticTag(MimeTypeUtils.TEXT_XML, portfolioUuid, semantictag, 1L, 3L);
//		System.out.println(new XmlFormatter().format(node));
//	}
//
//	@Test
//	public void getNodesBySemanticTag() throws BusinessException {
//		System.out.println("TestNodeManager.getNodesBySemanticTag()");
//		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
//		String semantictag = "asmStructure";
//		String nodes = nodeManager.getNodesBySemanticTag(MimeTypeUtils.TEXT_XML, 1L, 3L, portfolioUuid, semantictag);
//		System.out.println(new XmlFormatter().format(nodes));
//	}
//
//	@Test
//	public void testGetParentNodes() throws Exception {
//		System.out.println("TestNodeManager.getParentNodes()");
//		String parentNodeCode = "karuta.karuta-resources";
//		String childSemtag = "asmStructure";
//		String parentSemtag = "karuta-components";
//		String parentNodes = nodeManager.getChildNodes(parentNodeCode, parentSemtag, childSemtag);
//		System.out.println(new XmlFormatter().format(parentNodes));
//	}
//
//	@Test
//	public void testIsCodeExist() {
//		System.out.println("TestNodeManager.isCodeExist()");
//		String nodeCode = "karuta.karuta-resources";
//		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		Boolean codeExists = nodeManager.isCodeExist(nodeCode, nodeUuid);
//		Assert.assertFalse(codeExists);
//	}
//
//	@Test
//	public void testGetPortfolioIdFromNode() throws DoesNotExistException, BusinessException {
//		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
//		System.out.println("TestNodeManager.getPortfolioIdFromNode()");
//		final String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String portId = nodeManager.getPortfolioIdFromNode(1L, nodeUuid);
//		System.out.println("portId : " + portId);
//		Assert.assertEquals(portfolioUuid, portId);
//	}
//
//	@Test
//	public void testGetNode() throws DoesNotExistException, BusinessException, Exception {
//		System.out.println("TestNodeManager.getNode()");
//		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String node = nodeManager.getNode(MimeTypeUtils.TEXT_XML, nodeUuid, true, 1L, 3L, "", null);
//		System.out.println(new XmlFormatter().format(node));
//	}
//
//	@Test
//	public void testGetNodeXmlOutput() {
//		System.out.println("TestNodeManager.getNodeXmlOutput()");
//		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String xmlOutput = nodeManager.getNodeXmlOutput(nodeUuid, true, "", 1L, 3L, "", true);
//		System.out.println(new XmlFormatter().format(xmlOutput));
//	}
//
//	@Test
//	public void testGetNodes() throws BusinessException {
//		System.out.println("TestNodeManager.getNodes()");
//		String rootNodeCode = "karuta.karuta-resources";
//		String childSemtag = "SendEmail";
//		String parentSemtag = "karuta-components";
//		String xmlOutput = nodeManager.getNodes(MimeTypeUtils.TEXT_XML, rootNodeCode, childSemtag, 1L, 3L, parentSemtag,
//				rootNodeCode, null);
//		System.out.println(new XmlFormatter().format(xmlOutput));
//	}
//
//	@Test
//	public void testGetRoleByNode() throws BusinessException {
//		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		long groupId = nodeManager.getRoleByNode(1L, nodeUuid, "root");
//		Assert.assertEquals(3, groupId);
//	}
//
//	@Test
//	public void testGetNodeMetadataWad() throws DoesNotExistException, BusinessException {
//		System.out.println("TestNodeManager.getNodeMetadataWad()");
//		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String metadata = nodeManager.getNodeMetadataWad(MimeTypeUtils.TEXT_XML, nodeUuid, 1L, 3L);
//		Assert.assertNotNull(metadata);
//		System.out.println(new XmlFormatter().format(metadata));
//	}
//
//	@Test
//	public void testChangeParentNode() throws BusinessException {
//		String uuidParent = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String uuidChild = "4a00b5ed-432c-4c67-adef-696b6693d0ae";
//		boolean parentNode = nodeManager.changeParentNode(1L, uuidChild, uuidParent);
//		Assert.assertTrue(parentNode);
//	}

//	@Test
//	public void testChangeNode() throws Exception {
//		String nodeUuid = "6b133b85-e159-4b79-b96c-08ab64e4f1f4";
//		Long userId = 1L;
//		Long groupId = 3L;
//
//		String xmlNode = "<asmStructure id=\"6b133b85-e159-4b79-b96c-08ab64e4f1f4\">";
//		xmlNode += "<metadata-wad seenoderoles=\"all\"/>";
//		xmlNode += "<metadata-epm/>";
//		xmlNode += "<metadata multilingual-node=\"Y\" semantictag=\"asmStructure\" sharedNode=\"N\" sharedNodeResource=\"N\"/>";
//		xmlNode += "<code/>";
//		xmlNode += "<label/>";
//		xmlNode += "<description/>";
//		xmlNode += "<semanticTag>asmStructure</semanticTag>";
//		xmlNode += "<asmResource contextid=\"6b133b85-e159-4b79-b96c-08ab64e4f1f4\" id=\"35fdc7fc-9c4f-402d-b3f5-94302fb6851e\" xsi_type=\"nodeRes\">";
//		xmlNode += "<lastmodified/><code/>";
//		xmlNode += "<label lang=\"fr\">Nouvelle Section</label>";
//		xmlNode += "<label lang=\"en\">New Section</label></asmResource>";
//		xmlNode += "<asmResource contextid=\"6b133b85-e159-4b79-b96c-08ab64e4f1f4\" id=\"a794fac7-d871-4651-9233-dfc2335894ff\" xsi_type=\"context\">";
//		xmlNode += "<text lang=\"fr\"/>";
//		xmlNode += "<text lang=\"en\"/>";
//		xmlNode += "</asmResource>";
//		xmlNode += "</asmStructure>";
//		Integer nodeId = nodeManager.changeNode(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, userId, groupId);
//		Assert.assertNotNull(nodeId);
//	}

// @Test
//	public void testChangeRights() throws BusinessException {
//		GroupRights rights = new GroupRights();
//		final String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String changeRights = nodeManager.changeRights(1L, nodeUuid, "root", rights);
//		Assert.assertNotNull(changeRights);
//	}
//
//	@Test
//	public void testChangeNodeMetadataWad() throws Exception {
//		final String xmlmetawad = "<metadata-wad seenoderoles=\"nearly-all\"/>";
//		final String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String nodeMetadataWad = nodeManager.changeNodeMetadataWad(MimeTypeUtils.TEXT_XML, nodeUuid, xmlmetawad, 1L,
//				3L);
//		Assert.assertNotNull(nodeMetadataWad);
//	}
//
//	@Test
//	public void testChangeNodeMetadataEpm() throws Exception, BusinessException, DoesNotExistException {
//		final String xmlmetaEpm = "<metadata-epm height=\"1400px\" left=\"1400\" top=\"1400\" width=\"1400px\"/>";
//		final String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String nodeMetadataEpm = nodeManager.changeNodeMetadataEpm(MimeTypeUtils.TEXT_XML, nodeUuid, xmlmetaEpm, 1L,
//				3L);
//		Assert.assertNotNull(nodeMetadataEpm);
//	}
//
//	@Test
//	public void testChangeNodeMetadata() throws DoesNotExistException, BusinessException, Exception {
//		final String xmlMetadata = "<metadata semantictag=\"karuta-components\" sharedNode=\"Y\" sharedNodeResource=\"N\" />";
//		final String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String nodeMetadata = nodeManager.changeNodeMetadata(MimeTypeUtils.TEXT_XML, nodeUuid, xmlMetadata, 1L, 3L);
//		Assert.assertNotNull(nodeMetadata);
//	}
//
//	@Test
//	public void testChangeNodeContext() throws BusinessException, Exception {
//		String nodeUuid = "d4f10d93-c0b4-414f-b4ad-da23ac332aa1";
//		String xmlNode = "<asmResource contextid=\"d4f10d93-c0b4-414f-b4ad-da23ac332aa1\"";
//		xmlNode += " id=\"027900ab-9d77-49e5-a4c7-57c333a1faba\" xsi_type=\"SendEmail\">";
//		xmlNode += " <lastmodified/>";
//		xmlNode += " <firstname lang=\"fr\"/>";
//		xmlNode += " <firstname lang=\"en\"/>";
//		xmlNode += " <lastname lang=\"fr\"/>";
//		xmlNode += " <lastname lang=\"en\"/>";
//		xmlNode += " <email lang=\"fr\"/>";
//		xmlNode += " <email lang=\"en\"/>";
//		xmlNode += " </asmResource>";
//		String nodeContext = nodeManager.changeNodeContext(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, 1L, 3L);
//		Assert.assertNotNull(nodeContext);
//	}

	@Test
	public void testAddNode() throws Exception {
		String nodeUuid = "d4f10d93-c0b4-414f-b4ad-da23ac332aa1";
		final List<Node> children = nodeDao.getFirstLevelChildren(nodeUuid);
		final String xmlNode = "<metadata semantictag=\"karuta-components\" sharedNode=\"Y\" sharedNodeResource=\"N\" />";
		String result = nodeManager.addNode(MimeTypeUtils.TEXT_XML, nodeUuid, xmlNode, 1L, 3L, true);
		final List<Node> children2 = nodeDao.getFirstLevelChildren(nodeUuid);
		System.out.println(new XmlFormatter().format(result));
		Assert.assertEquals(children.size() + 1, children2.size());
	}
//	
//	@Test
//	public void addNodeFromModelBySemanticTag() throws Exception {
//		String parentNodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String semanticTag = "karuta.karuta-resources";
//		nodeManager.addNodeFromModelBySemanticTag(MimeTypeUtils.TEXT_XML, parentNodeUuid, semanticTag, 1L, 3L);
//	}
//
//	@Test
//	public void testCopyNode() throws Exception {
//		String destUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String srcuuid = "d4f10d93-c0b4-414f-b4ad-da23ac332aa1";
//		String code = "karuta-components";
//		String tag = "karuta-components";
//		String copyNode = nodeManager.copyNode(MimeTypeUtils.TEXT_XML, destUuid, tag, code, srcuuid, 1L, 3L);
//		Assert.assertNotNull(copyNode);
//	}

//	@Test
//	public void testImportNode() throws BusinessException, Exception {
//		String destUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		String srcuuid = "d4f10d93-c0b4-414f-b4ad-da23ac332aa1";
//		String code = "karuta-components";
//		String tag = "karuta-components";
//		String importNode = nodeManager.importNode(MimeTypeUtils.TEXT_XML, destUuid, tag, code, srcuuid, 1L, 3L);
//		Assert.assertNotNull(importNode);
//	}
//
//	@Test
//	public void testRemoveNode() throws BusinessException {
//		String parentNodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
//		final List<Node> children = nodeDao.getFirstLevelChildren(parentNodeUuid);
//		nodeManager.removeNode(children.get(children.size() - 1).getId().toString(), 1L, 3L);
//		final List<Node> children2 = nodeDao.getFirstLevelChildren(parentNodeUuid);
//		Assert.assertEquals(children.size() - 1, children2.size());
//	}

}
