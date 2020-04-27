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

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class ValidateUtil {

	private static final Pattern CONFIG_NAME = Pattern.compile("^[a-zA-Z_0-9-]+$");
	private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
	private static final Pattern MD5 = Pattern.compile("^[a-f0-9A-F]{32}$");
	// private static final Integer ADMIN_PASSWORD_LENGTH = 8;
	private static final Integer PASSWORD_LENGTH = 5;

	private static final Pattern MAIL = Pattern.compile(
			"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
			Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern MAIL_NAME = Pattern.compile("^[^<>;=#{}]*$", Pattern.UNICODE_CASE);
	private static final Pattern MAIL_SUBJECT = Pattern.compile("^[^<>;{}]*$", Pattern.UNICODE_CASE);
	private static final Pattern TPL_NAME = Pattern.compile("^[a-z0-9_-]+$");
	private static final Pattern GENERIC_NAME = Pattern.compile("^[^<>;=#{}]*$", Pattern.UNICODE_CASE);
	private static final Pattern APE = Pattern.compile("^[0-9]{3,4}[a-zA-Z]{1}$", Pattern.DOTALL);
	private static final Pattern URL = Pattern.compile("^[~:#,%&_=\\(\\)\\.\\? \\+\\-@\\/a-zA-Z0-9]+$");
	private static final Pattern ABSOLUTE_URL = Pattern
			.compile("^https?:\\/\\/[~:#,%&_=\\(\\)\\[\\]\\.\\? \\+\\-@\\/a-zA-Z0-9]+$");
	private static final Pattern LanguageIsoCode = Pattern.compile("^[a-zA-Z]{2,3}$");
	private static final Pattern MODULE_NAME = Pattern.compile("^[a-zA-Z0-9_-]+$");
	private static final Pattern ORDER_BY = Pattern.compile("^[a-zA-Z0-9.!_-]+$");
	private static final Pattern POSTCODE = Pattern.compile("^[a-zA-Z 0-9-]+$");
	private static final Pattern DNILITE = Pattern.compile("^[0-9A-Za-z-.]{1,16}$", Pattern.UNICODE_CASE);

	private static Set<String> dates = new HashSet<String>();
	static {
		for (int year = 1900; year < 2050; year++) {
			for (int month = 1; month <= 12; month++) {
				for (int day = 1; day <= daysInMonth(year, month); day++) {
					StringBuilder date = new StringBuilder();
					date.append(String.format("%04d", year));
					date.append(String.format("%02d", month));
					date.append(String.format("%02d", day));
					dates.add(date.toString());
				}
			}
		}
	}

	/**
	 * Check for standard name validity
	 *
	 * @param name Name to validate
	 * @return Validity is ok or not
	 */
	static public boolean isGenericName(String name) {
		return GENERIC_NAME.matcher(name).matches();
	}

	/**
	 * Check for template name validity
	 *
	 * @param tplName Template name to validate
	 * @return Validity is ok or not
	 */
	static public boolean isTplName(String tplName) {
		return TPL_NAME.matcher(tplName).matches();
	}

	/**
	 * Check for e-mail validity
	 *
	 * @param email e-mail address to validate
	 * @return Validity is ok or not
	 */
	static public boolean isEmail(String email) {
		return MAIL.matcher(email).matches();
	}

	/**
	 * Check for configuration key validity
	 *
	 * @param config_name Configuration key to validate
	 * @return Validity is ok or not
	 */
	static public boolean isConfigName(String config_name) {
		return CONFIG_NAME.matcher(config_name).matches();
	}

	/**
	 * Check for sender name validity
	 *
	 * @param mailName Sender name to validate
	 * @return Validity is ok or not
	 */
	static public boolean isMailName(String mailName) {
		return MAIL_NAME.matcher(mailName).matches();
	}

	/**
	 * Check for e-mail subject validity
	 *
	 * @param mailSubject e-mail subject to validate
	 * @return Validity is ok or not
	 */
	static public boolean isMailSubject(String mailSubject) {
		return MAIL_SUBJECT.matcher(mailSubject).matches();
	}

	/**
	 * Check for an integer validity
	 *
	 * @param value Integer to validate
	 * @return Validity is ok or not
	 */
	static public boolean isInt(final String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Check for an integer validity (unsigned)
	 *
	 * @param value Integer to validate
	 * @return Validity is ok or not
	 */
	static public boolean isUnsignedInt(final String value) {
		if (isInt(value)) {
			Integer newValue = Integer.parseInt(value);
			if (newValue < 4294967296L && newValue >= 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean isUnsignedId(int id) {
		return isUnsignedInt(id);
	}

	/**
	 * Check for an integer validity (unsigned)
	 *
	 * @param value Integer to validate
	 * @return Validity is ok or not
	 */
	static public boolean isUnsignedInt(final Integer value) {
		if (value < 4294967296L && value >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Check object validity
	 *
	 * @param obj Object to validate
	 * @return Validity is ok or not
	 */
	static public boolean isLoadedObject(Object obj) {
		try {
			Method getter = obj.getClass().getMethod("getId");
			Integer i = (Integer) getter.invoke(obj);
			if (i != null && i != 0 && isUnsignedInt(i)) {
				return true;
			} else if (i == 0) {
				obj = null;
			}
		} catch (Exception e) {
			return isLoadedObject2(obj);
		}
		return false;
	}

	static private boolean isLoadedObject2(Object obj) {
		try {
			String className = obj.getClass().getSimpleName().substring(2);
			Method method = obj.getClass().getMethod("getId" + className);
			Integer i = (Integer) method.invoke(obj);
			if (i != null && i != 0 && isUnsignedInt(i)) {
				return true;
			} else if (i == 0) {
				obj = null;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Fonction permettant de contrôler la validité d'un numéro SIREN
	 * 
	 * @param siren
	 * @return
	 */
	static public boolean isSiren(String siren) {
		if (siren.length() != 9)
			return false; // le SIREN doit contenir 9 caractères
		if (!NumberUtils.isDigits(siren))
			return false; // le SIREN ne doit contenir que des chiffres

		// on prend chaque chiffre un par un
		// si son index (position dans la chaîne en commence à 0 au premier caractère)
		// est impair
		// on double sa valeur et si cette dernière est supérieure à 9, on lui retranche
		// 9
		// on ajoute cette valeur à la somme totale

		int sum = 0, number;
		for (int index = 0; index < 9; index++) {
			number = Integer.parseInt(String.valueOf(siren.charAt(index)));
			if ((index % 2) != 0) {
				if ((number *= 2) > 9)
					number -= 9;
			}
			sum += number;
		}

		// le numéro est valide si la somme des chiffres est multiple de 10
		if ((sum % 10) != 0)
			return false;
		else
			return true;
	}

	/**
	 * Validate SIRET Code Fonction permettant de contrôler la validité d'un numéro
	 * SIRET.
	 * 
	 * @static
	 * @param siret SIRET Code
	 * @return Return true if is valid
	 */
	static public boolean isSiret(String siret) {
		if (siret.length() != 14)
			return false; // le SIRET doit contenir 14 caractères
		if (!NumberUtils.isDigits(siret))
			return false; // le SIRET ne doit contenir que des chiffres

		// on prend chaque chiffre un par un
		// si son index (position dans la chaîne en commence à 0 au premier caractère)
		// est pair
		// on double sa valeur et si cette dernière est supérieure à 9, on lui retranche
		// 9
		// on ajoute cette valeur à la somme totale

		int sum = 0, number, tmp;
		for (int index = 0; index < 14; index++) {
			number = Integer.parseInt(String.valueOf(siret.charAt(index)));
			tmp = (((index + 1) % 2) + 1) * number;
			if (tmp >= 10) {
				tmp -= 9;
			}
			sum += tmp;
		}

		// le numéro est valide si la somme des chiffres est multiple de 10
		if ((sum % 10) != 0)
			return false;
		else
			return true;
	}

	// fonction permettant de contrôler la validité d'un code-barres de type EAN-13
	static public boolean isEan13(String ean13) {
		if (ean13.length() != 13)
			return false; // le code-barres doit contenir 13 caractères
		if (!NumberUtils.isDigits(ean13))
			return false; // le code-barres ne doit contenir que des chiffres

		// on prend chaque chiffre un par un
		// si son index (position dans la chaîne en commence à 0 au premier caractère)
		// est impair
		// on triple sa valeurF
		// on ajoute cette valeur à la somme totale

		int sum = 0, number;
		for (int index = 0; index < 12; index++) {
			number = Integer.parseInt(String.valueOf(ean13.charAt(index)));
			if ((index % 2) != 0)
				number *= 3;
			sum += number;
		}

		int key = Integer.parseInt(String.valueOf(ean13.charAt(12))); // clé de contrôle égale au dernier chiffre

		// la clé de contrôle doit être égale à : 10 - (reste de la division de la somme
		// des 12 premiers chiffres)
		if (10 - (sum % 10) != key)
			return false;
		else
			return true;
	}

	/**
	 * Validate APE Code
	 * 
	 * @static
	 * @param ape APE Code
	 * @return Return true if is valid
	 */
	static public boolean isApe(String ape) {
		return APE.matcher(ape).matches();
	}

	/**
	 * Check url validity (disallowed empty string)
	 *
	 * @param url Url to validate
	 * @return Validity is ok or not
	 */
	static public boolean isUrl(String url) {
		return URL.matcher(url).matches();
	}

	/**
	 * Check url validity (allowed empty string)
	 *
	 * @param url Url to validate
	 * @return Validity is ok or not
	 */
	static public boolean isUrlOrEmpty(String url) {
		return StringUtils.isEmpty(url) || isUrl(url);
	}

	/**
	 * Check if URL is absolute
	 *
	 * @param url URL to validate
	 * @return Validity is ok or not
	 */
	static public boolean isAbsoluteUrl(String url) {
		if (StringUtils.isNotEmpty(url)) {
			return ABSOLUTE_URL.matcher(url).matches();
		}
		return true;
	}

	/**
	 * Check for password validity
	 *
	 * @param passwd Password to validate
	 * @return Validity is ok or not
	 */
	public static boolean isPasswd(String passwd) {
		return isPasswd(passwd, PASSWORD_LENGTH);
	}

	/**
	 * Check for password validity
	 *
	 * @param passwd Password to validate
	 * @param size
	 * @return Validity is ok or not
	 */
	public static boolean isPasswd(String passwd, Integer size) {
		return (Tools.strlen(passwd) >= size && Tools.strlen(passwd) < 255);
	}

	/**
	 * Check for language code (ISO) validity
	 *
	 * @param string isoCode Language code (ISO) to validate
	 * @return Validity is ok or not
	 */
	public static boolean isLanguageIsoCode(String isoCode) {
		return LanguageIsoCode.matcher(isoCode).matches();
	}

	/**
	 * Check for HTML field validity (no XSS please !)
	 *
	 * @param bodyHtml HTML field to validate
	 * @return bool Validity is ok or not
	 */
	public static boolean isCleanHtml(String bodyHtml) {
		return Jsoup.isValid(bodyHtml, Whitelist.basic());
	}
	/*
	 * public static boolean isLanguageCode(String s) { return
	 * preg_match('/^[a-zA-Z]{2}(-[a-zA-Z]{2})?$/', s); }
	 * 
	 * public static boolean isStateIsoCodeprivate { return
	 * preg_match('/^[a-zA-Z0-9]{1,4}((-)[a-zA-Z0-9]{1,4})?$/', iso_code); }
	 * 
	 * public static boolean isNumericIsoCode(String iso_code) { return
	 * preg_match('/^[0-9]{2,3}$/', iso_code); }
	 */

	/**
	 * Check for MD5 string validity
	 *
	 * @param md5 MD5 string to validate
	 * @return Validity is ok or not
	 */
	public static boolean isMd5(String md5) {
		return MD5.matcher(md5).matches();
	}

	/**
	 * Check for SHA1 string validity
	 *
	 * @param sha1 SHA1 string to validate
	 * @return Validity is ok or not
	 */
	public static boolean isSha1(String sha1) {
		return SHA1.matcher(sha1).matches();
	}

	public static boolean isModuleName(String module_name) {
		return MODULE_NAME.matcher(module_name).matches();
	}

	/**
	 * Check for table or identifier validity Mostly used in database for ordering :
	 * ORDER BY field
	 *
	 * @param order Field to validate
	 * @return Validity is ok or not
	 */
	public static boolean isOrderBy(String order) {
		return ORDER_BY.matcher(order).matches();
	}

	/**
	 * Check for table or identifier validity Mostly used in database for ordering :
	 * ASC / DESC
	 *
	 * @param way Keyword to validate
	 * @return Validity is ok or not
	 */
	public static boolean isOrderWay(String way) {
		return (StringUtils.equals(way, "ASC") | StringUtils.equals(way, "DESC") | StringUtils.equals(way, "asc")
				| StringUtils.equals(way, "desc"));
	}

	/**
	 * Check for postal code validity
	 *
	 * @param String postcode Postal code to validate
	 * @return Validity is ok or not
	 */
	public static boolean isPostCode(String postcode) {
		return StringUtils.isEmpty(postcode) || POSTCODE.matcher(postcode).matches();
	}

	/**
	 * @param string dni to validate
	 * @return boolean
	 */
	public static boolean isDniLite(String dni) {
		return StringUtils.isEmpty(dni) || DNILITE.matcher(dni).matches();
	}

	private static int daysInMonth(int year, int month) {
		int daysInMonth;
		switch (month) {
		case 1: // fall through
		case 3: // fall through
		case 5: // fall through
		case 7: // fall through
		case 8: // fall through
		case 10: // fall through
		case 12:
			daysInMonth = 31;
			break;
		case 2:
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				daysInMonth = 29;
			} else {
				daysInMonth = 28;
			}
			break;
		default:
			// returns 30 even for nonexistant months
			daysInMonth = 30;
		}
		return daysInMonth;
	}

	public static boolean isValidDate(String dateString) {
		return dates.contains(dateString);
	}

	public static boolean isValidDate(Date d, SimpleDateFormat sdf) {
		if (d == null || sdf == null) {
			return false;
		}
		return dates.contains(sdf.format(d));
	}

}
