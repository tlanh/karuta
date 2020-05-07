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

package eportfolium.com.karuta.business.contract;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.springframework.util.MimeType;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author mlengagne
 *
 */
public interface PortfolioManager {

	int addPortfolioInGroup(UUID portfolioUuid, Long portfolioGroupId, String label, Long userId);

	Long addPortfolioGroup(String groupname, String type, Long parentId, Long userId);

	void removePortfolio(UUID portfolioId, Long userId, Long groupId) throws Exception;

	boolean removePortfolioFromPortfolioGroups(UUID portfolioId, Long portfolioGroupId);

	boolean removePortfolioGroups(Long portfolioGroupId);

	String getPortfolio(MimeType outMimeType, UUID portfolioId, Long userId, Long groupId, String label,
			String resource, String files, long substid, Integer cutoff)
			throws BusinessException, ParserConfigurationException;

	String getPortfolioByCode(MimeType mimeType, String portfolioCode, Long userId, Long groupId, String resources,
			long substid) throws BusinessException, ParserConfigurationException;

	String getPortfoliosByPortfolioGroup(Long portfolioGroupId);

	Long getPortfolioGroupIdFromLabel(String groupLabel, Long userId);

	String getPortfolioGroupList();

	String getPortfolioGroupListFromPortfolio(UUID portfolioId);

	String getPortfolios(MimeType outMimeType, long userId, long groupId, Boolean portfolioActive,
			long substid, Boolean portfolioProject, String projectId, Boolean countOnly, String search);

	String getPortfolioShared(Long userId);

	GroupRights getRightsOnPortfolio(Long userId, Long groupId, UUID portfolioId);

	int changePortfolioActive(UUID portfolioId, Boolean active);

	UUID postPortfolioParserights(UUID portfolioId, Long userId);

	boolean changePortfolioDate(final UUID nodeId, final UUID portfolioId);

	boolean isOwner(Long userId, UUID portfolioId);

	/**
	 * Has rights, whether ownership, or given by someone
	 * 
	 * @param userId
	 * @param portfolioUuid
	 * @return
	 */
	boolean hasRights(Long userId, UUID portfolioUuid);

	boolean changePortfolioOwner(UUID portfolioId, long newOwner);

	Portfolio changePortfolioConfiguration(UUID portfolioId, Boolean portfolioActive, Long userId)
			throws BusinessException;

	boolean rewritePortfolioContent(MimeType inMimeType, MimeType outMimeType, String xmlPortfolio,
			UUID portfolioId, Long userId, Boolean portfolioActive) throws BusinessException, Exception;

	String instanciatePortfolio(MimeType mimeType, String portfolioId, String srccode, String tgtcode, Long id,
			int groupId, boolean copyshared, String groupname, boolean setOwner);

	String importZippedPortfolio(MimeType mimeType, MimeType mimeType2, String path, String userName,
			InputStream fileInputStream, Long id, Long groupId, String modelId, Long credentialSubstitutionId,
			boolean instantiate, String projectName) throws BusinessException, FileNotFoundException, Exception;

	String addPortfolio(MimeType inMimeType, MimeType outMimeType, String xmlPortfolio, long userId, long groupId,
			UUID portfolioModelId, long substid, boolean parseRights, String projectName)
			throws BusinessException, Exception;

	String getGroupRightsInfos(Long id, UUID portfolioId) throws BusinessException;

	String addRoleInPortfolio(Long userId, UUID portfolioUuid, String data) throws BusinessException;

	String getRoleByPortfolio(MimeType mimeType, String role, UUID portfolioId, Long userId);

	String getRolesByPortfolio(UUID portfolioId, Long userId);

	UUID copyPortfolio(MimeType mimeType, UUID portfolioId, String srccode, String tgtcode, Long userId,
			boolean setOwner) throws Exception;

	UUID getPortfolioUuidFromNode(UUID nodeId);

	void updateTime(UUID portfolioId);

	boolean updateTimeByNode(UUID portfolioId);

	List<Portfolio> getPortfolios(Long userId,
								  Long substid,
								  Boolean portfolioActive,
								  Boolean portfolioProject);
}
