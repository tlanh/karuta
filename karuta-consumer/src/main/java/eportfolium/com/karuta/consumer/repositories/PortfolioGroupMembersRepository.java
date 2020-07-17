package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembersId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioGroupMembersRepository
        extends CrudRepository<PortfolioGroupMembers, PortfolioGroupMembersId> {

    @Query("SELECT pgm FROM PortfolioGroupMembers pgm " +
            "INNER JOIN pgm.id.portfolio p WITH p.id = :uuid")
    List<PortfolioGroupMembers> getByPortfolioID(@Param("uuid") UUID uuid);

    @Query("SELECT pgm FROM PortfolioGroupMembers pgm " +
            "LEFT JOIN pgm.id.portfolioGroup pg " +
            "WHERE pg.id = :groupId")
    List<PortfolioGroupMembers> getByPortfolioGroupID(@Param("groupId") Long portfolioGroupID);
}
