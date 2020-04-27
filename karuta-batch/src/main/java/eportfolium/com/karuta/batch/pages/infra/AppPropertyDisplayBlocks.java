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

// Based on http://tapestry.apache.org/tapestry5/guide/beaneditform.html

package eportfolium.com.karuta.batch.pages.infra;

import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.services.PropertyOutputContext;

public class AppPropertyDisplayBlocks {

	@Property
	@Environmental
	private PropertyOutputContext context;

}
