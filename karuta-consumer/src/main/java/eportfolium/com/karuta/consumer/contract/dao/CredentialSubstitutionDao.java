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

import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

public interface CredentialSubstitutionDao {

	void persist(CredentialSubstitution transientInstance);

	void remove(CredentialSubstitution persistentInstance);

	CredentialSubstitution merge(CredentialSubstitution detachedInstance);

	CredentialSubstitution findById(Serializable id) throws DoesNotExistException;

	CredentialSubstitution getSubstitutionRule(Long userId, Long csId, String type) throws DoesNotExistException;

	void removeById(final Serializable id) throws DoesNotExistException;

	Long getSubuidFromUserType(String login, Long userId);

	Long getSubuidFromGroupType(String login, Long userId);

	ResultSet findAll(String table, Connection con) ;
	
	List<CredentialSubstitution> findAll();
	
	void removeAll();
	
	

}