package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembersId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class PortfolioGroupMembersRepositoryTest extends TestHelpers {
    @Autowired
    private PortfolioGroupMembersRepository repository;

    @Autowired
    private PortfolioGroupRepository portfolioGroupRepository;

    private PortfolioGroupMembers portfolioGroupMembersRecord() {
        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setLabel("");
        portfolioGroup.setType("");
        portfolioGroupRepository.save(portfolioGroup);

        Portfolio portfolio = portfolioRecord();

        PortfolioGroupMembers pgm = new PortfolioGroupMembers();
        pgm.setId(new PortfolioGroupMembersId());
        pgm.setPortfolioGroup(portfolioGroup);
        pgm.setPortfolio(portfolio);

        repository.save(pgm);

        return pgm;
    }

    @Test
    public void getByPortfolioID() {
        PortfolioGroupMembers pgm = portfolioGroupMembersRecord();
        Portfolio portfolio = pgm.getPortfolio();

        List<PortfolioGroupMembers> members = repository.getByPortfolioID(portfolio.getId());

        assertEquals(1, members.size());
        assertEquals(pgm.getId(), members.get(0).getId());
    }

    @Test
    public void getByPortfolioGroupID() {
        PortfolioGroupMembers pgm = portfolioGroupMembersRecord();
        PortfolioGroup portfolioGroup = pgm.getPortfolioGroup();

        List<PortfolioGroupMembers> members = repository
                .getByPortfolioGroupID(portfolioGroup.getId());

        assertEquals(1, members.size());
        assertEquals(pgm.getId(), members.get(0).getId());
    }
}