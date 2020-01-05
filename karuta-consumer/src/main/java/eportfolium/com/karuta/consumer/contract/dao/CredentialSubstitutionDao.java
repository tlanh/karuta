package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface CredentialSubstitutionDao {

	void persist(CredentialSubstitution transientInstance);

	void remove(CredentialSubstitution persistentInstance);

	CredentialSubstitution merge(CredentialSubstitution detachedInstance);

	CredentialSubstitution findById(Serializable id) throws DoesNotExistException;

	CredentialSubstitution getSubstitutionRule(Long userId, Long csId, String type) throws DoesNotExistException;

	void removeById(final Serializable id) throws DoesNotExistException;

	Long getSubuidFromUserType(String login, Long userId);

	Long getSubuidFromGroupType(String login, Long userId);

	ResultSet findAll(String table, Connection con) ;
	
	List<CredentialSubstitution> findAll();
	
	void removeAll();
	
	

}