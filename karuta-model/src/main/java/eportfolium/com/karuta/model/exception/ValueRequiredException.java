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
public class ValueRequiredException extends BusinessException {
	private String entityLabelMessageId;
	private String fieldLabelMessageId;

	/**
	 * Throw this exception from an entity that has a required property that has not been given a value (eg. null, empty
	 * or 0).
	 * 
	 * @param entity the entity being set.
	 * @param fieldLabelMessageId the key of a message that represents the field that has not been given a value.
	 */
	public ValueRequiredException(Serializable entity, String fieldLabelMessageId) {

		// Don't convert the message ids to messages yet because we're in the server's locale, not the user's.

		super();
		this.entityLabelMessageId = ClassUtil.extractUnqualifiedName(entity);
		this.fieldLabelMessageId = fieldLabelMessageId;
	}

	@Override
	public String getMessage() {

		// We deferred converting the message ids to messages until now, when we are more likely to be in the user's
		// locale.

		Object[] msgArgs = new Object[] { MessageUtil.toText(entityLabelMessageId),
				MessageUtil.toText(fieldLabelMessageId) };

		String msg = MessageUtil.toText("ValueRequiredException", msgArgs);
		return msg;
	}

	public String getEntityLabelMessageId() {
		return entityLabelMessageId;
	}

	public String getFieldLabelMessageId() {
		return fieldLabelMessageId;
	}
}
