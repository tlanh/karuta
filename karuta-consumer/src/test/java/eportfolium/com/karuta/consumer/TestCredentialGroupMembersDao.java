package eportfolium.com.karuta.consumer;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupMembersDao;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestCredentialGroupMembersDao {

	@Autowired
	private CredentialGroupMembersDao credentialGroupMembersDao;

	@Test
	public void testGetGroupByUser() {
		List<CredentialGroupMembers> cg = credentialGroupMembersDao.getByUser(1L);
		Assert.assertEquals(1, cg.size());
	}

	@Test
	@Transactional
	@Rollback
	public void testDeleteUsersFromUserGroups() {
		Boolean result = credentialGroupMembersDao.deleteUserFromGroup(1L, 1L);
		Assert.assertTrue(result);
	}

	@Test
	@Rollback
	public void testGetByUserGroup() {
		List<CredentialGroupMembers> result = credentialGroupMembersDao.getByGroup(3L);
		Assert.assertEquals(0, result.size());
	}
}
