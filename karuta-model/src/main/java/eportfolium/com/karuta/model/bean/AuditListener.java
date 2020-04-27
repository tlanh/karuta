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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class AuditListener {

	@PrePersist
	private void beforePersist(Object object) {
		final Map<String, Object> defaultsProperties = new HashMap<String, Object>();
		final Date d = Calendar.getInstance().getTime();
		defaultsProperties.put("setModifDate", d);
		Entry<String, Object> tmpProperty = null;
		Method method = null;
		for (Iterator<Entry<String, Object>> property = defaultsProperties.entrySet().iterator(); property.hasNext();) {
			tmpProperty = property.next();
			try {
				method = object.getClass().getMethod(tmpProperty.getKey(), tmpProperty.getValue().getClass());
				method.invoke(object, tmpProperty.getValue());
			} catch (Exception e) {
			}
		}
	}

	@PreUpdate
	private void beforeUpdate(Object object) {
		final Map<String, Object> defaultsProperties = new HashMap<String, Object>();
		final Date d = Calendar.getInstance().getTime();
		defaultsProperties.put("setModifDate", d);
		Entry<String, Object> tmpProperty = null;
		Method method = null;
		for (Iterator<Entry<String, Object>> property = defaultsProperties.entrySet().iterator(); property.hasNext();) {
			tmpProperty = property.next();
			try {
				method = object.getClass().getMethod(tmpProperty.getKey(), tmpProperty.getValue().getClass());
				method.invoke(object, tmpProperty.getValue());
			} catch (Exception e) {
			}
		}
	}

	@PreRemove
	private void beforeRemove(Object object) {
	}

}
