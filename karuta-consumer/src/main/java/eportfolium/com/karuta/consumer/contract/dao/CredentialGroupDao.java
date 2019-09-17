package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
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

	CredentialGroup getGroupByGroupLabel(String groupLabel);

	Boolean putUserGroupLabel(Long siteGroupId, String label);

	/**
	 * REPLACE postUserGroup
	 * 
	 * @param label
	 * @return
	 * @throws Exception
	 */
	Long createUserGroup(String label) throws Exception;

}