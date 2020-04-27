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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.FieldValidator;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.DateField;
import org.apache.tapestry5.services.PropertyEditContext;

public class AppPropertyEditBlocks {

	@Property
	@Environmental
	private PropertyEditContext context;

	@InjectComponent
	private DateField dateMidnight;

	@InjectComponent
	private DateField localDate;

	public DateFormat getDateInputFormat() {
		return new SimpleDateFormat("dd MMMM yyyy");
	}

	public FieldTranslator<?> getDateMidnightTranslator() {
		return context.getTranslator(dateMidnight);
	}

	public FieldValidator<?> getDateMidnightValidator() {
		return context.getValidator(dateMidnight);
	}

	public FieldTranslator<?> getLocalDateTranslator() {
		return context.getTranslator(localDate);
	}

	public FieldValidator<?> getLocalDateValidator() {
		return context.getValidator(localDate);
	}

}
