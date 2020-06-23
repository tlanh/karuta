package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.business.ServiceTest;
import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.document.CredentialGroupList;
import eportfolium.com.karuta.document.GroupInfoList;
import eportfolium.com.karuta.document.GroupRightsList;
import eportfolium.com.karuta.document.RoleGroupList;
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
public class GroupManagerTest {
    @MockBean
    private CredentialGroupMembersRepository credentialGroupMembersRepository;

    @MockBean
    private GroupRightsRepository groupRightsRepository;

    @MockBean
    private GroupRightInfoRepository groupRightInfoRepository;

    @MockBean
    private CredentialRepository credentialRepository;

    @MockBean
    private GroupInfoRepository groupInfoRepository;

    @MockBean
    private GroupUserRepository groupUserRepository;

    @MockBean
    private CredentialGroupRepository credentialGroupRepository;

    @SpyBean
    private GroupManager manager;

    @Test
    public void getCredentialGroupByUser() {
        Long userId = 42L;
        Long groupId = 23L;

        String label = "designer";

        CredentialGroup credentialGroup = new CredentialGroup();

        credentialGroup.setId(groupId);
        credentialGroup.setLabel(label);

        CredentialGroupMembers credentialGroupMembers = new CredentialGroupMembers();
        credentialGroupMembers.setId(new CredentialGroupMembersId());
        credentialGroupMembers.setCredentialGroup(credentialGroup);

        doReturn(Collections.singletonList(credentialGroupMembers))
                .when(credentialGroupMembersRepository)
                .findByUser(userId);

        CredentialGroupList credentialGroupList = manager.getCredentialGroupByUser(userId);

        assertEquals(1, credentialGroupList.getGroups().size());

        assertEquals(groupId, credentialGroupList.getGroups().get(0).getId());
        assertEquals(label, credentialGroupList.getGroups().get(0).getLabel());
    }

    @Test
    @AsAdmin
    public void changeNotifyRoles() {
        UUID nodeId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();

        String notifyRoles = "foo";

        GroupRights groupRights1 = new GroupRights();
        GroupRights groupRights2 = new GroupRights();

        List<GroupRights> groups = Arrays.asList(groupRights1, groupRights2);

        doReturn(groups)
                .when(groupRightsRepository)
                .getRightsByPortfolio(nodeId, portfolioId);

        manager.changeNotifyRoles(portfolioId, nodeId, notifyRoles);

        assertEquals(notifyRoles, groupRights1.getNotifyRoles());
        assertEquals(notifyRoles, groupRights2.getNotifyRoles());

        verify(groupRightsRepository).getRightsByPortfolio(nodeId, portfolioId);
        verify(groupRightsRepository).saveAll(groups);
        verifyNoMoreInteractions(groupRightsRepository);
    }

    @Test
    public void setPublicState_WithUnexistingGroup_AsPublic() throws BusinessException {
        Long userId = 42L;
        Long publicId = 49L;
        Long groupInfoId = 99L;

        UUID portfolioId = UUID.randomUUID();

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(userId);

        doReturn(publicId)
                .when(credentialRepository)
                .getPublicId();

        doReturn(null)
                .when(groupRightInfoRepository)
                .getDefaultByPortfolio(portfolioId);

        ArgumentCaptor<GroupRightInfo> griCaptor = ArgumentCaptor.forClass(GroupRightInfo.class);
        ArgumentCaptor<GroupInfo> giCaptor = ArgumentCaptor.forClass(GroupInfo.class);
        ArgumentCaptor<GroupUser> guCaptor = ArgumentCaptor.forClass(GroupUser.class);

        doReturn(null)
                .when(groupRightInfoRepository)
                .save(griCaptor.capture());

        // This one is a bit hacky but we need to make sure that the newly
        // created element's id is properly used for the relationship with
        // the GroupUser instance.
        doAnswer(invocation -> {
            ((GroupInfo)invocation.getArgument(0)).setId(groupInfoId);

            return null;
        })
                .when(groupInfoRepository)
                .save(giCaptor.capture());

        doReturn(null)
                .when(groupUserRepository)
                .save(guCaptor.capture());

        manager.setPublicState(userId, portfolioId, true);

        assertEquals("all", griCaptor.getValue().getLabel());
        assertEquals("all", giCaptor.getValue().getLabel());

        assertEquals(groupInfoId, giCaptor.getValue().getId());
        assertEquals(groupInfoId, guCaptor.getValue().getGroupInfo().getId());

        assertEquals(publicId, guCaptor.getValue().getCredential().getId());

        verify(groupRightInfoRepository).getDefaultByPortfolio(portfolioId);
        verify(groupRightInfoRepository).save(any(GroupRightInfo.class));

        verify(groupInfoRepository).save(any(GroupInfo.class));

        verify(groupUserRepository).save(any(GroupUser.class));
        verify(groupUserRepository).existsById(any(GroupUserId.class));

        verifyNoMoreInteractions(
                groupUserRepository,
                groupInfoRepository,
                groupRightInfoRepository);
    }

    @Test
    public void setPublicState_WithExistingGroup_AsPublic() throws BusinessException {
        Long userId = 42L;
        Long publicId = 49L;

        UUID portfolioId = UUID.randomUUID();

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(89L);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setGroupInfo(groupInfo);

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(userId);

        doReturn(publicId)
                .when(credentialRepository)
                .getPublicId();

        doReturn(groupRightInfo)
                .when(groupRightInfoRepository)
                .getDefaultByPortfolio(portfolioId);

        ArgumentCaptor<GroupUser> captor = ArgumentCaptor.forClass(GroupUser.class);

        doReturn(null)
                .when(groupUserRepository)
                .save(captor.capture());

        manager.setPublicState(userId, portfolioId, true);

        assertEquals(groupInfo.getId(), captor.getValue().getGroupInfo().getId());
        assertEquals(publicId, captor.getValue().getCredential().getId());

        verify(groupRightInfoRepository).getDefaultByPortfolio(portfolioId);
        verifyNoMoreInteractions(groupRightInfoRepository);

        verify(groupUserRepository).save(captor.getValue());
        verify(groupUserRepository).existsById(any(GroupUserId.class));
        verifyNoMoreInteractions(groupUserRepository);

        verifyNoInteractions(
                groupInfoRepository,
                groupRightsRepository);
    }

    @Test
    public void setPublicState_WithExistingGroup_AsPrivate() throws BusinessException {
        Long userId = 42L;
        Long publicId = 49L;

        UUID portfolioId = UUID.randomUUID();

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(89L);

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setGroupInfo(groupInfo);

        doReturn(true)
                .when(credentialRepository)
                .isAdmin(userId);

        doReturn(publicId)
                .when(credentialRepository)
                .getPublicId();

        doReturn(groupRightInfo)
                .when(groupRightInfoRepository)
                .getDefaultByPortfolio(portfolioId);

        ArgumentCaptor<GroupUserId> captor = ArgumentCaptor.forClass(GroupUserId.class);

        doNothing()
                .when(groupUserRepository)
                .deleteById(captor.capture());

        manager.setPublicState(userId, portfolioId, false);

        assertEquals(groupInfo.getId(), captor.getValue().getGroupInfo().getId());
        assertEquals(publicId, captor.getValue().getCredential().getId());

        verify(groupRightInfoRepository).getDefaultByPortfolio(portfolioId);
        verifyNoMoreInteractions(groupRightInfoRepository);

        verify(groupUserRepository).deleteById(captor.getValue());
        verifyNoMoreInteractions(groupUserRepository);

        verifyNoInteractions(
                groupInfoRepository,
                groupRightsRepository);
    }

    @Test
    public void getGroupsByRole() {
        UUID portfolioId = UUID.randomUUID();
        String role = "";

        GroupInfo group1 = new GroupInfo();
        GroupInfo group2 = new GroupInfo();

        group1.setId(56L);
        group2.setId(59L);

        doReturn(Arrays.asList(group1, group2))
                .when(groupInfoRepository)
                .getGroupsByRole(portfolioId, role);

        RoleGroupList roleGroupList = manager.getGroupsByRole(portfolioId, role);

        assertEquals(2, roleGroupList.getGroups().size());
        assertEquals(Arrays.asList(group1.getId(), group2.getId()), roleGroupList.getGroups());
    }

    @Test
    public void getUserGroups() {
        Long userId = 42L;

        GroupUser groupUser1 = new GroupUser(new GroupUserId());
        GroupUser groupUser2 = new GroupUser(new GroupUserId());

        GroupInfo groupInfo1 = new GroupInfo();
        GroupInfo groupInfo2 = new GroupInfo();

        groupUser1.setGroupInfo(groupInfo1);
        groupUser2.setGroupInfo(groupInfo2);

        groupInfo1.setGroupRightInfo(new GroupRightInfo());
        groupInfo2.setGroupRightInfo(new GroupRightInfo());

        groupInfo1.setLabel("foo");
        groupInfo2.setLabel("bar");

        doReturn(Arrays.asList(groupUser1, groupUser2))
                .when(groupUserRepository)
                .getByUser(userId);

        GroupInfoList groupInfoList = manager.getUserGroups(userId);

        assertEquals(2, groupInfoList.getGroups().size());

        assertEquals(groupInfo1.getLabel(), groupInfoList.getGroups().get(0).getLabel());
        assertEquals(groupInfo2.getLabel(), groupInfoList.getGroups().get(1).getLabel());
    }

    @Test
    @AsAdmin
    public void changeUserGroup_WithMissingRecord() {
        Long groupId = 78L;
        Long grid = 86L;

        doReturn(Optional.empty())
                .when(groupInfoRepository)
                .findById(groupId);

        manager.changeUserGroup(grid, groupId);

        verify(groupInfoRepository).findById(groupId);
        verifyNoMoreInteractions(groupInfoRepository);
    }

    @Test
    @AsAdmin
    public void changeUserGroup_WithExistingRecord() {
        Long groupId = 78L;
        Long grid = 86L;

        GroupInfo groupInfo = new GroupInfo();

        doReturn(Optional.of(groupInfo))
                .when(groupInfoRepository)
                .findById(groupId);

        manager.changeUserGroup(grid, groupId);

        assertEquals(new GroupRightInfo(grid), groupInfo.getGroupRightInfo());

        verify(groupInfoRepository).findById(groupId);
        verify(groupInfoRepository).save(groupInfo);
        verifyNoMoreInteractions(groupInfoRepository);
    }

    @Test
    public void addGroupRights_WithoutRightOrLabel() {
        UUID nodeId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();

        Long userId = 42L;

        manager.addGroupRights("   ", nodeId, "foo", portfolioId, userId);
        manager.addGroupRights("foo", nodeId, null, portfolioId, userId);

        verifyNoInteractions(groupUserRepository,
                groupRightInfoRepository,
                groupRightsRepository);
    }

    @Test
    public void addGroupRights_WithUserLabel() {
        String label = "user";
        Long userId = 42L;

        UUID nodeId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(76L);

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupRightInfo(groupRightInfo);

        GroupUser groupUser = new GroupUser();
        groupUser.setId(new GroupUserId());
        groupUser.setGroupInfo(groupInfo);

        doReturn(Collections.singletonList(groupUser))
                .when(groupUserRepository)
                .getByPortfolioAndUser(portfolioId, userId);

        GroupRights groupRights = new GroupRights();

        doReturn(groupRights)
                .when(groupRightsRepository)
                .getRightsByGrid(nodeId, groupRightInfo.getId());

        manager.addGroupRights(label, nodeId, GroupRights.READ, portfolioId, userId);

        assertTrue(groupRights.isRead());

        verify(groupUserRepository).getByPortfolioAndUser(portfolioId, userId);
        verifyNoMoreInteractions(groupUserRepository);

        verify(groupRightsRepository).getRightsByGrid(nodeId, groupRightInfo.getId());
        verify(groupRightsRepository).save(groupRights);
        verifyNoMoreInteractions(groupRightsRepository);
    }

    @Test
    public void addGroupRights_WithPortfolioId() {
        String label = "foo";
        Long userId = 42L;

        UUID nodeId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(76L);

        doReturn(groupRightInfo)
                .when(groupRightInfoRepository)
                .getByPortfolioAndLabel(portfolioId, label);

        GroupRights groupRights = new GroupRights();

        doReturn(groupRights)
                .when(groupRightsRepository)
                .getRightsByGrid(nodeId, groupRightInfo.getId());

        manager.addGroupRights(label, nodeId, GroupRights.READ, portfolioId, userId);

        assertTrue(groupRights.isRead());

        verify(groupRightInfoRepository).getByPortfolioAndLabel(portfolioId, label);
        verifyNoMoreInteractions(groupRightInfoRepository);

        verify(groupRightsRepository).getRightsByGrid(nodeId, groupRightInfo.getId());
        verify(groupRightsRepository).save(groupRights);
        verifyNoMoreInteractions(groupRightsRepository);
    }

    @Test
    public void addGroupRights_WithNodeId() {
        String label = "foo";
        Long userId = 42L;

        UUID nodeId = UUID.randomUUID();

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setId(78L);

        GroupRights groupRights = new GroupRights();
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);

        doReturn(groupRights)
                .when(groupRightsRepository)
                .getRightsByIdAndLabel(nodeId, label);

        manager.addGroupRights(label, nodeId, GroupRights.READ, null, userId);

        assertTrue(groupRights.isRead());

        verify(groupRightsRepository).getRightsByIdAndLabel(nodeId, label);
        verify(groupRightsRepository).save(groupRights);
        verifyNoMoreInteractions(groupRightsRepository);
    }

    @Test
    @AsAdmin
    public void getGroupRights() {
        Long groupId = 74L;

        GroupRightInfo groupRightInfo = new GroupRightInfo();
        groupRightInfo.setGroupInfo(new GroupInfo());

        GroupRights groupRights = new GroupRights();
        groupRights.setId(new GroupRightsId());
        groupRights.setGroupRightInfo(groupRightInfo);

        doReturn(Collections.singletonList(groupRights))
                .when(groupRightsRepository)
                .getRightsByGroupId(groupId);

        GroupRightsList groupRightsList = manager.getGroupRights(groupId);

        assertEquals(1, groupRightsList.getRights().size());
    }

    @Test
    @AsAdmin
    public void removeRights() {
        long groupId = 78L;

        manager.removeRights(groupId);

        verify(groupInfoRepository).deleteById(groupId);
        verifyNoMoreInteractions(groupInfoRepository);
    }

    @Test
    public void getCredentialGroupList() {
        String label = "foo";

        CredentialGroup credentialGroup = new CredentialGroup();
        credentialGroup.setLabel(label);

        doReturn(Collections.singletonList(credentialGroup))
                .when(credentialGroupRepository)
                .findAll();

        CredentialGroupList credentialGroupList = manager.getCredentialGroupList();

        assertEquals(1, credentialGroupList.getGroups().size());
        assertEquals(label, credentialGroupList.getGroups().get(0).getLabel());
    }

    @Test
    public void addCredentialGroup() {
        String label = "designer";

        ArgumentCaptor<CredentialGroup> captor = ArgumentCaptor.forClass(CredentialGroup.class);

        doReturn(null)
                .when(credentialGroupRepository)
                .save(captor.capture());

        manager.addCredentialGroup(label);

        assertEquals(label, captor.getValue().getLabel());
    }

    @Test
    public void renameCredentialGroup_WithMissingRecord() {
        Long groupId = 74L;

        doReturn(Optional.empty())
                .when(credentialGroupRepository)
                .findById(groupId);

        assertFalse(manager.renameCredentialGroup(groupId, "foo"));

        verify(credentialGroupRepository).findById(groupId);
        verifyNoMoreInteractions(credentialGroupRepository);
    }

    @Test
    public void renameCredentialGroup_WithExistingRecord() {
        Long groupId = 74L;
        String label = "foo";

        CredentialGroup credentialGroup = new CredentialGroup();

        doReturn(Optional.of(credentialGroup))
                .when(credentialGroupRepository)
                .findById(groupId);

        assertTrue(manager.renameCredentialGroup(groupId, label));

        verify(credentialGroupRepository).findById(groupId);
        verify(credentialGroupRepository).save(credentialGroup);
        verifyNoMoreInteractions(credentialGroupRepository);
    }

    @Test
    public void getCredentialGroupByName() {
        String label = "foo";

        manager.getCredentialGroupByName(label);

        verify(credentialGroupRepository).findByLabel(label);
        verifyNoMoreInteractions(credentialGroupRepository);
    }

    @Test
    public void removeCredentialGroup() {
        Long groupId = 74L;

        doReturn(Collections.emptyList())
                .when(credentialGroupMembersRepository)
                .findByGroup(groupId);

        manager.removeCredentialGroup(groupId);

        verify(credentialGroupMembersRepository).findByGroup(groupId);
        verify(credentialGroupMembersRepository).deleteAll(Collections.emptyList());

        verify(credentialGroupRepository).deleteById(groupId);

        verifyNoMoreInteractions(credentialGroupMembersRepository, credentialGroupRepository);
    }

    @Test
    public void getByPortfolioAndLabel() {
        UUID portfolioId = UUID.randomUUID();
        String label = "foo";

        manager.getByPortfolioAndLabel(portfolioId, label);

        verify(groupRightInfoRepository).getByPortfolioAndLabel(portfolioId, label);
    }
}