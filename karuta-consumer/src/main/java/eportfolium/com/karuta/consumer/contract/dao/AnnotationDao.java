package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;

import eportfolium.com.karuta.model.bean.Annotation;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface AnnotationDao {

	void persist(Annotation transientInstance);

	void remove(Annotation persistentInstance);

	Annotation merge(Annotation detachedInstance);

	Annotation findById(Serializable id) throws DoesNotExistException;

}