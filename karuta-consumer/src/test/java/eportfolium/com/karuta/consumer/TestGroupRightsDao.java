package eportfolium.com.karuta.consumer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;

/**
 * Home object implementation for domain model class GroupRights.
 * 
 * @see dao.GroupRights
 * @author mlengagne
 *
 */
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestGroupRightsDao {

	@Autowired
	private GroupRightsDao groupRightsDao;

	private String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
	private String nodeUuid = "4a00b5ed-432c-4c67-adef-696b6693d0ae";

	public TestGroupRightsDao() {
	}

	@Test
	public void testGetRightsById() {
		List<GroupRights> grList = groupRightsDao.getRightsById(nodeUuid);
		Assert.assertEquals(0, grList.size());
	}

	@Test
	public void testGetRightsByUserAndGroup() {
		GroupRights gr = groupRightsDao.getRightsByUserAndGroup(nodeUuid, 1L, 3L);
		Assert.assertNull(gr);
	}

	@Test
	public void testGetRightsByIdAndGroup() {
		List<GroupRights> grList = groupRightsDao.getRightsByIdAndGroup(nodeUuid, 3L);
		Assert.assertEquals(0, grList.size());
	}

	@Test
	public void testGetRightsByGroupId() {
		List<GroupRights> grList = groupRightsDao.getRightsByGroupId(3L);
		Assert.assertEquals(0, grList.size());
	}

	@Test
	public void testGetPublicRightsByUserId() {
		GroupRights gr = groupRightsDao.getPublicRightsByUserId(nodeUuid, 3L);
		Assert.assertNull(gr);
	}

	@Test
	public void testGetPublicRightsByGroupId() {
		GroupRights gr = groupRightsDao.getPublicRightsByGroupId(nodeUuid, 1L);
		Assert.assertNull(gr);
	}

	@Test
	public void testGetRightsByIdAndLabel() {
		GroupRights gr = groupRightsDao.getRightsByIdAndLabel(nodeUuid, "root");
		Assert.assertNull(gr);
	}

	@Test
	public void testGetRightsByIdAndUser() {
		GroupRights gr = groupRightsDao.getRightsByIdAndUser(nodeUuid, 1L);
		Assert.assertNull(gr);
	}

	@Test
	public void testGetRightsByGrid() {
		GroupRights gr = groupRightsDao.getRightsByGrid(nodeUuid, 1L);
		Assert.assertNull(gr);
	}

	@Test
	public void testGetRightsByPortfolio() {
		List<GroupRights> grList = groupRightsDao.getRightsByPortfolio(nodeUuid, portfolioUuid);
		Assert.assertEquals(0, grList.size());
	}

	@Test
	public void testGetSpecificRightsForUser() {
		GroupRights gr = groupRightsDao.getSpecificRightsForUser(nodeUuid, 1L);
		Assert.assertNull(gr);
	}

	@Test
	@Transactional
	@Rollback
	public void testGetUserIdFromNode() {
		boolean result = groupRightsDao.updateNodesRights(Arrays.asList(new Node(UUID.fromString(nodeUuid))), 3L);
		Assert.assertEquals(true, result);
	}

	@Test
	public void testGetByPortfolioAndGridList() {
		List<GroupRights> result = groupRightsDao.getByPortfolioAndGridList(portfolioUuid, 3L, 4L, 5L);
		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testGetPortfolioAndUserRights() {
		List<GroupRights> result = groupRightsDao.getPortfolioAndUserRights(UUID.fromString(portfolioUuid), "root", 3L);
		Assert.assertEquals(0, result.size());
	}

	@Test
	@Transactional
	@Rollback
	public void testUpdateNodeRights() {
		boolean result = groupRightsDao.updateNodesRights(Arrays.asList(new Node(UUID.fromString(nodeUuid))), 3L);
		Assert.assertEquals(true, result);
	}

	@Test
	@Transactional
	@Rollback
	public void testUpdateAllNodesRights() {
		boolean result = groupRightsDao.updateAllNodesRights(Arrays.asList(new Node(UUID.fromString(nodeUuid))), 3L);
		Assert.assertEquals(true, result);
	}

	@Test
	@Transactional
	@Rollback
	public void testRemoveById() throws Exception {
		groupRightsDao.removeById(UUID.fromString(nodeUuid));
	}

}
