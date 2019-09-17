package eportfolium.com.karuta.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.crypto.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.validator.routines.InetAddressValidator;

public class PhpUtil {

	private final static Pattern LTRIM = Pattern.compile("^\\s+");
	private final static Pattern RTRIM = Pattern.compile("\\s+$");
	private static final Log log = LogFactory.getLog(PhpUtil.class);

	/***
	 * Copy of uniqid in php http://php.net/manual/fr/function.uniqid.php
	 * 
	 * @param prefix
	 * @param more_entropy
	 * @return
	 */
	public static String uniqid(String prefix, boolean more_entropy) {
		long time = System.currentTimeMillis();
		String uniqid = "";
		if (!more_entropy) {
			uniqid = String.format("%s%08x%05x", prefix, time / 1000, time);
		} else {
			SecureRandom sec = new SecureRandom();
			byte[] sbuf = sec.generateSeed(8);
			ByteBuffer bb = ByteBuffer.wrap(sbuf);

			uniqid = String.format("%s%08x%05x", prefix, time / 1000, time);
			uniqid += "." + String.format("%.8s", "" + bb.getLong() * -1);
		}

		return uniqid;
	}

	public static String md5(String input) {
		String result = input;
		if (input != null) {
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("MD5"); // or "SHA-1"
				md.update(input.getBytes());
				BigInteger hash = new BigInteger(1, md.digest());
				result = hash.toString(16);
				while (result.length() < 32) { // 40 for SHA-1
					result = "0" + result;
				}
			} catch (NoSuchAlgorithmException e) {
				log.error(e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
		}
		return result;
	}

	public static String sha1(String input) {
		String result = input;
		if (input != null) {
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(input.getBytes());
				BigInteger hash = new BigInteger(1, md.digest());
				result = hash.toString(16);
				while (result.length() < 40) {
					result = "0" + result;
				}
			} catch (NoSuchAlgorithmException e) {
				log.error(e.getMessage());
				throw new RuntimeException(e.getMessage());
			}
		}
		return result;
	}

	public static String rand() {
		Random rand = new Random();
		int min = 0;
		short max = Short.MAX_VALUE;
		Integer value = rand.nextInt(max - min + 1) + min;
		return value.toString();
	}

	public static String microtime() {
		long mstime = System.currentTimeMillis();
		long seconds = mstime / 1000;
		double decimal = (mstime - (seconds * 1000)) / 1000d;
		return decimal + " " + seconds;
	}

	public static String generateSecureKey() throws NoSuchAlgorithmException {
		return md5(uniqid(rand(), true));
	}

	public static String strip_tags(String input) {
		// return input.replaceAll("<(?!\\/?a(?=>|\\s.*>))\\/?.*?>", "");
		return HtmlHelper.stripTags(input, true, true);
		// return HtmlHelper.stripTags(input,new String[] { });
	}

	public static String strip_tags(String input, String[] allowedTags) {
		return HtmlHelper.stripTags(input, allowedTags);
	}

	public static String htmlEntities(String string) {
		StringBuffer sb = new StringBuffer(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				//
				// HTML Special Chars
				if (c == '"')
					sb.append("&quot;");
				else if (c == '&')
					sb.append("&amp;");
				else if (c == '<')
					sb.append("&lt;");
				else if (c == '>')
					sb.append("&gt;");
				else if (c == '\n')
					// Handle Newline
					sb.append("&lt;br/&gt;");
				else {
					int ci = 0xffff & c;
					if (ci < 160)
						// nothing special only 7 Bit
						sb.append(c);
					else {
						// Not 7 Bit use the unicode system
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * @return gets you to Unix epoch.
	 */
	public static long time() {
		return System.currentTimeMillis() / 1000L;
	}

	public static boolean isNumeric(String str) {
		return NumberUtils.isCreatable(str);
	}

	/**
	 * Supprime les espaces de début de chaîne.
	 * 
	 * @param s
	 * @return Retourne la chaîne str, après avoir supprimé tous les caractères
	 *         blancs du début de chaîne.
	 */
	public static String ltrim(String s) {
		return LTRIM.matcher(s).replaceAll("");
	}

	/**
	 * rtrim — Supprime les espaces de fin de chaîne
	 * 
	 * @param s
	 * @return Retourne la chaîne str, après avoir supprimé tous les caractères
	 *         blancs de fin de chaîne.
	 */
	public static String rtrim(String s) {
		return RTRIM.matcher(s).replaceAll("");
	}

	/**
	 * ltrim — Supprime les autres caractères du début de chaîne.
	 * 
	 * @param s
	 * @param character_mask
	 * @return
	 */
	public static String ltrim(String s, String character_mask) {
		String result = s;
		if (StringUtils.isNotEmpty(s) && StringUtils.isNotEmpty(character_mask)) {
			result = s.replaceAll("^" + Pattern.quote(character_mask) + "+", "");
		}
		return result;
	}

	/**
	 * rtrim — Supprime les autres caractères de fin de chaîne.
	 * 
	 * @param s
	 * @param character_mask
	 * @return
	 */
	public static String rtrim(String s, String character_mask) {
		String result = s;
		if (StringUtils.isNotEmpty(s) && StringUtils.isNotEmpty(character_mask)) {
			result = s.replaceAll(Pattern.quote(character_mask) + "+$", "");
		}
		return result;
	}

	/**
	 * Convertit une chaîne contenant une adresse (IPv4) IP numérique en adresse
	 * littérale
	 * 
	 * @param ip_address Une adresse au format standard.
	 * @return Génère une adresse IPv4 à partir de son équivalent numérique.
	 */
	public static Long Dot2LongIP(String ip_address) {
		long num = 0;
		if (ip_address != null && InetAddressValidator.getInstance().isValidInet4Address(ip_address)) {
			String[] addrArray = ip_address.split("\\.");
			for (int i = 0; i < addrArray.length; i++) {
				int power = 3 - i;
				num += ((Integer.parseInt(addrArray[i]) % 256) * Math.pow(256, power));
			}
		}
		return num;
	}

	public static String stripslashes(String message) {
		return message != null ? message.replace("\\", "") : null;
	}

	/**
	 * Rassemble les éléments d'un tableau en une chaîne.
	 * 
	 * @param glue   Par défaut, une chaîne vide.
	 * @param pieces Le tableau de chaînes à rassembler.
	 * @return Retourne une chaîne contenant la représentation en chaîne de
	 *         caractères de tous les éléments du tableau pieces, dans le même
	 *         ordre, avec la chaîne glue, placée entre deux éléments.
	 */
	public static <T> String implode(final String glue, final Collection<T> pieces) {
		final StringBuffer sb = new StringBuffer();
		if (CollectionUtils.isNotEmpty(pieces)) {
			Iterator<T> it = pieces.iterator();
			for (int i = 0; it.hasNext(); i++) {
				sb.append(it.next());
				if (i != pieces.size() - 1) {
					sb.append(glue);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Vérifie si une classe a été définie
	 * 
	 * @param className Le nom de la classe. Il est recherché de manière insensible
	 *                  à la casse.
	 * @return Retourne TRUE si className est une classe définie, FALSE sinon.
	 */
	public static boolean class_exists(String className) {
		try {
			ReflectionUtils.getClassByName(className);
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Vérifie si la propriété property existe dans la classe spécifiée.
	 * 
	 * @param className Le nom de la classe ou un objet de la classe à tester
	 * @param property  Le nom de la propriété
	 * @return Retourne TRUE si la propriété existe, FALSE si elle n'existe pas ou
	 *         si une erreur survient.
	 */
	public static boolean property_exists(String className, String property) {
		Boolean result = false;
		try {
			java.lang.reflect.Field f = org.springframework.util.ReflectionUtils
					.findField(ReflectionUtils.getClassByName(className), property);
			if (f != null)
				result = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Vérifie si la propriété property existe dans la classe spécifiée.
	 * 
	 * @param className la classe à introspecter
	 * @param property  Le nom de la propriété
	 * @return Retourne TRUE si la propriété existe, FALSE si elle n'existe pas ou
	 *         si une erreur survient.
	 */
	public static boolean property_exists(Class<?> theClass, String property) {
		Boolean result = false;
		java.lang.reflect.Field f = org.springframework.util.ReflectionUtils.findField(theClass, property);
		if (f != null)
			result = true;
		return result;
	}

	public static boolean preg_match_all(String pattern, String content) {
		return Pattern.compile(pattern).matcher(content).matches();
	}

	public static boolean preg_match_all(String pattern, int flags, String content) {
		return Pattern.compile(pattern, flags).matcher(content).matches();
	}

	public static boolean preg_match(String pattern, String subject, List<String> matches, int flags) {
		Pattern p = Pattern.compile(pattern, flags);
		Matcher m = p.matcher(subject);
		if (matches != null) {
			while (m.find()) {
				matches.add(subject.substring(m.start(), m.end()));
			}
		}
		return m.lookingAt();
	}

	public static boolean preg_match(String pattern, String subject, List<String> matches) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(subject);
		if (matches != null) {
			while (m.find()) {
				matches.add(subject.substring(m.start(), m.end()));
			}
		}
		return m.lookingAt();
	}

	public static boolean preg_match(String pattern, String subject) {
		return Pattern.compile(pattern).matcher(subject).lookingAt();
	}

	/**
	 * Éclate une chaîne par expression rationnelle.
	 * 
	 * @param pattern Le masque à chercher, sous la forme d'une chaîne de
	 *                caractères.
	 * @param subject La chaîne d'entrée.
	 * @param limit   Si limit est spécifié, alors seules les limit premières
	 *                sous-chaînes sont retournées avec le reste de la chaîne placé
	 *                dans la dernière sous-chaîne. Si vous définissez le paramètre
	 *                limit à -1, 0, ou NULL, cela signifie "aucune limite".
	 * @return Retourne un tableau contenant les sous-chaînes de subject, séparées
	 *         par les chaînes qui vérifient pattern.
	 */
	public static List<String> preg_split_no_empty(String pattern, String subject, Integer limit) {
		if (limit == null) {
			// Trailing empty strings are removed but not internal ones
			limit = Integer.valueOf(0);
		}
		final String[] raw_results = subject.split(pattern, limit);

		// Cleansing of the results
		final List<String> php_like_results = new ArrayList<String>();
		for (String tmp : raw_results) {
			if (tmp.length() > 0) {
				php_like_results.add(tmp);
			}
		}
		return php_like_results;
	}

	/**
	 * Éclate une chaîne par expression rationnelle.
	 * 
	 * @param pattern Le masque à chercher, sous la forme d'une chaîne de
	 *                caractères.
	 * @param subject La chaîne d'entrée.
	 * @param limit   Si limit est spécifié, alors seules les limit premières
	 *                sous-chaînes sont retournées avec le reste de la chaîne placé
	 *                dans la dernière sous-chaîne. Si vous définissez le paramètre
	 *                limit à -1, 0, ou NULL, cela signifie "aucune limite".
	 * @return Retourne un tableau contenant les sous-chaînes de subject, séparées
	 *         par les chaînes qui vérifient pattern.
	 */
	public static List<String> preg_split_no_empty(Pattern pattern, String subject, Integer limit) {
		if (limit == null) {
			// Trailing empty strings are removed but not internal ones
			limit = Integer.valueOf(0);
		}
		final String[] raw_results = pattern.split(subject, limit);

		// Cleansing of the results
		final List<String> php_like_results = new ArrayList<String>();
		for (String tmp : raw_results) {
			if (tmp.length() > 0) {
				php_like_results.add(tmp);
			}
		}
		return php_like_results;
	}

	public static String preg_replace(final String pattern, final String replacement, final String subject) {
		return preg_replace(pattern, replacement, subject, -1, null);
	}

	/**
	 * @param pattern     Le masque à chercher.
	 * @param replacement La chaîne pour le remplacement
	 * @param subject     La chaîne contenant des chaînes à chercher et à remplacer.
	 * @param limit       Le nombre maximal de remplacement pour chaque masque dans
	 *                    chaque chaîne subject. -1 = (aucune limite).
	 * @param count       Si fournie, cette variable contiendra le nombre de
	 *                    remplacements effectués.
	 * @return une chaîne.
	 */
	public static String preg_replace(final String pattern, final String replacement, final String subject,
			final Integer limit, MutableInt count) {

		final Pattern p = Pattern.compile(pattern);
		final Matcher m = p.matcher(subject);
		final StringBuffer sb = new StringBuffer();

		if (count != null) {
			count.setValue(0);
		} else {
			count = new MutableInt();
		}

		while (m.find()) {
			count.add(1);
			m.appendReplacement(sb, StringUtils.defaultString(replacement));
		}
		m.appendTail(sb);
		final String newtext = sb.toString();
		return newtext;
	}

	/**
	 * htmlspecialchars — Convertit les caractères spéciaux en entités HTML
	 * 
	 * @param string La chaîne à convertir.
	 * @return La chaîne convertie.
	 */
	public static String htmlspecialchars(String string) {
		return StringEscapeUtils.escapeHtml4(string);
	}

	/**
	 * Retourne le code ASCII d'un caractère
	 * 
	 * @param str Une chaine.
	 * @return Retourne la valeur ASCII du premier caractère de @{str}, sous la
	 *         forme d'un entier. .
	 */
	public static int ord(String str) {
		return StringUtils.isNotEmpty(str) ? (int) str.charAt(0) : 0;
	}

	/**
	 * Retourne le code ASCII d'un caractère
	 * 
	 * @param c Un caractère.
	 * @return Retourne la valeur ASCII, sous la forme d'un entier. .
	 */
	public static int ord(char c) {
		return (int) c;
	}

	/**
	 * 
	 * @param ascii Le code ascii étendu.
	 * @return Retourne un caractère à partir de son code ASCII
	 */
	public static String chr(int ascii) {
		while (ascii < 0) {
			ascii += 256;
		}
		ascii %= 256;
		char c2 = (char) ascii;
		return String.valueOf(c2);
	}

	/**
	 * Génère une chaîne de requête en encodage URL
	 * 
	 * @param data un tableau contenant des propriétés.
	 * @return Retourne une chaîne de caractères encodée URL.
	 * @throws UnsupportedEncodingException
	 */
	public static String httpBuildQuery(Map<String, String[]> data) {
		QueryStringBuilder builder = new QueryStringBuilder();
		for (Entry<String, String[]> queryParameter : data.entrySet()) {
			builder.addQueryParameter(queryParameter.getKey(), StringUtils.join(queryParameter.getValue(), "+"));
		}
		try {
			return builder.encode("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String str_replace(String search, String replace, String subject) {
		return subject.replace(search, replace);
	}

	public static String str_replace(List<String> search, String replace, String subject) {
		String[] search_array = new String[search.size()];
		search_array = search.toArray(search_array);
		String[] replace_array = new String[1];
		replace_array[0] = replace;
		return StringUtils.replaceEach(subject, search_array, replace_array);
	}

	/**************************
	 *
	 * STR_PAD IMPLEMENTED
	 *
	 **************************/
	public static String str_pad(String input, int length, String pad, String sense) {
		int resto_pad = length - input.length();
		String padded = "";

		if (resto_pad <= 0) {
			return input;
		}

		if (sense.equals("STR_PAD_RIGHT")) {
			padded = input;
			padded += _fill_string(pad, resto_pad);
		} else if (sense.equals("STR_PAD_LEFT")) {
			padded = _fill_string(pad, resto_pad);
			padded += input;
		} else // STR_PAD_BOTH
		{
			int pad_left = (int) Math.ceil(resto_pad / 2);
			int pad_right = resto_pad - pad_left;

			padded = _fill_string(pad, pad_left);
			padded += input;
			padded += _fill_string(pad, pad_right);
		}
		return padded;
	}

	protected static String _fill_string(String pad, int resto) {
		boolean first = true;
		String padded = "";

		if (resto >= pad.length()) {
			for (int i = resto; i >= 0; i = i - pad.length()) {
				if (i >= pad.length()) {
					if (first) {
						padded = pad;
					} else {
						padded += pad;
					}
				} else {
					if (first) {
						padded = pad.substring(0, i);
					} else {
						padded += pad.substring(0, i);
					}
				}
				first = false;
			}
		} else {
			padded = pad.substring(0, resto);
		}
		return padded;
	}

	public static int strncmp(String str1, String str2, int n) {
		str1 = str1.substring(0, n);
		str2 = str2.substring(0, n);
		return ((str1 == str2) ? 0 : (str1.compareTo(str2) > 1 ? 1 : -1));
	}

	/**
	 * strstr — Trouve la première occurrence dans une chaîne
	 * 
	 * @param haystack La chaîne d'entrée
	 * @param needle   La chaine à trouver.
	 * @return Retourne l'indice de la première occurrence de needle dans haystack,
	 *         ou -1 si needle n'a pas été trouvée.
	 */
	public static int strStr(String haystack, String needle) {
		if (haystack == null || needle == null)
			return 0;

		int h = haystack.length();
		int n = needle.length();

		if (n > h)
			return -1;
		if (n == 0)
			return 0;

		int[] next = getNext(needle);
		int i = 0;

		while (i <= h - n) {
			int success = 1;
			for (int j = 0; j < n; j++) {
				if (needle.charAt(0) != haystack.charAt(i)) {
					success = 0;
					i++;
					break;
				} else if (needle.charAt(j) != haystack.charAt(i + j)) {
					success = 0;
					i = i + j - next[j - 1];
					break;
				}
			}
			if (success == 1)
				return i;
		}

		return -1;
	}

	// calculate KMP array
	private static int[] getNext(String needle) {
		int[] next = new int[needle.length()];
		next[0] = 0;

		for (int i = 1; i < needle.length(); i++) {
			int index = next[i - 1];
			while (index > 0 && needle.charAt(index) != needle.charAt(i)) {
				index = next[index - 1];
			}

			if (needle.charAt(index) == needle.charAt(i)) {
				next[i] = next[i - 1] + 1;
			} else {
				next[i] = 0;
			}
		}

		return next;
	}

	/**
	 * Retourne FALSE si var existe et est non-vide, et dont la valeur n'est pas
	 * zéro.
	 * 
	 * @param var Variable à vérifier.
	 * @return
	 */
	public static boolean empty(Object var) {
		if (var == null) {
			return true;
		} else if (var instanceof Collection) {
			((Collection<?>) var).isEmpty();
		} else if (var instanceof Boolean) {
			return Boolean.FALSE.equals(var);
		} else if (var instanceof String) {
			return StringUtils.isEmpty((String) var) || StringUtils.equals((String) var, "0");
		} else if (var instanceof Integer) {
			return Integer.valueOf(0).equals(var);
		} else if (var instanceof Byte) {
			return Byte.valueOf("0").equals(var);
		} else if (var instanceof Short) {
			return Short.valueOf("0").equals(var);
		} else if (var instanceof Long) {
			return Long.valueOf(0).equals(var);
		} else if (var instanceof Float) {
			return Float.valueOf("0f").equals(var);
		} else if (var instanceof Double) {
			return Double.valueOf("0d").equals(var);
		} else if (var instanceof BigDecimal) {
			return BigDecimal.ZERO.equals(var);
		}
		return false;
	}

	/**
	 * Convertit toutes les entités HTML en caractères normaux
	 * 
	 * @param string La chaîne d'entrée.
	 * @return Retourne la chaîne décodée
	 */
	public static String html_entity_decode(String string) {
		return StringEscapeUtils.unescapeHtml4(string);
	}

	/**
	 * Trie les tableaux multidimensionnels
	 * 
	 * Multi-sorts the given arrays with the quicksort algorithm. It assumes that
	 * all arrays have the same sizes and it sorts on the first dimension of these
	 * arrays. If the given arrays are null or empty, it will do nothing, if just a
	 * single array was passed it will sort it via {@link Arrays} sort;
	 */
	public static <T extends Comparable<?>> void array_multisort(T[]... arrays) {
		array_multisort(0, arrays);
	}

	/**
	 * Trie les tableaux multidimensionnels
	 * 
	 * Multi-sorts the given arrays with the quicksort algorithm. It assumes that
	 * all arrays have the same sizes and it sorts on the given dimension index
	 * (starts with 0) of these arrays. If the given arrays are null or empty, it
	 * will do nothing, if just a single array was passed it will sort it via
	 * {@link Arrays} sort;
	 */
	public static <T extends Comparable<?>> void array_multisort(int sortDimension, T[]... arrays) {
		// check if the lengths are equal, break if everything is empty
		if (arrays == null || arrays.length == 0) {
			return;
		}
		// if the array only has a single dimension, sort it and return
		if (arrays.length == 1) {
			Arrays.sort(arrays[0]);
			return;
		}
		// also return if the sort dimension is not in our array range
		if (sortDimension < 0 || sortDimension >= arrays.length) {
			return;
		}
		// check sizes
		int firstArrayLength = arrays[0].length;
		for (int i = 1; i < arrays.length; i++) {
			if (arrays[i] == null || firstArrayLength != arrays[i].length)
				return;
		}

		multiQuickSort(arrays, 0, firstArrayLength, sortDimension);
	}

	/**
	 * Internal multi quicksort, doing the real algorithm.
	 */
	private static <T extends Comparable<?>> void multiQuickSort(T[][] a, int offset, int length, int indexToSort) {
		if (offset < length) {
			int pivot = multiPartition(a, offset, length, indexToSort);
			multiQuickSort(a, offset, pivot, indexToSort);
			multiQuickSort(a, pivot + 1, length, indexToSort);
		}
	}

	/**
	 * Partitions the given array in-place and uses the end element as pivot,
	 * everything less than the pivot will be placed left and everything greater
	 * will be placed right of the pivot. It returns the index of the pivot element
	 * after partitioning. This is a multi way partitioning algorithm, you have to
	 * provide a partition array index to know which is the array that needs to be
	 * partitioned. The swap operations are applied on the other elements as well.
	 */
	private static <T extends Comparable<?>> int multiPartition(T[][] array, int start, int end,
			int partitionArrayIndex) {
		final int ending = end - 1;
		final T x = array[partitionArrayIndex][ending];
		int i = start - 1;
		for (int j = start; j < ending; j++) {
			if (((Comparable<T>) array[partitionArrayIndex][j]).compareTo(x) <= 0) {
				i++;
				for (int arrayIndex = 0; arrayIndex < array.length; arrayIndex++) {
					swap(array[arrayIndex], i, j);
				}
			}
		}
		i++;
		for (int arrayIndex = 0; arrayIndex < array.length; arrayIndex++) {
			swap(array[arrayIndex], i, ending);
		}
		return i;
	}

	/**
	 * Swaps the given indices x with y in the array.
	 */
	private static <T extends Comparable<?>> void swap(T[] array, int x, int y) {
		T tmpIndex = array[x];
		array[x] = array[y];
		array[y] = tmpIndex;
	}

	public static void main(String[] args) {

		// Integer[] first = new Integer[] { 10, 100, 100, 0 };
		// BigDecimal[] second = new BigDecimal[] { BigDecimal.ONE,
		// BigDecimal.valueOf(3), BigDecimal.valueOf(2),
		// BigDecimal.valueOf(4) };
		// Integer[] resFirst = new Integer[] { 0, 10, 100, 100 };
		// BigDecimal[] resSecond = new BigDecimal[] { BigDecimal.valueOf(4),
		// BigDecimal.ONE, BigDecimal.valueOf(2),
		// BigDecimal.valueOf(3) };
		//
		// PhpUtil.array_multisort(first, second);
		//
		// for (int i = 0; i < first.length; i++) {
		// System.out.println(resFirst[i] + "=?" + first[i]);
		// System.out.println(resSecond[i] + "=?" + second[i]);
		// }
		//
		// String str = "Votre nom est-il O\\'reilly ?";

		// Affiche : Votre nom est-il O\'reilly ?
	}

	public static <T> Set<T> array_intersect(Collection<T> a, Collection<T> b) {
		// unnecessary; just an optimization to iterate over the smaller set
		if (a.size() > b.size()) {
			return array_intersect(b, a);
		}
		Set<T> results = new HashSet<>();

		for (T element : a) {
			if (b.contains(element)) {
				results.add(element);
			}
		}
		return results;
	}

	public static <T extends Comparable<? super T>> Map<T, MutableInt> asort(Map<T, MutableInt> passedMap) {
		List<T> mapKeys = new ArrayList<>(passedMap.keySet());
		List<MutableInt> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<T, MutableInt> sortedMap = new LinkedHashMap<>();

		Iterator<MutableInt> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			MutableInt val = valueIt.next();
			Iterator<T> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				T key = keyIt.next();
				MutableInt comp1 = passedMap.get(key);
				MutableInt comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public static <T extends Comparable<? super T>> Map<T, MutableInt> arsort(Map<T, MutableInt> passedMap) {
		List<T> mapKeys = new ArrayList<>(passedMap.keySet());
		List<MutableInt> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues, Collections.reverseOrder());
		Collections.sort(mapKeys, Collections.reverseOrder());
		LinkedHashMap<T, MutableInt> sortedMap = new LinkedHashMap<>();
		Iterator<MutableInt> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			MutableInt val = valueIt.next();
			Iterator<T> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				T key = keyIt.next();
				MutableInt comp1 = passedMap.get(key);
				MutableInt comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public static List<String> array_uintersect(List<String> array1, List<String> array2) {
		List<String> intersection = new ArrayList<String>();
		for (String value1 : array1) {
			for (String value2 : array2) {
				if (array_uintersect_compare(value1, value2) == 0) {
					intersection.add(value1);
					break;
				}
			}
		}
		return intersection;
	}

	private static int array_uintersect_compare(String a, String b) {
		if (a.equals(b)) {
			return 0;
		}

		String[] asplit = a.split("-");
		String[] bsplit = b.split("-");
		if (asplit[0].equals(bsplit[0]) && (!asplit[1].equals("0") || bsplit[1].equals("0"))) {
			return 0;
		}
		return 1;
	}

	public static int booleanToInt(boolean myBoolean) {
		return myBoolean ? 1 : 0;
	}

	public static String urlencode(String code) {
		try {
			return URLEncoder.encode(code, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return code;
	}

	public static String addSlashes(String text) {
		if (text == null || text.equals("")) {
			return "";
		}
		StringBuffer sb = new StringBuffer(text.length() * 2);
		StringCharacterIterator iterator = new StringCharacterIterator(text);
		char character = iterator.current();
		// StringCharacterIterator.DONE = caractère renvoyée lorsque l'itérateur a
		// atteint la fin ou le début du texte
		while (character != StringCharacterIterator.DONE) {
			switch (character) {
			case '\'': /* Antislash */
			case '"': /* Guillemets doubles */
			case '\\': /* double antislash */
				sb.append("\\"); /* double antislash */
			default:
				sb.append(character);
				break;
			}
			character = iterator.next();
		}
		return sb.toString();
	}

}
