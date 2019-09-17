package eportfolium.com.karuta.business.contract;

import java.util.UUID;

import javax.activation.MimeType;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface PortfolioManager {

	boolean updatePortfolioDate(final String fromNodeuuid, final String fromPortuuid);

	boolean deletePortfolioGroups(Long portfolioGroupId);

	boolean deletePortfolioFromPortfolioGroups(String uuid, Long portfolioGroupId);

	String getPortfolioByPortfolioGroup(Long portfolioGroupId);

	int setPortfolioActive(String portfolioUuid, Boolean active);

	String getPortfolio(MimeType outMimeType, String portfolioUuid, Long userId, Long groupId, String label,
			String resource, String files, long substid, Integer cutoff) throws Exception;

	GroupRights getRightsOnPortfolio(Long userId, Long groupId, String portfolioUuid);

	String getPortfolioShared(Long userId);

	String getPortfolioByCode(MimeType mimeType, String portfolioCode, Long userId, Long groupId, String resources,
			long substid);

	/**
	 * Test pour l'affichage du getPortfolio
	 * 
	 * @param userId
	 * @param groupId
	 * @param portfolioUuid
	 * @param droit
	 * @return
	 */
	GroupRights getPortfolioRight(Long userId, Long groupId, String portfolioUuid, String droit);

	boolean isOwner(Long id, String portfolioUuid);

	boolean putPortfolioOwner(String portfolioUuid, long newOwner);

	Portfolio putPortfolioConfiguration(String portfolioUuid, Boolean portfolioActive, Long id)
			throws BusinessException;

	Object getPortfolios(MimeType outMimeType, long userId, long groupId, Boolean portfolioActive, long substid,
			Boolean portfolioProject, String projectId, Boolean countOnly, String search);

	boolean putPortfolio(MimeType inMimeType, MimeType outMimeType, String in, String portfolioUuid, Long userId,
			Boolean portfolioActive, int groupId, UUID portfolioModelId) throws BusinessException, Exception;

}
