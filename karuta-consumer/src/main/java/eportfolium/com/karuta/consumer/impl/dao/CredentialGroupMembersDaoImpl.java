package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

	@PersistenceContext
	private EntityManager em;

	public CredentialGroupMembersDaoImpl() {
		super();
		setCls(CredentialGroupMembers.class);
	}

	public boolean isUserMemberOfGroup(int userId, int groupId) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getRoleUser(int userId, int userid2) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

	public List<CredentialGroupMembers> getUserGroupList(Long userId) {
		return null;
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

	public int getGroupByGroupLabel(String groupLabel, int userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getUsersByUserGroup(int userGroupId, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGroupsByRole(int userId, String portfolioUuid, String role) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGroupsPortfolio(String portfolioUuid, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getRoleByNode(int userId, String nodeUuid, String role) {
		// TODO Auto-generated method stub
		return null;
	}

	public Boolean putUserGroupLabel(Integer user, int siteGroupId, String label) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer putUserGroup(String siteGroupId, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public Boolean putUserInUserGroup(int user, int siteGroupId, int currentUid) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object postGroup(String xmlgroup, int userId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean postGroupsUsers(int user, int userId, int groupId) {
		// TODO Auto-generated method stub
		return false;
	}

	public int postUserGroup(String label, int userid) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Boolean deleteUsersFromUserGroups(Long userId, Long usersgroupId) {
		Boolean result = Boolean.FALSE;
		String sql = "SELECT cg FROM CredentialGroupMembers cgm";
		sql += " WHERE cgm.id.credentialGroup.id = :usersgroupId";
		sql += " AND cgm.id.credential.id = :userId";

		try {
			List<CredentialGroupMembers> cgmList = em.createQuery(sql, CredentialGroupMembers.class).getResultList();
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
