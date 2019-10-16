package eportfolium.com.karuta.consumer.impl.dao;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;

/**
 * Home object implementation for domain model class GroupInfo.
 * 
 * @see dao.GroupInfo
 * @author Hibernate Tools
 */
@Repository
public class GroupInfoDaoImpl extends AbstractDaoImpl<GroupInfo> implements GroupInfoDao {

	@PersistenceContext
	private EntityManager em;

	public GroupInfoDaoImpl() {
		super();
		setCls(GroupInfo.class);
	}

	public GroupInfo getGroupByName(String label) {
		String sql;
		GroupInfo res = null;

		sql = "SELECT gi FROM GroupInfo gi";
		sql += " WHERE label = :label";
		TypedQuery<GroupInfo> q = em.createQuery(sql, GroupInfo.class);
		q.setParameter("label", label);
		try {
			res = q.getSingleResult();
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}

		return res;
	}

	public boolean exists(GroupRightInfo gri, long owner, String label) {
		String sql;
		boolean exists = false;

		sql = "SELECT gi FROM GroupInfo gi";
		sql += " WHERE gi.label = :label";
		TypedQuery<GroupInfo> q = em.createQuery(sql, GroupInfo.class);
		q.setParameter("label", label);
		try {
			q.getSingleResult();
			exists = true;
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return exists;

	}

	public Long add(GroupRightInfo gri, long owner, String label) {
		GroupInfo gi = new GroupInfo();
		gi.setGroupRightInfo(gri);
		gi.setOwner(owner);
		gi.setLabel(label);
		persist(gi);
		return gi.getId();
	}

	public List<GroupInfo> getGroupsByRole(String portfolioUuid, String role) {
		List<GroupInfo> res = null;
		try {
			String sql = "SELECT DISTINCT gi FROM GroupInfo gi";
			sql += " INNER JOIN gi.groupUser gu";
			sql += " INNER JOIN gi.groupRightInfo gri";
			sql += " WHERE gri.portfolio.id = :portfolioUuid";
			sql += " AND gri.label = :label";
			TypedQuery<GroupInfo> q = em.createQuery(sql, GroupInfo.class);
			q.setParameter("portfolioUuid", UUID.fromString(portfolioUuid));
			q.setParameter("label", role);
			res = q.getResultList();
		} catch (Exception ex) {
			ex.printStackTrace();
			res = Arrays.asList();
		}
		return res;
	}

	/**
	 * Recupere une instance de group_info associé à un grid.
	 */
	public GroupInfo getGroupByGrid(Long grid) {
		String sql;
		GroupInfo res = null;

		sql = "SELECT gi FROM GroupInfo gi";
		sql += " INNER JOIN gi.groupRightInfo gri WITH gri.id = :grid";
		TypedQuery<GroupInfo> q = em.createQuery(sql, GroupInfo.class);
		q.setParameter("grid", grid);
		try {
			res = q.getSingleResult();
		} catch (NoResultException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	public List<GroupInfo> getByPortfolio(String portfolioUuid) {
		String sql = "SELECT gi FROM GroupInfo gi";
		sql += " INNER JOIN gi.groupRightInfo gri WITH gri.portfolio.id = :portUuid";
		sql += " ORDER BY gi.label ASC";
		try {
			TypedQuery<GroupInfo> query = em.createQuery(sql, GroupInfo.class);
			query.setParameter("portUuid", UUID.fromString(portfolioUuid));
			return query.getResultList();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return null;
	}

}
