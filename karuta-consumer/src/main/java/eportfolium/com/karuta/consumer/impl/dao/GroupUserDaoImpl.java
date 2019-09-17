package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.GroupUserId;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.util.Tools;

/**
 * Home object implementation for domain model class GroupUser.
 * 
 * @see dao.GroupUser
 * @author Hibernate Tools
 */
@Repository
public class GroupUserDaoImpl extends AbstractDaoImpl<GroupUser> implements GroupUserDao {

	@PersistenceContext
	private EntityManager em;

	public GroupUserDaoImpl() {
		super();
		setCls(GroupUser.class);
	}

	public List<GroupUser> getUserGroups() {
		String sql = "SELECT gu FROM GroupUser gu";
		sql += " INNER JOIN FETCH gu.id.credential cr";
		sql += " INNER JOIN FETCH gu.id.groupInfo gi";
		sql += " ORDER BY gi.label ASC ";
		TypedQuery<GroupUser> q = em.createQuery(sql, GroupUser.class);
		return q.getResultList();
	}

	public List<GroupUser> getUserGroups(final Long userId) {
		if (PhpUtil.empty(userId)) {
			throw new IllegalArgumentException(Tools.displayError("userId invalide"));
		}

		String sql = "SELECT gu FROM GroupUser gu";
		sql += " INNER JOIN FETCH gu.id.credential cr";
		sql += " INNER JOIN FETCH gu.id.groupInfo gi";
		sql += " INNER JOIN FETCH gi.groupRightInfo gri";
		sql += "  WHERE cr.id = :userId ";
		TypedQuery<GroupUser> q = em.createQuery(sql, GroupUser.class);
		q.setParameter("userId", userId);
		return q.getResultList();
	}

	public boolean isUserInGroup(final String uid, final String gid) {
		String sql;
		boolean retval = false;

		sql = "SELECT gu FROM GroupUser gu";
		sql += " WHERE gu.id.credential.id = :userID";
		sql += " AND gu.id.groupInfo.id = :groupID";
		Query q = em.createQuery(sql);
		q.setParameter("userID", uid);
		q.setParameter("groupID", gid);
		try {
			q.getSingleResult();
			retval = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public boolean isUserMemberOfGroup(Long userId, Long groupId) {
		boolean status = false;
		String sql = "SELECT gu FROM GroupUser gu";
		sql += " WHERE gu.id.credential.id = :userId";
		sql += " AND gu.id.groupInfo.id = :groudId";
		TypedQuery<GroupUser> q = em.createQuery(sql, GroupUser.class);
		q.setParameter("userId", userId);
		q.setParameter("groudId", groupId);
		try {
			q.getSingleResult();
			status = true;
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return status;
	}

	public boolean postGroupsUsers(int user, int userId, int groupId) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<GroupUser> getUserGroupByPortfolio(String portfolioUuid) {
		return getUserGroupByPortfolio(UUID.fromString(portfolioUuid));
	}

	public List<GroupUser> getUserGroupByPortfolio(UUID portfolioUuid) {
		String sql = "SELECT gu FROM GroupUser gu";
		sql += " INNER JOIN FETCH gu.id.groupInfo gi";
		sql += " INNER JOIN FETCH gi.groupRightInfo gri";
		sql += " WHERE gri.portfolio.id = :portfolioUuid";
		TypedQuery<GroupUser> q = em.createQuery(sql, GroupUser.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		return q.getResultList();
	}

	public List<GroupUser> getUserGroupByPortfolioAndUser(String portfolioUuid, Long userId) {
		return getUserGroupByPortfolioAndUser(UUID.fromString(portfolioUuid), userId);
	}

	public List<GroupUser> getUserGroupByPortfolioAndUser(UUID portfolioUuid, Long userId) {
		String sql = "SELECT gu FROM GroupUser gu";
		sql += " INNER JOIN FETCH gu.id.credential cr";
		sql += " INNER JOIN FETCH gu.id.groupInfo gi";
		sql += " INNER JOIN FETCH gi.groupRightInfo gri";
		sql += " WHERE gri.portfolio.id = :portfolioUuid";
		sql += " AND cr.id = :userId";
		TypedQuery<GroupUser> q = em.createQuery(sql, GroupUser.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		q.setParameter("userId", userId);
		return q.getResultList();
	}

	public Integer putUserGroup(String usergroup, String userPut) {
		Integer retval = Integer.valueOf(0);
		try {
			final Long gid = Long.valueOf(usergroup);
			final Long uid = Long.valueOf(userPut);
			final GroupUser gu = new GroupUser(new GroupUserId(new GroupInfo(gid), new Credential(uid)));
			persist(gu);
		} catch (Exception ex) {
			ex.printStackTrace();
			retval = Integer.valueOf(1);
		}
		return retval;
	}

	public int deleteUserGroupByPortfolio(UUID portId) {
		int result = 0;
		List<GroupUser> l = getUserGroupByPortfolio(portId);
		Iterator<GroupUser> it = l.iterator();
		try {
			while (it.hasNext()) {
				remove(it.next());
			}
		} catch (Exception e) {
			result = 1;
		}
		return result;
	}

	public int deleteUserGroupByPortfolio(String portId) {
		return deleteUserGroupByPortfolio(UUID.fromString(portId));
	}

}
