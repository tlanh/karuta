package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.consumer.RepositoryTest;
import eportfolium.com.karuta.consumer.TestHelpers;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.CredentialGroupMembersId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@RepositoryTest
public class CredentialGroupMembersRepositoryTest extends TestHelpers {
    @Autowired
    private CredentialGroupMembersRepository repository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private CredentialGroupRepository credentialGroupRepository;

    private CredentialGroupMembers createRecord() {
        Credential credential = credentialRecord();

        CredentialGroup credentialGroup = new CredentialGroup();
        credentialGroup.setLabel("");
        credentialGroupRepository.save(credentialGroup);

        CredentialGroupMembers cgm = new CredentialGroupMembers();

        cgm.setId(new CredentialGroupMembersId());
        cgm.setCredential(credential);
        cgm.setCredentialGroup(credentialGroup);
        repository.save(cgm);

        return cgm;
    }

    @Test
    public void findByGroup() {
        CredentialGroupMembers cgm = createRecord();

        List<CredentialGroupMembers> members = repository
                .findByGroup(cgm.getCredentialGroup().getId());

        assertEquals(1, members.size());
        assertEquals(cgm.getId(), members.get(0).getId());
    }

    @Test
    public void findByUser() {
        CredentialGroupMembers cgm = createRecord();

        List<CredentialGroupMembers> members = repository
                .findByUser(cgm.getCredential().getId());

        assertEquals(1, members.size());
        assertEquals(cgm.getId(), members.get(0).getId());
    }

    @Test
    public void deleteUserFromGroup() {
        CredentialGroupMembers cgm = createRecord();

        assertEquals(1, repository.count());

        Long groupId = cgm.getCredentialGroup().getId();
        Long userId = cgm.getCredential().getId();

        repository.deleteUserFromGroup(groupId, userId);

        assertEquals(0, repository.count());
    }
}