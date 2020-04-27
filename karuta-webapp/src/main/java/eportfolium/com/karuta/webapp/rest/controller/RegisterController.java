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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.DomUtils;

@RestController
public class RegisterController {

	@Autowired
	private SecurityManager securityManager;

	@InjectLogger
	private static Logger logger;

	@PostMapping(value = "/register", consumes = {
			MediaType.APPLICATION_XML_VALUE }, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> register(@RequestBody String data) throws Exception {

		Document doc = DomUtils.xmlString2Document(data, new StringBuffer());
		Element credentialElement = doc.getDocumentElement();

		String username = null;
		String mail = null;
		boolean hasChanged = false;

		if (credentialElement.getNodeName().equals("users")) {
			NodeList children = credentialElement.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeName().equals("user")) {
					NodeList children2 = null;
					children2 = children.item(i).getChildNodes();
					for (int y = 0; y < children2.getLength(); y++) {
						if (children2.item(y).getNodeName().equals("username")) {
							username = DomUtils.getInnerXml(children2.item(y));
						}
						if (children2.item(y).getNodeName().equals("email")) {
							mail = DomUtils.getInnerXml(children2.item(y));
						}
					}
					break;
				}
			}
		}

		if (StringUtils.isNotEmpty(username)) {
			boolean isRegistered = securityManager.addUser(username, mail, true, 1L);
			if (isRegistered) {
				logger.debug("Account creation successful");
				hasChanged = true;
			} else
				logger.debug("Account creation fail: " + username);
		}

		ResponseEntity<String> response = null;

		// Username should be in an email format
		if (hasChanged) {
			response = ResponseEntity.status(HttpStatus.OK).body("created");
		} else {
			response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username exists");
		}
		return response;
	}

}
