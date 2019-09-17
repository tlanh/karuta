package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.activation.MimeType;

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

	Portfolio getPortfolioByPortfolioCode(String portfolioCode);

	UUID getPortfolioUuidByPortfolioCode(String portfolioCode);

	UUID getPortfolioModelUuid(UUID portfolioUuid);

	UUID getPortfolioModelUuid(String portfolioUuid);

	Long getPortfolioUserId(String portfolioUuid);

	Node getPortfolioRootNode(UUID portfolioUuid);

	Node getPortfolioRootNode(String portfolioUuid);

	int updatePortfolioModelId(UUID portfolioUuid, UUID portfolioModelId);

	int updatePortfolioModelId(String portfolioUuid, String portfolioModelId);

	UUID getPortfolioUuidByNodeUuid(String nodeUuid);

	UUID getPortfolioUuidByNodeUuid(UUID nodeUuid);

//	String getPortfolioShared(int user, int userId) throws SQLException;

	List<Map<String, Object>> getPortfolioShared(Long userId);

	Long getOwner(String portfolioId);

	Long getOwner(UUID portfolioId);

	boolean isOwner(Long userId, String portfolioUuid);

	boolean isOwner(Long userId, UUID portfolioUuid);

	boolean putPortfolioOwner(String portfolioUuid, Long ownerId);

	boolean putPortfolioOwner(UUID portfolioUuid, Long ownerId);

	Portfolio putPortfolioConfiguration(String portfolioUuid, Boolean portfolioActive);

	Portfolio putPortfolioConfiguration(UUID portfolioUuid, Boolean portfolioActive);

	Portfolio addPortfolio(String rootNodeUuid, UUID modelId, Long userId, Portfolio porfolio) throws BusinessException;

	void changePortfolio(Portfolio portfolio) throws BusinessException;
	// ---------------------------------------------------------------------------------------------------------

	Portfolio getPortfolio(MimeType outMimeType, String portfolioUuid, int userId, int groupId, String label,
			String resource, String files, int substid, Integer cutoff) throws Exception;

	Portfolio getPortfolioZip(MimeType mimeType, String portfolioUuid, int userId, int groupId, String label,
			Boolean resource, Boolean files) throws Exception;

	Object putPortfolio(MimeType inMimeType, MimeType outMimeType, String in, String portfolioUuid, int userId,
			Boolean portfolioActive, int groupId, String modelId) throws Exception;

	Object postPortfolio(MimeType inMimeType, MimeType outMimeType, String in, int userId, int groupId, String modelId,
			int substid, boolean parseRights, String projectName) throws Exception;

//	Object postPortfolioZip(MimeType mimeType, MimeType mimeType2, HttpServletRequest httpServletRequest,
//			InputStream inputStream, int userId, int groupId, String modelId, int substid, boolean parseRights,
//			String projectName) throws Exception;

	Object postInstanciatePortfolio(MimeType inMimeType, String portfolioUuid, String srcCode, String newCode,
			int userId, int groupId, boolean copyshared, String portfGroupName, boolean setOwner) throws Exception;

	Object postCopyPortfolio(MimeType inMimeType, String portfolioUuid, String srcCode, String newCode, int userId,
			boolean setOwner) throws Exception;

	Object postPortfolioParserights(String portfolioUuid, int userId);

	@Deprecated
	Object getModels(MimeType mimeType, int userId) throws Exception;

	@Deprecated
	Object getModel(MimeType mimeType, Integer modelId, int userId) throws Exception;

	@Deprecated
	Object postModels(MimeType mimeType, String xmlModel, int userId) throws Exception;


}