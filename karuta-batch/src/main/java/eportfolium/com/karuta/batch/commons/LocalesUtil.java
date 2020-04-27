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

package eportfolium.com.karuta.batch.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocalesUtil {

	public static List<Locale> convertToLocales(String supportedLocaleCodesString) {
		List<Locale> locales = new ArrayList<Locale>();

		String[] localeCodes = supportedLocaleCodesString.split(",");
		for (String localeCode : localeCodes) {
			locales.add(convertToLocale(localeCode));
		}

		return locales;
	}

	public static Locale convertToLocale(String localeCode) {
		Locale locale = null;
		String[] elements = localeCode.split("_");

		switch (elements.length) {
		case 1:
			locale = new Locale(elements[0]);
			break;
		case 2:
			locale = new Locale(elements[0], elements[1]);
			break;
		case 3:
			locale = new Locale(elements[0], elements[1], elements[2]);
			break;
		case 4:
			locale = new Locale(elements[0], elements[1], elements[2] + "_" + elements[3]);
			break;
		default:
			throw new RuntimeException(
					"Can't handle localeCode = \"" + localeCode + "\".  Elements.length = " + elements.length);
		}

		return locale;
	}
}