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

	Portfolio merge(Portfolio detachedInstance);

	Portfolio findById(Serializable id) throws DoesNotExistException;

	Portfolio getPortfolio(String portfolioUuid);

	List<Portfolio> getPortfolios(Long userId, Long substid, Boolean portfolioActive);

	Portfolio getPortfolioFromNodeCode(String portfolioCode);

	String getPortfolioUuidFromNodeCode(String portfolioCode);

	String getPortfolioModelUuid(String portfolioUuid);

	Long getPortfolioUserId(String portfolioUuid);

	Node getPortfolioRootNode(String portfolioUuid);

	Portfolio getPortfolioFromNode(String nodeUuid);

	String getPortfolioUuidFromNode(String nodeUuid);

//	String getPortfolioShared(int user, int userId) throws SQLException;

	List<Map<String, Object>> getPortfolioShared(Long userId);

	Long getOwner(String portfolioId);

	boolean isOwner(Long userId, String portfolioUuid);

	boolean changePortfolioOwner(String portfolioUuid, Long ownerId);

	Portfolio add(String rootNodeUuid, String modelId, Long userId, Portfolio porfolio) throws BusinessException;

	boolean isPublic(String portfolioUuid);

	Portfolio changePortfolioConfiguration(String portfolioUuid, Boolean portfolioActive);

	int updatePortfolioModelId(String portfolioUuid, String portfolioModelId);

	void updateTime(String portfolioUuid) throws DoesNotExistException;

	void updateTime(UUID portfolioUuid) throws DoesNotExistException;

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