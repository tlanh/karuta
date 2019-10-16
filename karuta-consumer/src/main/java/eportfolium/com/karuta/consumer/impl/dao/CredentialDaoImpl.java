package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.config.Settings;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.util.query.QueryBuilder;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.util.ValidateUtil;

/**
 * Home object implementation for domain model class Credential.
 * 
 * @see dao.Credential
 * @author Hibernate Tools
 */
@Repository
public class CredentialDaoImpl extends AbstractDaoImpl<Credential> implements CredentialDao {

	private static final Log log = LogFactory.getLog(CredentialDaoImpl.class);

	@PersistenceContext
	private EntityManager em;

	public CredentialDaoImpl() {
		super();
		setCls(Credential.class);
	}

	public boolean isAdmin(String uid) {
		if (!NumberUtils.isCreatable(uid)) {
			throw new IllegalArgumentException();
		} else if (PhpUtil.empty(NumberUtils.createLong(uid))) {
			throw new IllegalArgumentException();
		}
		return isAdmin(Long.valueOf(uid));
	}

	public boolean isAdmin(Long uid) {
		boolean status = false;
		String query = "SELECT c FROM Credential c";
		query += " WHERE c.id = :userid";
		query += " AND c.isAdmin = 1 ";
		Query q = em.createQuery(query);
		q.setParameter("userid", uid);
		try {
			q.getSingleResult();
			status = true;
		} catch (NoResultException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * Specific portfolio designer
	 * 
	 * @param userId
	 * @param nodeId
	 * @return
	 */
	public boolean isDesigner(Long userId, String nodeId) {
		return isDesigner(userId, UUID.fromString(nodeId));
	}

	/**
	 * Specific portfolio designer
	 * 
	 * @param userId
	 * @param nodeId
	 * @return
	 */
	public boolean isDesigner(Long userId, UUID nodeId) {
		boolean status = false;
		if (userId == null)
			return status;

		String query = "SELECT gu.id.credential.id FROM Node n";
		query += " INNER JOIN n.portfolio p";
		query += " INNER JOIN p.groupRightInfo gri WITH gri.label='designer'";
		query += " INNER JOIN gri.groupInfo gi";
		query += " INNER JOIN gi.groupUser gu WITH gu.id.credential.id = :userID";
		query += " WHERE n.id = :nodeUuid";
		try {
			Query q = em.createQuery(query);
			q.setParameter("userID", userId);
			q.setParameter("nodeUuid", nodeId);
			q.getSingleResult();
			status = true;
		} catch (NoResultException e) {
		}
		return status;
	}

	/**
	 * Requete permettant de recuperer toutes les informations dans la table
	 * credential pour un userid(utilisateur)particulier
	 * 
	 * @param userId
	 * @return
	 */
	public Credential getInfUser(Long userId) {
		Credential res = null;
		String sql = "SELECT c FROM credential c";
		sql += " LEFT JOIN c.credentialSubstitution cs";
		sql += " WHERE c.id = :userId";
		try {
			TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
			q.setParameter("userId", userId);
			res = q.getSingleResult();
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	public List<CredentialGroupMembers> getUsersByUserGroup(Long userGroupId) {
		String sql = "FROM CredentialGroupMembers";
		sql += " WHERE id.credentialGroup.id = :userGroupId";
		TypedQuery<CredentialGroupMembers> q = em.createQuery(sql, CredentialGroupMembers.class);
		q.setParameter("userGroupId", userGroupId);
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Credential> getUsers(Long userId, String username, String firstname, String lastname) {
		// Build query
		QueryBuilder builder = new QueryBuilder();
		builder.append("FROM Credential c");
		builder.append("LEFT JOIN FETCH c.credentialSubstitution cs");
		builder.appendLikeIgnoreCaseSkipEmpty("c.login", username);
		builder.appendLikeIgnoreCaseSkipEmpty("c.displayFirstname", firstname);
		builder.appendLikeIgnoreCaseSkipEmpty("cs.displayLastname", lastname);
		builder.append("ORDER BY c.id");
		Query q = builder.createQuery(em);
		List<Credential> l = q.getResultList();
		return l;
	}

	public List<Credential> getUsersByRole(Long userId, String portfolioUuid, String role) {
		String sql = "SELECT c FROM Credential c, GroupRightInfo gri, GroupInfo gi, GroupUser gu";
		sql += " WHERE c.id = gu.id.credential.id";
		sql += " AND gu.id.groupInfo.id = gi.id";
		sql += " AND gi.groupRightInfo.id = gri.id";
		sql += " AND gri.portfolio.id = :portfolioUuid";
		sql += " AND gri.label = :role";

		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("portfolioUuid", UUID.fromString(portfolioUuid));
		q.setParameter("role", role);
		return q.getResultList();
	}

	public String[] postCredentialFromXml(Integer userId, String username, String password, String substitute)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserUidByTokenAndLogin(String login, String token) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public int deleteCredential(int userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getListUsers(Long userId, String username, String firstname, String lastname) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserGroupByPortfolio(String portfolioUuid, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object putUser(int userId, String oAuthToken, String oAuthSecret) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postUser(String xmluser, int userId) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Object deleteUser(int userid, int userId1) {
		// TODO Auto-generated method stub
		return null;
	}

	public int deleteUsers(Integer userId, Integer userid2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean userExists(String login) {
		if (StringUtils.isEmpty(login)) {
			throw new IllegalArgumentException();
		}
		String sql = "SELECT login FROM Credential";
		sql += " WHERE login = :login";
		String result = null;
		try {
			TypedQuery<String> q = em.createQuery(sql, String.class);
			q.setParameter("login", login);
			result = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return result != null;
	}

	public String getEmailByLogin(String login) {
		String result = null;
		String buf = "SELECT c.email FROM Credential c";
		buf += " WHERE c.login = :login";
		TypedQuery<String> q = em.createQuery(buf, String.class);
		q.setParameter("login", login);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			log.error("getEmailByLogin failed", e);
		}
		return result;
	}

	/**
	 * Return userid from its username.
	 *
	 * @param string username username
	 * @return userid
	 */

	public Long getUserId(String username) {
		return getUserId(username, null);
	}

	/**
	 * Return userid from its username or email.
	 *
	 * @param string username username
	 * @param string email email is checked if username omitted
	 * @return userid
	 */
	public Long getUserId(String username, String email) {
		TypedQuery<Long> q;
		String sql;
		Long retval = null;

		if (StringUtils.isNotEmpty(username)) {
			sql = "SELECT id FROM Credential WHERE login = :login";
			q = em.createQuery(sql, Long.class);
			q.setParameter("login", username);
		} else if (StringUtils.isNotEmpty(email)) {
			sql = "SELECT id FROM Credential WHERE email = :email";
			q = em.createQuery(sql, Long.class);
			q.setParameter("email", email);
		} else
			return retval;

		try {
			retval = q.getSingleResult();
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	/**
	 * Return credential instance from its login
	 *
	 * @param string login login
	 * @return Credential instance
	 */
	public Credential getByLogin(String login) {
		return getByLogin(login, null);
	}

	/**
	 * Return credential instance from its login
	 *
	 * @param string login login
	 * @return Credential instance
	 */
	public Credential getUserByLogin(String login) {
		if (StringUtils.isEmpty(login)) {
			throw new IllegalArgumentException();
		}

		Credential res = null;
		String sql = "SELECT c FROM Credential c WHERE c.login = :login ";
		TypedQuery<Credential> query = em.createQuery(sql, Credential.class);
		query.setParameter("login", login);
		try {
			res = query.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Return credential instance from its login
	 *
	 * @param string login login
	 * @param string passwd Password is also checked if specified
	 * @return Credential instance
	 */
	public Credential getByLogin(String login, String passwd) {
		if (StringUtils.isEmpty(login)) {
			throw new IllegalArgumentException();
		}

		Credential result = null;
		String sql = "SELECT c FROM Credential c";
		sql += " WHERE c.login = :login";
		if (StringUtils.isNotEmpty(passwd)) {
			sql += " AND c.password = :passwd";
		}
		sql += " AND c.active = 1";
		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("login", login);
		if (StringUtils.isNotEmpty(passwd)) {
			q.setParameter("passwd", passwd);
		}
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			log.error("getByLogin failed", e);
		}
		return result;
	}

	/**
	 * Return credential instance from its e-mail
	 *
	 * @param string email e-mail
	 * @return Credential instance
	 */
	public Credential getByEmail(String email) {
		return getByEmail(email, null);
	}

	/**
	 * Return credential instance from its e-mail (optionnaly check password)
	 *
	 * @param string email e-mail
	 * @param string passwd Password is also checked if specified
	 * @return Credential instance
	 */
	public Credential getByEmail(String email, String passwd) {
		if (!ValidateUtil.isEmail(email) || (passwd != null && !ValidateUtil.isPasswd(passwd))) {
			throw new IllegalArgumentException();
		}

		Credential result = null;
		String sql = "SELECT c.login FROM " + Settings._DB_PREFIX_ + "Credential c";
		sql += " WHERE c.email = :email";
		sql += " AND c.active = 1";
		if (StringUtils.isNotEmpty(passwd)) {
			sql += " AND c.password = :passwd";
		}
		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("email", email);
		if (StringUtils.isNotEmpty(passwd)) {
			q.setParameter("passwd", passwd);
		}
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			log.error("getByEmail failed", e);
		}
		return result;
	}

	public String getUserUid(String login) {
		TypedQuery<Long> q;
		Long res = 0L;

		String sql = "SELECT id FROM Credential";
		sql += " WHERE login = :login ";
		q = em.createQuery(sql, Long.class);
		q.setParameter("login", login);
		try {
			res = q.getSingleResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res.toString();
	}

	public Credential getUser(Long userId) {
		log.debug("getting PsProduct instance with id: " + userId);
		try {
			Credential instance = em.find(Credential.class, userId);
			log.debug("get successful");
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public Long getPublicUid() {
		Long publicId = null;
		// Fetching 'sys_public' userid
		String sql = "SELECT id FROM Credential";
		sql += " WHERE login='sys_public'";
		TypedQuery<Long> q = em.createQuery(sql, Long.class);
		try {
			publicId = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return publicId;
	}

	public int updateCredentialToken(Long userId, String token) {
		int result = 0;
		String sql = "SELECT c FROM Credential c WHERE c.id  = :userId ";
		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("userId", userId);

		try {
			Credential cred = q.getSingleResult();
			cred.setToken(token);
			merge(cred);

		} catch (Exception ex) {
			ex.printStackTrace();
			result = 1;
		}
		return result;
	}

	public boolean isCreator(Long userId) {
		boolean status = false;
		if (userId == null)
			return status;

		TypedQuery<Long> query = null;
		String sql = "SELECT c.id FROM Credential c";
		sql += " WHERE c.id = :userId";
		sql += " AND c.isDesigner = 1";
		query = em.createQuery(sql, Long.class);
		query.setParameter("userId", userId);
		try {
			query.getSingleResult();
			status = true;
		} catch (NoResultException e) {
			e.printStackTrace();
			status = false;
		}
		return status;
	}

	public Credential getActiveByUserId(Long userID) {
		if (!ValidateUtil.isUnsignedId(userID.intValue())) {
			log.error("Fatal Error : userID is not correct");
			throw new RuntimeException();
		}
		Credential result = null;
		String sql = "SELECT cr FROM Credential cr";
		sql += " WHERE cr.id = :userID";
		sql += " AND c.active = 1";
		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("customerID", userID);

		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			log.error("getActiveByUserId failed", e);
		}
		return result;
	}

	public Credential getByLogin(String login, boolean isAdmin) {
		if (StringUtils.isEmpty(login)) {
			throw new IllegalArgumentException();
		}
		Credential cr = null;
		String sql = "SELECT c FROM Credential c";
		sql += " WHERE c.login = :login";
		sql += " AND c.isAdmin = :isAdmin";
		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("login", login);
		q.setParameter("isAdmin", BooleanUtils.toInteger(isAdmin));
		try {
			cr = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return cr;
	}

	public boolean isUserMemberOfRole(Long userId, Long roleId) {
		boolean status = false;
		String sql = "SELECT gu.id.credential.id FROM GroupUser gu";
		sql += " INNER JOIN gu.id.groupInfo gi";
		sql += " WHERE gu.id.credential.id = :userid";
		sql += " AND gi.groupRightInfo.id = :grid";
		TypedQuery<Long> q = em.createQuery(sql, Long.class);
		q.setParameter("userid", userId);
		q.setParameter("grid", roleId);
		try {
			q.getSingleResult();
			status = true;
		} catch (NoResultException e) {
			// TODO: handle exception
		}
		return status;
	}

	public String getLoginById(Long userId) {
		String result = null;
		String buf = "SELECT c.login FROM Credential c";
		buf += " WHERE c.id = :userId";
		TypedQuery<String> q = em.createQuery(buf, String.class);
		q.setParameter("userId", userId);
		try {
			result = q.getSingleResult();
		} catch (NoResultException e) {
			log.error("getLoginById failed", e);
		}
		return result;
	}

}
