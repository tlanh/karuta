package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class GroupRightInfoRepositoryTest extends TestHelpers {
    @Autowired
    private GroupRightInfoRepository repository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private GroupRightsRepository groupRightsRepository;

    @Test
    public void getByPortfolioAndLabel() {
        Portfolio portfolio = portfolioRecord();

        String label = "designer";

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel(label);
        repository.save(groupRightInfo);

        GroupRightInfo found = repository.getByPortfolioAndLabel(portfolio.getId(), label);
        assertEquals(groupRightInfo.getId(), found.getId());

        GroupRightInfo dummy = repository.getByPortfolioAndLabel(portfolio.getId(), "");
        assertNull(dummy);
    }

    @Test
    public void getByPortfolioID() {
        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("");
        repository.save(groupRightInfo);

        List<GroupRightInfo> groups = repository.getByPortfolioID(portfolio.getId());

        assertEquals(1, groups.size());
        assertEquals(groupRightInfo.getId(), groups.get(0).getId());
    }

    @Test
    public void getIdByNodeAndLabel() {
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setPortfolio(portfolio);
        nodeRepository.save(node);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("designer");
        groupRightInfo.setPortfolio(portfolio);
        repository.save(groupRightInfo);

        Long foundId = repository.getIdByNodeAndLabel(node.getId(), "designer");

        assertEquals(groupRightInfo.getId(), foundId);
    }

    @Test
    public void getDefaultByPortfolio() {
        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("all");
        groupRightInfo.setPortfolio(portfolio);
        repository.save(groupRightInfo);

        GroupRightInfo group = repository.getDefaultByPortfolio(portfolio.getId());

        assertEquals(groupRightInfo.getId(), group.getId());
    }

    @Test
    public void isOwner() {
        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("");
        repository.save(groupRightInfo);

        assertEquals(Long.valueOf(42), portfolio.getModifUserId());
        assertTrue(repository.isOwner(42L, groupRightInfo.getId()));
    }
}