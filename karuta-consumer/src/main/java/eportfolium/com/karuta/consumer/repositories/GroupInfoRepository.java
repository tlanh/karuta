package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.GroupInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupInfoRepository extends CrudRepository<GroupInfo, Long> {

    List<GroupInfo> findByLabelAndOwner(String label, String owner);

    boolean existsByLabel(String label);

    boolean existsByLabelAndOwner(String label, String owner);

    @Query("SELECT DISTINCT gi FROM GroupInfo gi " +
            "INNER JOIN gi.groupUser gu" +
            "INNER JOIN gi.groupRightInfo gri " +
            "WHERE gri.portfolio.id = :portfolioId " +
            "AND gri.label = :label")
    List<GroupInfo> getGroupsByRole(@Param("portfolioId") UUID portfolioId, @Param("label") String label);

    @Query("SELECT gi FROM GroupInfo gi INNER JOIN gi.groupRightInfo gri WITH gri.id = :grid")
    GroupInfo getGroupByGrid(@Param("grid") Long grid);

    @Query("SELECT gi FROM GroupInfo gi " +
            "INNER JOIN gi.groupRightInfo gri WITH gri.portfolio.id = :portfolioId " +
            "ORDER BY gi.label ASC")
    List<GroupInfo> getByPortfolio(@Param("portfolioId") UUID portfolioId);
}
