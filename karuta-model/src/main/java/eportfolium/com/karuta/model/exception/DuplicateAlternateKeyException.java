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

import eportfolium.com.karuta.model.bean.BaseEntity;
import eportfolium.com.karuta.util.ClassUtil;
import eportfolium.com.karuta.util.MessageUtil;

@SuppressWarnings("serial")
//@ApplicationException(rollback = true)
public class DuplicateAlternateKeyException extends BusinessException {
	static public final int INFORMATIONLEVEL_ENTITY_TECHMSG = 1;
	static public final int INFORMATIONLEVEL_TECHMSG = 2;
	static public final int INFORMATIONLEVEL_KEY_VALUE = 3;

	private int informationLevel = INFORMATIONLEVEL_ENTITY_TECHMSG;
	private String entityLabelMessageId;
	private String technicalMessageText;
	private String keyValue;

	/**
	 * This exception is thrown by an IPersistenceExceptionInterpreter when an attempt to create an entity has failed because
	 * the entity specifies an alternate key (a unique key other than the primary key) that is already in use.
	 * 
	 * @param entity	the entity being created.
	 * @param technicalMessageText	typically this is a constraint violation message from the database.
	 */
	public DuplicateAlternateKeyException(BaseEntity entity, String technicalMessageText) {

		// Don't convert the message ids to messages yet because we're in the
		// server's locale, not the user's.

		super();
		this.informationLevel = INFORMATIONLEVEL_ENTITY_TECHMSG;
		this.entityLabelMessageId = ClassUtil.extractUnqualifiedName(entity);
		this.technicalMessageText = technicalMessageText;
	}

	/**
	 * This exception is thrown by an IPersistenceExceptionInterpreter when an attempt to create an entity has failed because
	 * the entity specifies an alternate key (a unique key other than the primary key) that is already in use.
	 * 
	 * @param technicalMessageText	typically this is a constraint violation message from the database.
	 */
	public DuplicateAlternateKeyException(String technicalMessageText) {

		// Don't convert the message ids to messages yet because we're in the
		// server's locale, not the user's.

		super();
		this.informationLevel = INFORMATIONLEVEL_TECHMSG;
		this.technicalMessageText = technicalMessageText;
	}
	/**
	 * This exception is thrown by an IPersistenceExceptionInterpreter when an attempt to create an entity has failed because
	 * the entity specifies an alternate key (a unique key other than the primary key) that is already in use.
	 * 
	 * @param entity	the entity being created.
	 * @param technicalMessageText	typically this is a constraint violation message from the database.
	 */
	public DuplicateAlternateKeyException(String keyValue, String technicalMessageText) {

		// Don't convert the message ids to messages yet because we're in the
		// server's locale, not the user's.

		super();
		this.informationLevel = INFORMATIONLEVEL_KEY_VALUE;
		this.keyValue = keyValue;
		this.technicalMessageText = technicalMessageText;
	}


	@Override
	public String getMessage() {
		String msg;
		Object[] msgArgs;

		// We deferred converting the message ids to messages until now, when we
		// are more likely to be in the user's locale.

		if (informationLevel == INFORMATIONLEVEL_ENTITY_TECHMSG) {
			msgArgs = new Object[] { MessageUtil.toText(entityLabelMessageId), technicalMessageText };
			msg = MessageUtil.toText("DuplicateAlternateKeyException", msgArgs);
		}
		else if (informationLevel == INFORMATIONLEVEL_TECHMSG) {
			msgArgs = new Object[] { technicalMessageText };
			msg = MessageUtil.toText("DuplicateAlternateKeyException_2", msgArgs);
		}
		else if (informationLevel == INFORMATIONLEVEL_KEY_VALUE) {
			msgArgs = new Object[] { keyValue, technicalMessageText };
			msg = MessageUtil.toText("DuplicateAlternateKeyException_3", msgArgs);
		}
		else {
			throw new IllegalStateException("informationLevel = " + informationLevel);
		}

		return msg;
	}

	public String getEntityLabelMessageId() {
		return entityLabelMessageId;
	}

	public String getTechnicalMessageText() {
		return technicalMessageText;
	}

	public int getInformationLevel() {
		return informationLevel;
	}
}
