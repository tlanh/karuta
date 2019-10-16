package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * @author mlengagne
 *
 */
/**
 * @author mlengagne
 *
 */
public interface GroupRightsDao {

	void persist(GroupRights transientInstance);

	GroupRights merge(GroupRights detachedInstance);

	void remove(GroupRights persistentInstance);

	/**
	 * @return Tous les droits disponibles
	 */
	List<GroupRights> findAll();

	void removeAll();

	GroupRights findById(Serializable id) throws DoesNotExistException;

	List<GroupRights> getRightsById(String uuid);

	List<GroupRights> getRightsById(UUID uuid);

	List<GroupRights> getRightsByGroupId(Long groupId);

	List<GroupRights> getRightsByIdAndGroup(String uuid, Long groupId);

	GroupRights getPublicRightsByGroupId(String uuid, Long groupId);

	GroupRights getPublicRightsByGroupId(UUID uuid, Long groupId);

	GroupRights getRightsByGrid(String uuid, Long groupId);

	GroupRights getRightsByGrid(UUID uuid, Long grid);

	GroupRights getPublicRightsByUserId(UUID uuid, Long userId);

	GroupRights getPublicRightsByUserId(String uuid, Long userId);

	List<GroupRights> getRightsByPortfolio(UUID uuid, UUID portfolioUuid);

	List<GroupRights> getRightsByPortfolio(String uuid, String portfolioUuid);

	/**
	 * Regarde si l'utilisateur à un droit sur ce noeud dans l'un des groupes du
	 * portfolio. <br>
	 * 
	 * Pas de sélection de groupe donc le noeud pourrait etre référencé dans
	 * plusieurs groupes. On retourne le premier de la liste.
	 * 
	 * @param uuid
	 * @param userId
	 * @return
	 */
	GroupRights getRightsByIdAndUser(String uuid, Long userId);

	/**
	 * Regarde si l'utilisateur à un droit sur ce noeud dans l'un des groupes du
	 * portfolio. <br>
	 * 
	 * Pas de sélection de groupe donc le noeud pourrait etre référencé dans
	 * plusieurs groupes. On retourne le premier de la liste.
	 * 
	 * @param uuid
	 * @param userId
	 * @return
	 */
	GroupRights getRightsByIdAndUser(UUID uuid, Long userId);

	GroupRights getRightsByUserAndGroup(String uuid, Long userId, Long groupId);

	GroupRights getSpecificRightsForUser(String uuid, Long userId);

	GroupRights getRightsByIdAndLabel(String uuid, String label);

	void removeById(UUID groupRightsId) throws Exception;

	List<GroupRights> getByPortfolioAndGridList(String portfolioUuid, Long grid1, Long grid2, Long grid3);

	/**
	 * Récupère les droits donnés par le portfolio à 'tout le monde' et les droits
	 * donnés specifiquement à un utilisateur
	 * 
	 * @param portfolioUuid
	 * @param userLogin
	 * @param groupId
	 * @return
	 */
	List<GroupRights> getPortfolioAndUserRights(UUID portfolioUuid, String userLogin, Long groupId);

	Long getUserIdFromNode(String uuid);

	void updateNodeRights(String nodeUuid, List<String> asList, String macroName);

	void updateNodeRights(UUID nodeUuid, List<String> labels, String macroName);

	void updateNodeRights(UUID nodeUuid, List<String> labels);

	void updateNodeRights(String nodeUuid, List<String> asList);

	boolean updateAllNodesRights(List<Node> nodes, Long grid);

	boolean updateNodesRights(List<Node> nodes, Long grid);

	ResultSet findAll(String table, Connection con);

	ResultSet getMysqlGroupRights(Connection con) throws SQLException;
}