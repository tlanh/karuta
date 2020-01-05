package eportfolium.com.karuta.consumer;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestNodeDao {

	private EasyRandom easyRandom;

	public TestNodeDao() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plus(1, ChronoUnit.DAYS);
		final LocalTime minTime = LocalTime.of(5, 0);
		final LocalTime maxTime = LocalTime.of(9, 0);
		EasyRandomParameters parameters = new EasyRandomParameters().seed(123L).objectPoolSize(100)
				.randomizationDepth(3).charset(Charset.forName("UTF-8")).timeRange(minTime, maxTime)
				.dateRange(today, tomorrow).stringLengthRange(5, 50).collectionSizeRange(1, 10)
				.scanClasspathForConcreteTypes(true).overrideDefaultInitialization(false)
				.ignoreRandomizationErrors(true);
		parameters.excludeField(
				org.jeasy.random.FieldPredicates.named("id").and(org.jeasy.random.FieldPredicates.ofType(Long.class)));

		easyRandom = new EasyRandom(parameters);
	}

	@Autowired
	private NodeDao nodeDAO;

	private String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
	private String portfolioModelUuid = "30000000-0000-0000-0000-000000000000";
	private String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";

	private String childNodeUuid = "4a00b5ed-432c-4c67-adef-696b6693d0ae";

	@Test
	public void testGetNodeOrderByNodeUuid() {
		Integer nodeOrder = nodeDAO.getNodeOrderByNodeUuid(nodeUuid);
		Assert.assertEquals(0, nodeOrder.intValue());
	}

	@Test
	public void testGetPortfolioIdFromNode() {
		UUID portId = nodeDAO.getPortfolioIdFromNode(nodeUuid);
		Assert.assertEquals(portId.toString(), portfolioUuid);
	}

	@Test
	public void testGetNodeBySemtagAndCode() throws Exception {
		String semtag = "asmStructure";
		String code = "";
		Node result = nodeDAO.getNodeBySemtagAndCode(portfolioUuid, semtag, code);
		Assert.assertEquals(result.getId().toString(), "6b133b85-e159-4b79-b96c-08ab64e4f1f4");
	}

	@Test
	public void testFindById() throws DoesNotExistException {
		Assert.assertNotNull(nodeDAO.findById(UUID.fromString(nodeUuid)));
	}

	@Test
	public void testGetParentNodeByNodeUuid() {
		Node parentNode = nodeDAO.getParentNodeByNodeUuid(childNodeUuid);
		Assert.assertEquals(parentNode.getId().toString(), nodeUuid);
	}

	@Test
	public void testGetParentNodeUuidByNodeUuid() {
		UUID parentNodeUuid = nodeDAO.getParentNodeUuidByNodeUuid(childNodeUuid);
		Assert.assertEquals(parentNodeUuid.toString(), nodeUuid);
	}

	@Test
	public void testGetNodeBySemanticTag() {
		String semanticTag = "asmStructure";
		String nodeId = "6b133b85-e159-4b79-b96c-08ab64e4f1f4";
		Node node = nodeDAO.getNodeBySemanticTag(nodeId, semanticTag);
		Assert.assertEquals(node.getId().toString(), nodeId);
	}

	@Test
	public void testGetNodesBySemanticTag() {
		String semanticTag = "asmStructure";
		List<Node> nodes = nodeDAO.getNodesBySemanticTag(portfolioUuid, semanticTag);
		Assert.assertNotNull(nodes);
		Assert.assertEquals(nodes.size(), 1);
	}

	@Test
	public void testGetNodes() {
		List<Node> nodes = nodeDAO.getNodes(portfolioUuid);
		Assert.assertNotNull(nodes);
		Assert.assertEquals(nodes.size(), 43);
	}

	@Test
	public void testGetSharedNodes() {
		List<Node> sharedNodes = nodeDAO.getSharedNodes(nodeUuid);
		Assert.assertEquals(sharedNodes.size(), 0);
	}

	@Test
	public void testIsCodeExist() {
		String nodeCode = "karuta.karuta-resources";
		final boolean codeExist = nodeDAO.isCodeExist(nodeCode, nodeUuid);
		Assert.assertFalse(codeExist);
	}

	@Test
	public void testGetMetadataWad() {
		String metadataWad = nodeDAO.getMetadataWad(nodeUuid);
		Assert.assertNotNull(metadataWad);
	}

	@Test
	public void testGetChildren() throws DoesNotExistException {
		List<Node> children = nodeDAO.getChildren(nodeUuid);
		Assert.assertNotNull(children);
		Assert.assertEquals(children.size(), 43);

	}

	@Test
	public void testGetFirstLevelChildren() throws DoesNotExistException {
		List<Node> children = nodeDAO.getFirstLevelChildren(nodeUuid);
		Assert.assertNotNull(children);
		Assert.assertEquals(children.size(), 33);
	}

	@Test
	public void testGetNodeUuidBySemtag() throws DoesNotExistException {
		String semtag = "asmStructure";
		String nodeId = nodeDAO.getNodeUuidBySemtag(semtag, nodeUuid);
		Assert.assertEquals("6b133b85-e159-4b79-b96c-08ab64e4f1f4", nodeId);
	}

	@Test
	public void testGetNodeNextOrderChildren() {
		int nextOrder = nodeDAO.getNodeNextOrderChildren(nodeUuid);
		Assert.assertEquals(nextOrder, 33);
	}

	@Test
	public void testIsPublic() {
		Assert.assertFalse(nodeDAO.isPublic(nodeUuid));
	}

	@Test
	public void testIsNotPublic() {
		Assert.assertFalse(nodeDAO.isPublic(nodeUuid));
	}

	@Test
	public void testGetNodeUuidByPortfolioModelAndSemanticTag() {
		String semanticTag = "asmRoot";
		UUID nodeId = nodeDAO.getNodeUuidByPortfolioModelAndSemanticTag(portfolioModelUuid, semanticTag);
		Assert.assertEquals(nodeId.toString(), nodeUuid);
	}

	@Test
	public void testGetNodesByOrder() throws DoesNotExistException {
		List<Node> nodes = nodeDAO.getNodesByOrder(nodeUuid, 10);
		Assert.assertNotNull(nodes);
		Assert.assertEquals(nodes.size(), 2);
	}

	@Test
	public void testGetNodesWithResources() throws DoesNotExistException {
		List<Node> nodes = nodeDAO.getNodesWithResources(portfolioUuid);
		Assert.assertNotNull(nodes);
		Assert.assertEquals(nodes.size(), 43);
	}

	@Test
	public void testGetNodesWithNodes() throws DoesNotExistException {
		List<Node> nodes = nodeDAO.getNodes(Arrays.asList(nodeUuid));
		Assert.assertNotNull(nodes);
		Assert.assertEquals(nodes.size(), 1);
	}

	@Test
	@Transactional
	public void testCreate() throws DoesNotExistException {
		Node node = easyRandom.nextObject(Node.class);
		nodeDAO.persist(node);
	}

	@Test
	@Transactional
	public void testUpdate() throws DoesNotExistException {
		Node node = nodeDAO.findById(UUID.fromString(nodeUuid));
		node.setDescr("blablabla");
		node = nodeDAO.merge(node);
	}

	@Test
	@Transactional
	public void testUpdateNodeOrder() throws DoesNotExistException {
		int result = nodeDAO.updateNodeOrder(nodeUuid, 10);
		Assert.assertEquals(result, 0);
	}

	@Test
	@Transactional
	public void testUpdateNodeCode() throws DoesNotExistException {
		int result = nodeDAO.updateNodeCode(nodeUuid, "ceci-est-un-test");
		Assert.assertEquals(result, 0);
	}

	/*************************************************************************************************/

}
