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

// Based on a solution by Stephan Windmüller in http://tapestry.1045711.n5.nabble.com/Cross-Validation-in-dynamic-Forms-td2427275.html 
// and Shing Hing Man in http://tapestry.1045711.n5.nabble.com/how-to-recordError-against-a-form-field-in-a-loop-td5719832.html .

package eportfolium.com.karuta.batch.commons;

import org.apache.tapestry5.Field;

/**
 * An immutable copy of a Field. Handy for taking a copy of a Field in a row as
 * a Loop iterates through them.
 */
public class FieldCopy implements Field {
	private String clientId;
	private String controlName;
	private String label;
	private boolean disabled;
	private boolean required;

	public FieldCopy(Field field) {
		clientId = field.getClientId();
		controlName = field.getControlName();
		label = field.getLabel();
		disabled = field.isDisabled();
		required = field.isRequired();
	}

	public String getClientId() {
		return clientId;
	}

	public String getControlName() {
		return controlName;
	}

	public String getLabel() {
		return label;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public boolean isRequired() {
		return required;
	}

}
