package eportfolium.com.karuta.consumer;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.consumer.contract.dao.PortfolioDao;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestPortfolioDao {

	@Autowired
	private PortfolioDao portfolioDAO;

	private String portfolioModelUuid = "30000000-0000-0000-0000-000000000000";
	private String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
	private String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";

	@Test
	public void testGetPortfolio() {
		Portfolio p = portfolioDAO.getPortfolio(portfolioUuid);
		// Then
		Assert.assertEquals(portfolioUuid, p.getId().toString());
	}

	@Test
	public void testGetOwner() {
		Long owner = portfolioDAO.getOwner(portfolioUuid);
		Assert.assertEquals(owner.longValue(), 1L);
	}

	@Test
	public void testIsNotPublic() {
		Assert.assertFalse(portfolioDAO.isPublic(portfolioUuid));
	}

	@Test
	public void testFindById() throws DoesNotExistException {
		Assert.assertNotNull(portfolioDAO.findById(UUID.fromString(portfolioUuid)));
	}

	@Test
	public void testGetPortfolios() {
		//List<Portfolio> ports = portfolioDAO.getPortfolios(1L, 0L, true);
		//Assert.assertEquals(ports.size(), 15);
	}

	@Test
	public void testGetPortfolioFromNodeCode() {
		String nodeCode = "karuta.karuta-resources";
		Portfolio p = portfolioDAO.getPortfolioFromNodeCode(nodeCode);
		Assert.assertNotNull(p);
	}

	@Test
	public void testGetPortfolioUuidFromNodeCode() {
		String nodeCode = "karuta.karuta-resources";
		String result = portfolioDAO.getPortfolioUuidFromNodeCode(nodeCode);
		Assert.assertEquals(result, portfolioUuid);
	}

	@Test
	public void testGetPortfolioModelUuid() {
		UUID portfolioModelUuid = portfolioDAO.getPortfolioModelUuid(portfolioUuid);
		Assert.assertEquals(portfolioModelUuid.toString(), this.portfolioModelUuid);
	}

	@Test
	public void testGetPortfolioUserId() {
		Long result = portfolioDAO.getPortfolioUserId(portfolioUuid);
		Assert.assertEquals(result.longValue(), 1L);
	}

	@Test
	public void testGetPortfolioRootNode() {
		Node rootNode = portfolioDAO.getPortfolioRootNode(portfolioUuid);
		Assert.assertNotNull(rootNode);
	}

	@Test
	public void testGetPortfolioFromNode() {
		Node rootNode = portfolioDAO.getPortfolioRootNode(portfolioUuid);
		Assert.assertNotNull(rootNode);
	}

	@Test
	public void testGetPortfolioUuidFromNode() {
		UUID result = portfolioDAO.getPortfolioUuidFromNode(nodeUuid);
		Assert.assertEquals(result.toString(), portfolioUuid);
	}

	@Test
	public void testGetPortfolioShared() {
		List<Map<String, Object>> results = portfolioDAO.getPortfolioShared(1L);
		Assert.assertEquals(results.size(), 15);
	}

	@Test
	public void testIsOwner() {
		Assert.assertTrue(portfolioDAO.isOwner(1L, portfolioUuid));
	}

	@Test
	@Transactional(rollbackFor = Exception.class)
//	@Rollback
	public void testChangePortfolioOwner() {
		Assert.assertTrue(portfolioDAO.changePortfolioOwner(portfolioUuid, 2L));
	}

	// Portfolio add(String rootNodeUuid, String modelId, Long userId, Portfolio
	// porfolio) throws BusinessException;

	@Test
	@Transactional
	@Rollback
	public void testChangePortfolioConfiguration() {
		Portfolio p = portfolioDAO.changePortfolioConfiguration(portfolioUuid, false);
		Assert.assertEquals(p.getActive(), 0);
	}

	@Test
	@Transactional
	@Rollback
	public void testUpdatePortfolioModelId() {
		int result = portfolioDAO.updatePortfolioModelId(portfolioUuid, portfolioModelUuid);
		Assert.assertEquals(result, 0);
	}

	@Test
	@Transactional
	@Rollback
	public void testUpdateTime() throws DoesNotExistException {
		Portfolio p1 = portfolioDAO.findById(UUID.fromString(portfolioUuid));
		Date previousTime = p1.getModifDate();
		portfolioDAO.updateTime(portfolioUuid);
		Portfolio p2 = portfolioDAO.findById(UUID.fromString(portfolioUuid));
		Assert.assertNotEquals(previousTime, p2.getModifDate());
	}

	@Test
	@Rollback
	public void updateTimeByNode() {
		portfolioDAO.updateTimeByNode(nodeUuid);
	}

	@Test
	public void testHasSharedNodes() {
		Assert.assertFalse(portfolioDAO.hasSharedNodes(portfolioUuid));

	}

}
