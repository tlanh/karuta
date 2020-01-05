package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import eportfolium.com.karuta.model.bean.DataTable;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface DataTableDao {

	void persist(DataTable transientInstance);

	void remove(DataTable persistentInstance);

	DataTable merge(DataTable detachedInstance);

	DataTable findById(Serializable id) throws DoesNotExistException;

	ResultSet findAll(String table, Connection con);

	List<DataTable> findAll();

	void removeAll();
}