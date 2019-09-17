package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.util.List;

import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface PortfolioGroupDao {

	void persist(PortfolioGroup transientInstance);

	void remove(PortfolioGroup persistentInstance);

	PortfolioGroup merge(PortfolioGroup detachedInstance);

	PortfolioGroup findById(Serializable id) throws DoesNotExistException;

	Long getPortfolioGroupIdFromLabel(String groupLabel);

	int postPortfolioGroup(String groupname, String type, Integer parent, int userId);

	String getPortfolioGroupListFromPortfolio(String portfolioid, int userId);

	String getPortfolioGroupList(int userId);

	List<Portfolio> getPortfolioByPortfolioGroup(Long portfolioGroupId);

	String deletePortfolioGroups(int portfolioGroupId, int userId);

	int putPortfolioInGroup(String uuid, Integer portfolioGroupId, String label, int userId);

	String deletePortfolioFromPortfolioGroups(String uuid, int portfolioGroupId, int userId);

}