package eportfolium.com.karuta.business;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.exception.AuthenticationException;

/**
 * @author mlengagne
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
public class TestSecurityManager {

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private UserManager userManager;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private GroupInfoDao groupInfoDao;

	@Autowired
	private GroupUserDao groupUserDao;

	private final String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";

	private final String userLogin = "Tarzan-low-cost";
	private final String userFirstName = "Georges";
	private final String userLastName = "De la jungle";

	private final String userEmail = "georgesdelajungle@greenpeace.com";
	private final String userPassword = "tookie";
	private final String newUserPassword = "ursula";

	@Test
	public void test_creating_everything_one_by_one() throws Exception {

		String generatedLogin = RandomStringUtils.randomAlphabetic(10);
		String generatedEmail = RandomStringUtils.randomAlphabetic(10) + "@gmail.com";

		// Create a user with random login and random email.
		Long result = securityManager.addUser(generatedLogin, generatedEmail);
		Assert.assertNotNull(result);

		securityManager.registerUser(userLogin, userPassword);

		// Create 3 users with formatted XML.
		String xmlUsers = "<users> ";
		xmlUsers += " <user> ";
		xmlUsers += " 	<username>FooUser</username>";
		xmlUsers += " 	<firstname>Roger</firstname>";
		xmlUsers += " 	<lastname>Rabbit</lastname>";
		xmlUsers += " 	<designer>0</designer>";
		xmlUsers += " 	<email>roger.rabbit@rabbits.com</email>";
		xmlUsers += " 	<password>carrots</password>";
		xmlUsers += " 	<active>1</active>";
		xmlUsers += " 	<substitute>0</substitute>";
		xmlUsers += " </user>";
		xmlUsers += " <user> ";
		xmlUsers += " 	<username>BarUser</username>";
		xmlUsers += " 	<firstname>Peppa</firstname>";
		xmlUsers += " 	<lastname>Pig</lastname>";
		xmlUsers += " 	<designer>1</designer>";
		xmlUsers += " 	<email>peppa.pig@porks.com</email>";
		xmlUsers += " 	<password>pedroponey</password>";
		xmlUsers += " 	<active>1</active>";
		xmlUsers += " 	<substitute>0</substitute>";
		xmlUsers += " </user>";
		xmlUsers += "	<user> ";
		xmlUsers += " 	<username>BazUser</username>";
		xmlUsers += " 	<firstname>Pedro</firstname>";
		xmlUsers += " 	<lastname>Poney</lastname>";
		xmlUsers += " 	<designer>1</designer>";
		xmlUsers += " 	<email>pedro.poney@horses.com</email>";
		xmlUsers += " 	<password>sugar</password>";
		xmlUsers += " 	<active>1</active>";
		xmlUsers += " 	<substitute>0</substitute>";
		xmlUsers += " </user>";
		xmlUsers += "</users>";
		String addUsers = securityManager.addUsers(xmlUsers, 1L);
		Assert.assertNotNull(addUsers);

		final Long userId = userManager.getUserId(userLogin);

		/******* Check if our test user has expected attributes *************/
		// In this case, user is not admin
		boolean isAdmin = securityManager.isAdmin(userId);
		Assert.assertFalse(isAdmin);

		// In this case, user is not a creator
		boolean isCreator = securityManager.isCreator(1L);
		Assert.assertFalse(isCreator);

		// Changement du mot de passe admin.
		final String rootPasswd = "sarahconnor";
		final boolean isChangePasswordOk = securityManager.changePassword("root", rootPasswd);
		Assert.assertTrue(isChangePasswordOk);

		String anotherXmlUser = "<user><username>" + userLogin
				+ "</username><firstname>Jean</firstname><lastname>Dupont</lastname><designer>1</designer><prevpass>"
				+ rootPasswd + "</prevpass><password>" + userPassword
				+ "</password><email>jean.dupont@test.com</email><active>1</active><substitute>0</substitute></user>";

		String changeUser = securityManager.changeUser(1L, userId, anotherXmlUser);
		Assert.assertEquals(userId.toString(), changeUser);

		anotherXmlUser = "<user><username>" + userLogin + "</username><firstname>" + userFirstName
				+ "</firstname><lastname>" + userLastName
				+ "</lastname><designer>1</designer><admin>1</admin><prevpass>" + userPassword + "</prevpass><password>"
				+ userPassword + "</password><email>" + userEmail
				+ "</email><active>1</active><substitute>0</substitute></user>";

		String changeUserInfo = securityManager.changeUserInfo(userId, userId, anotherXmlUser);
		Assert.assertNotNull(changeUserInfo);

		// Changement du mot de passe de l'utilsateur de test.
		boolean isOk = securityManager.changePassword(userLogin, newUserPassword);
		Assert.assertTrue(isOk);

		// Lorsque l'utilisateur envoie un mot de passe incorrect, il se voit refuser
		// l'accès
		try {
			securityManager.authenticateUser(userLogin, userPassword);
		} catch (AuthenticationException e) {
			// Nous aurions dû nous identifier avec le nouveau mot de passe :)
			System.out.println(e.getMessage());
		}

		// Lorsque l'utilisateur envoie le bon mot de passe, l'utilisateur doit obtenir
		// l'accès
		Credential cr = securityManager.authenticateUser(userLogin, newUserPassword);
		Assert.assertNotNull(cr);

		// lorsqu'un utilisateur change son mot de passe, nous remettons ici à
		// l'utilisateur son mot de passe d'origine !
		securityManager.changeUserPassword(userId, newUserPassword, userPassword);

		// Test when user password matches with record in database
		boolean passMatches = securityManager.checkPassword(userId, userPassword);
		Assert.assertTrue(passMatches);

		// Test if generated password method works
		String randomPassword = securityManager.generatePassword();
		Assert.assertNotNull(randomPassword);

		// Test when add role successful
		String userRole = "Seigneur de la jungle";
		Long gid = securityManager.addRole(portfolioUuid, userRole, userId);
		Assert.assertNotNull(gid);

		// Test when changeRole is ok
		GroupRightInfo gri = groupRightInfoDao.getByPortfolioAndLabel(portfolioUuid, userRole);
		String xmlRole = "<role><label>Grand seigneur de la jungle</label></role>";
		Long grid = securityManager.changeRole(1L, gri.getId(), xmlRole);
		Assert.assertNotNull(grid);

		// Test when add users to a role is ok
		int count = groupUserDao.findAll().size();
		String xmlUser = "<users><user id=\"" + userId + "\"></user></users>";
		securityManager.addUsersToRole(1L, gri.getId(), xmlUser);
		int count2 = groupUserDao.findAll().size();
		Assert.assertEquals(count + 1, count2);

		// Test when add User To Group is ok
		GroupInfo gi = groupInfoDao.getGroupByGrid(gri.getId());
		securityManager.addUserToGroup(1L, userId, gi.getId());

		// test if user has role
		boolean hasRole = securityManager.userHasRole(userId, gri.getId());
		Assert.assertTrue(hasRole);
	}

	@Test
	public void test_removing_with_an_aggregation_relationship() throws Exception {
		String userRole = "Grand seigneur de la jungle";
		GroupRightInfo gri = groupRightInfoDao.getByPortfolioAndLabel(portfolioUuid, userRole);
		final Long userId = userManager.getUserId(userLogin);
		securityManager.removeUsers(userId, userId);
		securityManager.removeRole(1L, gri.getId());
		final Long userId1 = userManager.getUserId("FooUser");
		final Long userId2 = userManager.getUserId("BarUser");
		final Long userId3 = userManager.getUserId("BazUser");
		securityManager.removeUser(1L, userId1);
		securityManager.removeUser(1L, userId2);
		securityManager.removeUser(1L, userId3);

	}

}
