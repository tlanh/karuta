package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;

import eportfolium.com.karuta.model.bean.GroupGroup;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface GroupGroupDao {

	void persist(GroupGroup transientInstance);

	void remove(GroupGroup persistentInstance);

	GroupGroup merge(GroupGroup detachedInstance);

	GroupGroup findById(Serializable id) throws DoesNotExistException;

	ResultSet findAll(String table, Connection con);

	void removeAll();

}