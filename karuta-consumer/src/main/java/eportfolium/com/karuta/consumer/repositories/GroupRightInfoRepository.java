package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.GroupRightInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface GroupRightInfoRepository extends CrudRepository<GroupRightInfo, Long> {
    @Query("SELECT gri FROM GroupRightInfo gri " +
            "INNER JOIN FETCH gri.portfolio p " +
            "WHERE p.id = :portfolioId " +
            "AND gri.label = :label")
    GroupRightInfo getByPortfolioAndLabel(@Param("portfolioId") UUID portfolioId,
                                          @Param("label") String label);

    @Query("SELECT DISTINCT gri FROM ResourceTable r, GroupRights gr " +
            "INNER JOIN gr.id.groupRightInfo gri WITH gri.portfolio.id = :portfolioId " +
            "WHERE r.credential.id = :userId " +
            "AND gr.id.id = r.id")
    List<GroupRightInfo> getByPortfolioAndUser(@Param("portfolioId") UUID portfolioId,
                                               @Param("userId") Long userId);

    @Query("SELECT gri FROM GroupRightInfo gri " +
            "LEFT JOIN FETCH gri.groupInfo gi " +
            "LEFT JOIN FETCH gri.groupRights gr " +
            "LEFT JOIN FETCH gi.groupUser gu " +
            "WHERE gri.portfolio.id = :portfolioId")
    List<GroupRightInfo> getByPortfolioID(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT gri.id FROM GroupRightInfo gri, Node n " +
            "INNER JOIN gri.portfolio p1 " +
            "INNER JOIN n.portfolio p2 " +
            "INNER JOIN gri.groupInfo gi " +
            "WHERE p1.id = p2.id " +
            "AND n.id = :nodeId " +
            "AND gri.label = :label")
    Long getIdByNodeAndLabel(@Param("nodeId") UUID nodeId, @Param("label") String label);

    @Query("SELECT gri FROM GroupRightInfo gri, Node n " +
            "INNER JOIN gri.portfolio p1 " +
            "INNER JOIN n.portfolio p2 " +
            "INNER JOIN gri.groupInfo gi " +
            "WHERE p1.id = p2.id " +
            "AND n.id = :nodeId")
    List<GroupRightInfo> getByNode(@Param("nodeId") UUID nodeId);

    @Query("SELECT new map(portfolio.id as portfolioUUID, gr.notifyRoles AS notifyRoles) " +
            "FROM GroupUser gu " +
            "INNER JOIN gu.id.groupInfo gi WITH gi.id = :groupInfoId " +
            "INNER JOIN gi.groupRightInfo gri " +
            "INNER JOIN gri.portfolio portfolio " +
            "INNER JOIN gri.groupRights gr WITH gr.id.id = :groupRightId " +
            "WHERE gu.id.credential.id = :userId")
    Map<String, Object> getRolesToBeNotified(@Param("groupInfoId") Long groupId,
                                             @Param("userId") Long userId,
                                             @Param("groupRightId") UUID groupRightId);

    @Query("SELECT DISTINCT gri FROM ResourceTable r, GroupRights gr " +
            "INNER JOIN gr.id.groupRightInfo gri " +
            "WHERE r.credential.id = :userId " +
            "AND gr.id.id = r.id")
    List<GroupRightInfo> getByUser(@Param("userId") Long userId);

    @Query("SELECT gri FROM GroupRightInfo gri " +
            "LEFT JOIN gri.groupInfo gi " +
            "WHERE gri.portfolio.id = :portfolioId " +
            "AND gri.label = 'all'")
    List<GroupRightInfo> getDefaultByPortfolio(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT gri FROM GroupRightInfo gri " +
            "INNER JOIN gri.portfolio p WITH p.modifUserId = :userId " +
            "WHERE gri.id = :id")
    boolean isOwner(@Param("userId") Long userId, @Param("id") Long id);
}
