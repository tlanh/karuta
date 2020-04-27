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

import java.io.Serializable;

import eportfolium.com.karuta.util.ClassUtil;
import eportfolium.com.karuta.util.MessageUtil;

@SuppressWarnings("serial")
//@ApplicationException(rollback = true)
public class DoesNotExistException extends BusinessException {
	private String entityLabelMessageId;
	private Serializable id;

	/**
	 * Throw this exception when an object is requested but does not exist.
	 * 
	 * @param cls
	 *            the class of the object being requested. It will be stripped down to its unqualified name (eg.
	 *            jumpstart.Department would be stripped down to Department) to be used as a message key when generating a
	 *            message in getMessage().
	 * @param id
	 *            the id of the object being requested.
	 */
	public DoesNotExistException(Class<?> cls, Serializable id) {

		// Don't convert the message ids to messages yet because we're in the server's locale, not the user's.

		super();
		this.entityLabelMessageId = ClassUtil.extractUnqualifiedName(cls);
		this.id = id;
	}

	@Override
	public String getMessage() {

		// We deferred converting the message ids to messages until now, when we are more likely to be in the user's
		// locale.

		Object[] msgArgs = new Object[] { MessageUtil.toText(entityLabelMessageId), id };

		String msg = MessageUtil.toText("DoesNotExistException", msgArgs);
		return msg;
	}

	public String getEntityLabelMessageId() {
		return entityLabelMessageId;
	}

	public Serializable getId() {
		return id;
	}
}
