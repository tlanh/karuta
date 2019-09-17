package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.model.bean.GroupRightInfo;

/**
 * Home object implementation for domain model class GroupRightInfo.
 * 
 * @see dao.GroupRightInfo
 * @author Hibernate Tools
 */
@Repository
public class GroupRightInfoDaoImpl extends AbstractDaoImpl<GroupRightInfo> implements GroupRightInfoDao {

	@PersistenceContext
	private EntityManager em;

	public GroupRightInfoDaoImpl() {
		super();
		setCls(GroupRightInfo.class);
	}

	public List<GroupRightInfo> getByPortfolioID(UUID portfolioUuid) {
		String sql = "SELECT gri FROM GroupRightInfo gri";
		sql += " LEFT JOIN FETCH gri.groupInfo gi";
		sql += " LEFT JOIN FETCH gri.groupRights gr";
		sql += " LEFT JOIN FETCH gi.groupUser gu";
		sql += " WHERE gri.portfolio.id = :portfolioUuid";
		TypedQuery<GroupRightInfo> q = em.createQuery(sql, GroupRightInfo.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		return q.getResultList();
	}

	public GroupRightInfo getByPortfolioAndLabel(String portfolioUuid, String label) {
		return getByPortfolioAndLabel(UUID.fromString(portfolioUuid), label);
	}

	public GroupRightInfo getByPortfolioAndLabel(UUID portfolioUuid, String label) {
		GroupRightInfo gri = null;
		String sql = "SELECT gri FROM GroupRightInfo gri";
		sql += " INNER JOIN FETCH gri.portfolio p";
		sql += " WHERE p.id = :portfolioUuid";
		sql += " AND p.label = :label";
		TypedQuery<GroupRightInfo> q = em.createQuery(sql, GroupRightInfo.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		q.setParameter("label", label);
		try {
			gri = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		} catch (NonUniqueResultException e) {
			e.printStackTrace();
		}
		return gri;
	}

	public List<GroupRightInfo> getByPortfolioID(String portfolioUuid) {
		return getByPortfolioID(UUID.fromString(portfolioUuid));
	}

	public boolean groupRightInfoExists(Long grid) {
		boolean result = false;
		String sql = "SELECT gri FROM GroupRightInfo gri";
		sql += " WHERE gri.id = :grid";
		Query q = em.createQuery(sql);
		q.setParameter("grid", grid);
		try {
			q.getSingleResult();
			result = true;
		} catch (Exception e) {
		}
		return result;
	}

}
