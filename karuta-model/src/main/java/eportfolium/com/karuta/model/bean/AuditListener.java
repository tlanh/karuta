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

package eportfolium.com.karuta.model.bean;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AuditListener {

	@PrePersist
	@PreUpdate
	private void beforeUpdate(Object object) {
		final Map<String, Object> properties = new HashMap<>();

		properties.put("setModifDate", Calendar.getInstance().getTime());

		for (Entry<String, Object> field : properties.entrySet()) {
			try {
				Method method = object.getClass().getMethod(field.getKey(), field.getValue().getClass());
				method.invoke(object, field.getValue());
			} catch (Exception ignored) {
			}
		}
	}
}
