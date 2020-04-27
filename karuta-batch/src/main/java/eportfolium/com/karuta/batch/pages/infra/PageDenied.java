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

package eportfolium.com.karuta.batch.pages.infra;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Response;

/**
 * Intended for use with PageProtectionFilter, this displays the path of the page to which you are not authorised.
 */
public class PageDenied {

	// Activation context

	@Property
	private String urlDenied;

	// Other useful bits and pieces

	@Inject
	private Response response;

	// The code

	String onPassivate() {
		return urlDenied;
	}

	void onActivate(String urlDenied) {
		this.urlDenied = urlDenied;
	}

	public void setupRender() {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

}
