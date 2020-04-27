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

package eportfolium.com.karuta.batch.exception;

import org.apache.tapestry5.Link;

public class RedirectException extends RuntimeException {
	private static final long serialVersionUID = -2927007059196754228L;

	protected Link pageLink;
	protected Class<?> pageClass;

	public RedirectException(String pageName) {
		super(pageName);
	}

	public RedirectException(Class<?> pageClass) {
		this.pageClass = pageClass;
	}

	public RedirectException(Link link) {
		this.pageLink = link;
	}

	public Link getPageLink() {
		return pageLink;
	}

	public Class<?> getPageClass() {
		return pageClass;
	}
}
