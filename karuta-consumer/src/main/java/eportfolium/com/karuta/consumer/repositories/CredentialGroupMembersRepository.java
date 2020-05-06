package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.CredentialGroupMembersId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CredentialGroupMembersRepository extends CrudRepository<CredentialGroupMembers, CredentialGroupMembersId> {
    @Query("SELECT cgm FROM CredentialGroupMembers cgm " +
            "WHERE cgm.id.credentialGroup.id = :cgId")
    List<CredentialGroupMembers> findByGroup(@Param("groupId") Long groupId);

    @Query("SELECT cgm FROM CredentialGroupMembers cgm " +
            "LEFT JOIN FETCH cgm.id.credentialGroup cg " +
            "WHERE cgm.id.credential.id = :userId")
    List<CredentialGroupMembers> findByUser(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CredentialGroupMembers cgm " +
            "WHERE cgm.id.credentialGroup.id = :groupId" +
            "AND cgm.id.credential.id = :userId")
    void deleteUserFromGroup(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
