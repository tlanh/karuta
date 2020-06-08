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

package eportfolium.com.karuta.business.contract;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.MessagingException;

public interface EmailManager {

	/**
	 * Send Email
	 *
	 * @param template      the name of template not be a var but a string !
	 * @param subject       Subject of the email
	 * @param locals        Template variables for the email
	 * @param to            To email
	 * @param to_name       To name
	 * @return true, if sending was successful. false, otherwise
	 */
	boolean send(String template, String subject, Map<String, String> locals, String to,
			String to_name) throws UnsupportedEncodingException, MessagingException;

	/**
	 * @param template        the name of template not be a var but a string !
	 * @param subject         Subject of the email
	 * @param locals          Template variables for the email
	 * @param to              To email
	 * @param to_name         To name
	 * @param bcc             Bcc recipient (email address)
	 * @return true, if sending was successful. false, otherwise
	 */
	boolean send(String template, String subject, Map<String, String> locals, String to,
			String to_name, String bcc)
			throws MessagingException, UnsupportedEncodingException;

	/**
	 * Create a trust manager that does not validate certificate chains like the
	 * default
	 */
	void turnedOffSecurity();

}
