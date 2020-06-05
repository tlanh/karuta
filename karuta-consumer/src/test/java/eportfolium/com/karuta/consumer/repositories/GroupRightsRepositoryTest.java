package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class GroupRightsRepositoryTest extends TestHelpers {
    @Autowired
    private GroupRightsRepository repository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private GroupRightInfoRepository groupRightInfoRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private static class Tuple {
        public GroupRights groupRights;
        public Credential credential;

        public Tuple(GroupRights groupRights, Credential credential) {
            this.groupRights = groupRights;
            this.credential = credential;
        }
    }

    private Tuple groupRightsRecord() {
        return createGroupRightsRecord(credentialRecord());
    }

    private Tuple groupRightsRecord(String login) {
        return createGroupRightsRecord(credentialRecord(login));
    }

    private Tuple createGroupRightsRecord(Credential credential) {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfoRepository.save(groupInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setCredential(credential);
        groupUser.setGroupInfo(groupInfo);
        groupUserRepository.save(groupUser);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setGroupInfo(groupInfo);
        groupRightInfo.setLabel("");
        groupRightInfoRepository.save(groupRightInfo);

        GroupRights groupRights = new GroupRights();
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightsId(UUID.randomUUID());
        groupRights.setGroupRightInfo(groupRightInfo);
        repository.save(groupRights);
        testEntityManager.flush();

        return new Tuple(groupRights, credential);
    }

    @Test
    public void getRightsByIdAndUser() {
        Tuple tuple = groupRightsRecord();

        GroupRights found = repository.getRightsByIdAndUser(
                tuple.groupRights.getId().getId(),
                tuple.credential.getId());

        assertEquals(tuple.groupRights.getId(), found.getId());
    }

    @Test
    public void getRightsByUserAndGroup() {
        Tuple tuple = groupRightsRecord();

        GroupRights found = repository.getRightsByUserAndGroup(
                tuple.groupRights.getId().getId(),
                tuple.credential.getId(),
                tuple.groupRights.getGroupRightInfo().getGroupInfo().getId());

        assertEquals(tuple.groupRights.getId(), found.getId());
    }

    @Test
    public void getSpecificRightsForUser() {
        Portfolio portfolio = portfolioRecord();
        Node node = savableNode();

        node.setPortfolio(portfolio);
        nodeRepository.save(node);

        String login = "foo";
        Tuple tuple = groupRightsRecord(login);

        GroupRights groupRights = tuple.groupRights;
        Credential credential = tuple.credential;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();

        groupRights.setGroupRightsId(node.getId());
        repository.save(groupRights);

        groupRightInfo.setLabel(login);
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        GroupRights found = repository
                .getSpecificRightsForUser(node.getId(), credential.getId());

        assertEquals(groupRights.getId(), found.getId());
    }

    @Test
    public void getPublicRightsByGroupId() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();
        GroupInfo groupInfo = groupRightInfo.getGroupInfo();
        UUID id = UUID.randomUUID();

        groupRights.setGroupRightsId(id);
        repository.save(groupRights);

        Portfolio portfolio = portfolioRecord();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("all");
        groupRightInfoRepository.save(groupRightInfo);

        GroupRights found = repository.getPublicRightsByGroupId(id, groupInfo.getId());

        assertEquals(groupRights.getId(), found.getId());
    }

    @Test
    public void getPublicRightsByUserId() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        Credential credential = tuple.credential;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();
        UUID id = UUID.randomUUID();

        groupRights.setGroupRightsId(id);
        repository.save(groupRights);

        groupRightInfo.setLabel("all");
        groupRightInfoRepository.save(groupRightInfo);

        GroupRights found = repository.getPublicRightsByUserId(id, credential.getId());

        assertEquals(groupRights.getId(), found.getId());
    }

    @Test
    public void getPortfolioAndUserRights() {
        Tuple tuple = groupRightsRecord();

        Portfolio portfolio = portfolioRecord();

        GroupRights groupRights = tuple.groupRights;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();
        GroupInfo groupInfo = groupRightInfo.getGroupInfo();

        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        String login = "foo";

        Consumer<List<GroupRights>> assertValid = (list) -> {
            assertEquals(1, list.size());
            assertEquals(groupRights.getId(), list.get(0).getId());
        };

        // 1. Without any label (groupRightInfo's id picked)
        List<GroupRights> rights = repository
                .getPortfolioAndUserRights(portfolio.getId(), login, groupRightInfo.getId());

        assertValid.accept(rights);

        // 2. With 'all' as the label
        groupInfo.setLabel("all");
        groupInfoRepository.save(groupInfo);

        rights = repository.getPortfolioAndUserRights(portfolio.getId(), login, 0L);

        assertValid.accept(rights);

        // 3. With the login as the label
        groupInfo.setLabel(login);
        groupInfoRepository.save(groupInfo);

        rights = repository.getPortfolioAndUserRights(portfolio.getId(), login, 0L);

        assertValid.accept(rights);
    }

    @Test
    public void getRightsByPortfolio() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();
        Portfolio portfolio = portfolioRecord();
        UUID id = UUID.randomUUID();

        groupRights.setGroupRightsId(id);
        repository.save(groupRights);

        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        List<GroupRights> rights = repository.getRightsByPortfolio(id, portfolio.getId());

        assertEquals(1, rights.size());
        assertEquals(groupRights.getId(), rights.get(0).getId());
    }

    @Test
    public void getRightsByGrid() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        UUID id = UUID.randomUUID();

        groupRights.setGroupRightsId(id);
        repository.save(groupRights);

        GroupRights found = repository
                .getRightsByGrid(id, groupRights.getGroupRightInfo().getId());

        assertEquals(groupRights.getId(), found.getId());
    }

    @Test
    public void getRightsByIdAndLabel() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();
        UUID id = UUID.randomUUID();

        groupRights.setGroupRightsId(id);
        repository.save(groupRights);

        String label = "foo";
        groupRightInfo.setLabel(label);
        groupRightInfoRepository.save(groupRightInfo);

        GroupRights found = repository.getRightsByIdAndLabel(id, label);

        assertEquals(groupRights.getId(), found.getId());
    }

    @Test
    public void getRightsByGroupId() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        GroupInfo groupInfo = groupRights.getGroupRightInfo().getGroupInfo();

        List<GroupRights> rights = repository.getRightsByGroupId(groupInfo.getId());

        assertEquals(1, rights.size());
        assertEquals(groupRights.getId(), rights.get(0).getId());
    }

    @Test
    public void getRightsById() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        UUID id = UUID.randomUUID();

        groupRights.setGroupRightsId(id);
        repository.save(groupRights);

        List<GroupRights> rights = repository.getRightsById(id);

        assertEquals(1, rights.size());
        assertEquals(groupRights.getId(), rights.get(0).getId());
    }

    @Test
    public void getByPortfolioAndGridList() {
        Tuple tuple = groupRightsRecord();

        GroupRights groupRights = tuple.groupRights;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();

        Portfolio portfolio = portfolioRecord();

        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        List<GroupRights> rights = repository
                .getByPortfolioAndGridList(
                        portfolio.getId(), null, groupRightInfo.getId(), null);

        assertEquals(1, rights.size());
        assertEquals(groupRights.getId(), rights.get(0).getId());
    }

    @Test
    public void findByIdAndLabels() {
        Tuple tuple = groupRightsRecord();

        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setPortfolio(portfolio);
        nodeRepository.save(node);

        GroupRights groupRights = tuple.groupRights;
        GroupRightInfo groupRightInfo = groupRights.getGroupRightInfo();

        groupRights.setGroupRightsId(node.getId());
        repository.save(groupRights);

        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("bar");
        groupRightInfoRepository.save(groupRightInfo);

        List<GroupRights> rights = repository
                .findByIdAndLabels(node.getId(), Arrays.asList("foo", "bar"));

        assertEquals(1, rights.size());
        assertEquals(groupRights.getId(), rights.get(0).getId());
    }
}