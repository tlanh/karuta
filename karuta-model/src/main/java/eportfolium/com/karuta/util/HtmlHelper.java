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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * @author mathieu
 *
 */
public class HtmlHelper {
	private static final String[][] htmlNamedEntities = new String[][] { new String[] { "&quot;", "\"" },
			new String[] { "&lt;", "<" }, new String[] { "&gt;", ">" }, new String[] { "&nbsp;", " " },
			new String[] { "&iexcl;", "¡" }, new String[] { "&cent;", "¢" }, new String[] { "&pound;", "£" },
			new String[] { "&curren;", "¤" }, new String[] { "&yen;", "¥" }, new String[] { "&brvbar;", "¦" },
			new String[] { "&sect;", "§" }, new String[] { "&uml;", "¨" }, new String[] { "&copy;", "©" },
			new String[] { "&ordf;", "ª" }, new String[] { "&laquo;", "«" }, new String[] { "&not;", "¬" },
			new String[] { "&shy;", "­" }, new String[] { "&reg;", "®" }, new String[] { "&macr;", "¯" },
			new String[] { "&deg;", "°" }, new String[] { "&plusmn;", "±" }, new String[] { "&sup2;", "²" },
			new String[] { "&sup3;", "³" }, new String[] { "&acute;", "´" }, new String[] { "&micro;", "µ" },
			new String[] { "&para;", "¶" }, new String[] { "&middot;", "·" }, new String[] { "&cedil;", "¸" },
			new String[] { "&sup1;", "¹" }, new String[] { "&ordm;", "º" }, new String[] { "&raquo;", " »" },
			new String[] { "&frac14;", "¼" }, new String[] { "&frac12;", "½" }, new String[] { "&frac34;", "¾" },
			new String[] { "&iquest;", "¿" }, new String[] { "&Agrave;", "À" }, new String[] { "&Aacute;", "Á" },
			new String[] { "&Acirc;", "Â" }, new String[] { "&Atilde;", "Ã" }, new String[] { "&Auml;", "Ä" },
			new String[] { "&Aring;", "Å" }, new String[] { "&AElig;", "Æ" }, new String[] { "&Ccedil;", "Ç" },
			new String[] { "&Egrave;", "È" }, new String[] { "&Eacute;", "É" }, new String[] { "&Ecirc;", "Ê" },
			new String[] { "&Euml;", "Ë" }, new String[] { "&Igrave;", "Ì" }, new String[] { "&Iacute;", "Í" },
			new String[] { "&Icirc;", "Î" }, new String[] { "&Iuml;", "Ï" }, new String[] { "&ETH;", "Ð" },
			new String[] { "&Ntilde;", "Ñ" }, new String[] { "&Ograve;", "Ò" }, new String[] { "&Oacute;", "Ó" },
			new String[] { "&Ocirc;", "Ô" }, new String[] { "&Otilde;", "Õ" }, new String[] { "&Ouml;", "Ö" },
			new String[] { "&times;", "×" }, new String[] { "&Oslash;", "Ø" }, new String[] { "&Ugrave;", "Ù" },
			new String[] { "&Uacute;", "Ú" }, new String[] { "&Ucirc;", "Û" }, new String[] { "&Uuml;", "Ü" },
			new String[] { "&Yacute;", "Ý" }, new String[] { "&THORN;", "Þ" }, new String[] { "&szlig;", "ß" },
			new String[] { "&agrave;", "à" }, new String[] { "&aacute;", "á" }, new String[] { "&acirc;", "â" },
			new String[] { "&atilde;", "ã" }, new String[] { "&auml;", "ä" }, new String[] { "&aring;", "å" },
			new String[] { "&aelig;", "æ" }, new String[] { "&ccedil;", "ç" }, new String[] { "&egrave;", "è" },
			new String[] { "&eacute;", "é" }, new String[] { "&ecirc;", "ê" }, new String[] { "&euml;", "ë" },
			new String[] { "&igrave;", "ì" }, new String[] { "&iacute;", "í" }, new String[] { "&icirc;", "î" },
			new String[] { "&iuml;", "ï" }, new String[] { "&eth;", "ð" }, new String[] { "&ntilde;", "ñ" },
			new String[] { "&ograve;", "ò" }, new String[] { "&oacute;", "ó" }, new String[] { "&ocirc;", "ô" },
			new String[] { "&otilde;", "õ" }, new String[] { "&ouml;", "ö" }, new String[] { "&divide;", "÷" },
			new String[] { "&oslash;", "ø" }, new String[] { "&ugrave;", "ù" }, new String[] { "&uacute;", "ú" },
			new String[] { "&ucirc;", "û" }, new String[] { "&uuml;", "ü" }, new String[] { "&yacute;", "ý" },
			new String[] { "&thorn;", "þ" }, new String[] { "&yuml;", "ÿ" }, new String[] { "&OElig;", "Œ" },
			new String[] { "&oelig;", "œ" }, new String[] { "&Scaron;", "Š" }, new String[] { "&scaron;", "š" },
			new String[] { "&Yuml;", "Ÿ" }, new String[] { "&fnof;", "ƒ" }, new String[] { "&circ;", "ˆ" },
			new String[] { "&tilde;", "˜" }, new String[] { "&Alpha;", "Α" }, new String[] { "&Beta;", "Β" },
			new String[] { "&Gamma;", "Γ" }, new String[] { "&Delta;", "Δ" }, new String[] { "&Epsilon;", "Ε" },
			new String[] { "&Zeta;", "Ζ" }, new String[] { "&Eta;", "Η" }, new String[] { "&Theta;", "Θ" },
			new String[] { "&Iota;", "Ι" }, new String[] { "&Kappa;", "Κ" }, new String[] { "&Lambda;", "Λ" },
			new String[] { "&Mu;", "Μ" }, new String[] { "&Nu;", "Ν" }, new String[] { "&Xi;", "Ξ" },
			new String[] { "&Omicron;", "Ο" }, new String[] { "&Pi;", "Π" }, new String[] { "&Rho;", "Ρ" },
			new String[] { "&Sigma;", "Σ" }, new String[] { "&Tau;", "Τ" }, new String[] { "&Upsilon;", "Υ" },
			new String[] { "&Phi;", "Φ" }, new String[] { "&Chi;", "Χ" }, new String[] { "&Psi;", "Ψ" },
			new String[] { "&Omega;", "Ω" }, new String[] { "&alpha;", "α" }, new String[] { "&beta;", "β" },
			new String[] { "&gamma;", "γ" }, new String[] { "&delta;", "δ" }, new String[] { "&epsilon;", "ε" },
			new String[] { "&zeta;", "ζ" }, new String[] { "&eta;", "η" }, new String[] { "&theta;", "θ" },
			new String[] { "&iota;", "ι" }, new String[] { "&kappa;", "κ" }, new String[] { "&lambda;", "λ" },
			new String[] { "&mu;", "μ" }, new String[] { "&nu;", "ν" }, new String[] { "&xi;", "ξ" },
			new String[] { "&omicron;", "ο" }, new String[] { "&pi;", "π" }, new String[] { "&rho;", "ρ" },
			new String[] { "&sigmaf;", "ς" }, new String[] { "&sigma;", "σ" }, new String[] { "&tau;", "τ" },
			new String[] { "&upsilon;", "υ" }, new String[] { "&phi;", "φ" }, new String[] { "&chi;", "χ" },
			new String[] { "&psi;", "ψ" }, new String[] { "&omega;", "ω" }, new String[] { "&thetasym;", "ϑ" },
			new String[] { "&upsih;", "ϒ" }, new String[] { "&piv;", "ϖ" }, new String[] { "&ensp;", " " },
			new String[] { "&emsp;", " " }, new String[] { "&thinsp;", " " }, new String[] { "&zwnj;", "‌" },
			new String[] { "&zwj;", "‍" }, new String[] { "&lrm;", "‎" }, new String[] { "&rlm;", "‏" },
			new String[] { "&ndash;", "–" }, new String[] { "&mdash;", "—" }, new String[] { "&lsquo;", "‘" },
			new String[] { "&rsquo;", "’" }, new String[] { "&sbquo;", "‚" }, new String[] { "&ldquo;", "“" },
			new String[] { "&rdquo;", "”" }, new String[] { "&bdquo;", "„" }, new String[] { "&dagger;", "†" },
			new String[] { "&Dagger;", "‡" }, new String[] { "&bull;", "•" }, new String[] { "&hellip;", "…" },
			new String[] { "&permil;", "‰" }, new String[] { "&prime;", "′" }, new String[] { "&Prime;", "″" },
			new String[] { "&lsaquo;", "‹" }, new String[] { "&rsaquo;", "›" }, new String[] { "&oline;", "‾" },
			new String[] { "&frasl;", "⁄" }, new String[] { "&euro;", "€" }, new String[] { "&image;", "ℑ" },
			new String[] { "&weierp;", "℘" }, new String[] { "&real;", "ℜ" }, new String[] { "&trade;", "™" },
			new String[] { "&alefsym;", "ℵ" }, new String[] { "&larr;", "←" }, new String[] { "&uarr;", "↑" },
			new String[] { "&rarr;", "→" }, new String[] { "&darr;", "↓" }, new String[] { "&harr;", "↔" },
			new String[] { "&crarr;", "↵" }, new String[] { "&lArr;", "⇐" }, new String[] { "&uArr;", "⇑" },
			new String[] { "&rArr;", "⇒" }, new String[] { "&dArr;", "⇓" }, new String[] { "&hArr;", "⇔" },
			new String[] { "&forall;", "∀" }, new String[] { "&part;", "∂" }, new String[] { "&exist;", "∃" },
			new String[] { "&empty;", "∅" }, new String[] { "&nabla;", "∇" }, new String[] { "&isin;", "∈" },
			new String[] { "&notin;", "∉" }, new String[] { "&ni;", "∋" }, new String[] { "&prod;", "∏" },
			new String[] { "&sum;", "∑" }, new String[] { "&minus;", "−" }, new String[] { "&lowast;", "∗" },
			new String[] { "&radic;", "√" }, new String[] { "&prop;", "∝" }, new String[] { "&infin;", "∞" },
			new String[] { "&ang;", "∠" }, new String[] { "&and;", "∧" }, new String[] { "&or;", "∨" },
			new String[] { "&cap;", "∩" }, new String[] { "&cup;", "∪" }, new String[] { "&int;", "∫" },
			new String[] { "&there4;", "∴" }, new String[] { "&sim;", "∼" }, new String[] { "&cong;", "≅" },
			new String[] { "&asymp;", "≈" }, new String[] { "&ne;", "≠" }, new String[] { "&equiv;", "≡" },
			new String[] { "&le;", "≤" }, new String[] { "&ge;", "≥" }, new String[] { "&sub;", "⊂" },
			new String[] { "&sup;", "⊃" }, new String[] { "&nsub;", "⊄" }, new String[] { "&sube;", "⊆" },
			new String[] { "&supe;", "⊇" }, new String[] { "&oplus;", "⊕" }, new String[] { "&otimes;", "⊗" },
			new String[] { "&perp;", "⊥" }, new String[] { "&sdot;", "⋅" }, new String[] { "&lceil;", "⌈" },
			new String[] { "&rceil;", "⌉" }, new String[] { "&lfloor;", "⌊" }, new String[] { "&rfloor;", "⌋" },
			new String[] { "&lang;", "〈" }, new String[] { "&rang;", "〉" }, new String[] { "&loz;", "◊" },
			new String[] { "&spades;", "♠" }, new String[] { "&clubs;", "♣" }, new String[] { "&hearts;", "♥" },
			new String[] { "&diams;", "♦" }, new String[] { "&amp;", "&" } };

	public static void main(String[] args) {
		String text = "<p>Test paragraph.</p><!-- Comment --><a href=\"#fragment\">Other text</a>";
		System.out.println(stripTags(text, false, false));
		String test1 = stripTags("<p>George</p><b>W</b><i>Bush</i>", new String[] { "i", "b" });
		String test2 = stripTags("<p>George <img src='someimage.png' onmouseover='someFunction()'>W <i>Bush</i></p>",
				new String[] { "p" });
		String test3 = stripTags("<a href='http://www.dijksterhuis.org'>Martijn <b>Dijksterhuis</b></a>",
				new String[] {});

		System.out.println(stripTags(text, new String[] { }));
		System.out.println(test1);
		System.out.println(test2);
		System.out.println(test3);

		String test4 = "<a class=\"classof69\" onClick='titi.bim()' href='http://www.dijksterhuis.org'>Martijn Dijksterhuis</a>";
		System.out.println(stripTagsAndAttributes(test4, new String[] { "a" }));
	}

	private static String replaceFirst(String haystack, String needle, String replacement) {
		int pos = haystack.indexOf(needle);
		if (pos < 0)
			return haystack;
		return haystack.substring(0, pos) + replacement + haystack.substring(pos + needle.length());
	}

	private static String replaceAll(String haystack, String needle, String replacement) {
		int pos;
		// Avoid a possible infinite loop
		if (needle == replacement)
			return haystack;
		while ((pos = haystack.indexOf(needle)) > 0)
			haystack = haystack.substring(0, pos) + replacement + haystack.substring(pos + needle.length());
		return haystack;
	}

	public static String stripTags(String htmlContent, boolean replaceNamedEntities, boolean replaceNumberedEntities) {
		if (htmlContent == null)
			return null;
		htmlContent = htmlContent.trim();
		if (htmlContent.isEmpty())
			return "";
	
		int bodyStartTagIdx = StringUtils.indexOfIgnoreCase(htmlContent, "<body");
		int bodyEndTagIdx = StringUtils.indexOfIgnoreCase(htmlContent, "</body>");
	
		int startIdx = 0, endIdx = htmlContent.length() - 1;
		if (bodyStartTagIdx >= 0)
			startIdx = bodyStartTagIdx;
		if (bodyEndTagIdx >= 0)
			endIdx = bodyEndTagIdx;
	
		boolean insideTag = false, insideAttributeValue = false, insideHtmlComment = false, insideScriptBlock = false,
				insideNoScriptBlock = false, insideStyleBlock = false;
		char attributeValueDelimiter = '"';
	
		StringBuilder sb = new StringBuilder(htmlContent.length());
		for (int i = startIdx; i <= endIdx; i++) {
			// html comment block
			if (!insideHtmlComment) {
				if (i + 3 < htmlContent.length() && htmlContent.charAt(i) == '<' && htmlContent.charAt(i + 1) == '!'
						&& htmlContent.charAt(i + 2) == '-' && htmlContent.charAt(i + 3) == '-') {
					i += 3;
					insideHtmlComment = true;
					continue;
				}
			}
			else // inside html comment
			{
				if (i + 2 < htmlContent.length() && htmlContent.charAt(i) == '-' && htmlContent.charAt(i + 1) == '-'
						&& htmlContent.charAt(i + 2) == '>') {
					i += 2;
					insideHtmlComment = false;
					continue;
				}
				else
					continue;
			}
	
			// noscript block
			if (!insideNoScriptBlock) {
				if (i + 9 < htmlContent.length() && htmlContent.charAt(i) == '<'
						&& (htmlContent.charAt(i + 1) == 'n' || htmlContent.charAt(i + 1) == 'N')
						&& (htmlContent.charAt(i + 2) == 'o' || htmlContent.charAt(i + 2) == 'O')
						&& (htmlContent.charAt(i + 3) == 's' || htmlContent.charAt(i + 3) == 'S')
						&& (htmlContent.charAt(i + 4) == 'c' || htmlContent.charAt(i + 4) == 'C')
						&& (htmlContent.charAt(i + 5) == 'r' || htmlContent.charAt(i + 5) == 'R')
						&& (htmlContent.charAt(i + 6) == 'i' || htmlContent.charAt(i + 6) == 'I')
						&& (htmlContent.charAt(i + 7) == 'p' || htmlContent.charAt(i + 7) == 'P')
						&& (htmlContent.charAt(i + 8) == 't' || htmlContent.charAt(i + 8) == 'T')
						&& (Character.isWhitespace(htmlContent.charAt(i + 9)) || htmlContent.charAt(i + 9) == '>')) {
					i += 9;
					insideNoScriptBlock = true;
					continue;
				}
			}
			else // inside noscript block
			{
				if (i + 10 < htmlContent.length() && htmlContent.charAt(i) == '<' && htmlContent.charAt(i + 1) == '/'
						&& (htmlContent.charAt(i + 2) == 'n' || htmlContent.charAt(i + 2) == 'N')
						&& (htmlContent.charAt(i + 3) == 'o' || htmlContent.charAt(i + 3) == 'O')
						&& (htmlContent.charAt(i + 4) == 's' || htmlContent.charAt(i + 4) == 'S')
						&& (htmlContent.charAt(i + 5) == 'c' || htmlContent.charAt(i + 5) == 'C')
						&& (htmlContent.charAt(i + 6) == 'r' || htmlContent.charAt(i + 6) == 'R')
						&& (htmlContent.charAt(i + 7) == 'i' || htmlContent.charAt(i + 7) == 'I')
						&& (htmlContent.charAt(i + 8) == 'p' || htmlContent.charAt(i + 8) == 'P')
						&& (htmlContent.charAt(i + 9) == 't' || htmlContent.charAt(i + 9) == 'T')
						&& (Character.isWhitespace(htmlContent.charAt(i + 10)) || htmlContent.charAt(i + 10) == '>')) {
					if (htmlContent.charAt(i + 10) != '>') {
						i += 9;
						while (i < htmlContent.length() && htmlContent.charAt(i) != '>')
							i++;
					}
					else
						i += 10;
					insideNoScriptBlock = false;
				}
				continue;
			}
	
			// script block
			if (!insideScriptBlock) {
				if (i + 7 < htmlContent.length() && htmlContent.charAt(i) == '<'
						&& (htmlContent.charAt(i + 1) == 's' || htmlContent.charAt(i + 1) == 'S')
						&& (htmlContent.charAt(i + 2) == 'c' || htmlContent.charAt(i + 2) == 'C')
						&& (htmlContent.charAt(i + 3) == 'r' || htmlContent.charAt(i + 3) == 'R')
						&& (htmlContent.charAt(i + 4) == 'i' || htmlContent.charAt(i + 4) == 'I')
						&& (htmlContent.charAt(i + 5) == 'p' || htmlContent.charAt(i + 5) == 'P')
						&& (htmlContent.charAt(i + 6) == 't' || htmlContent.charAt(i + 6) == 'T')
						&& (Character.isWhitespace(htmlContent.charAt(i + 7)) || htmlContent.charAt(i + 7) == '>')) {
					i += 6;
					insideScriptBlock = true;
					continue;
				}
			}
			else // inside script block
			{
				if (i + 8 < htmlContent.length() && htmlContent.charAt(i) == '<' && htmlContent.charAt(i + 1) == '/'
						&& (htmlContent.charAt(i + 2) == 's' || htmlContent.charAt(i + 2) == 'S')
						&& (htmlContent.charAt(i + 3) == 'c' || htmlContent.charAt(i + 3) == 'C')
						&& (htmlContent.charAt(i + 4) == 'r' || htmlContent.charAt(i + 4) == 'R')
						&& (htmlContent.charAt(i + 5) == 'i' || htmlContent.charAt(i + 5) == 'I')
						&& (htmlContent.charAt(i + 6) == 'p' || htmlContent.charAt(i + 6) == 'P')
						&& (htmlContent.charAt(i + 7) == 't' || htmlContent.charAt(i + 7) == 'T')
						&& (Character.isWhitespace(htmlContent.charAt(i + 8)) || htmlContent.charAt(i + 8) == '>')) {
					if (htmlContent.charAt(i + 8) != '>') {
						i += 7;
						while (i < htmlContent.length() && htmlContent.charAt(i) != '>')
							i++;
					}
					else
						i += 8;
					insideScriptBlock = false;
				}
				continue;
			}
	
			// style block
			if (!insideStyleBlock) {
				if (i + 7 < htmlContent.length() && htmlContent.charAt(i) == '<'
						&& (htmlContent.charAt(i + 1) == 's' || htmlContent.charAt(i + 1) == 'S')
						&& (htmlContent.charAt(i + 2) == 't' || htmlContent.charAt(i + 2) == 'T')
						&& (htmlContent.charAt(i + 3) == 'y' || htmlContent.charAt(i + 3) == 'Y')
						&& (htmlContent.charAt(i + 4) == 'l' || htmlContent.charAt(i + 4) == 'L')
						&& (htmlContent.charAt(i + 5) == 'e' || htmlContent.charAt(i + 5) == 'E')
						&& (Character.isWhitespace(htmlContent.charAt(i + 6)) || htmlContent.charAt(i + 6) == '>')) {
					i += 5;
					insideStyleBlock = true;
					continue;
				}
			}
			else // inside style block
			{
				if (i + 8 < htmlContent.length() && htmlContent.charAt(i) == '<' && htmlContent.charAt(i + 1) == '/'
						&& (htmlContent.charAt(i + 2) == 's' || htmlContent.charAt(i + 2) == 'S')
						&& (htmlContent.charAt(i + 3) == 't' || htmlContent.charAt(i + 3) == 'T')
						&& (htmlContent.charAt(i + 4) == 'y' || htmlContent.charAt(i + 4) == 'Y')
						&& (htmlContent.charAt(i + 5) == 'l' || htmlContent.charAt(i + 5) == 'L')
						&& (htmlContent.charAt(i + 6) == 'e' || htmlContent.charAt(i + 6) == 'E')
						&& (Character.isWhitespace(htmlContent.charAt(i + 7)) || htmlContent.charAt(i + 7) == '>')) {
					if (htmlContent.charAt(i + 7) != '>') {
						i += 7;
						while (i < htmlContent.length() && htmlContent.charAt(i) != '>')
							i++;
					}
					else
						i += 7;
					insideStyleBlock = false;
				}
				continue;
			}
	
			if (!insideTag) {
				if (i < htmlContent.length() && htmlContent.charAt(i) == '<') {
					insideTag = true;
					continue;
				}
			}
			else // inside tag
			{
				if (!insideAttributeValue) {
					if (htmlContent.charAt(i) == '"' || htmlContent.charAt(i) == '\'') {
						attributeValueDelimiter = htmlContent.charAt(i);
						insideAttributeValue = true;
						continue;
					}
					if (htmlContent.charAt(i) == '>') {
						insideTag = false;
						sb.append(' '); // prevent words from different tags (<td>s for example) from joining together
						continue;
					}
				}
				else // inside tag and inside attribute value
				{
					if (htmlContent.charAt(i) == attributeValueDelimiter) {
						insideAttributeValue = false;
						continue;
					}
				}
				continue;
			}
	
			sb.append(htmlContent.charAt(i));
		}
	
		String tmp;
		if (replaceNamedEntities)
			for (String[] htmlNamedEntity : htmlNamedEntities) {
				tmp = sb.toString().replace(htmlNamedEntity[0], htmlNamedEntity[1]);
				sb.setLength(0);
				sb.append(tmp);
			}
	
		if (replaceNumberedEntities)
			for (int i = 0; i < 512; i++) {
				tmp = sb.toString().replace("&#" + i + ";", new Character((char) i).toString());
				sb.setLength(0);
				sb.append(tmp);
			}
		return sb.toString();
	}

	public static String stripTags(String input, String[] allowedTags) {
		Pattern stripHTMLExp = Pattern.compile("(<\\/?[^>]+>)");
		String output = input;

		Matcher m = stripHTMLExp.matcher(input);

		while (m.find()) {
			String tag = m.group();
			String HTMLTag = tag.toLowerCase();
			boolean isAllowed = false;

			for (String allowedTag : allowedTags) {
				int offset = -1;

				// Determine if it is an allowed tag
				// "<tag>" , "<tag " and "</tag"
				if (offset != 0)
					offset = HTMLTag.indexOf('<' + allowedTag + '>');
				if (offset != 0)
					offset = HTMLTag.indexOf('<' + allowedTag + ' ');
				if (offset != 0)
					offset = HTMLTag.indexOf("</" + allowedTag);

				// If it matched any of the above the tag is allowed
				if (offset == 0) {
					isAllowed = true;
					break;
				}
			}

			// Remove tags that are not allowed
			if (!isAllowed)
				output = replaceFirst(output, tag, "");
		}

		return output;
	}

	public static String stripTagsAndAttributes(String input, String[] allowedTags) {
		/* Remove all unwanted tags first */
		String output = stripTags(input, allowedTags);

		/* Allow the "href" attribute */
		output = hrefMatch(output);

		/* Allow the "class" attribute */
		output = classMatch(output);

		/* Remove unsafe attributes in any of the remaining tags */
		output = unsafeMatch(output);

		/* Return the allowed tags to their proper form */
		output = replaceAll(output, "..;,;..", "=");

		return output;
	}
	/*
	 * Create a sanitizing policy that only allow tag '<p>' and '<strong>' public static String
	 * stripTagsAndAttributes(String input, String[] allowedTags) { PolicyFactory policy = new
	 * HtmlPolicyBuilder().allowElements("p", "strong").toFactory();
	 * 
	 * String safeOutput = policy.sanitize(outputToUser); }
	 */

	/* Allow the "href" attribute */
	private static String hrefMatch(String output) {
		Pattern p = Pattern.compile("(<a.*)href=(.*>)");
		Matcher m = p.matcher(output);

		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1) + "href..;,;.." + m.group(2));
		}
		m.appendTail(sb);

		String result = sb.toString();
		return result;
	}

	/* Allow the "class" attribute */
	private static String classMatch(String output) {
		Pattern p = Pattern.compile("(<a.*)class=(.*>)");
		Matcher m = p.matcher(output);

		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1) + "class..;,;.." + m.group(2));
		}
		m.appendTail(sb);

		String result = sb.toString();
		return result;
	}

	private static String unsafeMatch(String output) {
		Pattern p = Pattern.compile("(<.*) .*=(\'|\"|\\w)[\\w|.|(|)]*(\'|\"|\\w)(.*>)");
		Matcher m = p.matcher(output);

		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(1) + m.group(4));
		}
		m.appendTail(sb);

		String result = sb.toString();
		return result;
	}

}
