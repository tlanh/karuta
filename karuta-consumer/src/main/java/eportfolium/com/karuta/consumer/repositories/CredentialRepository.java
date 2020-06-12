package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Credential;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends CrudRepository<Credential, Long> {
    Credential findByLogin(String login);

    @Query("SELECT c FROM Credential c WHERE c.login = :login AND c.isAdmin = :admin")
    Credential findByLoginAndAdmin(@Param("login") String login, @Param("admin") Integer admin);

    boolean existsByLogin(String login);

    @Query("SELECT c FROM Credential c WHERE c.id = :id AND c.active = 1")
    Optional<Credential> findActiveById(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Credential c WHERE c.id = :id AND c.isAdmin = 1")
    boolean isAdmin(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Credential c WHERE c.id = :id AND c.isDesigner = 1")
    boolean isCreator(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(gu.id.credential) > 0 THEN true ELSE false END " +
            "FROM Node n " +
            "INNER JOIN n.portfolio p " +
            "INNER JOIN p.groupRightInfo gri WITH gri.label = 'designer' " +
            "INNER JOIN gri.groupInfo gi " +
            "INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :id " +
            "WHERE n.id = :nodeId")
    boolean isDesigner(@Param("id") Long id, @Param("nodeId") UUID nodeId);

    @Query("SELECT c.id FROM Credential c WHERE c.login = 'sys_public'")
    Long getPublicId();

    @Query("SELECT c.login FROM Credential c WHERE c.id = :id")
    String getLoginById(@Param("id") Long id);

    @Query("SELECT c.email FROM Credential c WHERE c.login = :login")
    String getEmailByLogin(@Param("login") String login);

    @Query("SELECT c.id FROM Credential c WHERE c.login = :login")
    Long getIdByLogin(@Param("login") String login);

    @Query("SELECT c.id FROM Credential c WHERE c.login = :login AND c.email = :email")
    Long getIdByLoginAndEmail(@Param("login") String login, @Param("email") String email);

    @Query("SELECT c FROM Credential c " +
            "LEFT JOIN FETCH c.credentialSubstitution cs " +
            "WHERE lower(c.login) LIKE %:username% " +
            "AND lower(c.displayFirstname) LIKE %:firstname% " +
            "AND lower(c.displayLastname) LIKE %:lastname% " +
            "ORDER BY c.id")
    List<Credential> getUsers(@Param("username") String username,
                              @Param("firstname") String firstname,
                              @Param("lastname") String lastname);

    @Query("SELECT c FROM Credential c, GroupRightInfo gri, GroupInfo gi, GroupUser gu " +
            "WHERE c.id = gu.id.credential.id " +
            "AND gu.id.groupInfo.id = gi.id " +
            "AND gi.groupRightInfo.id = gri.id " +
            "AND gri.portfolio.id = :portfolioId " +
            "AND gri.label = :role")
    List<Credential> getUsersByRole(@Param("portfolioId") UUID portfolioId,
                                    @Param("role") String role);

    @Query("SELECT c FROM Credential c " +
            "LEFT JOIN FETCH c.credentialSubstitution cs " +
            "WHERE c.id = :id")
    Credential getUserInfos(@Param("id") Long id);
}
