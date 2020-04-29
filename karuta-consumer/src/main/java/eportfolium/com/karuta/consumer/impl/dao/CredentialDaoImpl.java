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

package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.model.bean.Credential;

@Repository
public class CredentialDaoImpl extends AbstractDaoImpl<Credential> implements CredentialDao {

	private static final Log log = LogFactory.getLog(CredentialDaoImpl.class);

	public CredentialDaoImpl() {
		super();
		setCls(Credential.class);
	}

	public boolean isAdmin(Long userId) {
		boolean status = false;
		String query = "FROM Credential c";
		query += " WHERE c.id = :userid";
		query += " AND c.isAdmin=1";
		Query q = em.createQuery(query);
		q.setParameter("userid", userId);
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
	@Override
	public boolean isDesigner(Long userId, String nodeId) {
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
			q.setParameter("nodeUuid", UUID.fromString(nodeId));
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

	@SuppressWarnings("unchecked")
	public List<Credential> getUsers(String username, String firstname, String lastname) {

		String sql = "SELECT c FROM Credential c";
		sql += " LEFT JOIN FETCH c.credentialSubstitution cs";
		sql += " WHERE lower(c.login) LIKE :username";
		sql += " AND lower(c.displayFirstname) LIKE :firstname";
		sql += " AND lower(cs.displayLastname) LIKE :lastname";
		sql += "ORDER BY c.id";
		Query q = em.createQuery(sql);

		q.setParameter("username", username);
		q.setParameter("firstname", firstname);
		q.setParameter("lastname", lastname);

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
	 * Return userid from its login.
	 *
	 * @param string userLogin login
	 * @return userid
	 */

	public Long getUserId(String userLogin) {
		return getUserId(userLogin, null);
	}

	/**
	 * Return userid from its login or email.
	 *
	 * @param string userLogin login
	 * @param string email email is checked if login omitted
	 * @return userid
	 */
	public Long getUserId(String userLogin, String email) {
		TypedQuery<Long> q;
		String sql;
		Long retval = null;

		if (StringUtils.isNotEmpty(userLogin)) {
			sql = "SELECT id FROM Credential WHERE login = :login";
			q = em.createQuery(sql, Long.class);
			q.setParameter("login", userLogin);
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
		if (!EmailValidator.getInstance().isValid(email) || (passwd != null
				&& (passwd.length() >= PASSWORD_LENGTH && passwd.length() < 255))) {
			throw new IllegalArgumentException();
		}

		Credential result = null;
		String sql = "SELECT c FROM Credential c";
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
		if (userId == null) {
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

	public boolean userHasRole(Long userId, Long roleId) {
		boolean status = false;
		String sql = "FROM GroupUser gu";
		sql += " INNER JOIN gu.id.groupInfo gi";
		sql += " WHERE gu.id.credential.id = :userid";
		sql += " AND gi.groupRightInfo.id = :grid";
		Query q = em.createQuery(sql);
		q.setParameter("userid", userId);
		q.setParameter("grid", roleId);
		try {
			q.getSingleResult();
			status = true;
		} catch (NoResultException e) {
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
