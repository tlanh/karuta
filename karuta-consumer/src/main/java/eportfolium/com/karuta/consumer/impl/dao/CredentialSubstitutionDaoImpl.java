package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.CredentialSubstitutionDao;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

/**
 * Home object implementation for domain model class CredentialSubstitution.
 * 
 * @see dao.CredentialSubstitution
 * @author Hibernate Tools
 */
@Repository
public class CredentialSubstitutionDaoImpl extends AbstractDaoImpl<CredentialSubstitution>
		implements CredentialSubstitutionDao {

	@PersistenceContext
	private EntityManager entityManager;

	public CredentialSubstitutionDaoImpl() {
		super();
		setCls(CredentialSubstitution.class);
	}

	/**
	 * Specific lenient substitution rule
	 */
	public CredentialSubstitution getSubstitutionRule(Long userId, Long csId, String type)
			throws DoesNotExistException {
		CredentialSubstitution cs = null;
		String sql = "SELECT cs FROM CredentialSubstitution cs";
		sql += " WHERE cs.id.credential.id = :userId";
		sql += " AND cs.id.id = :id";
		sql += " AND cs.id.type = :type";
		TypedQuery<CredentialSubstitution> q = entityManager.createQuery(sql, CredentialSubstitution.class);
		q.setParameter("userid", userId);
		q.setParameter("id", csId);
		q.setParameter("type", "USER");
		try {
			cs = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return cs;
	}

	/**
	 * As specific user
	 */
	@Override
	public Long getSubuidFromUserType(final String login, final Long userId) {
		Long subuid = null;
		String sql = "SELECT c.id FROM Credential c, CredentialSubstitution cs";
		sql += " INNER JOIN cs.id.credential cr";
		sql += " WHERE c.id = cs.id.id";
		sql += " AND c.login = :login";
		sql += " AND cr.id = :userId";
		sql += " AND cs.type = 'USER' "; // As specific user
		TypedQuery<Long> q = entityManager.createQuery(sql, Long.class);
		q.setParameter("login", login);
		q.setParameter("userId", userId);
		try {
			subuid = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return subuid;
	}

	public Long getSubuidFromGroupType(final String login, final Long userId) {
		Long subuid = null;
		String sql = "SELECT c.id FROM Credential c, CredentialSubstitution cs, GroupUser gu";
		sql += " WHERE c.id = gu.id.credential.id";
		sql += " AND gu.id.groupInfo.id = cs.id.id";
		sql += " AND c.login = :login";
		sql += " AND cs.id.credential.id = :userId";
		sql += " AND cs.type = 'GROUP'";
		TypedQuery<Long> q = entityManager.createQuery(sql, Long.class);
		q.setParameter("login", login);
		q.setParameter("userId", userId);
		try {
			subuid = q.getSingleResult();
		} catch (NoResultException e) {
		}
		return subuid;
	}

}
