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

	Boolean deleteUsersFromUserGroups(Long userId, Long cgId);

	List<CredentialGroupMembers> getGroupByUser(Long userId);
	
	ResultSet findAll(String table, Connection con) ;
	
	List<CredentialGroupMembers> findAll();
	
	void removeAll();

}