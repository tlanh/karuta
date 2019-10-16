package eportfolium.com.karuta.business.contract;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.exception.BusinessException;

public interface GroupManager {

	String addGroup(String name);

	String getGroupByUser(Long userId);

	String getGroupsByUser(Long id);

	String getGroupsByRole(String portfolioUuid, String role);

	boolean postNotifyRoles(Long userId, String portfolioId, String uuid, String notify) throws BusinessException;

	boolean postNotifyRoles(Long userId, UUID portfolioId, UUID uuid, String notify) throws BusinessException;

	boolean setPublicState(Long userId, String portfolioUuid, boolean isPublic) throws BusinessException;

	Long addUserGroup(String groupName) throws Exception;

	boolean renameUserGroup(Long groupId, String newName);

	CredentialGroup getGroupByName(String name);

	String getUserGroupList();

	/**
	 * Get groups from a user
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	String getUserGroups(Long userId) throws Exception;

	Boolean deleteUsersGroups(Long userGroupId);

	String addUserGroup(String xmlgroup, Long id) throws BusinessException, Exception;

	void changeUserGroup(Long groupRightId, Long groupId, Long userId) throws BusinessException;

	String getGroupRights(Long userId, Long groupId) throws Exception;

	/**
	 * Ajout des droits du portfolio dans GroupRightInfo et GroupRights
	 * 
	 * @param label
	 * @param uuid
	 * @param droit
	 * @param portfolioUuid
	 * @param userId
	 * @return
	 */
	boolean addGroupRights(String label, String nodeUuid, String droit, String portfolioUuid, Long userId);

	void removeRights(long groupId, Long id) throws BusinessException;

	void transferGroupGroupTable(Connection con, Map<Long, Long> giIds) throws SQLException;

	Map<Long, Long> transferGroupRightInfoTable(Connection con, Map<String, String> portIds) throws SQLException;

	Map<Long, Long> transferGroupInfoTable(Connection con, Map<Long, Long> griIds) throws SQLException;

	void transferGroupRightsTable(Connection con, Map<Long, Long> griIds) throws SQLException;

	void transferGroupUserTable(Connection con, Map<Long, Long> giIds, Map<Long, Long> userIds) throws SQLException;

	Map<Long, Long> transferCredentialGroupTable(Connection con) throws SQLException;

	void removeGroups();

}
