package eportfolium.com.karuta.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.model.bean.GroupRightInfo;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestGroupRightInfoDao {

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Test
	public void testGetByPortfolioID() {
		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
		List<GroupRightInfo> result = groupRightInfoDao.getByPortfolioID(portfolioUuid);
		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	public void testGetByPortfolioAndLabel() {
		String portfolioUuid = "ece5deaa-6492-4eb2-97f3-7f7a65f95ed9";
		GroupRightInfo result = groupRightInfoDao.getByPortfolioAndLabel(portfolioUuid, "all");
		assertNotNull(result);
		assertEquals(22l, result.getId().longValue());
	}

	@Test
	public void testGroupRightInfoExists() {
		boolean result = groupRightInfoDao.groupRightInfoExists(3L);
		assertTrue(result);
	}

	@Test
	public void testGetDefaultByPortfolio() {
		String portfolioUuid = "ece5deaa-6492-4eb2-97f3-7f7a65f95ed9";
		List<GroupRightInfo> result = groupRightInfoDao.getDefaultByPortfolio(portfolioUuid);
		assertEquals(1, result.size());
	}

	@Test
	public void testGetIdByNodeAndLabel() {
		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
		Long result = groupRightInfoDao.getIdByNodeAndLabel(nodeUuid, "designer");
		assertEquals(3L, result.longValue());
	}

	@Test
	public void testGetByNodeAndLabel() {
		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
		List<Long> result = groupRightInfoDao.getByNodeAndLabel(nodeUuid, Arrays.asList("designer", "all"));
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(3L, result.get(0).longValue());
	}

	@Test
	public void testIsOwner() {
		boolean result = groupRightInfoDao.isOwner(1L, 3L);
		assertTrue(result);
	}

	@Test
	public void testGetByPortfolioAndUser() {
		String portfolioUuid = "ece5deaa-6492-4eb2-97f3-7f7a65f95ed9";
		List<GroupRightInfo> result = groupRightInfoDao.getByPortfolioAndUser(portfolioUuid, 1L);
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	@Test
	public void testGetByUser() {
		List<GroupRightInfo> result = groupRightInfoDao.getByUser(1L);
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	@Test
	public void testGetByNode() {
		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
		List<GroupRightInfo> result = groupRightInfoDao.getByNode(nodeUuid);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(3L, result.get(0).getId().longValue());
	}

	@Test
	public void testGetRolesToBeNotified() {
		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
		Map<String, Object> result = groupRightInfoDao.getRolesToBeNotified(3L, 1L, nodeUuid);
		assertNull(result);
	}

	@Test
	@Transactional
	public void testAddThenRemove() throws Exception {
		List<GroupRightInfo> griList1 = groupRightInfoDao.findAll();
		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
		Long roleId = groupRightInfoDao.add(portfolioUuid, "toto");
		assertNotNull(roleId);
		List<GroupRightInfo> griList2 = groupRightInfoDao.findAll();
		Assert.assertEquals(griList1.size() + 1, griList2.size());
		groupRightInfoDao.removeById(roleId);
		List<GroupRightInfo> griList3 = groupRightInfoDao.findAll();
		Assert.assertEquals(griList2.size() - 1, griList3.size());
	}

}
