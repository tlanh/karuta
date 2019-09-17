package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface PortfolioGroupMembersDao {

	void persist(PortfolioGroupMembers transientInstance);

	void remove(PortfolioGroupMembers persistentInstance);

	PortfolioGroupMembers merge(PortfolioGroupMembers detachedInstance);

	PortfolioGroupMembers findById(Serializable id) throws DoesNotExistException;

	List<PortfolioGroupMembers> getByPortfolioGroupID(Long portfolioGroupID);

	List<PortfolioGroupMembers> getByPortfolioID(String portfolioUuid);

	List<PortfolioGroupMembers> getByPortfolioID(UUID portfolioUuid);

}