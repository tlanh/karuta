/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

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

	ResourceTable getResourceByParentNodeUuid(String parentNodeUuid) throws DoesNotExistException;

	ResourceTable getResourceOfResourceByNodeUuid(String nodeUuid);

	ResourceTable getResourceByNodeUuid(String nodeUuid);

	String getResNodeContentByNodeUuid(String nodeUuid);

	ResourceTable getResourceByNodeParentUuid(UUID parentNodeUuid) throws DoesNotExistException;

	List<ResourceTable> getResourcesByPortfolioUUID(String portfolioUuid);

	List<ResourceTable> getContextResourcesByPortfolioUUID(String portfolioUuid);

	List<ResourceTable> getResourcesOfResourceByPortfolioUUID(String portfolioUuid);

	int updateResource(String uuid, String xsiType, String content, Long userId);

	UUID getResourceUuidByParentNodeUuid(String parentNodeUuid) throws DoesNotExistException;

	int updateResource(UUID nodeUuid, String content, Long userId);

	int updateResource(String nodeUuid, String content, Long userId);

	int updateResResource(UUID nodeUuid, String content, Long userId);

	int updateResResource(String nodeUuid, String content, Long userId);

	int updateContextResource(UUID nodeUuid, String content, Long userId);

	int updateContextResource(String nodeUuid, String content, Long userId);

	int addResource(String uuid, String parentUuid, String xsiType, String content, String portfolioModelId,
			boolean sharedNodeRes, boolean sharedRes, Long userId);

	/********************************************************************************************************************/
	
	ResultSet getMysqlResources(Connection con) throws SQLException;

	ResultSet findAll(String table, Connection con);

	void removeAll();
}