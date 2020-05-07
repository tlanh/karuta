package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Node;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NodeRepository extends CrudRepository<Node, UUID> {

    Node findByIdAndSemantictag(UUID id, String semantictag);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Node n " +
            "INNER JOIN n.portfolio p " +
            "INNER JOIN p.groupRightInfo gri WITH gri.label='all' " +
            "INNER JOIN gri.groupInfo gi " +
            "INNER JOIN gi.groupUser gu " +
            "INNER JOIN gu.id.credential c WITH c.login = 'sys_public'" +
            "WHERE n.id = :nodeId")
    boolean isPublic(@Param("nodeId") UUID nodeId);

    @Query("SELECT p.id FROM Node n " +
            "LEFT JOIN n.portfolio p " +
            "WHERE n.id = :nodeId")
    UUID getPortfolioIdFromNode(@Param("nodeId") UUID nodeId);

    @Query("SELECT n FROM Node n " +
            "LEFT JOIN FETCH n.resource r1 " +
            "LEFT JOIN FETCH n.resResource r2 " +
            "LEFT JOIN FETCH n.contextResource r3 " +
            "WHERE n.portfolio.id = :portfolioId")
    List<Node> getNodesWithResources(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT n FROM Node n " +
            "INNER JOIN n.portfolio p WITH p.id = :portfolioId" +
            "LEFT JOIN FETCH n.parentNode")
    List<Node> getNodes(@Param("portfolioId") UUID portfoioId);

    @Query("SELECT n FROM Node n " +
            "LEFT JOIN FETCH n.resource r1 " +
            "LEFT JOIN FETCH n.resResource r2 " +
            "LEFT JOIN FETCH n.contextResource r3 " +
            "WHERE n.id IN :nodeIds")
    List<Node> getNodes(@Param("nodeIds") List<UUID> nodeIds);

    @Query("SELECT n FROM Node n " +
            "WHERE n.portfolio.id = :portfolioId " +
            "AND n.sharedNode = TRUE")
    List<Node> getSharedNodes(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT n FROM Node n " +
            "INNER JOIN n.portfolio p WITH p.id = :portfolioId " +
            "WHERE n.asmType = 'asmRoot'")
    Node getRootNodeByPortfolio(@Param("portfolioId") UUID portfolioId);

    @Query("SELECT p.id FROM Node n1 " +
            "INNER JOIN n1.portfolio p " +
            "WHERE n1.asmType = 'asmRoot' " +
            "AND n1.code = :code")
    boolean isCodeExist(@Param("code") String code);

    @Query("SELECT p.id FROM Node n1 " +
            "INNER JOIN n1.portfolio p " +
            "WHERE n1.asmType = 'asmRoot' " +
            "AND n1.code = :code " +
            "AND n1.id != :nodeId " +
            "AND p.id = (SELECT n2.portfolio.id FROM Node n2 WHERE n2.id = :nodeId)")
    boolean isCodeExist(@Param("code") String code, @Param("nodeId") UUID nodeId);

    @Query("SELECT n FROM Node n " +
            "WHERE n.parentNode.id = :id " +
            "ORDER by n.nodeOrder ASC")
    List<Node> getFirstLevelChildren(@Param("id") UUID id);

    @Query("SELECT n FROM Node n " +
            "WHERE n.portfolio.id = :id " +
            "AND n.metadata LIKE CONCAT('%semantictag=%', :semantictag, '%') " +
            "AND n.code = :code")
    Node getNodeBySemtagAndCode(@Param("id") UUID id,
                                @Param("semantictag") String semantictag,
                                @Param("code") String code);

    @Query("SELECT n FROM Node n " +
            "INNER JOIN n.portfolio p WITH p.id = :portfolioId " +
            "WHERE n.semantictag LIKE CONCAT('%', :semantictag, '%') " +
            "ORDER BY n.code, n.nodeOrder")
    List<Node> getNodesBySemanticTag(@Param("portfolioId") UUID portfolioId,
                                     @Param("semantictag") String semantictag);

    @Query("SELECT n.metadataWad FROM Node n WHERE n.id = :nodeId")
    String getMetadataWad(@Param("nodeId") UUID nodeId);


    @Query("SELECT n FROM Node n WHERE n.parentNode.id IN :ids")
    List<Node> getDirectChildren(@Param("ids") List<UUID> ids);

    @Query("SELECT n FROM Node n " +
            "WHERE n.semantictag LIKE CONCAT('%', :semantictag, '%') " +
            "AND n.parentNode.id = :parentId")
    Node getParentNode(@Param("parentId") UUID parentId,
                       @Param("semantictag") String semantictag);

    @Query("SELECT COUNT(n) FROM Node n " +
            "WHERE n.parentNode.id  = :parentNodeId " +
            "GROUP BY n.parentNode.id")
    Integer getNodeNextOrderChildren(@Param("parentNodeId") UUID parentNodeId);

    @Query("SELECT n FROM Node n " +
            "WHERE n.nodeOrder IN (:order - 1, :order) " +
            "AND n.parentNode.id = :nodeId")
    List<Node> getNodesByOrder(UUID nodeId, int order);

    @Query("SELECT n.id FROM Node n " +
            "INNER JOIN n.portfolio p WITH p.modelId = :modelId " +
            "WHERE n.semantictag = :semantictag")
    UUID getNodeUuidByPortfolioModelAndSemanticTag(@Param("modelId") UUID modelId,
                                                   @Param("semantictag") String semantictag);

    @Query("SELECT COALESCE(n.sharedNodeUuid, n.id) AS value FROM Node n " +
            "INNER JOIN n.parentNode pNode WITH pNode.id = :nodeId " +
            "GROUP BY pNode.id " +
            "ORDER BY n.nodeOrder")
    List<UUID> getParentNodeUUIDs(@Param("nodeId") UUID nodeId);
}
