package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;

import eportfolium.com.karuta.model.bean.DataTable;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface DataTableDao {

	void persist(DataTable transientInstance);

	void remove(DataTable persistentInstance);

	DataTable merge(DataTable detachedInstance);

	DataTable findById(Serializable id) throws DoesNotExistException;

}