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
//@ApplicationException(rollback = true)
public class GenericBusinessException extends BusinessException {
	String messageId;
	Object[] messageArgs;

	/**
	 * Throw this exception when a business rule is violated. For example, Employee
	 * could throw this exception if salary > department salary cap.
	 *
	 * @param messageId the message key of a message that describes the rule
	 *                  violation.
	 */
	public GenericBusinessException(String messageId) {
		this(messageId, new Object[0]);
	}

	/**
	 * Throw this exception when a business rule is violated. For example, Employee
	 * could throw this exception if salary > department salary cap.
	 *
	 * @param messageId  the message key of a message that describes the rule
	 *                   violation.
	 * @param messageArg a message argument to be substituted into the message.
	 */
	public GenericBusinessException(String messageId, Object... messageArgs) {
		super();
		this.messageId = messageId;
		this.messageArgs = messageArgs;
	}

	@Override
	public String getMessage() {

		// We deferred converting the message ids to messages until now, when we are
		// more likely to be in the user's
		// locale.

		String msg = MessageUtil.toText(messageId, messageArgs);
		return msg;
	}

	public Object[] getMessageArgs() {
		return messageArgs;
	}

	public String getMessageId() {
		return messageId;
	}

}
