package eportfolium.com.karuta.business.contract;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.util.MimeType;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * @author mlengagne
 *
 */
public interface PortfolioManager {

	int addPortfolioInGroup(String portfolioUuid, Long portfolioGroupId, String label, Long userId);

	Long createPortfolioGroup(String groupname, String type, Long parentId, Long userId);

	void deletePortfolio(String portfolioUuid, Long userId, Long groupId) throws Exception;

	boolean deletePortfolioFromPortfolioGroups(String portfolioUuid, Long portfolioGroupId);

	boolean deletePortfolioGroups(Long portfolioGroupId);

	String getPortfolio(MimeType outMimeType, String portfolioUuid, Long userId, Long groupId, String label,
			String resource, String files, long substid, Integer cutoff)
			throws DoesNotExistException, BusinessException, Exception;

	String getPortfolioByCode(MimeType mimeType, String portfolioCode, Long userId, Long groupId, String resources,
			long substid) throws DoesNotExistException, BusinessException, Exception;

	String getPortfolioByPortfolioGroup(Long portfolioGroupId);

	Long getPortfolioGroupIdFromLabel(String groupLabel, Long userId);

	String getPortfolioGroupList();

	String getPortfolioGroupListFromPortfolio(String portfolioUuid);

	Object getPortfolios(MimeType outMimeType, long userId, long groupId, Boolean portfolioActive, long substid,
			Boolean portfolioProject, String projectId, Boolean countOnly, String search);

	String getPortfolioShared(Long userId);

	GroupRights getRightsOnPortfolio(Long userId, Long groupId, String portfolioUuid);

	int setPortfolioActive(String portfolioUuid, Boolean active);

	String postPortfolioParserights(String portfolioUuid, Long id);

	boolean changePortfolioDate(final String fromNodeuuid, final String fromPortuuid);

	boolean isOwner(Long id, String portfolioUuid);

	boolean isOwner(Long userId, UUID pid);

	/**
	 * Has rights, whether ownership, or given by someone
	 * 
	 * @param userId
	 * @param portfolioUuid
	 * @return
	 */
	boolean hasRights(Long userId, String portfolioUuid);

	boolean changePortfolioOwner(String portfolioUuid, long newOwner);

	Portfolio changePortfolioConfiguration(String portfolioUuid, Boolean portfolioActive, Long id)
			throws BusinessException;

	boolean putPortfolio(MimeType inMimeType, MimeType outMimeType, String in, String portfolioUuid, Long userId,
			Boolean portfolioActive, int groupId, UUID portfolioModelId) throws BusinessException, Exception;

	Object postInstanciatePortfolio(MimeType mimeType, String portfolioId, String srccode, String tgtcode, Long id,
			int groupId, boolean copyshared, String groupname, boolean setOwner);

	String postPortfolioZip(MimeType mimeType, MimeType mimeType2, String path, String userName,
			InputStream fileInputStream, Long id, Long groupId, String modelId, Long credentialSubstitutionId,
			boolean instantiate, String projectName) throws BusinessException, FileNotFoundException, Exception;

	String addPortfolio(MimeType inMimeType, MimeType outMimeType, String in, long userId, long groupId,
			String portfolioModelId, long substid, boolean parseRights, String projectName)
			throws BusinessException, Exception;

	String getGroupRightsInfos(Long id, String portfolioId) throws BusinessException;

	String addRoleInPortfolio(Long userId, String portfolio, String data) throws BusinessException;

	String findRoleByPortfolio(MimeType mimeType, String role, String portfolioId, Long id);

	String findRolesByPortfolio(String portfolioUuid, Long id);

	GroupRights getRightsOnPortfolio(Long userId, Long groupId, UUID portfolioUuid);

	String copyPortfolio(MimeType mimeType, String portfolioId, String srccode, String tgtcode, Long id,
			boolean setOwner) throws Exception;

}
