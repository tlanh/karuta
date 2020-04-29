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

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupDao;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;

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
		if (portfolioGroupId == null || portfolioGroupId == 0L) {
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
