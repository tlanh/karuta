package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.GroupInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupInfoRepository extends CrudRepository<GroupInfo, Long> {

    @Query("SELECT DISTINCT gi FROM GroupInfo gi " +
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
