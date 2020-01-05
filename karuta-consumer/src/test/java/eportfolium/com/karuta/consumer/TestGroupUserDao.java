package eportfolium.com.karuta.consumer;

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

import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.exception.BusinessException;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestGroupUserDao {

	private String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";

	@Autowired
	private GroupUserDao groupUserDao;

	public TestGroupUserDao() {
	}

	@Test
	public void testGetByUser() {
		List<GroupUser> guList = groupUserDao.getByUser(1L);
		Assert.assertEquals(15, guList.size());
	}

	@Test
	public void isUserInGroup() {
		boolean isUserInGroup = groupUserDao.isUserInGroup(1L, 3L);
		Assert.assertTrue(isUserInGroup);
	}

	@Test
	public void isUserMemberOfGroup() {
		boolean isUserMemberOfGroup = groupUserDao.isUserMemberOfGroup(1L, 3L);
		Assert.assertTrue(isUserMemberOfGroup);
	}

	@Test
	public void getByPortfolio() {
		List<GroupUser> guList = groupUserDao.getByPortfolio(portfolioUuid);
		Assert.assertEquals(1, guList.size());
	}

	@Test
	public void getByPortfolioAndUser() {
		Long userId = 1L;
		List<GroupUser> guList = groupUserDao.getByPortfolioAndUser(portfolioUuid, userId);
		Assert.assertEquals(1, guList.size());
	}

	@Test
	@Transactional
	@Rollback
	public void addUserInGroup() {
		Long userId = 1L;
		Long groupId = 1L;
		Long result = groupUserDao.addUserInGroup(userId, groupId);
		Assert.assertEquals(0, result.longValue());
	}

	@Test
	@Transactional
	@Rollback
	public void deleteByPortfolio() {
		int result = groupUserDao.deleteByPortfolio(portfolioUuid);
		Assert.assertEquals(0, result);
	}

	@Test
	public void getByUserAndRole() {
		GroupUser gu = groupUserDao.getByUserAndRole(1L, 3L);
		Assert.assertEquals("root", gu.getCredential().getLogin());
	}

	@Test
	public void getUniqueByUser() throws Exception {
		GroupUser gu = groupUserDao.getUniqueByUser(1L);
		Assert.assertEquals("root", gu.getCredential().getLogin());
	}

	@Test
	@Transactional
	@Rollback
	public void removeByUserAndRole() throws BusinessException {
		groupUserDao.removeByUserAndRole(1L, 1L);
	}

	@Test
	@Transactional
	@Rollback
	public void removeByPortfolio() throws Exception {
		groupUserDao.removeByPortfolio(portfolioUuid);
	}

	@Test
	@Transactional
	@Rollback
	public void deleteByPortfolio2() throws Exception {
		groupUserDao.deleteByPortfolio2(UUID.fromString(portfolioUuid));
	}

}
