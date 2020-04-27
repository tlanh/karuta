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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.UnexpectedException;
import eportfolium.com.karuta.util.ExceptionUtil;

public class BusinessServiceExceptionInterpreter {
	static private final Logger LOGGER = LoggerFactory.getLogger(BusinessServiceExceptionInterpreter.class);

	private IPersistenceExceptionInterpreter persistenceExceptionInterpreter = new HibernatePersistenceExceptionInterpreter();

	/**
	 * Interpret a Throwable into a JumpStart BusinessException or throw an UnexpectedException.
	 */
	public BusinessException interpret(Throwable t) {
		try {
			BusinessException be = null;
 
			if (t instanceof BusinessException) {
				be = (BusinessException) t;
			}

			else if (t instanceof javax.persistence.PersistenceException) {
				be = persistenceExceptionInterpreter.interpret((javax.persistence.PersistenceException) t);
			}

			else if (t instanceof java.sql.SQLException) {
				// SQLException is leaking out of JBoss 5.0 in duplicate alternate key situations
				be = persistenceExceptionInterpreter.interpret((java.sql.SQLException) t);
			}

			else if (t.getCause() != null && !t.getCause().equals(t)) {
				be = interpret(t.getCause());
			}

			else {
				LOGGER.error("Cannot interpret Throwable " + t);
				throw new UnexpectedException(t);
			}

			return be;
		}
		catch (UnexpectedException e) {
			LOGGER.error(ExceptionUtil.printStackTrace(e));
			LOGGER.error("  Caused by: " + e.getCause());
			if (e.getRootCause() != null) {
				LOGGER.error("   Root cause: " + e.getRootCause().toString());
			}
			throw e;
		}
	}

}
