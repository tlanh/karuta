package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupDao;
import eportfolium.com.karuta.model.bean.CredentialGroup;

/**
 * Home object implementation for domain model class CredentialGroup.
 * 
 * @see dao.CredentialGroup
 * @author Hibernate Tools
 */
@Repository
public class CredentialGroupDaoImpl extends AbstractDaoImpl<CredentialGroup> implements CredentialGroupDao {

	private static final Log log = LogFactory.getLog(CredentialGroupDaoImpl.class);

	@PersistenceContext
	private EntityManager em;

	public CredentialGroupDaoImpl() {
		super();
		setCls(CredentialGroup.class);
	}

	public CredentialGroup getGroupByName(String name) {
		CredentialGroup cr = null;
		String sql = "SELECT cg FROM CredentialGroup cg";
		sql += " WHERE cg.label = :label";
		TypedQuery<CredentialGroup> q = em.createQuery(sql, CredentialGroup.class);
		q.setParameter("label", name);
		try {
			cr = q.getSingleResult();
		} catch (NoResultException e) {
			e.printStackTrace();
		}
		return cr;
	}

	public Boolean renameCredentialGroup(Long groupId, String newName) {
		boolean isOK = true;

		String sql = "SELECT cg FROM CredentialGroup cg WHERE cg.id = :groupId";
		TypedQuery<CredentialGroup> q = em.createQuery(sql, CredentialGroup.class);
		q.setParameter("groupId", groupId);
		try {
			CredentialGroup cg = q.getSingleResult();
			cg.setLabel(newName);
			merge(cg);

		} catch (Exception e) {
			e.printStackTrace();
			isOK = false;
		}

		return isOK;
	}

	public Long createCredentialGroup(String name) throws Exception {
		try {
			CredentialGroup cg = new CredentialGroup();
			cg.setLabel(name);
			cg = merge(cg);
			return cg.getId();
		} catch (Exception re) {
			log.error("createUserGroup failed", re);
			throw re;
		}
	}

}
