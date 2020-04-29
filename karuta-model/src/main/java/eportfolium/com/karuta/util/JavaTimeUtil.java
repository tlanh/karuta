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

package eportfolium.com.karuta.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class JavaTimeUtil {
	public static ZoneId paris = ZoneId.of("Europe/Paris");
	public static ZoneId date_default_timezone = ZoneId.of("Europe/Paris");
	public static final DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("YYYYMMDD");


	public static java.util.Date toJavaDate(LocalDate ld) {
		return (ld == null ? null : Date.from(ld.atStartOfDay(paris).toInstant()));
	}

	public static String toString(LocalDate ld) {
		String s = (ld == null ? null : localDateFormatter.withZone(ZoneId.of("UTC")).format(ld));
		return s;
	}

	public static java.util.Date toJavaDate(LocalDateTime ldt) {
		java.util.Date d = (ldt == null ? null : Date.from(ldt.atZone(paris).toInstant()));
		return d;
	}

	public static String toString(LocalTime lt) {
		String s = (lt == null ? null : lt.toString());
		return s;
	}


	public static LocalDate toLocalDate(java.util.Date d) {
		LocalDate ld = (d == null ? null : d.toInstant().atZone(paris).toLocalDate());
		return ld;
	}
}
