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

	public List<CredentialGroupMembers> getByGroup(Long cgId) {
		String sql = "FROM CredentialGroupMembers cgm";
		sql += " WHERE cgm.id.credentialGroup.id = :cgId";
		TypedQuery<CredentialGroupMembers> q = em.createQuery(sql, CredentialGroupMembers.class);
		q.setParameter("cgId", cgId);
		return q.getResultList();
	}

	public List<CredentialGroupMembers> getByUser(Long userId) {
		String sql = "FROM CredentialGroupMembers cgm";
		sql += " LEFT JOIN FETCH cgm.id.credentialGroup cg";
		sql += " WHERE cgm.id.credential.id = :userId";
		TypedQuery<CredentialGroupMembers> q = em.createQuery(sql, CredentialGroupMembers.class);
		q.setParameter("userId", userId);
		List<CredentialGroupMembers> res = q.getResultList();
		return res;
	}

	@Override
	public Boolean deleteUserFromGroup(Long userId, Long cgId) {
		Boolean result = Boolean.FALSE;
		String sql = "FROM CredentialGroupMembers cgm";
		sql += " WHERE cgm.id.credentialGroup.id = :cgId";
		sql += " AND cgm.id.credential.id = :userId";
		TypedQuery<CredentialGroupMembers> q = em.createQuery(sql, CredentialGroupMembers.class);
		q.setParameter("cgId", cgId);
		q.setParameter("userId", userId);
		try {
			CredentialGroupMembers cgm = q.getSingleResult();
			em.remove(cgm);
			result = Boolean.TRUE;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
