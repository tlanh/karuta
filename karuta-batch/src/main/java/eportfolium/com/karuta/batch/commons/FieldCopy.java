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
