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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import eportfolium.com.karuta.document.*;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface PortfolioManager {

	int addPortfolioInGroup(UUID portfolioUuid, Long portfolioGroupId, String label, Long userId);

	Long addPortfolioGroup(String groupname, String type, Long parentId, Long userId);

	void removePortfolio(UUID portfolioId, Long userId, Long groupId) throws Exception;

	boolean removePortfolioFromPortfolioGroups(UUID portfolioId, Long portfolioGroupId);

	boolean removePortfolioGroups(Long portfolioGroupId);

	PortfolioDocument getPortfolio(UUID portfolioId,
								   Long userId,
								   Long groupId,
								   String label,
								   boolean resource,
								   boolean files,
								   long substid,
								   Integer cutoff) throws BusinessException, JsonProcessingException;

	String getZippedPortfolio(PortfolioDocument portfolio) throws IOException;

	PortfolioDocument getPortfolioByCode(String portfolioCode,
										 Long userId,
										 Long groupId,
										 boolean resources,
										 long substid) throws BusinessException, JsonProcessingException;

	PortfolioGroupDocument getPortfoliosByPortfolioGroup(Long portfolioGroupId);

	Long getPortfolioGroupIdFromLabel(String groupLabel, Long userId);

	String getPortfolioGroupList();

	PortfolioGroupList getPortfolioGroupListFromPortfolio(UUID portfolioId);

	PortfolioList getPortfolios(long userId, Boolean active, long substid, Boolean project);

	PortfolioList getPortfolioShared(Long userId);

	GroupRights getRightsOnPortfolio(Long userId, Long groupId, UUID portfolioId);

	UUID postPortfolioParserights(UUID portfolioId, Long userId) throws JsonProcessingException, BusinessException;

	boolean isOwner(Long userId, UUID portfolioId);

	/**
	 * Has rights, whether ownership, or given by someone
	 */
	boolean hasRights(Long userId, UUID portfolioUuid);

	boolean changePortfolioOwner(UUID portfolioId, long newOwner);

	Portfolio changePortfolioConfiguration(UUID portfolioId, Boolean portfolioActive, Long userId)
			throws BusinessException;

	boolean rewritePortfolioContent(PortfolioDocument portfolio, UUID portfolioId, Long userId, Boolean portfolioActive)
			throws BusinessException, JsonProcessingException;

	String instanciatePortfolio(String portfolioId, String srccode, String tgtcode, Long id,
			int groupId, boolean copyshared, String groupname, boolean setOwner);

	String importZippedPortfolio(String path, String userName, InputStream fileInputStream, Long id, Long groupId,
								 String modelId, Long credentialSubstitutionId, boolean instantiate, String projectName)
			throws BusinessException, IOException;

	PortfolioList addPortfolio(PortfolioDocument portfolio, long userId, long groupId,
			UUID portfolioModelId, long substid, boolean parseRights, String projectName)
			throws BusinessException, JsonProcessingException;

	GroupRightInfoList getGroupRightsInfos(Long id, UUID portfolioId) throws BusinessException;

	String getRoleByPortfolio(String role, UUID portfolioId, Long userId);

	GroupInfoList getRolesByPortfolio(UUID portfolioId, Long userId);

	UUID copyPortfolio(UUID portfolioId, String srccode, String tgtcode, Long userId, boolean setOwner)
			throws Exception;

	void updateTime(UUID portfolioId);

	boolean updateTimeByNode(UUID portfolioId);

	List<Portfolio> getPortfolios(Long userId,
								  Long substid,
								  Boolean portfolioActive,
								  Boolean portfolioProject);
}
