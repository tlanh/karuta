package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;

import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface CredentialSubstitutionDao {

	void persist(CredentialSubstitution transientInstance);

	void remove(CredentialSubstitution persistentInstance);

	CredentialSubstitution merge(CredentialSubstitution detachedInstance);

	CredentialSubstitution findById(Serializable id) throws DoesNotExistException;
	
}