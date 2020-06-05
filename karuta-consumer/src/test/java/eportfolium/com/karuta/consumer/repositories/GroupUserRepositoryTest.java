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
public class GroupUserRepositoryTest extends TestHelpers {
    @Autowired
    private GroupUserRepository repository;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private GroupRightInfoRepository groupRightInfoRepository;

    private GroupUser groupUserRecord() {
        Credential credential = credentialRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("");
        groupRightInfoRepository.save(groupRightInfo);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfo.setGroupRightInfo(groupRightInfo);
        groupInfoRepository.save(groupInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setCredential(credential);
        groupUser.setGroupInfo(groupInfo);
        repository.save(groupUser);

        return groupUser;
    }

    @Test
    public void getByUser() {
        GroupUser groupUser = groupUserRecord();

        List<GroupUser> groups = repository.getByUser(groupUser.getCredential().getId());

        assertEquals(1, groups.size());
        assertEquals(groupUser.getId(), groups.get(0).getId());
    }

    @Test
    public void getByUserAndRole() {
        GroupUser groupUser = groupUserRecord();

        GroupUser found = repository
                .getByUserAndRole(
                        groupUser.getCredential().getId(),
                        groupUser.getGroupInfo().getGroupRightInfo().getId());

        assertEquals(groupUser.getId(), found.getId());
    }

    @Test
    public void getUniqueByUser() {
        String login = "johnd";

        GroupUser groupUser = groupUserRecord();

        Credential credential = groupUser.getCredential();
        credential.setLogin(login);
        credentialRepository.save(credential);

        GroupRightInfo groupRightInfo = groupUser.getGroupInfo().getGroupRightInfo();
        groupRightInfo.setLabel(login);
        groupRightInfoRepository.save(groupRightInfo);

        GroupUser found = repository.getUniqueByUser(groupUser.getCredential().getId());

        assertEquals(credential.getLogin(), groupRightInfo.getLabel());
        assertEquals(groupUser.getId(), found.getId());
    }

    @Test
    public void getByRole() {
        GroupUser groupUser = groupUserRecord();

        List<GroupUser> groups = repository
                .getByRole(groupUser.getGroupInfo().getGroupRightInfo().getId());

        assertEquals(1, groups.size());
        assertEquals(groupUser.getId(), groups.get(0).getId());
    }

    @Test
    public void getByPortfolioAndUser() {
        GroupUser groupUser = groupUserRecord();
        Portfolio portfolio = portfolioRecord();

        portfolioRepository.save(portfolio);

        GroupRightInfo groupRightInfo = groupUser.getGroupInfo().getGroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        List<GroupUser> groups = repository
                .getByPortfolioAndUser(
                        portfolio.getId(),
                        groupUser.getCredential().getId());

        assertEquals(1, groups.size());
        assertEquals(groupUser.getId(), groups.get(0).getId());
    }

    @Test
    public void deleteByPortfolio() {
        GroupUser first = groupUserRecord();
        GroupUser second = groupUserRecord();

        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = second.getGroupInfo().getGroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        assertEquals(2, repository.count());

        repository.deleteByPortfolio(portfolio.getId());

        assertEquals(1, repository.count());
        assertEquals(first.getId(), repository.findAll().iterator().next().getId());

    }

    @Test
    public void hasRole() {
        GroupUser groupUser = groupUserRecord();

        Credential credential = groupUser.getCredential();
        GroupRightInfo groupRightInfo = groupUser.getGroupInfo().getGroupRightInfo();

        assertTrue(repository.hasRole(credential.getId(), groupRightInfo.getId()));
        assertFalse(repository.hasRole(-1L, groupRightInfo.getId()));
    }
}