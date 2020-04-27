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

import java.util.Date;

import eportfolium.com.karuta.util.MessageUtil;

@SuppressWarnings("serial")
public class UnexpectedException extends SystemException {
	private String referenceCode;
	private Throwable rootCause;

	/**
	 * This exception is thrown by an IPersistenceExceptionInterpreter when it cannot interpret the exception it has
	 * been asked to interpret. Ideally, this will never occur, but if it does occur then the cause should be identified
	 * and the IPersistenceExceptionInterpreter modified to cope in future.
	 */
	public UnexpectedException(Throwable throwable) {
		this(throwable, null);
	}

	/**
	 * This exception is thrown by an IPersistenceExceptionInterpreter when it cannot interpret the exception it has
	 * been asked to interpret. Ideally, this will never occur, but if it does occur then the cause should be identified
	 * and the IPersistenceExceptionInterpreter modified to cope in future.
	 */
	public UnexpectedException(Throwable throwable, Throwable rootCause) {
		super(throwable);
		this.referenceCode = (new Date()).toString();
		this.rootCause = rootCause;
	}

	public String getReferenceCode() {
		return referenceCode;
	}

	public Throwable getRootCause() {
		return rootCause;
	}

	@Override
	public String getMessage() {
		String msg = MessageUtil.toText("UnexpectedException", new Object[] { referenceCode });
		return msg;
	}
}
