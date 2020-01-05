package eportfolium.com.karuta.consumer;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupMembersDao;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;

/**
 * Home object implementation for domain model class PortfolioGroup.
 * 
 * @see dao.PortfolioGroup
 * @author Hibernate Tools
 */
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestPortfolioGroupMembersDao {

	private String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";

	@Autowired
	private PortfolioGroupMembersDao portfolioGroupMembersDao;

	public TestPortfolioGroupMembersDao() {
	}

	@Test
	public void getByPortfolioGroupID() {
		List<PortfolioGroupMembers> result = portfolioGroupMembersDao.getByPortfolioGroupID(1L);
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void getByPortfolioID() {
		List<PortfolioGroupMembers> result = portfolioGroupMembersDao.getByPortfolioID(portfolioUuid);
		Assert.assertEquals(1, result.size());
	}

}
