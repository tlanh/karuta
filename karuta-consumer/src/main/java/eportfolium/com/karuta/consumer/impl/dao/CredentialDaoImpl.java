package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.persistence.NoResultException;
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
		String query = "FROM Credential c";
		query += " WHERE c.id = :userid";
		query += " AND c.isAdmin=1";
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
		boolean status = false;
		if (userId == null)
			return status;

		String query = "SELECT gu.id.credentialId FROM Node n";
		query += " INNER JOIN n.portfolio p";
		query += " INNER JOIN p.groupRightInfo gri WITH gri.label='designer'";
		query += " INNER JOIN gri.groupInfo gi";
		query += " INNER JOIN gi.groupUser gu";
//		query += " WHERE gu.id.credentialId = :userID";
		query += " WHERE n.id = :nodeUuid";
		try {
			Query q = em.createQuery(query);
//			q.setParameter("userID", userId);
			q.setParameter("nodeUuid", nodeId);
			q.getSingleResult();
			status = true;
		} catch (NoResultException e) {
		}
		return status;
	}

	@Override
	public Credential getUserInfos(Long userId) {
		Credential res = null;
		String sql = "SELECT c FROM Credential c";
		sql += " LEFT JOIN FETCH c.credentialSubstitution cs";
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
		String sql = "FROM CredentialGroupMembers cgm";
		sql += " WHERE cgm.id.credentialGroupId = :userGroupId";
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
		String sql = "SELECT c FROM Credential c";
		sql += " INNER JOIN c.groups gu";
		sql += " INNER JOIN gu.groupInfo gi";
		sql += " INNER JOIN gi.groupRightInfo gri";
		sql += " INNER JOIN gri.portfolio p";
		sql += " WHERE gri.label = :role";
		sql += " AND p.id = :portfolioUuid";

//		sql += " WHERE c.id = gu.id.credentialId";
//		sql += " AND gu.id.groupInfoId = gi.id";
//		sql += " AND gi.groupRightInfo.id = gri.id";
//		sql += " AND gri.portfolio.id = :portfolioUuid";
//		sql += " AND gri.label = :role";

		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		q.setParameter("role", role);
		return q.getResultList();
	}

	public String[] postCredentialFromXml(Integer userId, String username, String password, String substitute)
			throws BusinessException {
		// TODO Auto-generated method stub
		return null;
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
		String sql = "SELECT c FROM " + Settings._DB_PREFIX_ + "Credential c";
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

	public Credential getActiveByUserId(Long userId) {
		if (userId == null || !ValidateUtil.isUnsignedId(userId.intValue())) {
			log.error("Fatal Error : userId is not correct");
			throw new IllegalArgumentException();
		}
		Credential result = null;
		String sql = "SELECT cr FROM Credential cr";
		sql += " WHERE cr.id = :userId";
		sql += " AND cr.active = 1";
		TypedQuery<Credential> q = em.createQuery(sql, Credential.class);
		q.setParameter("userId", userId);

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
		String sql = "FROM GroupUser gu";
		sql += " WHERE gu.id.credentialId = :userid";
		sql += " AND gu.groupInfo.groupRightInfo.id = :grid";
		Query q = em.createQuery(sql);
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

	/***************************************************************************************************************/
	public ResultSet getMysqlUsers(Connection con, String username, String firstname, String lastname) {
		PreparedStatement st;
		String sql;
		try {
			// On récupère d'abord les informations dans la table structures
			sql = "SELECT * FROM credential c " + "LEFT JOIN credential_substitution cs " + "ON c.userid=cs.userid ";
			int count = 0;
			if (username != null)
				count++;
			if (firstname != null)
				count++;
			if (lastname != null)
				count++;
			if (count > 0) {
				sql += "WHERE ";
				if (username != null) {
					sql += "login LIKE ? ";
					if (count > 1)
						sql += "AND ";
				}
				if (firstname != null) {
					sql += "display_firstname LIKE ? ";
					if (count > 1)
						sql += "AND ";
				}
				if (lastname != null) {
					sql += "display_lastname LIKE ? ";
				}
			}
			sql += "ORDER BY c.userid";
			st = con.prepareStatement(sql);

			int start = 1;
			if (username != null) {
				st.setString(start, "%" + username + "%");
				start++;
			}
			if (firstname != null) {
				st.setString(start, "%" + firstname + "%");
				start++;
			}
			if (lastname != null) {
				st.setString(start, "%" + lastname + "%");
				start++;
			}

			return st.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
