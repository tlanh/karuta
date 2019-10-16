package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface CredentialGroupDao {

	void persist(CredentialGroup transientInstance);

	void remove(CredentialGroup persistentInstance);

	CredentialGroup merge(CredentialGroup detachedInstance);

	CredentialGroup findById(Serializable id) throws DoesNotExistException;

	List<CredentialGroup> findAll();

	void removeById(Serializable id) throws DoesNotExistException;

	CredentialGroup getGroupByName(String name);

	Boolean renameCredentialGroup(Long groupId, String newName);

	/**
	 * REPLACE postUserGroup
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	Long createCredentialGroup(String name) throws Exception;

	ResultSet findAll(String table, Connection con);

	void removeAll();

}