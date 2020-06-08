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

package eportfolium.com.karuta.business.impl;

import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.EmailManager;
import freemarker.template.Configuration;

@Service
@Transactional
public class EmailManagerImpl implements EmailManager {
	private static final String MAIL_DIR =  "classpath:/META-INF/assets/core/mails/";
	private static final String IMG_DIR = "classpath:/META-INF/assets/images/";

	public static final int TYPE_HTML = 1;
	public static final int TYPE_TEXT = 2;
	public static final int TYPE_BOTH = 3;

	static private final Logger LOGGER = LoggerFactory.getLogger(EmailManagerImpl.class);

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private Configuration freemarkerConfiguration;

	@Autowired
	private ResourceLoader resourceLoader;

	private static final Map<String, String> LANGMAIL = createMap();

	private static Map<String, String> createMap() {
		Map<String, String> myMap = new HashMap<>();

		myMap.put("Your new password", "Votre nouveau mot de passe");
		myMap.put("Welcome!", "Bienvenue !");

		return myMap;
	}

	@Override
	public boolean send(String template, String subject, Map<String, String> locals, String to,
			String to_name) throws UnsupportedEncodingException, MessagingException {
		return send(template, subject, locals, to, to_name, null);
	}

	@Override
	public boolean send(String template, String subjectKey, Map<String, String> locals,
						String to, String to_name, String bcc)
			throws MessagingException, UnsupportedEncodingException {

		final Map<String, String> configuration = configurationManager.getMultiple(
				Arrays.asList("app_email", "mail_method", "mail_server", "mail_user",
						"mail_passwd", "app_name", "mail_smtp_encryption", "mail_smtp_port",
						"mail_type", "mail_color", "logo", "logo_mail"),
				Integer.valueOf(configurationManager.get("lang_default")));

		final int mailMethod = Integer.parseInt(configuration.get("mail_method"));
		final int mailType = Integer.parseInt(configuration.get("mail_type"));
		final int logoMail = Integer.parseInt(configuration.get("logo_mail"));
		final String color = configuration.get("mail_color");
		final String logo = configuration.get("logo_mail");
		final String appName = configuration.get("app_name");

		final boolean htmlSupported = mailType == TYPE_BOTH ||  mailType == TYPE_HTML;
		final boolean txtSupported = mailType == TYPE_BOTH || mailType == TYPE_TEXT;

		// Returns immediately if emails are deactivated
		if (mailMethod == 3)
			return true;

		final String from = configuration.get("app_email");
		final String fromName = configuration.get("app_name");

		final String subject = LANGMAIL.get(subjectKey);

		if (!configuration.containsKey("mail_smtp_encryption"))
			configuration.put("mail_smtp_encryption", "off");
		if (!configuration.containsKey("mail_smtp_port"))
			configuration.put("mail_smtp_port", "default");

		final String mailServer = configuration.get("mail_server");
		final String mailUser = configuration.get("mail_user");
		final String mailPasswd = configuration.get("mail_passwd");
		final String smtpPort = configuration.get("mail_smtp_port");
		final String encryption = configuration.get("mail_smtp_encryption");

		// Sending an e-mail can be of vital importance for the merchant, when his
		// password is lost for example, so we
		// must not die but do our best to send the e-mail
		EmailValidator emailValidator = EmailValidator.getInstance();

		Predicate<String> nameValidator = Pattern
				.compile("^[^<>;=#{}]*$", Pattern.UNICODE_CASE)
				.asPredicate();

		Predicate<String> subjectValidator = Pattern
				.compile("^[^<>;{}]*$", Pattern.UNICODE_CASE)
				.asPredicate();

		Predicate<String> templateValidator = Pattern.compile("^[a-z0-9_-]+$").asPredicate();


		// It would be difficult to send an e-mail if the e-mail is not valid, so this
		// time we can die if there is a
		// problem
		if (!emailValidator.isValid(to)) {
			LOGGER.error("Parameter \"to\" is corrupted");
			return false;
		}

		// Do not crash for this error, that may be a complicated customer name
		if (StringUtils.isNotEmpty(to_name) && !nameValidator.test(to_name)) {
			to_name = null;
		}

		if (!templateValidator.test(template)) {
			LOGGER.error("Invalid e-mail template");
			return false;
		}

		if (!subjectValidator.test(subject)) {
			LOGGER.error("Invalid e-mail subject");
			return false;
		}

		Map<String, InternetAddress> recipients = new HashMap<>();

		if (StringUtils.isNotBlank(bcc)) {
			recipients.put("bcc", new InternetAddress(bcc));
		}

		if (to_name == null || to_name.equals(to)) {
			to_name = "";
		}

		recipients.put("to", new InternetAddress(to, to_name));

		// Create a Properties object to contain connection configuration information.
		Properties properties = System.getProperties();
		/* Connect with the appropriate configuration */
		if (mailMethod == 2) {
			if (mailServer.isEmpty() || smtpPort.isEmpty()) {
				LOGGER.error("Invalid SMTP server or SMTP port");

				return false;
			}

			properties.setProperty("mail.smtp.host", mailServer);
			properties.setProperty("mail.smtp.port", smtpPort);

			properties.setProperty("mail.smtp.starttls.enable",
					encryption.equalsIgnoreCase("tls") ? "true" : "false");

			properties.setProperty("mail.smtp.ssl.enable",
					encryption.equalsIgnoreCase("ssl") ? "true" : "false");

			properties.setProperty("mail.smtp.connectiontimeout", "4000");
			properties.setProperty("mail.transport.protocol", "smtp");
			properties.setProperty("mail.smtp.auth", "true");
		}

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(properties);

		// Create a transport.
		Transport transport = session.getTransport();

		// Create a message with the specified information.
		MimeMessage message = new MimeMessage(session);

		message.setFrom(new InternetAddress(from, fromName));
		message.addRecipients(Message.RecipientType.TO, recipients.get("to").toString());
		message.addRecipients(Message.RecipientType.BCC, recipients.get("bcc").toString());
		message.setSubject(subject, "UTF-8");

		// get templatePath
		String isoTemplate = "fr/" + template;
		String htmlTemplatePath = MAIL_DIR + isoTemplate + ".html.ftl";
		String txtTemplatePath = MAIL_DIR + isoTemplate + ".txt.ftl";

		Resource htmlTemplate = resourceLoader.getResource(htmlTemplatePath);
		Resource txtTemplate = resourceLoader.getResource(txtTemplatePath);

		if (txtSupported && !txtTemplate.exists()) {
			LOGGER.error("The following e-mail template is missing: " + txtTemplatePath);
			return false;
		} else if (htmlSupported && !htmlTemplate.exists()) {
			LOGGER.error("The following e-mail template is missing: " + htmlTemplatePath);
			return false;
		}

		final String logoPath = IMG_DIR + logo;
		locals.put("shop_logo", "");

		if (logo != null && logoMail == 1 && resourceLoader.getResource(logoPath).exists()) {
			Resource asset = resourceLoader.getResource(logoPath);

			try {
				locals.put("shop_logo", asset.getURL().toExternalForm());
			} catch (IOException ignored) { }
		}


		locals.put("shop_name", StringEscapeUtils.escapeHtml4(appName));
		locals.put("color", StringEscapeUtils.escapeHtml4(color));

		if (!locals.containsKey("shop_url")) {
			locals.put("shop_url", configurationManager.getKarutaURL());
		}

		String htmlPart, txtPart;

		try {
			htmlPart = parseTemplate(isoTemplate + ".html.ftl", locals);
			txtPart = parseTemplate(isoTemplate + ".txt.ftl", locals);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		MimeBodyPart messageBodyPart = new MimeBodyPart();

		if (mailType == TYPE_BOTH || mailType == TYPE_TEXT) {
			messageBodyPart.setContent(txtPart, "text/plain; charset=UTF-8");
		}

		if (mailType == TYPE_BOTH || mailType == TYPE_HTML) {
			messageBodyPart.setContent(htmlPart, "text/html; charset=UTF-8");
		}

		// attach differents parts
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// Create email
		message.setContent(multipart);

		// Send the message.
		try {
			LOGGER.info("Sending email...");
			turnedOffSecurity();

			// 1. Connect to SES using the SMTP username and password from DB.
			// 2. Send the message.
			transport.connect(mailServer, mailUser, mailPasswd);
			transport.sendMessage(message, message.getAllRecipients());

			LOGGER.info("Email sent!");
		} catch (Exception ex) {
			LOGGER.error("The email was not sent.");
			LOGGER.error("Error message: " + ex.getMessage());
			return false;
		} finally {
			transport.close();
		}

		return true;
	}

	private String parseTemplate(String path, Map<String, String> vars)
			throws IOException, TemplateException {
		return FreeMarkerTemplateUtils.processTemplateIntoString(
				freemarkerConfiguration.getTemplate(path),
				vars
		);
	}

	/**
	 * Create a trust manager that does not validate certificate chains like the
	 * default
	 */
	public void turnedOffSecurity() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				// No need to implement.
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				// No need to implement.
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
