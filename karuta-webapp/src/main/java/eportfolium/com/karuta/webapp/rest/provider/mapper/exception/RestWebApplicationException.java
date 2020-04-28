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

package eportfolium.com.karuta.webapp.rest.provider.mapper.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class RestWebApplicationException extends Exception {
	private static final long serialVersionUID = -4729775688199298967L;

	HttpStatus stat;
	String msg;

	public RestWebApplicationException(HttpStatus status, String message) {
		super(ResponseEntity
				.status(status)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
				.body(message)
				.toString());
		msg = message;
		stat = status;
	}

	public String getCustomMessage() {
		return msg;
	}

	public HttpStatus getStatus() {
		return stat;
	}
}
