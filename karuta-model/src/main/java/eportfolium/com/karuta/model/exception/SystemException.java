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

package eportfolium.com.karuta.model.exception;

/**
 * A system exception is an exception that the application cannot recover from. They fall into two categories: (1)
 * system has become temporarily unavailable eg. because the database has stopped; and (2) system has failed with an
 * irrecoverable logic error. Typically, the system should display a special "system unavailable" page to the user and
 * notify operations immediately.
 * 
 */
@SuppressWarnings("serial")
public abstract class SystemException extends RuntimeException {

	public SystemException(Throwable throwable) {
		super(throwable);
	}

	public SystemException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
