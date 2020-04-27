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

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import eportfolium.com.karuta.business.contract.TransferManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;

@RestController
public class ExportController {

	@Autowired
	private TransferManager exportManager;

	@InjectLogger
	private static Logger logger;

	@GetMapping(value = "/export", consumes = {
			MediaType.APPLICATION_XML_VALUE }, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> export() throws Exception {

		ResponseEntity<String> response = null;

		try {
//			List<Map<String, String>> ids = 
			exportManager.transferDataFromMySQLToMongoDB();
//			if (!ids.isEmpty()) {
//				exportManager.transferDataFromMySQLToMongoDB2(ids.get(0), ids.get(1));
//			}
			response = ResponseEntity.status(HttpStatus.OK).body("created");
		} catch (Exception e) {
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Transfer not completed");
		}

		return response;
	}

}
