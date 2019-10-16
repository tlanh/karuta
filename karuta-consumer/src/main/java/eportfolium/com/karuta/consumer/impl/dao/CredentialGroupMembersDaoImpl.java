package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupMembersDao;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;

/**
 * Home object implementation for domain model class CredentialGroupMembers.
 * 
 * @see CredentialGroupMembersDao.CredentialGroupMembers
 * @author Hibernate Tools
 */
@Repository
public class CredentialGroupMembersDaoImpl extends AbstractDaoImpl<CredentialGroupMembers>
		implements CredentialGroupMembersDao {

	public CredentialGroupMembersDaoImpl() {
		super();
		setCls(CredentialGroupMembers.class);
	}

	public List<CredentialGroupMembers> getGroupByUser(Long userId) {
		String sql = "SELECT cgm FROM CredentialGroupMembers cgm";
		sql += " LEFT JOIN FETCH cgm.id.credentialGroup cg";
		sql += " WHERE cgm.id.credential.id = :userId";
		TypedQuery<CredentialGroupMembers> q = em.createQuery(sql, CredentialGroupMembers.class);
		q.setParameter("userId", userId);
		List<CredentialGroupMembers> res = q.getResultList();
		return res;
	}

	@Override
	public Boolean deleteUsersFromUserGroups(Long userId, Long cgId) {
		Boolean result = Boolean.FALSE;
		String sql = "SELECT cg FROM CredentialGroupMembers cgm";
		sql += " WHERE cgm.id.credentialGroup.id = :cgId";
		sql += " AND cgm.id.credential.id = :userId";
		TypedQuery<CredentialGroupMembers> q = em.createQuery(sql, CredentialGroupMembers.class);
		q.setParameter("cgId", cgId);
		q.setParameter("userId", userId);
		List<CredentialGroupMembers> cgmList = q.getResultList();
		try {
			for (CredentialGroupMembers cgm : cgmList) {
				em.remove(cgm);
			}
			result = Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
