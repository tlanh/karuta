package eportfolium.com.karuta.consumer.impl.dao;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupDao;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.util.ValidateUtil;

/**
 * Home object implementation for domain model class PortfolioGroup.
 * 
 * @see dao.PortfolioGroup
 * @author Hibernate Tools
 */
@Repository
public class PortfolioGroupDaoImpl extends AbstractDaoImpl<PortfolioGroup> implements PortfolioGroupDao {

	public PortfolioGroupDaoImpl() {
		super();
		setCls(PortfolioGroup.class);
	}

	public PortfolioGroup getPortfolioGroupFromLabel(String groupLabel) {
		PortfolioGroup group = null;
		String sql = "SELECT pg FROM PortfolioGroup pg";
		sql += " WHERE pg.label = :label";
		TypedQuery<PortfolioGroup> q = em.createQuery(sql, PortfolioGroup.class);
		q.setParameter("label", groupLabel);
		try {
			group = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return group;
	}

	public Long getPortfolioGroupIdFromLabel(String groupLabel) {
		PortfolioGroup group = getPortfolioGroupFromLabel(groupLabel);
		return group != null ? group.getId() : -1L;
	}

	/**
	 * Check if exist with correct type
	 */
	public boolean exists(Long id, String type) {
		boolean exists = false;
		String sql = "SELECT pg FROM PortfolioGroup pg";
		sql += " WHERE pg.id = :id";
		sql += " AND pg.type = :type";
		TypedQuery<PortfolioGroup> q = em.createQuery(sql, PortfolioGroup.class);
		q.setParameter("id", id);
		q.setParameter("type", type);
		try {
			q.getSingleResult();
			exists = true;
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return exists;
	}

	public List<Portfolio> getPortfoliosByPortfolioGroup(Long portfolioGroupId) {
		if (PhpUtil.empty(portfolioGroupId) || !ValidateUtil.isUnsignedId(portfolioGroupId.intValue())) {
			throw new IllegalArgumentException();
		}
		
		String sql = "SELECT p FROM PortfolioGroupMembers pgm";
		sql += " LEFT JOIN pgm.id.portfolio p";
		sql += " WHERE pgm.id.portfolioGroup.id = :portfolioGroupId";

		final TypedQuery<Portfolio> q = em.createQuery(sql, Portfolio.class);
		q.setParameter("portfolioGroupId", portfolioGroupId);
		return q.getResultList();
	}

}
