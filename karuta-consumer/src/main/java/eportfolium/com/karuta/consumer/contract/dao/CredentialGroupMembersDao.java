package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface CredentialGroupMembersDao {

	void persist(CredentialGroupMembers transientInstance);

	void remove(CredentialGroupMembers persistentInstance);

	CredentialGroupMembers merge(CredentialGroupMembers detachedInstance);

	CredentialGroupMembers findById(Serializable id) throws DoesNotExistException;

	/**
	 * Remove a user from a user group,
	 * 
	 * @param userId
	 * @param cgId
	 * @return
	 */
	Boolean deleteUserFromGroup(Long userId, Long cgId);

	List<CredentialGroupMembers> getByGroup(Long cgId);

	List<CredentialGroupMembers> getByUser(Long userId);

	ResultSet findAll(String table, Connection con);

	List<CredentialGroupMembers> findAll();

	void removeAll();

}