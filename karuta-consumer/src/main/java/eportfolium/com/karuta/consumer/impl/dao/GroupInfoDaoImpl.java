package eportfolium.com.karuta.consumer.impl.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.model.bean.GroupInfo;

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

}
