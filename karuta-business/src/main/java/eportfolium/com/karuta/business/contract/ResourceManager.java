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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.MimeType;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface ResourceManager {

	String getResource(MimeType outMimeType, String nodeParentUuid, Long userId, Long groupId) throws BusinessException;

	String getResource(String nodeUuid);

	String getResource(UUID nodeUuid);

	String getResources(MimeType outMimeType, String portfolioUuid, Long userId, Long groupId) throws Exception;

	Integer changeResource(MimeType inMimeType, String nodeParentUuid, String in, Long userId, Long groupId)
			throws BusinessException, Exception;

	String addResource(MimeType inMimeType, String nodeParentUuid, String in, Long userId, Long groupId)
			throws BusinessException, Exception;

	void removeResource(String resourceUuid, Long userId, Long groupId) throws DoesNotExistException, BusinessException;

	void changeResourceByXsiType(String nodeUuid, String xsiType, String content, Long userId) throws Exception;

	Map<String, String> transferResourceTable(Connection con, Map<Long, Long> userIds) throws SQLException;

	void removeResources();

}
