package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupMembersDao;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;

/**
 * Home object implementation for domain model class PortfolioGroupMembers.
 * 
 * @see dao.PortfolioGroupMembers
 * @author Hibernate Tools
 */
@Repository
public class PortfolioGroupMembersDaoImpl extends AbstractDaoImpl<PortfolioGroupMembers>
		implements PortfolioGroupMembersDao {

	@PersistenceContext
	private EntityManager em;

	public PortfolioGroupMembersDaoImpl() {
		super();
		setCls(PortfolioGroupMembers.class);
	}

	public List<PortfolioGroupMembers> getByPortfolioGroupID(Long portfolioGroupID) {
		String sql = "SELECT pgm FROM PortfolioGroupMembers pgm";
		sql += " LEFT JOIN pgm.id.portfolioGroup pg";
		sql += " WHERE pg.id = :portfolioGroupID";
		TypedQuery<PortfolioGroupMembers> q = em.createQuery(sql, PortfolioGroupMembers.class);
		q.setParameter("portfolioGroupID", portfolioGroupID);
		return q.getResultList();
	}

	public List<PortfolioGroupMembers> getByPortfolioID(String portfolioUuid) {
		return getByPortfolioID(UUID.fromString(portfolioUuid));
	}

	public List<PortfolioGroupMembers> getByPortfolioID(UUID portfolioUuid) {
		String sql = "SELECT pgm FROM PortfolioGroupMembers pgm";
		sql += " INNER JOIN pgm.id.portfolio p WITH p.id = :portfolioID";
		TypedQuery<PortfolioGroupMembers> q = em.createQuery(sql, PortfolioGroupMembers.class);
		q.setParameter("portfolioID", portfolioUuid);
		return q.getResultList();
	}

}
