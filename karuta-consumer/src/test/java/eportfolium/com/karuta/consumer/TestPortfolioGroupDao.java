package eportfolium.com.karuta.consumer;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupDao;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;

/**
 * Home object implementation for domain model class PortfolioGroup.
 * 
 * @see dao.PortfolioGroup
 * @author Hibernate Tools
 */
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestPortfolioGroupDao {

	@Autowired
	private PortfolioGroupDao portfolioGroupDao;

	private String groupLabel = "test";

	public TestPortfolioGroupDao() {
	}

	@Test
	public void testGetPortfolioGroupFromLabel() {
		PortfolioGroup result = portfolioGroupDao.getPortfolioGroupFromLabel(groupLabel);
		Assert.assertNotNull(result);
		Assert.assertEquals(groupLabel, result.getLabel());
	}

	@Test
	public void testGetPortfolioGroupIdFromLabel() {
		Long pg = portfolioGroupDao.getPortfolioGroupIdFromLabel(groupLabel);
		Assert.assertEquals(1L, pg.longValue());
	}

	@Test
	public void testExists() {
		boolean exists = portfolioGroupDao.exists(1L, "PORTFOLIO");
		Assert.assertTrue(exists);
	}

	@Test
	public void testGetPortfolioByPortfolioGroup() {
		List<Portfolio> pgList = portfolioGroupDao.getPortfoliosByPortfolioGroup(1L);
		Assert.assertEquals(1, pgList.size());
	}

}
