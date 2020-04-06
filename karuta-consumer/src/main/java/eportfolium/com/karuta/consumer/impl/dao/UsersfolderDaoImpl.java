package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.UsersfolderDao;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.Usersfolder;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.util.Tools;

/**
 * Home object implementation for domain model class Usersfolder.
 * 
 * @see dao.Usersfolder
 */
@Repository
public class UsersfolderDaoImpl extends AbstractDaoImpl<Usersfolder> implements UsersfolderDao {

	private static final Log log = LogFactory.getLog(UsersfolderDaoImpl.class);

	public UsersfolderDaoImpl() {
		super();
		setCls(Usersfolder.class);
	}

	/** Method: getFolder
	 * @param folderId
	 * @return
	 * TODO
	 */
	@Override
	public Usersfolder getFolder(Long folderId) {
		try {
			Usersfolder instance = em.find(Usersfolder.class, folderId);
			return instance;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	/** Method: getFolderByUser
	 * @param userId
	 * @return
	 * TODO
	 */
	@Override
	public Usersfolder getFolderByUser(Long userId) {
		try {
			Credential user = em.find(Credential.class, userId);
	        if (user != null)
	        	return user.getFolder();
	        else return null;
		} catch (RuntimeException re) {
			throw re;
		}
	}

	/** Method: getUsers
	 * @param folderId
	 * @return
	 * TODO
	 */
	@Override
	public List<Credential> getUsers(Long folderId) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Method: getFolders
	 * @param folderId
	 * @return
	 * TODO
	 */
	@Override
	public List<Usersfolder> getFolders(Long folderId) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Method: addUserInFolder
	 * @param folderId
	 * @param userId
	 * @return
	 * TODO
	 */
	@Override
	public int addUserInFolder(Long folderId, Long userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** Method: moveUser
	 * @param userId
	 * @param srcFolderId
	 * @param targetFolderId
	 * @return
	 * TODO
	 */
	@Override
	public int moveUser(Long userId, Long srcFolderId, Long targetFolderId) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** Method: moveFolder
	 * @param folderId
	 * @param srcFolderId
	 * @param targetFolderId
	 * @return
	 * TODO
	 */
	@Override
	public int moveFolder(Long folderId, Long srcFolderId, Long targetFolderId) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** Method: activateFolder
	 * @param folderId
	 * @return
	 * TODO
	 */
	@Override
	public int activateFolder(Long folderId) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** Method: deactivateFolder
	 * @param folderId
	 * @return
	 * TODO
	 */
	@Override
	public int deactivateFolder(Long folderId) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** Method: removeAll
	 * 
	 * TODO
	 */
	@Override
	public void removeAll() {
		// TODO Auto-generated method stub
		super.removeAll();
	}

	/** Method: remove
	 * @param entity
	 * TODO
	 */
	@Override
	public void remove(Usersfolder entity) {
		// TODO Auto-generated method stub
		super.remove(entity);
	}

	/** Method: removeById
	 * @param id
	 * @throws DoesNotExistException
	 * TODO
	 */
	@Override
	public void removeById(Serializable id) throws DoesNotExistException {
		// TODO Auto-generated method stub
		super.removeById(id);
	}

}
