package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;

import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface GroupInfoDao {

	void persist(GroupInfo transientInstance);

	void remove(GroupInfo persistentInstance);

	GroupInfo merge(GroupInfo detachedInstance);

	GroupInfo findById(Serializable id) throws DoesNotExistException;

	GroupInfo getGroupByName(String name);

}