package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

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
		sql += " INNER JOIN pgm.id.portfolio p WITH p.id = :portfolioUuid";
		TypedQuery<PortfolioGroupMembers> q = em.createQuery(sql, PortfolioGroupMembers.class);
		q.setParameter("portfolioUuid", portfolioUuid);
		return q.getResultList();
	}

	@Override
	public ResultSet getMysqlPortfolioGroupMembers(Connection con) {
		PreparedStatement st;
		String sql;
		try {
			// On récupère d'abord les informations dans la table structures
			sql = "SELECT pg, bin2uuid(pgm.portfolio_id) AS portfolio_id FROM portfolio_group_members pgm";
			st = con.prepareStatement(sql);

			return st.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
