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

import eportfolium.com.karuta.util.MessageUtil;

@SuppressWarnings("serial")
public class SystemUnavailableException extends SystemException {
	String symptom;

	/**
	 * Throw this exception when the system becomes unavailable eg. due to database connection failure.
	 */
	public SystemUnavailableException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * Throw this exception when the system becomes unavailable eg. due to database connection failure.
	 */
	public SystemUnavailableException(String symptom, Throwable throwable) {
		super(throwable);
		this.symptom = symptom;
	}

	@Override
	public String getMessage() {
		String msg = MessageUtil.toText("SystemUnavailableException", symptom);
		return msg;
	}

	public String getSymptom() {
		return symptom;
	}

}
