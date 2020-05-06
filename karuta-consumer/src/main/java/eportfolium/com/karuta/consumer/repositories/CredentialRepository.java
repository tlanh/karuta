package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Credential;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CredentialRepository extends CrudRepository<Credential, Long> {
    public static final int PASSWORD_LENGTH = 5;

    Credential findByLogin(String login);

    @Query("SELECT c FROM Credential c WHERE c.login = :login AND c.isAdmin = :admin")
    Credential findByLoginAndAdmin(@Param("login") String login, @Param("admin") Integer admin);

    boolean existsByLogin(String login);

    @Query("SELECT c FROM Credential c WHERE c.id = :id AND c.active = 1")
    Credential findActiveById(@Param("id") Long id);

    @Query("FROM c.isAdmin WHERE c.id = :id")
    boolean isAdmin(@Param("id") Long id);

    @Query("SELECT c.isDesigner FROM Credential c WHERE c.id = :id")
    boolean isCreator(@Param("id") Long id);

    @Query("SELECT gu.id.credential.id FROM Node n " +
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
            "AND lower(cs.displayLastname) LIKE %:lastname% " +
            "ORDER BY c.id")
    List<Credential> getUsers(@Param("username") String username,
                              @Param("firstname") String firstname,
                              @Param("lastname") String lastname);

    @Query("SELECT c FROM Credential c, GroupRightInfo gri, GroupInfo gi, GroupUser gu " +
            "WHERE c.id = gu.id.credential.id " +
            "AND gu.id.groupInfo.id = gi.id " +
            "AND gi.groupRightInfo.id = gri.id " +
            "AND gri.portfolio.id = :portfolioUuid " +
            "AND gri.label = :role")
    List<Credential> getUsersByRole(@Param("portfolioId") UUID portfolioId,
                                    @Param("role") String role);

    @Query("SELECT c FROM Credential c " +
            "LEFT JOIN FETCH c.credentialSubstitution cs " +
            "WHERE c.id = :id")
    Credential getUserInfos(@Param("id") Long id);
}
