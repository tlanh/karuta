package eportfolium.com.karuta.webapp.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eportfolium.com.karuta.config.Settings;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.util.Tools;

public class HttpUtils {

	final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	/**
	 * Returns the App URL based on the request.
	 * 
	 * The port name will be appended to the host if it's non-standard.
	 *
	 * @param request Request represents an HTTP request.
	 * @return the host url
	 */
	public static String getAppUrl(HttpServletRequest request) {
		// default to the request scheme and port
		String scheme = request.getScheme();
		int port = request.getServerPort();

		// try to use reverse-proxy server's port
		String forwardedPort = request.getHeader("X-Forwarded-Port");
		if (StringUtils.isEmpty(forwardedPort)) {
			forwardedPort = request.getHeader("X_Forwarded_Port");
		}
		if (!StringUtils.isEmpty(forwardedPort)) {
			// reverse-proxy server has supplied the original port
			try {
				port = Integer.parseInt(forwardedPort);
			} catch (Throwable t) {
			}
		}

		// try to use reverse-proxy server's scheme
		String forwardedScheme = request.getHeader("X-Forwarded-Proto");
		if (StringUtils.isEmpty(forwardedScheme)) {
			forwardedScheme = request.getHeader("X_Forwarded_Proto");
		}
		if (!StringUtils.isEmpty(forwardedScheme)) {
			// reverse-proxy server has supplied the original scheme
			scheme = forwardedScheme;

			if ("https".equals(scheme) && port == 80) {
				// proxy server is https, inside server is 80
				// this is likely because the proxy server has not supplied
				// x-forwarded-port. since 80 is almost definitely wrong,
				// make an educated guess that 443 is correct.
				port = 443;
			}
		}

		// try to use reverse-proxy's context
		String context = request.getContextPath();
		String forwardedContext = request.getHeader("X-Forwarded-Context");
		if (StringUtils.isEmpty(forwardedContext)) {
			forwardedContext = request.getHeader("X_Forwarded_Context");
		}
		if (!StringUtils.isEmpty(forwardedContext)) {
			context = forwardedContext;
		}

		// trim any trailing slash
		if (context.length() > 0 && context.charAt(context.length() - 1) == '/') {
			context = context.substring(1);
		}

		// try to use reverse-proxy's hostname
		String host = request.getServerName();
		String forwardedHost = request.getHeader("X-Forwarded-Host");
		if (StringUtils.isEmpty(forwardedHost)) {
			forwardedHost = request.getHeader("X_Forwarded_Host");
		}
		if (!StringUtils.isEmpty(forwardedHost)) {
			host = forwardedHost;
		}

		// build result
		StringBuilder sb = new StringBuilder();
		sb.append(scheme);
		sb.append("://");
		sb.append(host);
		if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
			if (!host.endsWith(":" + port)) {
				sb.append(":").append(port);
			}
		}
		sb.append(context);
		return sb.toString();
	}

	public static boolean isIpAddress(String address) {
		if (StringUtils.isEmpty(address)) {
			return false;
		}
		String[] fields = address.split("\\.");
		if (fields.length == 4) {
			// IPV4
			for (String field : fields) {
				try {
					int value = Integer.parseInt(field);
					if (value < 0 || value > 255) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
			return true;
		}
		// TODO IPV6?
		return false;
	}

	/**
	 * Get user agent
	 * 
	 * @param httpRequest Récupère tous les en-têtes HTTP de la requête
	 * @return
	 */
	public static String getUserAgent(HttpServletRequest httpRequest) {
		return httpRequest.getHeader("user-agent");
	}

	public static boolean isSubmit(HttpServletRequest request, String submit) {
		return ((request.getParameter(submit) != null) || (request.getParameter(submit + "_x") != null)
				|| (request.getParameter(submit + "_y") != null));
	}

	/**
	 * Get a value from _GET if unavailable, take a default value
	 *
	 * @param request
	 * @param key          Value key
	 * @param defaultValue defaultValue
	 * @return mixed Value
	 */
	public static String getValue(HttpServletRequest request, String key) {
		return getValue(request, key, "false");
	}

	/**
	 * Get a value from _GET if unavailable, take a default value
	 *
	 * @param request
	 * @param key     Value key
	 * @return mixed Value
	 */
	public static String getValue(HttpServletRequest request, String key, String defaultValue) {
		String ret = defaultValue;
		if (StringUtils.isNotEmpty(key) && request.getParameter(key) != null) {
			ret = request.getParameter(key);
			try {
				return PhpUtil.stripslashes(URLDecoder
						.decode(URLEncoder.encode(ret, "UTF-8").replace("%5C0", "").replace("%00", ""), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("getValue failed", e);
			}
		}
		return ret;
	}

	/**
	 * Remplace la quasi-totalité des accents d'une chaine en leur équivalent
	 * non-accentués
	 * 
	 * @param s la chaine avec les accents.
	 * @return la chaine donnée en paramètres sans accents.
	 */
	public static String stripAccents(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return s;
	}

	/**
	 * Returns the HTTP host being requested with the protocol (http or https).
	 * 
	 * if @{http} is true, This function should not be used to choose http or https
	 * domain name.<br>
	 * Use PsShopUrl.getShopDomain() or PsShopUrl.getShopDomainSsl instead
	 * 
	 * The port name will be appended to the host if it's non-standard.
	 *
	 * @param configuration
	 * @param request       represents an HTTP request.
	 * @return the host url
	 */

	public static String getHttpHost(HttpServletRequest request) {
		return getHttpHost(request, false, false, false);
	}

	public static String getHttpHost(HttpServletRequest request, boolean http, boolean entities, boolean ignore_port) {
		String host = request.getHeader("X-Forwarded-Host") != null ? request.getHeader("X-Forwarded-Host")
				: request.getServerName();
		int pos = host.lastIndexOf(":");
		if (ignore_port && pos != -1) {
			host = host.substring(0, pos);
		}
		if (entities) {
			host = PhpUtil.htmlspecialchars(host);
		}
		if (http) {
			host = (Settings._KB_SSL_ENABLED_ ? "https://" : "http://") + host;
		}
		return host;
	}

	/**
	 * Get the server variable REMOTE_ADDR, or the first ip of HTTP_X_FORWARDED_FOR
	 * (when using proxy)
	 *
	 * @param httpRequest Récupère tous les en-têtes HTTP de la requête
	 * @return string remote_addr ip of client
	 */
	public static String getRemoteAddr(HttpServletRequest httpRequest) {
		String remoteAddr = "";

		if (httpRequest != null) {
			remoteAddr = httpRequest.getHeader("X-FORWARDED-FOR");
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = httpRequest.getRemoteAddr();
			}
		}

		return remoteAddr;

	}

	/**
	 * Get token to prevent CSRF (Cross-site request forgery)
	 *
	 * 
	 * @param page
	 * @param context
	 * @param request
	 * @return token encrypted
	 */
	public static String getToken(boolean page, Long userID, String userPassword, HttpServletRequest request) {
		if (userID == null) {
			throw new IllegalArgumentException("Tools.getToken() has been given null userID.");
		} else if (userID.equals(Long.valueOf(0))) {
			throw new IllegalArgumentException("Tools.getToken() has been given zero userID.");
		} else if (StringUtils.isEmpty(userPassword)) {
			throw new IllegalArgumentException("Tools.getToken() has been given empty userPassword.");
		}

		StringBuffer buf = new StringBuffer();
		if (page == true) {
			buf.append(userID).append(userPassword).append(request.getServletPath());
			return (Tools.encrypt(buf.toString().toCharArray()));
		} else {
			buf.append(userID).append(userPassword).append(page);
			return (Tools.encrypt(buf.toString().toCharArray()));
		}
	}

	public static boolean usingSecureMode(HttpServletRequest request) {
		return request.isSecure();
	}

	/**
	 * Secure an URL referrer
	 *
	 * @param string referrer URL referrer
	 * @return string secured referrer
	 */
	public static String secureReferrer(String referrer, HttpServletRequest request) {
		if (PhpUtil.preg_match("^http[s]?://" + getServerName(request) + "(:" + Settings._KB_SSL_PORT_ + ")?/.*$",
				referrer, null, Pattern.CASE_INSENSITIVE))
			return referrer;
		return Settings._KB_BASE_URI_.toString();
	}

	private static String getServerName(HttpServletRequest request) {
		return request.getServerName();
	}

}
