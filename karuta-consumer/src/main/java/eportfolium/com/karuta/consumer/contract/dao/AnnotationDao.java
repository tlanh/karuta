package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import eportfolium.com.karuta.model.bean.Annotation;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface AnnotationDao {

	void persist(Annotation transientInstance);

	void remove(Annotation persistentInstance);

	Annotation merge(Annotation detachedInstance);

	Annotation findById(Serializable id) throws DoesNotExistException;
	
	ResultSet findAll(String table, Connection con) ;
	
	List<Annotation> findAll();
	
	void removeAll(); 

}