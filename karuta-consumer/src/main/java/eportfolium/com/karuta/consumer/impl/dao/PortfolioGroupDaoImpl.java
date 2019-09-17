package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
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

	@PersistenceContext
	private EntityManager em;

//	private static final Log log = LogFactory.getLog(PortfolioGroupDaoImpl.class);

	public PortfolioGroupDaoImpl() {
		super();
		setCls(PortfolioGroup.class);
	}

	public int postPortfolioGroup(String groupname, String type, Integer parent, int userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Long getPortfolioGroupIdFromLabel(String groupLabel) {
		Long groupID = Long.valueOf(-1);

		String sql = "SELECT pg.id FROM PortfolioGroup pg";
		sql += " WHERE pg.label = :label";
		TypedQuery<Long> q = em.createQuery(sql, Long.class);
		q.setParameter(1, groupLabel);
		try {
			groupID = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return groupID;
	}

	public String getPortfolioGroupListFromPortfolio(String portfolioid, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPortfolioGroupList(int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Portfolio> getPortfolioByPortfolioGroup(Long portfolioGroupId) {
		if (PhpUtil.empty(portfolioGroupId) || !ValidateUtil.isUnsignedId(portfolioGroupId.intValue())) {
			throw new IllegalArgumentException();
		}
		String sql = "SELECT p FROM PortfolioGroupMembers pgm";
		sql += " LEFT JOIN FETCH pgm.id.portfolio p";
		sql += " WHERE id.portfolioGroup.id = :portfolioGroupId";
		TypedQuery<Portfolio> q = em.createQuery(sql, Portfolio.class);
		q.setParameter("portfolioGroupId", portfolioGroupId);
		return q.getResultList();
	}

	public String deletePortfolioGroups(int portfolioGroupId, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public int putPortfolioInGroup(String uuid, Integer portfolioGroupId, String label, int userId) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String deletePortfolioFromPortfolioGroups(String uuid, int portfolioGroupId, int userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
