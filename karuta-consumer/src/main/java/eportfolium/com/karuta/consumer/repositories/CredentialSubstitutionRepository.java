package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.CredentialSubstitutionId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialSubstitutionRepository extends CrudRepository<CredentialSubstitution, CredentialSubstitutionId> {

    @Query("SELECT cs FROM CredentialSubstitution cs " +
            "WHERE cs.id.credential.id = :userId " +
            "AND cs.id.id = :id " +
            "AND cs.id.type = :type")
    CredentialSubstitution getSubstitutionRule(@Param("userId") Long userId,
                                               @Param("id") Long id,
                                               @Param("type") String type);

    @Query("SELECT c.id FROM Credential c, CredentialSubstitution cs " +
            "INNER JOIN cs.id.credential cr " +
            "WHERE c.id = cs.id.id " +
            "AND c.login = :login " +
            "AND cr.id = :userId " +
            "AND cs.id.type = 'USER'")
    Long getSubuidFromUserType(@Param("login") String login, @Param("userId") Long userId);

    @Query("SELECT c.id FROM Credential c, CredentialSubstitution cs, GroupUser gu " +
            "WHERE c.id = gu.id.credential.id " +
            "AND gu.id.groupInfo.id = cs.id.id " +
            "AND c.login = :login " +
            "AND cs.id.credential.id = :userId " +
            "AND cs.id.type = 'GROUP'")
    Long getSubuidFromGroupType(@Param("login") String login, @Param("userId") Long userId);
}
