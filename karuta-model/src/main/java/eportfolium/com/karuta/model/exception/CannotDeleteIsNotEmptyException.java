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

import eportfolium.com.karuta.model.bean.BaseEntity;
import eportfolium.com.karuta.util.ClassUtil;
import eportfolium.com.karuta.util.MessageUtil;

@SuppressWarnings("serial")
//@ApplicationException(rollback = true)
public class CannotDeleteIsNotEmptyException extends BusinessException {
	private String entityLabelMessageId;
	private Serializable id;
	private String partEntityLabelMessageId;

	/**
	 * Throw this exception from an entity that "contains" other objects that have not been deleted
	 * and are not defined to be "cascade deleted".  For example, deleting a Department that still contains Teachers.
	 *
	 * @param	entity	the entity holding the set of objects, eg. a Department object.
	 * @param	id		the id of the entity.
	 * @param	partEntityLabelMessageId	the key of a message that represents the object still contained, eg. "Teacher".
	 */
	public CannotDeleteIsNotEmptyException(BaseEntity entity, Serializable id, String partEntityLabelMessageId) {

		// Don't convert the message ids to messages yet because we're in the server's locale, not the user's.

		super();
		this.entityLabelMessageId = ClassUtil.extractUnqualifiedName(entity);
		this.id = id;
		this.partEntityLabelMessageId = partEntityLabelMessageId;
	}

	@Override
	public String getMessage() {

		// We deferred converting the message ids to messages until now, when we are more likely to be in the user's
		// locale.

		Object[] msgArgs = new Object[] { MessageUtil.toText(entityLabelMessageId), id,
				MessageUtil.toText(partEntityLabelMessageId) };

		String msg = MessageUtil.toText("CannotDeleteIsNotEmptyException", msgArgs);
		return msg;
	}

	public String getPartEntityLabelMessageId() {
		return partEntityLabelMessageId;
	}

	public String getEntityLabelMessageId() {
		return entityLabelMessageId;
	}

	public Serializable getId() {
		return id;
	}

}
