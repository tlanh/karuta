package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;

import eportfolium.com.karuta.model.bean.GroupGroup;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface GroupGroupDao {

	void persist(GroupGroup transientInstance);

	void remove(GroupGroup persistentInstance);

	GroupGroup merge(GroupGroup detachedInstance);

	GroupGroup findById(Serializable id) throws DoesNotExistException;

}