package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.activation.MimeType;

import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface ResourceTableDao {

	void persist(ResourceTable transientInstance);

	void remove(ResourceTable persistentInstance);

	ResourceTable merge(ResourceTable detachedInstance);

	ResourceTable findById(Serializable id) throws DoesNotExistException;

	void removeById(Serializable id) throws DoesNotExistException;

	ResourceTable getResource(String nodeUuid);

	ResourceTable getResourceByNodeParentUuid(String nodeParentUuid);

	ResourceTable getResourceOfResourceByNodeUuid(String nodeUuid);

	ResourceTable getResourceByNodeUuid(String nodeUuid);

	String getResNodeContentByNodeUuid(String nodeUuid);

	ResourceTable getResourceByNodeParentUuid(UUID nodeParentUuid);

	List<ResourceTable> getResourcesByPortfolioUUID(String portfolioUuid);

	List<ResourceTable> getContextResourcesByPortfolioUUID(String portfolioUuid);

	List<ResourceTable> getResourcesOfResourceByPortfolioUUID(String portfolioUuid);

	int updateResource(String uuid, String xsiType, String content, Long userId);

	Object deleteResource(String resourceUuid, Long userId, Long groupId) throws Exception;

	UUID getResourceNodeUuidByParentNodeUuid(String nodeParentUuid);

	UUID getResourceNodeUuidByParentNodeUuid(UUID nodeParentUuid);

	int updateResource(UUID nodeUuid, String content, Long userId);

	int updateResource(String nodeUuid, String content, Long userId);

	int updateResResource(UUID nodeUuid, String content, Long userId);

	int updateResResource(String nodeUuid, String content, Long userId);

	int updateContextResource(UUID nodeUuid, String content, Long userId);

	int updateContextResource(String nodeUuid, String content, Long userId);
	// --------------------------------------------------------------------------------------------------------------------

	public ResourceTable getResource(MimeType outMimeType, String nodeParentUuid, int userId, int groupId)
			throws Exception;

	public List<ResourceTable> getResources(MimeType outMimeType, String portfolioUuid, int userId, int groupId)
			throws Exception;

	public Object putResource(MimeType inMimeType, String nodeParentUuid, String in, int userId, int groupId)
			throws Exception;

	public Object postResource(MimeType inMimeType, String nodeParentUuid, String in, int userId, int groupId)
			throws Exception;




}