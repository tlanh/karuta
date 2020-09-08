package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class PortfolioRepositoryTest extends TestHelpers {
    @Autowired
    private PortfolioRepository repository;

    @Autowired
    private PortfolioGroupRepository portfolioGroupRepository;

    @Autowired
    private PortfolioGroupMembersRepository portfolioGroupMembersRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private GroupInfoRepository groupInfoRepository;

    @Autowired
    private GroupUserRepository groupUserRepository;

    @Autowired
    private GroupRightInfoRepository groupRightInfoRepository;

    @Test
    public void modifDateIsUpdatedOnSave() {
        Portfolio portfolio = savablePortfolio();

        credentialRepository.save(portfolio.getCredential());

        assertNull(portfolio.getModifDate());

        repository.save(portfolio);

        assertNotNull(portfolio.getModifDate());
    }

    @Test
    public void findByPortfolioGroup() {
        Portfolio portfolio = portfolioRecord();

        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setLabel("");
        portfolioGroup.setType("");
        portfolioGroupRepository.save(portfolioGroup);

        PortfolioGroupMembers pgm = new PortfolioGroupMembers();
        pgm.setId(new PortfolioGroupMembersId());
        pgm.setPortfolio(portfolio);
        pgm.setPortfolioGroup(portfolioGroup);

        portfolioGroupMembersRepository.save(pgm);

        List<Portfolio> portfolios = repository.findByPortfolioGroup(portfolioGroup.getId());

        assertEquals(1, portfolios.size());
        assertEquals(portfolio.getId(), portfolios.get(0).getId());
    }

    @Test
    public void getPortfolioRootNode() {
        Node node = savableNode();
        nodeRepository.save(node);

        Portfolio portfolio = portfolioRecord();
        portfolio.setRootNode(node);
        repository.save(portfolio);

        Node found = repository.getPortfolioRootNode(portfolio.getId());

        assertEquals(node.getId(), found.getId());
    }

    @Test
    public void getOwner() {
        Portfolio portfolio = portfolioRecord();

        assertEquals(Long.valueOf(42), portfolio.getModifUserId());
        assertEquals(Long.valueOf(42), repository.getOwner(portfolio.getId()));
    }

    @Test
    public void isPublic() {
        Portfolio portfolio = portfolioRecord();
        Credential credential = credentialRecord("sys_public");

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfoRepository.save(groupInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(credential);
        groupUserRepository.save(groupUser);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setLabel("all");
        groupRightInfo.setGroupInfo(groupInfo);
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfoRepository.save(groupRightInfo);

        assertTrue(repository.isPublic(portfolio.getId()));
    }

    @Test
    public void isOwner() {
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setPortfolio(portfolio);
        nodeRepository.save(node);

        assertEquals(Long.valueOf(42), node.getModifUserId());
        assertTrue(repository.isOwner(portfolio.getId(), 42L));
    }

    @Test
    public void hasSharedNodes() {
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setPortfolio(portfolio);
        node.setSharedNode(true);
        nodeRepository.save(node);

        assertTrue(node.getSharedNode());
        assertTrue(repository.hasSharedNodes(portfolio.getId()));
    }

    @Test
    public void getPortfolioShared() {
        Credential credential = credentialRecord();
        Portfolio portfolio = portfolioRecord();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setPortfolio(portfolio);
        groupRightInfo.setLabel("");
        groupRightInfoRepository.save(groupRightInfo);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setLabel("");
        groupInfo.setGroupRightInfo(groupRightInfo);
        groupInfoRepository.save(groupInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);
        groupUser.setCredential(credential);
        groupUserRepository.save(groupUser);

        List<Map<String, Object>> shared = repository.getPortfolioShared(credential.getId());

        assertEquals(1, shared.size());

        Map<String, Object> firstRecord = shared.get(0);

        assertEquals(2, firstRecord.entrySet().size());
        assertEquals(portfolio.getId(), firstRecord.get("portfolio"));
        assertEquals(groupInfo.getId(), firstRecord.get("gid"));
    }

    @Test
    public void getPortfolioFromNodeCode() {
        String code = "foo";

        Portfolio portfolio = portfolioRecord();
        portfolio.setActive(1);
        repository.save(portfolio);

        Node node = savableNode();
        node.setPortfolio(portfolio);
        node.setAsmType("asmRoot");
        node.setCode(code);
        nodeRepository.save(node);

        Portfolio found = repository.getPortfolioFromNodeCode(code);

        assertEquals(portfolio.getId(), found.getId());

        portfolio.setActive(0);
        repository.save(portfolio);

        assertNull(repository.getPortfolioFromNodeCode(code));

        portfolio.setActive(1);
        repository.save(portfolio);

        node.setAsmType("random");
        nodeRepository.save(node);

        assertNull(repository.getPortfolioFromNodeCode(code));
    }

    @Test
    public void getPortfolioUuidFromNode() {
        Portfolio portfolio = portfolioRecord();

        Node node = savableNode();
        node.setPortfolio(portfolio);
        nodeRepository.save(node);

        assertEquals(portfolio.getId(), repository.getPortfolioUuidFromNode(node.getId()));
    }

    @Test
    public void getPortfolioUuidFromNodeCode() {
        String code = "foo";

        Portfolio portfolio = portfolioRecord();
        portfolio.setActive(1);
        repository.save(portfolio);

        Node node = savableNode();
        node.setAsmType("asmRoot");
        node.setCode(code);
        node.setPortfolio(portfolio);
        nodeRepository.save(node);

        assertEquals(portfolio.getId(), repository.getPortfolioUuidFromNodeCode(code));
    }
}
