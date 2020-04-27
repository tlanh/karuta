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

/**
 * Throw this exception when an authentication exception has occurred.  This implementation allows
 * any error message to be specified, enabling different messages for "invalid loginId" and
 * "wrong password".  For greater security you may prefer to replace it with one that returns just 
 * one consistent message from getMessage(), eg. "loginId or password is incorrect".
 */
@SuppressWarnings("serial")
public class AuthenticationException extends BusinessException {
	String messageId;
	Object[] messageArgs;

	public AuthenticationException(String messageId) {
		this(messageId, null);
	}

	public AuthenticationException(String messageId, Object messageArg) {
		super();
		this.messageId = messageId;
		this.messageArgs = new Object[] { messageArg };
	}

	@Override
	public String getMessage() {

		// We deferred converting the message ids to messages until now, when we are more likely to be in the user's
		// locale.

		String msg = MessageUtil.toText(messageId, messageArgs);
		return msg;
	}

	public String getMessageId() {
		return messageId;
	}
}
