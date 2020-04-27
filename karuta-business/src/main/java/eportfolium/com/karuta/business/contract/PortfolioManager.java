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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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

	Long addPortfolioGroup(String groupname, String type, Long parentId, Long userId);

	void removePortfolio(String portfolioUuid, Long userId, Long groupId) throws Exception;

	boolean removePortfolioFromPortfolioGroups(String portfolioUuid, Long portfolioGroupId);

	boolean removePortfolioGroups(Long portfolioGroupId);

	String getPortfolio(MimeType outMimeType, String portfolioUuid, Long userId, Long groupId, String label,
			String resource, String files, long substid, Integer cutoff)
			throws DoesNotExistException, BusinessException, Exception;

	String getPortfolioByCode(MimeType mimeType, String portfolioCode, Long userId, Long groupId, String resources,
			long substid) throws DoesNotExistException, BusinessException, Exception;

	String getPortfoliosByPortfolioGroup(Long portfolioGroupId);

	Long getPortfolioGroupIdFromLabel(String groupLabel, Long userId);

	String getPortfolioGroupList();

	String getPortfolioGroupListFromPortfolio(String portfolioUuid);

	String getPortfolios(MimeType outMimeType, long userId, long groupId, Boolean portfolioActive,
			long substid, Boolean portfolioProject, String projectId, Boolean countOnly, String search);

	String getPortfolioShared(Long userId);

	GroupRights getRightsOnPortfolio(Long userId, Long groupId, String portfolioUuid);

	int changePortfolioActive(String portfolioUuid, Boolean active);

	String postPortfolioParserights(String portfolioUuid, Long userId);

	boolean changePortfolioDate(final String fromNodeuuid, final String fromPortuuid);

	boolean isOwner(Long userId, String portfolioUuid);

	/**
	 * Has rights, whether ownership, or given by someone
	 * 
	 * @param userId
	 * @param portfolioUuid
	 * @return
	 */
	boolean hasRights(Long userId, String portfolioUuid);

	boolean changePortfolioOwner(String portfolioUuid, long newOwner);

	Portfolio changePortfolioConfiguration(String portfolioUuid, Boolean portfolioActive, Long userId)
			throws BusinessException;

	boolean rewritePortfolioContent(MimeType inMimeType, MimeType outMimeType, String xmlPortfolio,
			String portfolioUuid, Long userId, Boolean portfolioActive) throws BusinessException, Exception;

	String instanciatePortfolio(MimeType mimeType, String portfolioId, String srccode, String tgtcode, Long id,
			int groupId, boolean copyshared, String groupname, boolean setOwner);

	String importZippedPortfolio(MimeType mimeType, MimeType mimeType2, String path, String userName,
			InputStream fileInputStream, Long id, Long groupId, String modelId, Long credentialSubstitutionId,
			boolean instantiate, String projectName) throws BusinessException, FileNotFoundException, Exception;

	String addPortfolio(MimeType inMimeType, MimeType outMimeType, String xmlPortfolio, long userId, long groupId,
			String portfolioModelId, long substid, boolean parseRights, String projectName)
			throws BusinessException, Exception;

	String getGroupRightsInfos(Long id, String portfolioId) throws BusinessException;

	String addRoleInPortfolio(Long userId, String portfolioUuid, String data) throws BusinessException;

	String getRoleByPortfolio(MimeType mimeType, String role, String portfolioUuid, Long userId);

	String getRolesByPortfolio(String portfolioUuid, Long userId);

	String copyPortfolio(MimeType mimeType, String portfolioUuid, String srccode, String tgtcode, Long userId,
			boolean setOwner) throws Exception;

	String getPortfolioUuidFromNode(String nodeUuid);

	Map<Long, Long> transferPortfolioGroupTable(Connection con) throws SQLException;

	void transferParentPortfolioGroup(Connection con, Map<Long, Long> pgIds) throws SQLException;

	void transferPortfolioGroupMembersTable(Connection con, Map<String, String> portIds, Map<Long, Long> pgIds)
			throws SQLException;

	void removePortfolios();

	void removePortfolioGroups();

	Map<String, String> transferPortfolioTable(Connection con, Map<Long, Long> userIds) throws SQLException;

}
