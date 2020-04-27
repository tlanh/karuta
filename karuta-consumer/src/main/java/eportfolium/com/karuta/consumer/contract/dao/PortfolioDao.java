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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface PortfolioDao {

	void persist(Portfolio transientInstance);

	void remove(Portfolio persistentInstance);

	void removeById(Serializable id) throws DoesNotExistException;

	List<Portfolio> findAll();

	Portfolio merge(Portfolio detachedInstance);

	Portfolio findById(Serializable id) throws DoesNotExistException;

	Portfolio getPortfolio(String portfolioUuid);

	List<Portfolio> getPortfolios(Long userId, Long substid, Boolean portfolioActive, Boolean portfolioProject);

	Portfolio getPortfolioFromNodeCode(String nodeCode);

	String getPortfolioUuidFromNodeCode(String nodeCode);

	UUID getPortfolioModelUuid(String portfolioUuid);

	Long getPortfolioUserId(String portfolioUuid);

	Node getPortfolioRootNode(String portfolioUuid);

	Portfolio getPortfolioFromNode(String nodeUuid);

	UUID getPortfolioUuidFromNode(String nodeUuid);

	List<Map<String, Object>> getPortfolioShared(Long userId);

	Long getOwner(String portfolioId);

	boolean isOwner(Long userId, String portfolioUuid);

	boolean changePortfolioOwner(String portfolioUuid, Long ownerId);

	Portfolio add(String rootNodeUuid, String modelId, Long userId, Portfolio porfolio) throws BusinessException;

	boolean isPublic(String portfolioUuid);

	Portfolio changePortfolioConfiguration(String portfolioUuid, Boolean portfolioActive);

	int updatePortfolioModelId(String portfolioUuid, String portfolioModelId);

	void updateTime(String portfolioUuid) throws DoesNotExistException;

	boolean updateTimeByNode(String nodeUuid);

	/**
	 * 
	 * Check if there's shared node in this portfolio
	 * 
	 * @param portfolioUuid
	 * @return
	 */
	boolean hasSharedNodes(String portfolioUuid);

	ResultSet getMysqlPortfolios(Connection con);

	ResultSet getMysqlPortfolioGroupMembers(Connection con);

	void removeAll();

}