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

package eportfolium.com.karuta.model.interpreter;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import eportfolium.com.karuta.model.exception.BusinessException;

public interface IPersistenceExceptionInterpreter {

	public BusinessException interpret(PersistenceException e);
	
	// SQLException is leaking out of JBoss 5.0 in duplicate alternate key situations
	public BusinessException interpret(SQLException e);

}
