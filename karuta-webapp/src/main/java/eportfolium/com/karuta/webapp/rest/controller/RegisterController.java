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

package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.CredentialList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;

@RestController
public class RegisterController {

	@Autowired
	private SecurityManager securityManager;

	@InjectLogger
	private static Logger logger;

	@PostMapping(value = "/register")
	public ResponseEntity<String> register(@RequestBody CredentialList list) {
		CredentialDocument user = list.getUsers().get(0);
		String username = user.getUsername();

		if (StringUtils.isNotEmpty(username)) {
			boolean isRegistered = securityManager.addUser(username, user.getEmail());

			if (isRegistered) {
				logger.debug("Account creation successful");

				return ResponseEntity
						.status(HttpStatus.OK)
						.body("created");
			} else {
				logger.debug("Account creation fail: " + username);

				return ResponseEntity
						.status(HttpStatus.BAD_REQUEST)
						.body("username exists");
			}
		}

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body("No username provided");
	}

}
