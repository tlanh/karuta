package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Resource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceRepository extends CrudRepository<Resource, UUID> {
    @Query("SELECT r FROM Resource r " +
            "INNER JOIN r.contextNode n WITH n.portfolio.id = :portfolioId")
    List<Resource> getContextResourcesByPortfolioUUID(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT r FROM Resource r " +
            "INNER JOIN r.node n WITH n.portfolio.id = :portfolioId")
    List<Resource> getResourcesByPortfolioUUID(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT r FROM Resource r " +
            "INNER JOIN r.resNode n WITH n.portfolio.id = :portfolioId")
    List<Resource> getResourcesOfResourceByPortfolioUUID(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT r FROM Resource r " +
            "INNER JOIN r.node n WITH n.id = :parentNodeId")
    Resource getResourceByParentNodeUuid(@Param("parentNodeId") UUID parentNodeId);

    @Query("SELECT r FROM Resource r " +
            "INNER JOIN r.contextNode contextNode WITH contextNode.id = :nodeId")
    Resource getContextResourceByNodeUuid(@Param("nodeId") UUID nodeId);

    @Query("SELECT r FROM Resource r " +
            "INNER JOIN r.resNode resNode WITH resNode.id = :nodeId")
    Resource getResourceOfResourceByNodeUuid(@Param("nodeId") UUID nodeId);

    @Query("SELECT n.resource FROM Node n WHERE n.id = :nodeId")
    Resource findByNodeId(@Param("nodeId") UUID nodeId);
}
