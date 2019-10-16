package eportfolium.com.karuta.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.core.io.ResourceLoader;

import eportfolium.com.karuta.config.Consts;
import eportfolium.com.karuta.config.Settings;

public class Tools {

	private static Map<String, Boolean> file_exists_cache = new HashMap<String, Boolean>();

	/**
	 * Each token produced by this class uses this identifier as a prefix.
	 */
	public static final String ID = "$31$";

	/**
	 * The minimum recommended cost, used by default
	 */
	public static final int DEFAULT_COST = 16;

	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";

	private static final int SIZE = 128;

	private static final int cost = iterations(DEFAULT_COST);

	private static final SecureRandom random = new SecureRandom();

	private static Map<String, String> array_str = new HashMap<>();
	private static Boolean allow_accented_chars = null;
	private static Boolean has_mb_strtolower = Boolean.TRUE;

	private static final Log log = LogFactory.getLog(Tools.class);

	/**
	 * Sanitize a string
	 *
	 * @param string string String to sanitize
	 * @param bool   full String contains HTML or not (optional)
	 * @return string Sanitized string
	 */
	public static String safeOutput(String string) {
		return safeOutput(string, false);
	}

	/**
	 * Sanitize a string
	 *
	 * @param string string String to sanitize
	 * @param bool   full String contains HTML or not (optional)
	 * @return string Sanitized string
	 */
	public static String safeOutput(String string, Boolean html) {
		if (!html) {
			string = PhpUtil.strip_tags(string);
		}
		final List<String> output = Arrays.asList(string);
		htmlentitiesUTF8(output);
		return output.get(0);
	}

	public static void htmlentitiesUTF8(List<String> strings) {
		for (ListIterator<String> it = strings.listIterator(); it.hasNext();) {
			it.set(htmlentitiesUTF8(it.next()));
		}
	}

	public static String htmlentitiesUTF8(String string) {
		return StringEscapeUtils.escapeHtml4(string);
	}

	public static int strlen(String string) {
		return StringUtils.defaultString(StringEscapeUtils.unescapeHtml4(string)).length();
	}

	public static String passwdGen(Integer length) {
		return passwdGen(length, "ALPHANUMERIC");
	}

	/**
	 * Random password generator
	 * 
	 * @param length Desired length (optional)
	 * @param flag   Output type (NUMERIC, ALPHANUMERIC, NO_NUMERIC, RANDOM)
	 * @return null | string Password
	 */
	public static String passwdGen(Integer length, String flag) {
		if (length <= 0) {
			return null;
		}

		String str = null;
		Random random = new Random();
		switch (flag) {
		case "NUMERIC":
			str = "0123456789";
			break;
		case "NO_NUMERIC":
			str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			break;
		case "RANDOM":
			Double num_bytes = Math.ceil(length * 0.75);
			byte[] bytes = new byte[num_bytes.intValue()];
			random.nextBytes(bytes);
			return new String(Base64.encodeBase64(bytes)).replaceAll("\\s+$", "").substring(0, length);
		case "ALPHANUMERIC":
		default:
			str = "abcdefghijkmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			break;
		}

		byte[] tmp = new byte[length];
		random.nextBytes(tmp);
		// String bytes = new String(tmp);

		Integer position = 0;
		String result = "";

		for (int i = 0; i < length; i++) {
			// position = (position + ASCII.toASCII(bytes.charAt(i))) % str.length();
			position = (position + Math.abs(tmp[i])) % str.length();
			result += str.charAt(position);
		}
		return result;
	}

	private static int iterations(int cost) {
		if ((cost < 0) || (cost > 31))
			throw new IllegalArgumentException("cost: " + cost);
		return 1 << cost;
	}

	public static String encrypt(char[] password) {
		byte[] salt = new byte[SIZE / 8];
		random.nextBytes(salt);
		byte[] dk = pbkdf2(password, salt, 1 << cost);
		byte[] hash = new byte[salt.length + dk.length];
		System.arraycopy(salt, 0, hash, 0, salt.length);
		System.arraycopy(dk, 0, hash, salt.length, dk.length);
		java.util.Base64.Encoder enc = java.util.Base64.getUrlEncoder().withoutPadding();
		return ID + cost + '$' + enc.encodeToString(hash);
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
		KeySpec spec = new PBEKeySpec(password, salt, iterations, SIZE);
		try {
			SecretKeyFactory f = SecretKeyFactory.getInstance(ALGORITHM);
			return f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Missing algorithm: " + ALGORITHM, ex);
		} catch (InvalidKeySpecException ex) {
			throw new IllegalStateException("Invalid SecretKeyFactory", ex);
		}
	}

	/**
	 * Before:
	 * <p>
	 * <a href='http://example.com/' onclick='stealCookies()'>Link</a>
	 * </p>
	 * "; After :
	 * <p>
	 * <a href="http://example.com/" rel="nofollow">Link</a>
	 * </p>
	 * 
	 * @param unsafeHTML
	 * @return
	 */
	public static String purifyHTML(String unsafeHTML) {
		return Jsoup.clean(unsafeHTML, Whitelist.basic());
	}

	public static String br2nl(String html) {
		Document document = Jsoup.parse(html);
		document.select("br").append("\\n");
		document.select("p").prepend("\\n\\n");
		return document.text().replace("\\n", "\n");
	}

	public static String nl2br(String text) {
		return text.replace("\n\n", "<p>").replace("\n", "<br>");
	}

	/**
	 * Display date regarding to language preferences
	 *
	 * @param string date Date to display format UNIX
	 * @param int    lang Language id DEPRECATED
	 * @param bool   full With time or not (optional)
	 * @return string Date
	 */
	public static String displayDate(Date date, Boolean full) {
		String result = "";
		if (date != null) {
			String format = full ? Settings.KB_DATE_FORMAT_FULL : Settings.DATE_FORMAT_LITE;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateFormatUtils.convertPHPToJava(format));
			result = simpleDateFormat.format(date);
		}
		return result;
	}

	/**
	 * Return price with currency sign for a given product
	 * 
	 * @param price    Product price
	 * @param currency Current currency (object, id_currency, NULL => context
	 *                 currency)
	 * @param no_utf8
	 * @param context
	 * @return Price correctly formated (sign, decimal separator...) if you modify
	 *         this function, don't forget to modify the Javascript function
	 *         formatCurrency (in tools.js)
	 */

	public static String generateNumberSigns(int n) {

		String s = "";
		for (int i = 0; i < n; i++) {
			s += "#";
		}
		return s;
	}

	/**
	 * returns the rounded value of {@value} to specified precision, according to
	 * your configuration;
	 *
	 * @param value
	 * @param precision
	 * @param round_mode
	 * @return BigDecimal
	 */
	public static BigDecimal ps_round(BigDecimal value, int precision, Integer round_mode) {

		switch (round_mode) {
		case Consts.PS_ROUND_UP:
			return value.setScale(precision, RoundingMode.CEILING);
		case Consts.PS_ROUND_DOWN:
			return value.setScale(precision, RoundingMode.FLOOR);
		case Consts.PS_ROUND_HALF_DOWN:
			return value.setScale(precision, RoundingMode.HALF_DOWN);
		case Consts.PS_ROUND_HALF_EVEN:
			return value.setScale(precision, RoundingMode.HALF_EVEN);
		case Consts.PS_ROUND_HALF_ODD:
			return new BigDecimal(RoundToNearestRoundHalfToOdd(value));
		case Consts.PS_ROUND_HALF_UP:
		default:
			return value.setScale(precision, RoundingMode.HALF_UP);
		}
	}

	/**
	 * First round half toward positive infinity. Then if the fraction part of the
	 * original value is 0.5 and the result of rounding is even, then subtract one.
	 * 
	 * @param value
	 * @return
	 */
	private static int RoundToNearestRoundHalfToOdd(BigDecimal value) {
		int temp = (int) Math.floor(value.add(new BigDecimal(0.5)).doubleValue());
		if (value.subtract(new BigDecimal(Math.floor(value.doubleValue()))).equals(new BigDecimal("0.5"))
				&& temp % 2 == 0)
			temp -= 1;
		return temp;
	}

	/**
	 * Vérifie si un fichier ou un dossier existe (wrapper avec cache pour accélérer
	 * les performances)
	 *
	 * @param filename Chemin vers le fichier ou le dossier.
	 * @param loader
	 * @return bool Résultat en cache de (@filename)
	 */
	public static boolean file_exists_cache(String filename, ResourceLoader loader) {
		if (!file_exists_cache.containsKey(filename)) {
			file_exists_cache.put(filename, ((ResourceLoader) loader).getResource(filename).exists());
		}
		return file_exists_cache.get(filename);
	}

	/**
	 * Check if a constant was already set
	 * 
	 * @param constant Constant
	 * @param value    Default value to set if not defined
	 */
	public static void safeDefine(String constant, String value) {
		if (StringUtils.isBlank(constant)) {
			constant = value;
		}
	}

	public static boolean isEmpty(String field) {
		return (field == null || field.equals(""));
	}

	public static String displayError() {
		String s = "Fatal error";
		log.error(s);
		return s;
	}

	public static String displayError(String string) {
		return displayError(string, true);
	}

	public static String displayError(String string, boolean htmlentities) {
		log.error(htmlentities ? htmlentitiesUTF8(string) : string);
		return htmlentities ? htmlentitiesUTF8(string) : string;
	}

	public static void redirect(String html_entity_decode) {

	}

	public static String substr(String str, int start, int length) {
		return StringUtils.substring(str, start, length);
	}

	/**
	 * Return the friendly url from the provided string
	 *
	 * @param string str
	 * @return String
	 */
	public static String link_rewrite(String str) {
		return Tools.str2url(str);
	}

	/**
	 * Return a friendly url made from the provided string If the mbstring library
	 * is available, the output is the same as the js function of the same name
	 *
	 * @param string str
	 * @return string
	 */
	/**
	 * Return a friendly url made from the provided string If the mbstring library
	 * is available, the output is the same as the js function of the same name
	 *
	 * @param string str
	 * @return string
	 */
	public static String str2url(String str) {

		if (array_str.containsKey(str)) {
			return array_str.get(str);
		}

		if (StringUtils.isEmpty(str)) {
			return "";
		}

		if (allow_accented_chars == null) {
			allow_accented_chars = false;
			// Configuration.get("PS_ALLOW_ACCENTED_CHARS_URL");
		}

		String return_str = str.trim();

		if (has_mb_strtolower) {
			return_str = return_str.toLowerCase();
		}
		if (!allow_accented_chars) {
			return_str = Tools.replaceAccentedChars(return_str);
		}

		// Remove all non-whitelist chars.
		if (allow_accented_chars) {
			return_str = PhpUtil.preg_replace("[^a-zA-Z0-9\\s\\'\\:\\/\\[\\]\\-\\p{L}]/u", "", return_str);
		} else {
			return_str = PhpUtil.preg_replace("[^a-zA-Z0-9\\s\\'\\:\\/\\[\\]\\-]", "", return_str);
		}

		return_str = PhpUtil.preg_replace("/[\\s\\'\\:\\/\\[\\]\\-]+/", " ", return_str);
		return_str = PhpUtil.str_replace(Arrays.asList(" ", "/"), "-", return_str);

		// If it was not possible to lowercase the string with mb_strtolower, we do it
		// after the transformations.
		// This way we lose fewer special chars.
		if (!has_mb_strtolower) {
			return_str = return_str.toLowerCase();
		}

		array_str.put(str, return_str);
		return return_str;
	}

	/**
	 * Replace all accented chars by their equivalent non accented chars.
	 *
	 * @param string str
	 * @return string
	 */
	public static String replaceAccentedChars(String str) {

		/*
		 * One source among others: http://www.tachyonsoft.com/uc0000.htm
		 * http://www.tachyonsoft.com/uc0001.htm http://www.tachyonsoft.com/uc0004.htm
		 */
		List<String> patterns = Arrays.asList(

				/* Lowercase */
				/* a */ "/[\\x{00E0}\\x{00E1}\\x{00E2}\\x{00E3}\\x{00E4}\\x{00E5}\\x{0101}\\x{0103}\\x{0105}\\x{0430}\\x{00C0}-\\x{00C3}\\x{1EA0}-\\x{1EB7}]/u",
				/* b */ "/[\\x{0431}]/u", /* c */ "/[\\x{00E7}\\x{0107}\\x{0109}\\x{010D}\\x{0446}]/u",
				/* d */ "/[\\x{010F}\\x{0111}\\x{0434}\\x{0110}]/u",
				/* e */ "/[\\x{00E8}\\x{00E9}\\x{00EA}\\x{00EB}\\x{0113}\\x{0115}\\x{0117}\\x{0119}\\x{011B}\\x{0435}\\x{044D}\\x{00C8}-\\x{00CA}\\x{1EB8}-\\x{1EC7}]/u",
				/* f */ "/[\\x{0444}]/u", /* g */ "/[\\x{011F}\\x{0121}\\x{0123}\\x{0433}\\x{0491}]/u",
				/* h */ "/[\\x{0125}\\x{0127}]/u",
				/* i */ "/[\\x{00EC}\\x{00ED}\\x{00EE}\\x{00EF}\\x{0129}\\x{012B}\\x{012D}\\x{012F}\\x{0131}\\x{0438}\\x{0456}\\x{00CC}\\x{00CD}\\x{1EC8}-\\x{1ECB}\\x{0128}]/u",
				/* j */ "/[\\x{0135}\\x{0439}]/u", /* k */ "/[\\x{0137}\\x{0138}\\x{043A}]/u",
				/* l */ "/[\\x{013A}\\x{013C}\\x{013E}\\x{0140}\\x{0142}\\x{043B}]/u", /* m */ "/[\\x{043C}]/u",
				/* n */ "/[\\x{00F1}\\x{0144}\\x{0146}\\x{0148}\\x{0149}\\x{014B}\\x{043D}]/u",
				/* o */ "/[\\x{00F2}\\x{00F3}\\x{00F4}\\x{00F5}\\x{00F6}\\x{00F8}\\x{014D}\\x{014F}\\x{0151}\\x{043E}\\x{00D2}-\\x{00D5}\\x{01A0}\\x{01A1}\\x{1ECC}-\\x{1EE3}]/u",
				/* p */ "/[\\x{043F}]/u", /* r */ "/[\\x{0155}\\x{0157}\\x{0159}\\x{0440}]/u",
				/* s */ "/[\\x{015B}\\x{015D}\\x{015F}\\x{0161}\\x{0441}]/u", /* ss */ "/[\\x{00DF}]/u",
				/* t */ "/[\\x{0163}\\x{0165}\\x{0167}\\x{0442}]/u",
				/* u */ "/[\\x{00F9}\\x{00FA}\\x{00FB}\\x{00FC}\\x{0169}\\x{016B}\\x{016D}\\x{016F}\\x{0171}\\x{0173}\\x{0443}\\x{00D9}-\\x{00DA}\\x{0168}\\x{01AF}\\x{01B0}\\x{1EE4}-\\x{1EF1}]/u",
				/* v */ "/[\\x{0432}]/u", /* w */ "/[\\x{0175}]/u",
				/* y */ "/[\\x{00FF}\\x{0177}\\x{00FD}\\x{044B}\\x{1EF2}-\\x{1EF9}\\x{00DD}]/u",
				/* z */ "/[\\x{017A}\\x{017C}\\x{017E}\\x{0437}]/u", /* ae */ "/[\\x{00E6}]/u",
				/* ch */ "/[\\x{0447}]/u", /* kh */ "/[\\x{0445}]/u", /* oe */ "/[\\x{0153}]/u",
				/* sh */ "/[\\x{0448}]/u", /* shh */ "/[\\x{0449}]/u", /* ya */ "/[\\x{044F}]/u",
				/* ye */ "/[\\x{0454}]/u", /* yi */ "/[\\x{0457}]/u", /* yo */ "/[\\x{0451}]/u",
				/* yu */ "/[\\x{044E}]/u", /* zh */ "/[\\x{0436}]/u",

				/* Uppercase */
				/* A */ "/[\\x{0100}\\x{0102}\\x{0104}\\x{00C0}\\x{00C1}\\x{00C2}\\x{00C3}\\x{00C4}\\x{00C5}\\x{0410}]/u",
				/* B */ "/[\\x{0411}]/u", /* C */ "/[\\x{00C7}\\x{0106}\\x{0108}\\x{010A}\\x{010C}\\x{0426}]/u",
				/* D */ "/[\\x{010E}\\x{0110}\\x{0414}]/u",
				/* E */ "/[\\x{00C8}\\x{00C9}\\x{00CA}\\x{00CB}\\x{0112}\\x{0114}\\x{0116}\\x{0118}\\x{011A}\\x{0415}\\x{042D}]/u",
				/* F */ "/[\\x{0424}]/u", /* G */ "/[\\x{011C}\\x{011E}\\x{0120}\\x{0122}\\x{0413}\\x{0490}]/u",
				/* H */ "/[\\x{0124}\\x{0126}]/u",
				/* I */ "/[\\x{0128}\\x{012A}\\x{012C}\\x{012E}\\x{0130}\\x{0418}\\x{0406}]/u",
				/* J */ "/[\\x{0134}\\x{0419}]/u", /* K */ "/[\\x{0136}\\x{041A}]/u",
				/* L */ "/[\\x{0139}\\x{013B}\\x{013D}\\x{0139}\\x{0141}\\x{041B}]/u", /* M */ "/[\\x{041C}]/u",
				/* N */ "/[\\x{00D1}\\x{0143}\\x{0145}\\x{0147}\\x{014A}\\x{041D}]/u",
				/* O */ "/[\\x{00D3}\\x{014C}\\x{014E}\\x{0150}\\x{041E}]/u", /* P */ "/[\\x{041F}]/u",
				/* R */ "/[\\x{0154}\\x{0156}\\x{0158}\\x{0420}]/u",
				/* S */ "/[\\x{015A}\\x{015C}\\x{015E}\\x{0160}\\x{0421}]/u",
				/* T */ "/[\\x{0162}\\x{0164}\\x{0166}\\x{0422}]/u",
				/* U */ "/[\\x{00D9}\\x{00DA}\\x{00DB}\\x{00DC}\\x{0168}\\x{016A}\\x{016C}\\x{016E}\\x{0170}\\x{0172}\\x{0423}]/u",
				/* V */ "/[\\x{0412}]/u", /* W */ "/[\\x{0174}]/u", /* Y */ "/[\\x{0176}\\x{042B}]/u",
				/* Z */ "/[\\x{0179}\\x{017B}\\x{017D}\\x{0417}]/u", /* AE */ "/[\\x{00C6}]/u",
				/* CH */ "/[\\x{0427}]/u", /* KH */ "/[\\x{0425}]/u", /* OE */ "/[\\x{0152}]/u",
				/* SH */ "/[\\x{0428}]/u", /* SHH */ "/[\\x{0429}]/u", /* YA */ "/[\\x{042F}]/u",
				/* YE */ "/[\\x{0404}]/u", /* YI */ "/[\\x{0407}]/u", /* YO */ "/[\\x{0401}]/u",
				/* YU */ "/[\\x{042E}]/u", /* ZH */ "/[\\x{0416}]/u");

		// ö to oe
		// å to aa
		// ä to ae

		List<String> replacements = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
				"o", "p", "r", "s", "ss", "t", "u", "v", "w", "y", "z", "ae", "ch", "kh", "oe", "sh", "shh", "ya", "ye",
				"yi", "yo", "yu", "zh", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
				"R", "S", "T", "U", "V", "W", "Y", "Z", "AE", "CH", "KH", "OE", "SH", "SHH", "YA", "YE", "YI", "YO",
				"YU", "ZH");

		for (int i = 0; i < replacements.size(); i++) {
			str = PhpUtil.preg_replace(patterns.get(i), replacements.get(i), str);
		}
		return str;
	}

	/**
	 * Sanitize data which will be injected into SQL query
	 *
	 * @param string $string SQL data which will be injected into SQL query
	 * @param bool   $html_ok Does data contain HTML code ? (optional)
	 * @return string Sanitized data
	 */
	public static String pSQL(String string) {
		return pSQL(string, false, false);
	}

	public static String pSQL(String string, boolean html_ok, boolean bq_sql) {

		if (!NumberUtils.isCreatable(string)) {
			string = StringUtil.mysql_real_escape_string(string);

			if (!html_ok) {
				string = PhpUtil.strip_tags(Tools.nl2br(string));
			}

			if (bq_sql == true) {
				string = PhpUtil.str_replace("`", "", string);
			}
		}

		return string;
	}

	/**
	 * ucwords — Met en majuscule la première lettre de tous les mots
	 * 
	 * La définition d'un mot est : toute séquence de caractères qui suit
	 * immédiatement n'importe quel caractère listé dans le paramètre delimiters
	 * (par défaut, ce sont : un espace, un saut à la ligne, une nouvelle ligne, un
	 * retour à la ligne, une tabulation horizontale, et une tabulation verticale).
	 * 
	 * 
	 * @param str La chaîne d'entrée.
	 * @return Retourne la chaîne str après avoir mis en majuscule la première
	 *         lettre de tous les mots, si ce caractère est alphabétique.
	 *
	 */
	public static String ucwords(String str) {
		return ucwords(str, ' ', '\t', '\r', '\n', '\f');
	}

	/**
	 * ucwords — Met en majuscule la première lettre de tous les mots
	 * 
	 * La définition d'un mot est : toute séquence de caractères qui suit
	 * immédiatement n'importe quel caractère listé dans le paramètre delimiters
	 * (par défaut, ce sont : un espace, un saut à la ligne, une nouvelle ligne, un
	 * retour à la ligne, une tabulation horizontale, et une tabulation verticale).
	 * 
	 * 
	 * @param str        La chaîne d'entrée.
	 * @param delimiters " \t\r\n\f\v"
	 * @return Retourne la chaîne str après avoir mis en majuscule la première
	 *         lettre de tous les mots, si ce caractère est alphabétique.
	 *
	 */
	public static String ucwords(String str, char... delimiters) {
		return WordUtils.capitalize(str, delimiters);
	}

	public static <T> List<List<T>> splitList(List<T> list, int numberOfParts) {
		List<List<T>> numberOfPartss = new ArrayList<>(numberOfParts);
		int size = list.size();
		int sizePernumberOfParts = (int) Math.ceil(((double) size) / numberOfParts);
		int leftElements = size;
		int i = 0;
		while (i < size && numberOfParts != 0) {
			numberOfPartss.add(list.subList(i, i + sizePernumberOfParts));
			i = i + sizePernumberOfParts;
			leftElements = leftElements - sizePernumberOfParts;
			sizePernumberOfParts = (int) Math.ceil(((double) leftElements) / --numberOfParts);
		}
		return numberOfPartss;
	}

	public static <T> List<List<T>> partitionList(List<T> list, int partitionSize) {
		List<List<T>> partitions = new LinkedList<List<T>>();
		for (int i = 0; i < list.size(); i += partitionSize) {
			partitions.add(list.subList(i, Math.min(i + partitionSize, list.size())));
		}
		return partitions;
	}

}
