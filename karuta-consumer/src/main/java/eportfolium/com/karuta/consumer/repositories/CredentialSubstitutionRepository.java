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
            "AND cs.id.type = :type")
    CredentialSubstitution getFor(@Param("userId") Long userId,
                                  @Param("type") String type);
}
