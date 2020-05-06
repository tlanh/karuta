package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.ResourceTable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResourceTableRepository extends CrudRepository<ResourceTable, UUID> {
    @Query("SELECT r FROM ResourceTable r " +
            "INNER JOIN r.contextNode n WITH n.portfolio.id = :portfolioId")
    List<ResourceTable> getContextResourcesByPortfolioUUID(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT r FROM ResourceTable r " +
            "INNER JOIN r.node n WITH n.portfolio.id = :portfolioId")
    List<ResourceTable> getResourcesByPortfolioUUID(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT r FROM ResourceTable r " +
            "INNER JOIN r.resNode n WITH n.portfolio.id = :portfolioId")
    List<ResourceTable> getResourcesOfResourceByPortfolioUUID(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT r FROM ResourceTable r " +
            "WHERE r.xsiType LIKE :xsiType " +
            "AND r.id = :id")
    ResourceTable getResourceByXsiType(@Param("id") UUID id, @Param("xsiType") String xsiType);

    @Query("SELECT r FROM ResourceTable r" +
            "INNER JOIN r.node n WITH n.id = :parentNodeId")
    ResourceTable getResourceByParentNodeUuid(@Param("parentNodeId") UUID parentNodeId);

    @Query("SELECT r FROM ResourceTable r " +
            "INNER JOIN r.contextNode contextNode WITH contextNode.id = :nodeId")
    ResourceTable getContextResourceByNodeUuid(@Param("nodeId") UUID nodeId);

    @Query("SELECT r FROM ResourceTable r " +
            "INNER JOIN r.resNode resNode WITH resNode.id = :nodeId")
    ResourceTable getResourceOfResourceByNodeUuid(@Param("nodeId") UUID nodeId);
}
