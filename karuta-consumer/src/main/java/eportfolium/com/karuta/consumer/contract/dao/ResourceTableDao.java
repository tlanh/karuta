package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface ResourceTableDao {

	void persist(ResourceTable transientInstance);

	void remove(ResourceTable persistentInstance);

	ResourceTable merge(ResourceTable detachedInstance);

	ResourceTable findById(Serializable id) throws DoesNotExistException;

	void removeById(Serializable id) throws DoesNotExistException;

	ResourceTable getResource(String resUuid);

	ResourceTable getResourceByXsiType(UUID resUuid, String xsiType);

	ResourceTable getResourceByXsiType(String resUuid, String xsiType);

	ResourceTable getResourceByNodeParentUuid(String nodeParentUuid) throws DoesNotExistException;

	ResourceTable getResourceOfResourceByNodeUuid(String nodeUuid);

	ResourceTable getResourceByNodeUuid(String nodeUuid);

	String getResNodeContentByNodeUuid(String nodeUuid);

	ResourceTable getResourceByNodeParentUuid(UUID nodeParentUuid) throws DoesNotExistException;

	List<ResourceTable> getResourcesByPortfolioUUID(String portfolioUuid);

	List<ResourceTable> getContextResourcesByPortfolioUUID(String portfolioUuid);

	List<ResourceTable> getResourcesOfResourceByPortfolioUUID(String portfolioUuid);

	int updateResource(String uuid, String xsiType, String content, Long userId);

	Object deleteResource(String resourceUuid, Long userId, Long groupId) throws Exception;

	String getResourceNodeUuidByParentNodeUuid(String nodeParentUuid) throws DoesNotExistException;

	int updateResource(UUID nodeUuid, String content, Long userId);

	int updateResource(String nodeUuid, String content, Long userId);

	int updateResResource(UUID nodeUuid, String content, Long userId);

	int updateResResource(String nodeUuid, String content, Long userId);

	int updateContextResource(UUID nodeUuid, String content, Long userId);

	int updateContextResource(String nodeUuid, String content, Long userId);

	int createResource(String uuid, String parentUuid, String xsiType, String content, String portfolioModelId,
			boolean sharedNodeRes, boolean sharedRes, Long userId);

	ResultSet getMysqlResources(Connection con) throws SQLException;

	ResultSet findAll(String table, Connection con);

	void removeAll();
}