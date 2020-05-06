package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.GroupUserId;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupUserRepository extends CrudRepository<GroupUser, GroupUserId> {
    @Query("SELECT gu FROM GroupUser gu " +
            "WHERE gu.id.credential.id = :userId " +
            "AND gu.id.groupInfo.id = :groupId")
    boolean isUserInGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Query("SELECT gu FROM GroupUser gu " +
            "INNER JOIN FETCH gu.id.credential cr " +
            "INNER JOIN FETCH gu.id.groupInfo gi " +
            "INNER JOIN FETCH gi.groupRightInfo gri " +
            "WHERE cr.id = :userId")
    List<GroupUser> getByUser(@Param("userId") Long userId);

    @Query("SELECT gu FROM GroupUser gu " +
            " INNER JOIN FETCH gu.id.credential cr " +
            " INNER JOIN FETCH gu.id.groupInfo gi " +
            " INNER JOIN FETCH gi.groupRightInfo gri " +
            " WHERE cr.id = :userId " +
            " AND gri.id = :grid")
    GroupUser getByUserAndRole(@Param("userId") Long userId, @Param("grid") Long grid);

    @Query("SELECT gu FROM GroupUser gu " +
            "INNER JOIN FETCH gu.id.credential cr " +
            "INNER JOIN FETCH gu.id.groupInfo gi " +
            "INNER JOIN FETCH gi.groupRightInfo gri " +
            "WHERE cr.id = :userId " +
            "AND cr.login = gri.label")
    GroupUser getUniqueByUser(@Param("userId") Long userId);

    @Query("SELECT gu FROM GroupUser gu " +
            "INNER JOIN FETCH gu.id.credential cr " +
            "INNER JOIN FETCH gu.id.groupInfo gi " +
            "INNER JOIN FETCH gi.groupRightInfo gri" +
            "WHERE gri.id = :grid")
    List<GroupUser> getByRole(@Param("grid") Long grid);

    @Query("SELECT gu FROM GroupUser gu " +
            "INNER JOIN FETCH gu.id.groupInfo gi " +
            "INNER JOIN FETCH gi.groupRightInfo gri " +
            "WHERE gri.portfolio.id = :portfolioId")
    List<GroupUser> getByPortfolio(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT gu FROM GroupUser gu " +
            "INNER JOIN FETCH gu.id.credential cr " +
            "INNER JOIN FETCH gu.id.groupInfo gi " +
            "INNER JOIN FETCH gi.groupRightInfo gri " +
            "WHERE gri.portfolio.id = :portfolioId " +
            "AND cr.id = :userId")
    List<GroupUser> getByPortfolioAndUser(@Param("portfolioId") UUID portfolioId,
                                          @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GroupUser gu " +
            "WHERE gu.id.groupInfo.id IN (" +
            " SELECT gi.id FROM GroupRightInfo gri INNER JOIN gri.groupInfo gi" +
            " WHERE gri.portfolio.id = :portfolioId" +
            ")")
    void deleteByPortfolio(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT CASE WHEN COUNT(gu) > 0 THEN true ELSE false FROM GroupUser gu " +
            "INNER JOIN gu.id.groupInfo gi " +
            "WHERE gu.id.credential.id = :userid " +
            "AND gi.groupRightInfo.id = :grid")
    boolean hasRole(@Param("userId") Long userId, @Param("grid") Long grid);
}
