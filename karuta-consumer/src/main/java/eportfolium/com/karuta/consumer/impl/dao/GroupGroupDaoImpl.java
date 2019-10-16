package eportfolium.com.karuta.consumer.impl.dao;
// Generated 17 juin 2019 11:33:18 by Hibernate Tools 5.2.10.Final

import org.springframework.stereotype.Repository;

import eportfolium.com.karuta.consumer.contract.dao.GroupGroupDao;
import eportfolium.com.karuta.model.bean.GroupGroup;

/**
 * Home object implementation for domain model class GroupGroup.
 * 
 * @see dao.GroupGroup
 * @author Hibernate Tools
 */
@Repository
public class GroupGroupDaoImpl extends AbstractDaoImpl<GroupGroup> implements GroupGroupDao {

	public GroupGroupDaoImpl() {
		super();
		setCls(GroupGroup.class);
	}

}
