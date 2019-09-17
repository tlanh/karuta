package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;

import eportfolium.com.karuta.model.bean.LogTable;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface LogTableDao {

	void persist(LogTable transientInstance);

	void remove(LogTable persistentInstance);

	LogTable merge(LogTable detachedInstance);

	LogTable findById(Serializable id) throws DoesNotExistException;

}