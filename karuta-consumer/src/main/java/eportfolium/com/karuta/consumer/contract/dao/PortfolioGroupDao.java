package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;

import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface PortfolioGroupDao {

	void persist(PortfolioGroup transientInstance);

	void remove(PortfolioGroup persistentInstance);

	PortfolioGroup merge(PortfolioGroup detachedInstance);

	PortfolioGroup findById(Serializable id) throws DoesNotExistException;

	PortfolioGroup getPortfolioGroupFromLabel(String groupLabel);

	Long getPortfolioGroupIdFromLabel(String groupLabel);

	int postPortfolioGroup(String groupname, String type, Integer parent, int userId);

	String getPortfolioGroupListFromPortfolio(String portfolioid, int userId);

	String getPortfolioGroupList(int userId);

	List<Portfolio> getPortfolioByPortfolioGroup(Long portfolioGroupId);

	String deletePortfolioGroups(int portfolioGroupId, int userId);

	String deletePortfolioFromPortfolioGroups(String uuid, int portfolioGroupId, int userId);

	boolean exists(Long id, String type);

	List<PortfolioGroup> findAll();

	ResultSet findAll(String table, Connection con) ;
	
	void removeAll();

}