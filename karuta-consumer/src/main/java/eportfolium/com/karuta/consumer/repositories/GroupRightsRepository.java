package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupRightsRepository extends CrudRepository<GroupRights, GroupRightsId> {

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN FETCH gr.id.groupRightInfo gri " +
            "INNER JOIN FETCH gri.groupInfo gi " +
            "INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId" +
            "WHERE gr.id.id = :id")
    GroupRights getRightsByIdAndUser(@Param("id") UUID id,
                                     @Param("userId") Long userId);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN FETCH gr.id.groupRightInfo gri " +
            "INNER JOIN FETCH gri.groupInfo gi " +
            "INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId " +
            "AND gu.id.groupInfo.id = :groupId " +
            "WHERE gr.id.id = :id")
    GroupRights getRightsByUserAndGroup(@Param("id") UUID id,
                                        @Param("userId") Long userId,
                                        @Param("groupId") Long groupId);

    @Query("SELECT gr FROM GroupRights gr " +
            "WHERE gr.id.id = :uuid " +
            "AND id.groupRightInfo.id = (" +
            "SELECT gri.id FROM Credential c, GroupRightInfo gri, Node n " +
            "WHERE c.login = gri.label " +
            "AND c.id = :userId " +
            "AND gri.portfolio.id = n.portfolio.id " +
            "AND n.id = :id)")
    GroupRights getSpecificRightsForUser(@Param("id") UUID id,
                                         @Param("userId") Long userId);

    @Query("SELECT gr FROM GroupRights gr " +
            "WHERE gr.id.id = :nodeId " +
            "AND id.groupRightInfo.id = (" +
            "SELECT gri2.id FROM GroupInfo gi, GroupRightInfo gri2 " +
            "INNER JOIN gi.groupRightInfo gri1 " +
            "WHERE gri1.portfolio.id = gri2.portfolio.id " +
            "AND gi.id = :groupId " +
            "AND gri2.label = 'all')")
    GroupRights getPublicRightsByGroupId(@Param("id") UUID id,
                                         @Param("groupId") Long groupId);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN gr.id.groupRightInfo gri WITH gri.label='all' " +
            "INNER JOIN gri.groupInfo gi " +
            "INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userId " +
            "WHERE gr.id.id = :id")
    GroupRights getPublicRightsByUserId(@Param("id") UUID id,
                                        @Param("userId") Long userId);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN FETCH gr.id.groupRightInfo gri " +
            "INNER JOIN FETCH gri.groupInfo gi " +
            "WHERE gri.portfolio.id = :portfolioId " +
            "AND (gi.label = 'all' OR gi.groupRightInfo.id = :groupId OR gi.label = :login)")
    List<GroupRights> getPortfolioAndUserRights(@Param("portfolioId") UUID portfolioId,
                                                @Param("login") String login,
                                                @Param("groupId") Long groupId);

    @Query("SELECT gr FROM GroupRights gr " +
            "WHERE gr.id.id = :id " +
            "AND gr.id.groupRightInfo.id IN (" +
                "SELECT gri.id FROM GroupRightInfo gri WHERE gri.portfolio.id = :portfolioId)")
    List<GroupRights> getRightsByPortfolio(@Param("id") UUID id,
                                           @Param("portfolioId") UUID portfolioId);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN gr.id.groupRightInfo gri WITH gri.id = :grid " +
            "WHERE gr.id.id = :id")
    GroupRights getRightsByGrid(@Param("id") UUID id,
                                @Param("grid") Long grid);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN gr.id.groupRightInfo gri WITH gri.label = :label " +
            "WHERE gr.id.id = :id")
    GroupRights getRightsByIdAndLabel(@Param("id") UUID id,
                                      @Param("label") String label);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN gr.id.groupRightInfo gri " +
            "INNER JOIN gri.groupInfo gi " +
            "WHERE gi.id = :groupId")
    List<GroupRights> getRightsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN FETCH gr.id.groupRightInfo gri " +
            "INNER JOIN gri.groupInfo gi WITH gi.id = :groupId " +
            "WHERE gr.id.id = :id")
    List<GroupRights> getRightsByIdAndGroup(@Param("id") UUID id,
                                            @Param("groupId") Long groupId);

    @Query("SELECT gr FROM GroupRights gr WHERE gr.id.id = :id")
    List<GroupRights> getRightsById(@Param("id") UUID id);

    @Query("SELECT gr FROM GroupRights gr " +
            "INNER JOIN FETCH gr.id.groupRightInfo gri " +
            "WHERE gri.portfolio.id = :portfolioId " +
            "AND gri.id IN (:id1, :id2, :id3)")
    List<GroupRights> getByPortfolioAndGridList(@Param("portfolioId") UUID portfolioId,
                                                @Param("id1") Long id1,
                                                @Param("id2") Long id2,
                                                @Param("id3") Long id3);
    @Query("SELECT cr.id FROM GroupRights gr, GroupInfo gi, GroupUser gu " +
            "INNER JOIN gu.id.credential cr " +
            "INNER JOIN gr.id.groupRightInfo gri " +
            "INNER JOIN gi.groupRightInfo gri2 " +
            "WHERE gr.id.id = :id " +
            "AND gri.id = gri2.id " +
            "AND gi.label LIKE gri.label " +
            "AND gu.id.groupInfo.id = gi.id)")
    Long getUserIdFromNode(@Param("id") UUID id);

    @Query("SELECT gr FROM GroupRights gr " +
            "WHERE gr.id.id = :id " +
            "AND gr.id.groupRightInfo.id IN (" +
                "SELECT gri.id FROM GroupRightInfo gri, Node n " +
                "INNER JOIN gri.portfolio p1 " +
                "INNER JOIN n.portfolio p2 " +
                "INNER JOIN gri.groupInfo gi " +
                "WHERE p1.id = p2.id " +
                "AND n.id = :id " +
                "AND gri.label IN :labels" +
            ")")
    List<GroupRights> findByIdAndLabels(@Param("id") UUID id,
                                        @Param("labels") List<String> labels);
}
