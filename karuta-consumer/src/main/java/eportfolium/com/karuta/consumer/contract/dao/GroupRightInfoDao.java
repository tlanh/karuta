package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface GroupRightInfoDao {

	void persist(GroupRightInfo transientInstance);

	void remove(GroupRightInfo persistentInstance);

	GroupRightInfo merge(GroupRightInfo detachedInstance);

	GroupRightInfo findById(Serializable id) throws DoesNotExistException;

	List<GroupRightInfo> getByPortfolioID(UUID portfolioUuid);

	List<GroupRightInfo> getByPortfolioID(String portfolioUuid);

	GroupRightInfo getByPortfolioAndLabel(String portfolioUuid, String label);

	GroupRightInfo getByPortfolioAndLabel(UUID portfolioUuid, String label);

	boolean groupRightInfoExists(Long grid);

}