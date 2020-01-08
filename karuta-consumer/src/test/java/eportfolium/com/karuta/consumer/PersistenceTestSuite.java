package eportfolium.com.karuta.consumer;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Suite de tests unitaires, on vérifie le bon fonctionnement de la partie
 * persistance de Karuta. La couche permettant l'accès aux données contenu dans
 * la base.
 * 
 * @author mlengagne
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCredentialDao.class, TestCredentialGroupDao.class, TestCredentialGroupMembersDao.class,
		TestGroupInfoDao.class, TestGroupRightsDao.class, TestGroupUserDao.class, TestLogTableDao.class,
		TestNodeDao.class, TestPortfolioDao.class, TestPortfolioGroupDao.class, TestPortfolioGroupMembersDao.class,
		TestResourceTableDao.class, TestGroupRightInfoDao.class })
public class PersistenceTestSuite extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public PersistenceTestSuite(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(PersistenceTestSuite.class);
	}

	public static void main(String[] args) {
		org.junit.runner.Result result = JUnitCore.runClasses(PersistenceTestSuite.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}
}
