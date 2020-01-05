package eportfolium.com.karuta.consumer;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class TestCredentialDao {

	private EasyRandom easyRandom;

	public TestCredentialDao() {
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
	private CredentialDao credentialDAO;

	@Test
	@Rollback
	public void testAddUser() {
		Credential credential = easyRandom.nextObject(Credential.class);
		credential.setCredentialSubstitution(null);
		credential.setGroups(null);
		credential.setPortfolios(null);
		credential.setIsAdmin(1);
		credential.setIsDesigner(1);
		credential.setActive(1);
		credential.setEmail("testGetUser@test.com");
		credential.setLogin("testGetUser");
		credentialDAO.persist(credential);
		// Then
		List<Credential> users = credentialDAO.findAll();
		Assert.assertEquals(credential.getDisplayFirstname(), users.get(users.size() - 1).getDisplayFirstname());
	}

	@Test
	@Rollback
	public void testGetUserByEmail() {
		final String email = "test@test.com";
		Credential credential = credentialDAO.getByEmail(email);
		Assert.assertNotNull(credential);
	}

	@Test
	@Rollback
	public void testUserExists() {
		Assert.assertTrue(credentialDAO.userExists("root"));
	}

	@Test
	@Rollback
	public void testUserByLogin() {
		Credential user = credentialDAO.getUserByLogin("root");
		Assert.assertNotNull(user);
	}

	@Test
	@Rollback
	public void testByLogin() {
		Credential result = credentialDAO.getByLogin("root");
		Assert.assertNotNull(result);
	}


	@Test
	@Rollback
	public void testgetUser() {
		Credential result = credentialDAO.getUser(1L);
		Assert.assertNotNull(result);

	}

//	@Test
//	public void testGetUsers() {
//		List<Credential> result = credentialDAO.getUsers(null, "root", null, null);
//	}

	@Test
	@Rollback
	public void testActiveByUserId() {
		Credential result = credentialDAO.getActiveByUserId(1L);
		Assert.assertNotNull(result);
	}

	@Test
	@Rollback
	public void testGetUserUid() {
		String result = credentialDAO.getUserUid("root");
		Assert.assertNotNull(result);
	}

	@Test
	@Rollback
	public void testGetUserId() {
		Long result = credentialDAO.getUserId("root");
		Assert.assertNotNull(result);
		result = credentialDAO.getUserId("root", "");
		Assert.assertNotNull(result);
	}

	@Test
	@Rollback
	public void testGetPublicUiD() {
		Long result = credentialDAO.getPublicUid();
		Assert.assertNotNull(result);
	}

	@Test
	@Rollback
	public void testGetUsersByRole() {
		String portfolioUuid = "8fb397d9-24e3-44f2-8e33-63bb3334906d";
		credentialDAO.getUsersByRole(1L, portfolioUuid, "all");
	}

	@Test
	@Rollback
	public void testupdateCredentialToken() {
		int result = credentialDAO.updateCredentialToken(1L, "toto");
		Assert.assertEquals(0, result);
	}

	@Test
	@Rollback
	public void testIsAdmin() {
		boolean result = credentialDAO.isAdmin(1L);
		Assert.assertEquals(true, result);
		result = credentialDAO.isAdmin(1L);
		Assert.assertEquals(true, result);
	}

	@Test
	@Rollback
	public void testIsDesigner() {
		String nodeId = "a429b0c1-c28d-495d-948f-b158c1ec8c77";
		boolean result = credentialDAO.isDesigner(1L, nodeId);
		Assert.assertEquals(false, result);
	}

	@Test
	@Rollback
	public void testIsCreator() {
		boolean result = credentialDAO.isCreator(1L);
		Assert.assertEquals(false, result);

	}

	@Test
	@Rollback
	public void testIsUserMemberOfRole() {
		boolean result = credentialDAO.userHasRole(1L, 3L);
		Assert.assertEquals(true, result);

	}

	@Test
	@Rollback
	public void testEmailByLogin() {
		String result = credentialDAO.getEmailByLogin("root");
		Assert.assertEquals("test@test.com", result);
	}

	@Test
	@Rollback
	public void testLoginById() {
		String result = credentialDAO.getLoginById(1L);
		Assert.assertEquals("root", result);

	}

	@Test
	@Rollback
	public void testUserInfos() {
		Credential result = credentialDAO.getUserInfos(1L);
		Assert.assertNotNull(result);
	}
}
