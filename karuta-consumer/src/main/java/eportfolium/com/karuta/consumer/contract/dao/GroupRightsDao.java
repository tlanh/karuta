package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.activation.MimeType;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface GroupRightsDao {

	void persist(GroupRights transientInstance);

	void remove(GroupRights persistentInstance);

	GroupRights merge(GroupRights detachedInstance);

	GroupRights findById(Serializable id) throws DoesNotExistException;

	List<GroupRights> getGroupRights(Long groupId) throws Exception;

	GroupRights getPublicRightsByUserId(Long userId, UUID nodeUUID);

	GroupRights getPublicRightsByGroupId(String nodeUuid, Long groupId);

	GroupRights getPublicRightsByGroupId(UUID nodeUuid, Long groupId);

	GroupRights getRightsByGrid(String nodeUuid, Long groupId);

	GroupRights getRightsByGrid(UUID nodeUuid, Long grid);

	List<GroupRights> getRightsByPortfolio(UUID nodeUuid, UUID portfolioUuid);

	List<GroupRights> getRightsByPortfolio(String nodeUuid, String portfolioUuid);

	GroupRights getRightsFromGroups(String nodeUuid, Long userId);

	GroupRights getRightsFromGroups(UUID nodeUuid, Long userId);

	GroupRights getRightsByGroupId(UUID nodeUuid, Long userId, Long groupId);

	GroupRights getRightsByGroupId(String nodeUuid, Long userId, Long groupId);

	GroupRights getSpecificRightsForUser(String nodeUuid, Long userId);

	GroupRights getSpecificRightsForUser(UUID nodeUuid, Long userId);

	/**
	 * Ajout des droits du portfolio dans group_right_info, group_rights
	 * 
	 * @param label
	 * @param uuid
	 * @param droit
	 * @param portfolioUuid
	 * @param userId
	 * @return
	 */
	boolean postGroupRight(String label, String uuid, String droit, String portfolioUuid, Long userId);

	// ---------------------------------------------------------------------------------------------------------------------------

	String getGroupRightsInfos(int userId, String portfolioId) throws SQLException;

	String getRolePortfolio(MimeType mimeType, String role, String portfolioId, int userId) throws SQLException;

	String getRole(MimeType mimeType, int grid, int userId) throws SQLException;

	Object putRole(String xmlRole, int userId, int roleId) throws Exception;

	String postRoleUser(int userId, int grid, Integer userid2) throws SQLException;

	boolean postNodeRight(int userId, String nodeUuid) throws Exception;

	boolean postRightGroup(int groupRightId, int groupId, Integer userId);

	boolean setPublicState(int userId, String portfolio, boolean isPublic);

	@Deprecated
	int postShareGroup(String portfolio, int user, Integer userId, String write);

	int deleteShareGroup(String portfolio, Integer userId);

	int deleteSharePerson(String portfolio, int user, Integer userId);

	Object deleteGroupRights(Integer groupId, Integer groupRightId, Integer userId);

}