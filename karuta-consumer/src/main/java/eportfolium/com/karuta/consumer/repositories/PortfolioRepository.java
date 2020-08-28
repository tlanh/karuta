package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface PortfolioRepository extends CrudRepository<Portfolio, UUID>,
        JpaSpecificationExecutor<Portfolio> {
    @Query("SELECT p FROM PortfolioGroupMembers pgm " +
            "LEFT JOIN pgm.id.portfolio p " +
            "WHERE pgm.id.portfolioGroup.id = :portfolioGroupId")
    List<Portfolio> findByPortfolioGroup(@Param("portfolioGroupId") Long portfolioGroupId);

    @Query("SELECT rn FROM Portfolio p " +
            "LEFT JOIN p.rootNode AS rn " +
            "WHERE p.id = :id")
    Node getPortfolioRootNode(@Param("id") UUID id);

    @Query("SELECT p.modifUserId FROM Portfolio p WHERE p.id = :id")
    Long getOwner(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Portfolio p " +
            "INNER JOIN p.groupRightInfo gri WITH gri.label='all' " +
            "INNER JOIN gri.groupInfo gi " +
            "INNER JOIN gi.groupUser gu " +
            "INNER JOIN gu.id.credential c WITH c.login = 'sys_public' " +
            "WHERE p.id = :id")
    boolean isPublic(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Node n " +
            "INNER JOIN n.portfolio p " +
            "WHERE n.modifUserId = :userId " +
            "AND p.id = :id")
    boolean isOwner(@Param("id") UUID id,
                    @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Node n " +
            "WHERE n.portfolio.id = :id " +
            "AND n.sharedNode = TRUE")
    boolean hasSharedNodes(@Param("id") UUID id);

    @Query("SELECT new map(gi.id AS gid, p.id AS portfolio) " +
            "FROM GroupUser gu " +
            "INNER JOIN gu.id.groupInfo gi " +
            "INNER JOIN gi.groupRightInfo gri " +
            "INNER JOIN gri.portfolio p " +
            "WHERE gu.id.credential.id = :userId")
    List<Map<String, Object>> getPortfolioShared(@Param("userId") Long userId);

    @Query("SELECT p " +
        "FROM GroupUser gu " +
        "INNER JOIN gu.id.groupInfo gi " +
        "INNER JOIN gi.groupRightInfo gri " +
        "INNER JOIN gri.portfolio p " +
        "WHERE gu.id.credential.id = :userId")
    List<Portfolio> getPortfolioSharedWithRights(@Param("userId") Long userId);

    @Query("SELECT p FROM Node n " +
            "INNER JOIN n.portfolio p WITH p.active = 1 " +
            "WHERE n.asmType = 'asmRoot' " +
            "AND n.code = :code")
    Portfolio getPortfolioFromNodeCode(@Param("code") String code);

    @Query("SELECT p.id FROM Node n " +
            "INNER JOIN n.portfolio p " +
            "WHERE n.id = :nodeId")
    UUID getPortfolioUuidFromNode(@Param("nodeId") UUID nodeId);

    @Query("SELECT p.id FROM Node n " +
            "INNER JOIN n.portfolio p WITH p.active = 1 " +
            "WHERE n.asmType = 'asmRoot' " +
            "AND n.code = :code")
    UUID getPortfolioUuidFromNodeCode(@Param("code") String code);
}
