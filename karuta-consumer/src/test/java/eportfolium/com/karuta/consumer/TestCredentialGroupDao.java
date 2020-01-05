package eportfolium.com.karuta.consumer;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupDao;
import eportfolium.com.karuta.model.bean.CredentialGroup;

@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestCredentialGroupDao {

	@Autowired
	private CredentialGroupDao credentialGroupDao;

	@Test
	public void testGetGroupByName() {
		CredentialGroup cg = credentialGroupDao.getByName("test");
		Assert.assertEquals(1L, cg.getId().longValue());
	}

	@Test
	@Transactional
	@Rollback
	public void testRenameCredentialGroup() {
		Boolean result = credentialGroupDao.rename(1L, "thisisatest");
		Assert.assertTrue(result);
	}

	@Test
	@Transactional
	@Rollback
	public void testCreateCredentialGroup() throws Exception {
		String generatedString = RandomStringUtils.randomAlphabetic(10);
		Long cg = credentialGroupDao.add(generatedString);
		Assert.assertNotNull(cg);
	}
}
