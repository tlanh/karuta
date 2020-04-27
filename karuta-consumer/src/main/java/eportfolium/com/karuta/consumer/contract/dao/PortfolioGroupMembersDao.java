/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.consumer.contract.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface PortfolioGroupMembersDao {

	void persist(PortfolioGroupMembers transientInstance);

	void remove(PortfolioGroupMembers persistentInstance);

	void removeById(final Serializable id) throws DoesNotExistException;

	PortfolioGroupMembers merge(PortfolioGroupMembers detachedInstance);

	PortfolioGroupMembers findById(Serializable id) throws DoesNotExistException;

	List<PortfolioGroupMembers> getByPortfolioGroupID(Long portfolioGroupID);

	List<PortfolioGroupMembers> getByPortfolioID(String portfolioUuid);

	List<PortfolioGroupMembers> getByPortfolioID(UUID portfolioUuid);

	ResultSet getMysqlPortfolioGroupMembers(Connection con);

	ResultSet findAll(String table, Connection con) ;
	
	List<PortfolioGroupMembers> findAll();
	
	void removeAll();
}