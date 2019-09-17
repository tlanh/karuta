package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.CredentialSubstitutionDao;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;

/**
 * Home object implementation for domain model class CredentialSubstitution.
 * 
 * @see dao.CredentialSubstitution
 * @author Hibernate Tools
 */
@Repository
public class CredentialSubstitutionDaoImpl extends AbstractDaoImpl<CredentialSubstitution>
		implements CredentialSubstitutionDao {

	@PersistenceContext
	private EntityManager entityManager;

	public CredentialSubstitutionDaoImpl() {
		super();
		setCls(CredentialSubstitution.class);
	}

}
