package eportfolium.com.karuta.business.contract;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.business.ServiceTest;
import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.*;
import eportfolium.com.karuta.model.exception.BusinessException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class PortfolioManagerTest {
    @MockBean
    private PortfolioGroupRepository portfolioGroupRepository;

    @MockBean
    private PortfolioGroupMembersRepository portfolioGroupMembersRepository;

    @MockBean
    private PortfolioRepository portfolioRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private GroupRightInfoRepository groupRightInfoRepository;

    @MockBean
    private GroupUserRepository groupUserRepository;

    @MockBean
    private ResourceRepository resourceRepository;

    @SpyBean
    private PortfolioManager manager;

    @MockBean
    private NodeManager nodeManager;

    @Test
    public void removePortfolioGroups_WithExistingPortfolioGroup() {
        Long groupId = 87L;

        PortfolioGroup portfolioGroup = new PortfolioGroup();

        doReturn(Optional.of(portfolioGroup))
                .when(portfolioGroupRepository)
                .findById(groupId);

        doReturn(Collections.emptyList())
                .when(portfolioGroupMembersRepository)
                .getByPortfolioGroupID(groupId);

        assertTrue(manager.removePortfolioGroups(groupId));

        verify(portfolioGroupRepository).findById(groupId);
        verify(portfolioGroupRepository).delete(portfolioGroup);

        verify(portfolioGroupMembersRepository).getByPortfolioGroupID(groupId);
        verify(portfolioGroupMembersRepository).deleteAll(Collections.emptyList());

        verifyNoMoreInteractions(portfolioGroupRepository, portfolioGroupMembersRepository);
    }

    @Test
    public void removePortfolioGroups_WithMissingPortfolioGroup() {
        Long groupId = 87L;

        doReturn(Optional.empty())
                .when(portfolioGroupRepository)
                .findById(groupId);

        assertFalse(manager.removePortfolioGroups(groupId));

        verify(portfolioGroupRepository).findById(groupId);
        verifyNoMoreInteractions(portfolioGroupRepository);
    }

    @Test
    public void removePortfolioFromPortfolioGroups() {
        UUID portfolioId = UUID.randomUUID();
        Long groupId = 78L;

        PortfolioGroupMembersId pgmId = new PortfolioGroupMembersId();

        pgmId.setPortfolio(new Portfolio(portfolioId));
        pgmId.setPortfolioGroup(new PortfolioGroup(groupId));

        assertTrue(manager.removePortfolioFromPortfolioGroups(portfolioId, groupId));

        verify(portfolioGroupMembersRepository).deleteById(pgmId);
    }

    @Test
    public void getPortfoliosByPortfolioGroup() {
        Long groupId = 84L;
        UUID portfolioId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);

        doReturn(Collections.singletonList(portfolio))
                .when(portfolioRepository)
                .findByPortfolioGroup(groupId);

        PortfolioGroupDocument portfolioGroupDocument = manager.getPortfoliosByPortfolioGroup(groupId);

        assertEquals(groupId, portfolioGroupDocument.getId());
        assertEquals(1, portfolioGroupDocument.getPortfolios().size());

        assertEquals(portfolioId, portfolioGroupDocument.getPortfolios().get(0).getId());
    }

    @Test
    public void getPortfolio() throws JsonProcessingException, BusinessException {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(89L);
        groupRightInfo.setLabel("foo");

        GroupRights groupRights = new GroupRights();
        groupRights.setId(new GroupRightsId());
        groupRights.setRead(true);
        groupRights.setGroupRightInfo(groupRightInfo);

        doReturn(groupRights)
                .when(manager)
                .getRightsOnPortfolio(userId, portfolioId);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);
        portfolio.setModifUserId(userId);

        Node rootNode = new Node();
        rootNode.setId(UUID.randomUUID());
        rootNode.setPortfolio(portfolio);

        portfolio.setRootNode(rootNode);

        doReturn(rootNode)
                .when(portfolioRepository)
                .getPortfolioRootNode(portfolioId);

        doReturn(true)
                .when(portfolioRepository)
                .isOwner(portfolioId, userId);

        Node firstNode = new Node();
        firstNode.setId(UUID.randomUUID());
        firstNode.setAsmType("asmContext");

        Node secondNode = new Node();
        secondNode.setId(UUID.randomUUID());
        secondNode.setAsmType("asmUnitStructure");

        Node thirdNode = new Node();
        thirdNode.setId(UUID.randomUUID());
        thirdNode.setAsmType("asmUnit");

        rootNode.setChildrenStr(firstNode.getId().toString() + "," + secondNode.getId().toString());
        secondNode.setChildrenStr(thirdNode.getId().toString());

        doReturn(Arrays.asList(rootNode, firstNode, secondNode, thirdNode))
                .when(nodeRepository)
                .getNodesWithResources(portfolio.getId());

        PortfolioDocument document = manager.getPortfolio(portfolioId, userId, null);

        assertEquals(portfolio.getId(), document.getId());
        assertEquals(rootNode.getId(), document.getRootNodeId());
        assertTrue(document.getOwner());
        assertEquals(userId, document.getOwnerId());

        NodeDocument rootDocument = document.getRoot();

        assertEquals(2, rootDocument.getChildren().size());

        NodeDocument firstChild = rootDocument.getChildren().get(0);

        assertEquals(firstNode.getId(), firstChild.getId());
        assertEquals(firstNode.getAsmType(), firstChild.getType());

        NodeDocument secondChild = rootDocument.getChildren().get(1);

        assertEquals(secondNode.getId(), secondChild.getId());
        assertEquals(secondNode.getAsmType(), secondChild.getType());
        assertEquals(1, secondChild.getChildren().size());

        NodeDocument thirdChild = secondChild.getChildren().get(0);

        assertEquals(thirdNode.getId(), thirdChild.getId());
        assertEquals(thirdNode.getAsmType(), thirdChild.getType());
    }

    @Test
    public void getPortfolioShared() {
        Long userId = 42L;
        Long gid = 84L;
        UUID portfolioId = UUID.randomUUID();

        Map<String, Object> portfolios = new HashMap<String, Object>() {{
            put("portfolio", portfolioId);
            put("gid", gid);
        }};

        List<Map<String, Object>> result = Collections.singletonList(portfolios);

        doReturn(result)
                .when(portfolioRepository)
                .getPortfolioShared(userId);

        PortfolioList portfolioList = manager.getPortfolioShared(userId);

        assertEquals(1, portfolioList.getCount());
        assertEquals(1, portfolioList.getPortfolios().size());

        assertEquals(portfolioId, portfolioList.getPortfolios().get(0).getId());
        assertEquals(gid, portfolioList.getPortfolios().get(0).getGid());
    }

    @Test
    public void getRightsOnPortfolio_WithMatchingOwnerId() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setModifUserId(userId);

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        GroupRights groupRights = manager.getRightsOnPortfolio(userId, portfolioId);

        assertTrue(groupRights.isRead());
        assertTrue(groupRights.isWrite());
        assertTrue(groupRights.isAdd());
        assertTrue(groupRights.isDelete());
        assertTrue(groupRights.isSubmit());
    }

    @Test
    public void getRightsOnPortfolio_WithOtherUsedId() {
        Long userId  = 42L;

        UUID portfolioId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        Node node = new Node();
        node.setId(nodeId);

        Portfolio portfolio = new Portfolio();
        portfolio.setModifUserId(userId - 12);
        portfolio.setRootNode(node);

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        GroupRights groupRights = new GroupRights();
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightsId(UUID.randomUUID());

        doReturn(groupRights)
                .when(nodeManager)
                .getRights(userId, nodeId);

        GroupRights found = manager.getRightsOnPortfolio(userId, portfolioId);

        assertEquals(groupRights.getId(), found.getId());
    }

    @Test
    public void getRightsOnPortfolio_WithMissingRecord() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        doReturn(Optional.empty())
                .when(portfolioRepository)
                .findById(portfolioId);

        assertEquals(new GroupRights(), manager.getRightsOnPortfolio(userId, portfolioId));
    }

    @Test
    public void hasRights_WithEitherParamAsNull() {
        assertFalse(manager.hasRights(42L, null));
        assertFalse(manager.hasRights(null, UUID.randomUUID()));
    }

    @Test
    public void hasRights_ForOwner() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        doReturn(userId)
                .when(portfolioRepository)
                .getOwner(portfolioId);

        assertTrue(manager.hasRights(userId, portfolioId));
    }

    @Test
    public void hasRights_WhenUserInGroup() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        doReturn(Collections.singletonList(new GroupUser()))
                .when(groupUserRepository)
                .getByPortfolioAndUser(portfolioId, userId);

        assertTrue(manager.hasRights(userId, portfolioId));
    }

    @Test
    public void removePortfolio_WithoutDeleteRight() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        doReturn(new GroupRights())
                .when(manager)
                .getRightsOnPortfolio(userId, portfolioId);

        verify(portfolioRepository, times(0)).deleteById(portfolioId);

        verifyNoInteractions(groupRightInfoRepository,
                resourceRepository,
                nodeRepository,
                portfolioGroupMembersRepository);
    }

    @Test
    public void removePortfolio_WithDeleteRight() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        GroupRights groupRights = new GroupRights();
        groupRights.setDelete(true);

        doReturn(groupRights)
                .when(manager)
                .getRightsOnPortfolio(userId, portfolioId);

        manager.removePortfolio(portfolioId, userId);

        verify(resourceRepository).getResourcesByPortfolioUUID(portfolioId);
        verify(resourceRepository).getContextResourcesByPortfolioUUID(portfolioId);
        verify(resourceRepository).getResourcesOfResourceByPortfolioUUID(portfolioId);
        verify(resourceRepository, times(3)).deleteAll(anyList());
        verifyNoMoreInteractions(resourceRepository);

        verify(groupRightInfoRepository).getByPortfolioID(portfolioId);
        verify(groupRightInfoRepository).deleteAll(anyList());
        verifyNoMoreInteractions(groupRightInfoRepository);

        verify(nodeRepository).getNodes(portfolioId);
        verify(nodeRepository).deleteAll(anyList());
        verifyNoMoreInteractions(nodeRepository);

        verify(portfolioGroupMembersRepository).getByPortfolioID(portfolioId);
        verify(portfolioGroupMembersRepository).deleteAll(anyList());

        verify(portfolioRepository).deleteById(portfolioId);
    }

    @Test
    public void changePortfolioOwner_WithMissingRecord() {
        UUID portfolioId = UUID.randomUUID();

        doReturn(Optional.empty())
                .when(portfolioRepository)
                .findById(portfolioId);

        assertFalse(manager.changePortfolioOwner(portfolioId, 42L));

        verify(portfolioRepository).findById(portfolioId);
        verifyNoMoreInteractions(portfolioRepository);

        verifyNoInteractions(nodeRepository);
    }

    @Test
    public void isOwner() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        manager.isOwner(userId, portfolioId);

        verify(portfolioRepository).isOwner(portfolioId, userId);
    }

    @Test
    public void changePortfolioOwner() {
        Long userId = 42L;
        UUID portfolioId = UUID.randomUUID();

        Node node = new Node();

        Portfolio portfolio = new Portfolio();
        portfolio.setRootNode(node);

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        assertTrue(manager.changePortfolioOwner(portfolioId, userId));

        assertEquals(userId, portfolio.getModifUserId());
        assertEquals(userId, node.getModifUserId());

        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository).save(portfolio);

        verify(nodeRepository).save(node);

        verifyNoMoreInteractions(portfolioRepository, nodeRepository);
    }

    @Test
    public void changePortfolioConfiguration() {
        UUID portfolioId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        manager.changePortfolioConfiguration(portfolioId, true);

        assertEquals(1, portfolio.getActive());

        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository).save(portfolio);

        verifyNoMoreInteractions(portfolioRepository);
    }

    @Test
    public void addPortfolioInGroup_WithNonNullLabel() {
        Long groupId = 74L;
        UUID portfolioId = UUID.randomUUID();
        String label = "foo";

        PortfolioGroup portfolioGroup = new PortfolioGroup();

        doReturn(Optional.of(portfolioGroup))
                .when(portfolioGroupRepository)
                .findById(groupId);

        assertEquals(0, manager.addPortfolioInGroup(portfolioId, groupId, label));

        assertEquals(label, portfolioGroup.getLabel());

        verify(portfolioGroupRepository).findById(groupId);
        verify(portfolioGroupRepository).save(portfolioGroup);
        verifyNoMoreInteractions(portfolioGroupRepository);

        verifyNoInteractions(portfolioRepository, portfolioGroupMembersRepository);
    }

    @Test
    public void addPortfolioInGroup_WithNullLabel_WithoutCorrectType() {
        Long groupId = 74L;
        UUID portfolioId = UUID.randomUUID();

        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setType("foo");

        doReturn(Optional.of(portfolioGroup))
                .when(portfolioGroupRepository)
                .findById(groupId);

        assertEquals(1, manager.addPortfolioInGroup(portfolioId, groupId, null));

        verify(portfolioGroupRepository).findById(groupId);
        verifyNoMoreInteractions(portfolioGroupRepository);

        verifyNoInteractions(portfolioRepository, portfolioGroupMembersRepository);
    }

    @Test
    public void addPortfolioInGroup_WithNullLabel_AndCorrectType() {
        Long groupId = 74L;
        UUID portfolioId = UUID.randomUUID();

        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setType("pOrtFolIo");

        doReturn(Optional.of(portfolioGroup))
                .when(portfolioGroupRepository)
                .findById(groupId);

        Portfolio portfolio = new Portfolio();

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        assertEquals(0, manager.addPortfolioInGroup(portfolioId, groupId, null));

        verify(portfolioGroupRepository).findById(groupId);
        verifyNoMoreInteractions(portfolioGroupRepository);

        verify(portfolioRepository).findById(portfolioId);

        verify(portfolioGroupMembersRepository).save(any(PortfolioGroupMembers.class));

        verifyNoMoreInteractions(portfolioRepository, portfolioGroupMembersRepository);
    }

    @Test
    public void getPortfolioGroupIdFromLabel_WithMissingRecord() {
        String label = "foo";

        doReturn(Optional.empty())
                .when(portfolioGroupRepository)
                .findByLabel(label);

        assertEquals(Long.valueOf(-1), manager.getPortfolioGroupIdFromLabel(label));
    }

    @Test
    public void getPortfolioGroupIdFromLabel_WithExistingRecord() {
        String label = "foo";

        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setId(74L);

        doReturn(Optional.of(portfolioGroup))
                .when(portfolioGroupRepository)
                .findByLabel(label);

        assertEquals(portfolioGroup.getId(), manager.getPortfolioGroupIdFromLabel(label));
    }

    @Test
    public void getPortfolioGroupListFromPortfolio() {
        UUID portfolioId = UUID.randomUUID();

        PortfolioGroup portfolioGroup = new PortfolioGroup();
        portfolioGroup.setId(74L);
        portfolioGroup.setType("Foo");
        portfolioGroup.setLabel("bar");

        PortfolioGroupMembers pgm = new PortfolioGroupMembers();
        pgm.setId(new PortfolioGroupMembersId());
        pgm.setPortfolioGroup(portfolioGroup);

        doReturn(Collections.singletonList(pgm))
            .when(portfolioGroupMembersRepository)
            .getByPortfolioID(portfolioId);

        PortfolioGroupList list = manager.getPortfolioGroupListFromPortfolio(portfolioId);

        assertEquals(1, list.getGroups().size());

        PortfolioGroupDocument document = list.getGroups().get(0);

        assertEquals(portfolioGroup.getId(), document.getId());
        assertEquals(portfolioGroup.getLabel(), document.getLabel());
        assertEquals(portfolioGroup.getType().toLowerCase(), document.getType());
    }

    @Test
    public void addPortfolioGroup_WithMissingParent() {
        Long parentId = 86L;

        doReturn(false)
                .when(portfolioGroupRepository)
                .existsByIdAndType(parentId, "GROUP");

        assertEquals(Long.valueOf(-1), manager.addPortfolioGroup("", "", parentId));

        verify(portfolioGroupRepository).existsByIdAndType(parentId, "GROUP");
        verifyNoMoreInteractions(portfolioGroupRepository);
    }

    @Test
    public void addPortfolioGroup_WithExistingParent() {
        String group = "foo";
        String type = "bar";
        Long parentId = 86L;

        doReturn(true)
                .when(portfolioGroupRepository)
                .existsByIdAndType(parentId, "GROUP");

        manager.addPortfolioGroup(group, type, parentId);

        ArgumentCaptor<PortfolioGroup> captor = ArgumentCaptor.forClass(PortfolioGroup.class);

        verify(portfolioGroupRepository).save(captor.capture());

        assertEquals(group, captor.getValue().getLabel());
        assertEquals(type, captor.getValue().getType());
        assertEquals(parentId, captor.getValue().getParent().getId());

        verify(portfolioGroupRepository).existsByIdAndType(parentId, "GROUP");
        verifyNoMoreInteractions(portfolioGroupRepository);
    }

    @Test
    public void getRoleByPortfolio_WithMissingRecord() {
        String role = "foo";
        UUID portfolioId = UUID.randomUUID();

        doReturn(null)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(portfolioId, role);

        assertEquals("Le grid n'existe pas", manager.getRoleByPortfolio(role, portfolioId));
    }

    @Test
    public void getRoleByPortfolio_WithExistingRecord() {
        String role = "foo";
        UUID portolioId = UUID.randomUUID();
        Long groupId = 78L;

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(groupId);

        doReturn(groupRightInfo)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(portolioId, role);

        assertEquals("grid = " + groupId, manager.getRoleByPortfolio(role, portolioId));
    }

    @Test
    @AsAdmin
    public void getGroupRightsInfos() {
        UUID portfolioId = UUID.randomUUID();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(78L);
        groupRightInfo.setLabel("foo");
        groupRightInfo.setOwner(45L);

        doReturn(Collections.singletonList(groupRightInfo))
                .when(groupRightInfoRepository)
                .getByPortfolioID(portfolioId);

        GroupRightInfoList list = manager.getGroupRightsInfos(portfolioId);

        assertEquals(1, list.getGroups().size());

        GroupRightInfoDocument document = list.getGroups().get(0);

        assertEquals(groupRightInfo.getId(), document.getId());
        assertEquals(groupRightInfo.getLabel(), document.getLabel());
        assertEquals(Long.valueOf(groupRightInfo.getOwner()), document.getOwner());
    }

    @Test
    public void updateTime() {
        UUID portfolioId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();

        doReturn(Optional.of(portfolio))
                .when(portfolioRepository)
                .findById(portfolioId);

        manager.updateTime(portfolioId);

        verify(portfolioRepository).findById(portfolioId);
        verify(portfolioRepository).save(portfolio);
        verifyNoMoreInteractions(portfolioRepository);
    }

    @Test
    public void updateTimeByNode() {
        UUID nodeId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();

        Node node = new Node();
        node.setPortfolio(portfolio);

        doReturn(Optional.of(node))
                .when(nodeRepository)
                .findById(nodeId);

        manager.updateTimeByNode(nodeId);

        verify(nodeRepository).findById(nodeId);
        verifyNoMoreInteractions(nodeRepository);

        verify(portfolioRepository).save(portfolio);
        verifyNoMoreInteractions(portfolioRepository);
    }
}
