package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
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

	Portfolio getPortfolio(UUID portfolioUuid);

	Portfolio getPortfolio(String portfolioUuid);

	List<Portfolio> getPortfolios(Long userId, Long substid, Boolean portfolioActive);

	Portfolio getPortfolioFromNodeCode(String portfolioCode);

	UUID getPortfolioUuidFromNodeCode(String portfolioCode);

	UUID getPortfolioModelUuid(UUID portfolioUuid);

	UUID getPortfolioModelUuid(String portfolioUuid);

	Long getPortfolioUserId(String portfolioUuid);

	Node getPortfolioRootNode(UUID portfolioUuid);

	Node getPortfolioRootNode(String portfolioUuid);

	Portfolio getPortfolioFromNode(UUID nodeUuid);

	Portfolio getPortfolioFromNode(String nodeUuid);

	UUID getPortfolioUuidFromNode(String nodeUuid);

	UUID getPortfolioUuidFromNode(UUID nodeUuid);

//	String getPortfolioShared(int user, int userId) throws SQLException;

	List<Map<String, Object>> getPortfolioShared(Long userId);

	Long getOwner(String portfolioId);

	Long getOwner(UUID portfolioId);

	boolean isOwner(Long userId, String portfolioUuid);

	boolean isOwner(Long userId, UUID portfolioUuid);

	boolean changePortfolioOwner(String portfolioUuid, Long ownerId);

	boolean changePortfolioOwner(UUID portfolioUuid, Long ownerId);

	Portfolio add(String rootNodeUuid, UUID modelId, Long userId, Portfolio porfolio) throws BusinessException;

	boolean isPublic(String portfolioUuid);

	boolean isPublic(UUID portfolioUuid);

	Portfolio updatePortfolioConfiguration(String portfolioUuid, Boolean portfolioActive);

	Portfolio updatePortfolioConfiguration(UUID portfolioUuid, Boolean portfolioActive);

	int updatePortfolioModelId(UUID portfolioUuid, UUID portfolioModelId);

	int updatePortfolioModelId(UUID portfolioUuid, String portfolioModelId);

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

	/**
	 * 
	 * Check if there's shared node in this portfolio
	 * 
	 * @param portfolioUuid
	 * @return
	 */
	boolean hasSharedNodes(UUID portfolioUuid);

}