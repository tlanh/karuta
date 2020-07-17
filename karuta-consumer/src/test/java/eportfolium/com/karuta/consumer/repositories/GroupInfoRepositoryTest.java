package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.Portfolio;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class GroupInfoRepositoryTest extends TestHelpers {
    @Autowired
    private GroupInfoRepository repository;

    @Autowired
    private GroupRightInfoRepository groupRightInfoRepository;

    @Test
    public void getGroupsByRole() {
        String label = "designer";

        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel(label);
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfo.setGroupRightInfo(groupRightInfo);
        repository.save(groupInfo);

        assertEquals(1, repository.getGroupsByRole(portfolio.getId(), label).size());
        assertEquals(0, repository.getGroupsByRole(portfolio.getId(), "").size());
    }

    @Test
    public void getGroupByGrid() {
        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("");
        groupRightInfoRepository.save(groupRightInfo);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfo.setGroupRightInfo(groupRightInfo);
        repository.save(groupInfo);

        assertEquals(groupInfo.getId(), repository.getGroupByGrid(groupRightInfo.getId()).getId());
    }

    @Test
    public void getByPortfolio() {
        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("");
        groupRightInfoRepository.save(groupRightInfo);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfo.setGroupRightInfo(groupRightInfo);
        repository.save(groupInfo);

        assertEquals(1, repository.getByPortfolio(portfolio.getId()).size());
    }
}