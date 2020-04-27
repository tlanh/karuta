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

package eportfolium.com.karuta.webapp.rest.resource;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public abstract class AbstractResource {

	protected static final String logFormat = "[%1$s] %2$s %3$s: %4$s -- %5$s (%6$s) === %7$s\n";
	protected static final String logFormatShort = "%7$s\n";

	class UserInfo {
		String subUser = "";
		long subId = 0L;
		String User = "";
		long userId = 0L;
	}

	/**
	 * Fetch user session info
	 * 
	 * @param request
	 * @param login
	 * @param token
	 * @param group
	 * @return
	 */
	public UserInfo checkCredential(HttpServletRequest request, String login, String token, String group) {
		HttpSession session = request.getSession(true);

		UserInfo ui = new UserInfo();
		Long val = (Long) session.getAttribute("uid");
		if (val != null)
			ui.userId = val;
		val = (Long) session.getAttribute("subuid");
		if (val != null)
			ui.subId = val;
		ui.User = (String) session.getAttribute("user");
		ui.subUser = (String) session.getAttribute("subuser");

		return ui;
	}

	protected boolean isUUID(String uuidstr) {
		try {
			UUID.fromString(uuidstr);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

}
