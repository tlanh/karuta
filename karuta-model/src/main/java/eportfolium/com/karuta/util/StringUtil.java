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

public class StringUtil {

	static public boolean isEmpty(String s) {
		if ((s == null) || (s.trim().length() == 0)) {
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static String abbreviate(String s, int maxLen) {
		if (s == null) {
			return null;
		}
		else {
			return s.length() > maxLen ? (s.substring(0, maxLen) + "...") : s.substring(0, s.length());
		}
	}

	/**
	 * Escape string to protected against SQL Injection
	 *
	 * You must add a single quote ' around the result of this function for data, or a backtick ` around table and row
	 * identifiers. If this function returns null than the result should be changed to "NULL" without any quote or
	 * backtick.
	 *
	 * @param str
	 * @return
	 * @throws Exception
	 */

	public static String mysql_real_escape_string(String str) {
		if (str == null) {
			return null;
		}

		if (str.replaceAll(
				"[a-zA-Z0-9ÀÁÂÃÄÅàáâãäåÒÓÔÕÖØòóôõöøÈÉÊËèéêëÇçÌÍÎÏìíîïÙÚÛÜùúûüÿÑñ_!@#$%^&*()-=+~.;:,\\\\Q[\\\\E\\\\Q]\\\\E<>{}\\\\/? ]",
				"").length() < 1) {
			return str;
		}

		String clean_string = str;
		clean_string = clean_string.replaceAll("\\\\", "\\\\\\\\");
		clean_string = clean_string.replaceAll("\\n", "\\\\n");
		clean_string = clean_string.replaceAll("\\r", "\\\\r");
		clean_string = clean_string.replaceAll("\\t", "\\\\t");
		clean_string = clean_string.replaceAll("\\00", "\\\\0");
		clean_string = clean_string.replaceAll("'", "\\\\'");
		clean_string = clean_string.replaceAll(",", "\\\\,");
		clean_string = clean_string.replaceAll("\\\"", "\\\\\"");

		if (clean_string.replaceAll(
				"[a-zA-Z0-9ÀÁÂÃÄÅàáâãäåÒÓÔÕÖØòóôõöøÈÉÊËèéêëÇçÌÍÎÏìíîïÙÚÛÜùúûüÿÑñ_!@#$%^&*()-=+~.;:,\\\\Q[\\\\E\\\\Q]\\\\E<>{}\\\\/? ]",
				"").length() < 1) {
			return clean_string;
		}
		return str;
	}

}
