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

public interface PortfolioManager extends BaseManager {

	int addPortfolioInGroup(UUID portfolioUuid, Long portfolioGroupId, String label);

	Long addPortfolioGroup(String groupname, String type, Long parentId);

	void removePortfolio(UUID portfolioId, Long userId);

	boolean removePortfolioFromPortfolioGroups(UUID portfolioId, Long portfolioGroupId);

	boolean removePortfolioGroups(Long portfolioGroupId);

	PortfolioDocument getPortfolio(UUID portfolioId,
								   Long userId,
								   Integer cutoff) throws BusinessException, JsonProcessingException;

	String getZippedPortfolio(PortfolioDocument portfolio) throws IOException;

	PortfolioDocument getPortfolioByCode(String portfolioCode,
										 Long userId,
										 boolean resources) throws BusinessException, JsonProcessingException;

	PortfolioGroupDocument getPortfoliosByPortfolioGroup(Long portfolioGroupId);

	Long getPortfolioGroupIdFromLabel(String groupLabel);

	String getPortfolioGroupList();

	PortfolioGroupList getPortfolioGroupListFromPortfolio(UUID portfolioId);

	String getPortfolios(long userId, Boolean active, long substid, boolean specialProject, String portfolioCode);

	PortfolioList getPortfolioShared(Long userId);

	GroupRights getRightsOnPortfolio(Long userId, UUID portfolioId);

	UUID postPortfolioParserights(UUID portfolioId, Long userId) throws JsonProcessingException, BusinessException;

	boolean isOwner(Long userId, UUID portfolioId);

	/**
	 * Has rights, whether ownership, or given by someone
	 */
	boolean hasRights(Long userId, UUID portfolioUuid);

	boolean changePortfolioOwner(UUID portfolioId, long newOwner);

	void changePortfolioConfiguration(UUID portfolioId, Boolean portfolioActive);

	boolean rewritePortfolioContent(PortfolioDocument portfolio, UUID portfolioId, Long userId, Boolean portfolioActive)
			throws BusinessException, JsonProcessingException;

	String instanciatePortfolio(String portfolioId, String srccode, String tgtcode, Long id,
			boolean copyshared, String groupname, boolean setOwner);

	String importPortfolio(String path, InputStream fileInputStream, Long id, boolean instantiate, String projectName)
			throws BusinessException, IOException;

	PortfolioList addPortfolio(PortfolioDocument portfolio, long userId, UUID portfolioModelId, boolean parseRights, String projectName)
			throws BusinessException, JsonProcessingException;

	GroupRightInfoList getGroupRightsInfos(UUID portfolioId);

	String getRoleByPortfolio(String role, UUID portfolioId);

	GroupInfoList getRolesByPortfolio(UUID portfolioId, Long userId);

	UUID copyPortfolio(UUID portfolioId, String srccode, String tgtcode, Long userId, boolean setOwner)
			throws BusinessException;

	void updateTime(UUID portfolioId);

	void updateTimeByNode(UUID nodeId);

	List<Portfolio> getPortfolios(Long userId,
								  Long substid,
								  Boolean portfolioActive,
								  boolean specialProject,
								  String portfolioCode);
}
