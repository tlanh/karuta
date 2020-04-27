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

package eportfolium.com.karuta.batch.exception;


// >>>> Temporary solution - needs renaming at least 


@SuppressWarnings("serial")
public class UtilRuntimeException extends RuntimeException {

	/**
	 * @param arg0
	 * @param arg1
	 */
	public UtilRuntimeException(String string, Throwable throwable) {
		super(string, throwable);
	}

	/**
	 * @param arg0
	 */
	public UtilRuntimeException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param string
	 */
	public UtilRuntimeException(String string) {
		super(string);
	}
	
}
