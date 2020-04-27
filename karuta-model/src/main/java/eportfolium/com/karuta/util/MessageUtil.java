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

package eportfolium.com.karuta.util;

import java.text.MessageFormat;
import java.util.Properties;

import eportfolium.com.karuta.model.exception.UtilRuntimeException;

public class MessageUtil {
	static private String MESSAGE_FILE_NAME = "eportfolium/com/karuta/util/messageDefs.properties";
	static private Properties messageDefs = null;

	static private Properties loadMessageDefs(Object anyObject) {

		if (messageDefs == null) {
			messageDefs = ResourceUtil.getAsProperties(MESSAGE_FILE_NAME);
		}

		return messageDefs;
	}

	static public String toText(String messageId) {
		try {
			Properties p = loadMessageDefs(messageId);

			String s = p.getProperty(messageId);
			if (s != null) {
				return s;
			} else {
				return ("[Contact I.T.  MessageUtil cannot find message id \"" + messageId + "\" in "
						+ MESSAGE_FILE_NAME + ".]");
			}
		} catch (UtilRuntimeException e) {
			System.err.println("Failed to get message for \"" + messageId + "\".");
			throw e;
		}

	}

	static public String toText(String messageId, Object messageArg) {
		return toText(messageId, new Object[] { messageArg });
	}

	static public String toText(String messageId, Object[] messageArgs) {
		String message = null;
		Properties p = loadMessageDefs(messageId);
		String s = p.getProperty(messageId);

		if (s != null) {
			if (messageArgs == null) {
				message = s;
			} else {
				MessageFormat messageFormat = new MessageFormat(s);
				message = messageFormat.format(messageArgs);
			}

			return message;
		} else {
			return ("[Contact I.T.  MessageUtil cannot find message id \"" + messageId + "\" in " + MESSAGE_FILE_NAME
					+ ".]");
		}

	}

}
