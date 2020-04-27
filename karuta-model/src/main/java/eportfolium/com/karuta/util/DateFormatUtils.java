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

public class DateFormatUtils {
	private static String[][] conversionTable = new String[][] { { "yy", "Y" }, { "y", "y" }, { "MMMMM", "F" },
			{ "MMMM", "F" }, { "MMM", "M" }, { "MM", "m" }, { "EEEEEE", "l" }, { "EEEEE", "l" }, { "EEEE", "l" },
			{ "EEE", "D" }, { "dd", "d" }, { "HH", "H" }, { "mm", "i" }, { "ss", "s" }, { "hh", "h" }, { "A", "a" },
			{ "S", "u" } };

	/**
	 * Converts PHP date format (used in DateField) to java date format (used in e.g. GridPanel) 
	 *
	 * @param phpFormat e.g. d-m-y
	 * @return e.g. dd-MM-yyyy
	 */
	public static String convertPHPToJava(String phpFormat) {
		String result = phpFormat;
		for (int i = 0; i < conversionTable.length; i++) {
			result = PhpUtil.preg_replace(conversionTable[i][1], conversionTable[i][0], result, -1, null);
		}
		return result;
	}

	/**
	 * Converts java date format (used in e.g. GridPanel) to PHP date format (used in DateField)
	 *
	 * @param javaFormat e.g. dd-MM-yyyy
	 * @return e.g. d-m-y
	 */
	public static String convertJavaToPHP(String javaFormat) {
		String result = javaFormat;
		for (int i = 0; i < conversionTable.length; i++) {
			result = PhpUtil.preg_replace(conversionTable[i][0], conversionTable[i][1], result, -1, null);
		}
		return result;
	}

}
