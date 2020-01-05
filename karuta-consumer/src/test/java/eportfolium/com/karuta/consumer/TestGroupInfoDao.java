package eportfolium.com.karuta.consumer;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.model.bean.GroupInfo;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestGroupInfoDao {

	@Autowired
	private GroupInfoDao groupInfoDao;

	@Test
	public void testGetGroupByName() {
		String role = "designer";
		List<GroupInfo> giList = groupInfoDao.getGroups(role, 1L);
		Assert.assertEquals(15, giList.size());
	}

	@Test
	public void testExists() {
		String label = "designer10";
		boolean result = groupInfoDao.exists(label);
		Assert.assertFalse(result);
	}

	@Test
	public void testGetGroupsByRole() {
		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
		String role = "designer";
		List<GroupInfo> giList = groupInfoDao.getGroupsByRole(portfolioUuid, role);
		Assert.assertEquals(1, giList.size());
	}

	@Test
	public void getGroupByGrid() {
		long grid = 3l;
		GroupInfo gi = groupInfoDao.getGroupByGrid(grid);
		Assert.assertEquals(grid, gi.getId().longValue());
	}

	@Test
	public void getByPortfolio() {
		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";
		List<GroupInfo> giList = groupInfoDao.getByPortfolio(portfolioUuid);
		Assert.assertEquals(1, giList.size());
	}

}
