package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.Usersfolder;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface UsersfolderDao {

	/**
	 * Get a folder by id 
	 * 
	 * @param folderId
	 * @return
	 */
	Usersfolder getFolder(Long folderId);

	/**
	 * Get the parent folder of a user 
	 * 
	 * @param userId
	 * @return
	 */
	Usersfolder getFolderByUser(Long userId);

	/**
	 * Get users in the parent folder folderId 
	 * 
	 * @param folderId
	 * @return
	 */
	List<Credential> getUsers(Long folderId);

	/**
	 * Get folders in the parent folder folderId 
	 * 
	 * @param folderId
	 * @return
	 */
	List<Usersfolder> getFolders(Long folderId);

	/**
	 * Place a user in a folder 
	 * 
	 * @param userId
	 * @param folderId
	 * @return
	 */
	int addUserInFolder(Long folderId, Long userId);

	/**
	 * Move a user from a folder to another folder 
	 * 
	 * @param userId
	 * @param srcFolderId
	 * @param targetFolderId
	 * @return
	 */
	int moveUser(Long userId, Long srcFolderId, Long targetFolderId);

	/**
	 * Move a folder from a folder to another folder 
	 * 
	 * @param folderId
	 * @param srcFolderId
	 * @param targetFolderId
	 * @return
	 */
	int moveFolder(Long folderId, Long srcFolderId, Long targetFolderId);

	/**
	 * Active a folder 
	 * 
	 * @param folderId
	 * @return
	 */
	int activateFolder(Long folderId);

	/**
	 * Deactive a folder 
	 * 
	 * @param folderId
	 * @return
	 */
	int deactivateFolder(Long folderId);

	void persist(Usersfolder transientInstance);

	void remove(Usersfolder persistentInstance);

	Usersfolder merge(Usersfolder detachedInstance);

	Usersfolder findById(Serializable id) throws DoesNotExistException;

	void removeById(final Serializable id) throws DoesNotExistException;

	List<Usersfolder> findAll();

	ResultSet findAll(String table, Connection con);

	void removeAll();

}