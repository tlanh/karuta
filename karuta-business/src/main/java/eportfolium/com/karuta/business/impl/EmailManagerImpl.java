package eportfolium.com.karuta.business.impl;

import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.EmailManager;
import eportfolium.com.karuta.config.Consts;
import eportfolium.com.karuta.consumer.contract.dao.ConfigurationDao;
import eportfolium.com.karuta.util.Tools;
import eportfolium.com.karuta.util.ValidateUtil;
import freemarker.template.Configuration;

@Service
@Transactional
public class EmailManagerImpl implements EmailManager {
	public static final int TYPE_HTML = 1;
	public static final int TYPE_TEXT = 2;
	public static final int TYPE_BOTH = 3;

	static private final Logger LOGGER = LoggerFactory.getLogger(EmailManagerImpl.class);

	@Autowired
	private ConfigurationDao configService;

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private Configuration freemarkerConfiguration;

	@Autowired
	private ResourceLoader resourceLoader;

	private static final Map<String, String> LANGMAIL = createMap();

	private static Map<String, String> createMap() {
		Map<String, String> myMap = new HashMap<String, String>();
		myMap.put("Your guest account has been transformed into a customer account",
				"Votre compte invité a été transformé en compte client");
		myMap.put("Your guest account has been transformed into a customer account",
				"Votre compte invité a été transformé en compte client");
		myMap.put("The virtual product that you bought is available for download",
				"Le produit  que vous avez acheté est prêt à être téléchargé");
		myMap.put("New voucher for your order %s", "Nouveau bon de réduction pour votre commande %s");
		myMap.put("Order confirmation", "Confirmation de commande");
		myMap.put("Estimate confirmation", "Demande de devis client");
		myMap.put("Log: You have a new alert from your shop",
				"Log : Vous avez un nouveau message d\'alerte dans votre boutique");
		myMap.put("Fwd: Customer message", "TR: Message d\'un client");
		myMap.put("An answer to your message is available #ct%1$s #tc%2$s",
				"Une réponse à votre message est disponible #ct%1$s #tc%2$s");
		myMap.put("Your new password", "Votre nouveau mot de passe");
		myMap.put("Package in transit", "Livraison en cours");
		myMap.put("New message regarding your order", "Nouveau message concernant votre commande");
		myMap.put("New credit slip regarding your order", "Nouvel avoir concernant votre commande");
		myMap.put("New voucher for your order #%s", "Nouveau bon de réduction pour votre commande %s");
		myMap.put("Process the payment of your order", "Régler votre commande");
		myMap.put("Your order return status has changed", "L\'état de votre retour produit a été modifié");
		myMap.put("Welcome!", "Bienvenue !");
		myMap.put("Your message has been correctly sent #ct%1$s #tc%2$s",
				"Votre message a été correctement envoyé #ct%1$s #tc%2$s");
		myMap.put("Your message has been correctly sent", "Votre message a bien été envoyé");
		myMap.put("Message from contact form", "Message depuis le formulaire de contact");
		myMap.put("Message from a customer", "Message d\'un client");
		myMap.put("Password query confirmation", "Confirmation de demande de mot de passe");
		myMap.put("Newsletter voucher", "Bon de réduction newsletter");
		myMap.put("Newsletter confirmation", "Confirmation newsletter");
		myMap.put("Email verification", "E-mail de vérification");
		myMap.put("Your wishlist\'s link", "Lien vers votre liste d\'envies");
		myMap.put("Message from %1$s %2$s", "Message de %1$s %2$s");
		myMap.put("%1$s sent you a link to %2$s", "%1$s vous a envoyé un lien vers %2$s");
		return myMap;
	}

	public boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to)
			throws UnsupportedEncodingException, MessagingException {
		return send(id_lang, template, subject, template_vars, to, null, null, null, null, null, Consts._PS_MAIL_DIR_,
				false, null, null, null);
	}

	public boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to,
			Object to_name) throws UnsupportedEncodingException, MessagingException {
		return send(id_lang, template, subject, template_vars, to, to_name, null, null, null, null,
				Consts._PS_MAIL_DIR_, false, null, null, null);
	}

	/**
	 * 
	 * @param id_lang
	 * @param template
	 * @param subject
	 * @param template_vars
	 * @param to
	 * @param to_name
	 * @param from
	 * @param from_name
	 * @param file_attachment
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, Object to,
			Object to_name, String from, String from_name, List<File> file_attachment)
			throws UnsupportedEncodingException, MessagingException {
		return send(id_lang, template, subject, template_vars, to, to_name, from, from_name, file_attachment, null,
				Consts._PS_MAIL_DIR_, false, null, null, null);
	}

	public boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to,
			Object to_name, String from, String from_name, List<File> file_attachment, Boolean mode_smtp,
			String template_path, boolean die, Integer id_shop)
			throws UnsupportedEncodingException, MessagingException {
		return send(id_lang, template, subject, template_vars, to, to_name, from, from_name, file_attachment, mode_smtp,
				template_path, die, id_shop, null, null);
	}

	/**
	 * Send Email
	 *
	 * @param int    id_lang Language ID of the email (to translate the template)
	 * @param string template Template: the name of template not be a var but a
	 *               string !
	 * @param string subject Subject of the email
	 * @param string template_vars Template variables for the email
	 * @param string to To email
	 * @param string to_name To name
	 * @param string from From email
	 * @param string from_name To email
	 * @param array  file_attachment Array with three parameters (content, mime and
	 *               name). You can use an array of array to attach multiple files
	 * @param bool   mode_smtp SMTP mode (deprecated)
	 * @param string template_path Template path
	 * @param bool   die Die after error
	 * @param int    id_shop Shop ID
	 * @param string bcc Bcc recipient (email address)
	 * @param string reply_to Email address for setting the Reply-To header
	 * @return bool|int Whether sending was successful. If not at all, false,
	 *         otherwise amount of recipients succeeded.
	 */

	@SuppressWarnings("unchecked")
	public boolean send(Integer langID, String template, String subject, Map<String, String> template_vars, Object to,
			Object to_name, String from, String from_name, List<File> file_attachment, Boolean mode_smtp,
			String template_path, boolean die, Integer id_shop, String bcc, String reply_to)
			throws MessagingException, UnsupportedEncodingException {

		final Map<String, String> configuration = configService.getMultiple(
				Arrays.asList("PS_SHOP_EMAIL", "PS_MAIL_METHOD", "PS_MAIL_SERVER", "PS_MAIL_USER", "PS_MAIL_PASSWD",
						"PS_SHOP_NAME", "PS_MAIL_SMTP_ENCRYPTION", "PS_MAIL_SMTP_PORT", "PS_MAIL_TYPE"),
				null, null, id_shop);

		// Returns immediately if emails are deactivated
		if (configService.get("PS_MAIL_METHOD").equals("3"))
			return true;

		if (!configuration.containsKey("PS_MAIL_SMTP_ENCRYPTION"))
			configuration.put("PS_MAIL_SMTP_ENCRYPTION", "off");
		if (!configuration.containsKey("PS_MAIL_SMTP_PORT"))
			configuration.put("PS_MAIL_SMTP_PORT", "default");

		// Sending an e-mail can be of vital importance for the merchant, when his
		// password is lost for example, so we
		// must not die but do our best to send the e-mail
		if (from == null || !ValidateUtil.isEmail(from))
			from = configuration.get("PS_SHOP_EMAIL");
		if (!ValidateUtil.isEmail(from))
			from = null;

		// from_name is not that important, no need to die if it is not valid
		if (from_name == null || !ValidateUtil.isMailName(from_name))
			from_name = configuration.get("PS_SHOP_NAME");
		if (!ValidateUtil.isMailName(from_name))
			from_name = null;

		// It would be difficult to send an e-mail if the e-mail is not valid, so this
		// time we can die if there is a
		// problem
		if (!(to instanceof Collection) && !ValidateUtil.isEmail((String) to)) {
			// Tools::dieOrLog(Tools::displayError(), $die);
			LOGGER.info("Error: parameter \"to\" is corrupted");
			return false;
		}

		if (template_vars == null || template_vars.isEmpty()) {
			template_vars = new HashMap<String, String>(0);
		}

		// Do not crash for this error, that may be a complicated customer name
		if (to_name instanceof String && StringUtils.isNotEmpty((String) to_name)
				&& !ValidateUtil.isMailName((String) to_name)) {
			to_name = null;
		}

		if (!ValidateUtil.isTplName(template)) {
			// Tools::dieOrLog(Tools::displayError('Error: invalid e-mail template'), $die);
			LOGGER.info("Error: invalid e-mail template");
			return false;
		}

		if (!ValidateUtil.isMailSubject(subject)) {
			// Tools::dieOrLog(Tools::displayError('Error: invalid e-mail subject'), $die);
			LOGGER.info("Error: invalid e-mail subject");
			return false;
		}

		/* Construct multiple recipients list if needed */
		Map<String, List<InternetAddress>> recipients_list = new HashMap<String, List<InternetAddress>>();
		List<InternetAddress> to_list = new ArrayList<InternetAddress>();
		List<InternetAddress> bcc_list = new ArrayList<InternetAddress>();
		String current_to_name = null;
		if (to instanceof List<?> && CollectionUtils.isNotEmpty((Collection<?>) to)) {
			List<String> tmp_to = (List<String>) to;
			java.util.Iterator<String> it = tmp_to.iterator();
			for (int key = 0; key < tmp_to.size(); key++) {
				String addr = it.next().trim();
				if (!ValidateUtil.isEmail(addr)) {
					// Tools::dieOrLog(Tools::displayError('Error: invalid e-mail address'), $die);
					LOGGER.info("Error: invalid e-mail address");
					return false;
				}

				if (to_name instanceof List<?>) {
					List<String> tmp_to_name = (List<String>) to_name;
					if (CollectionUtils.isNotEmpty(tmp_to_name) && ValidateUtil.isGenericName(tmp_to_name.get(key)))
						current_to_name = tmp_to_name.get(key);
				}

				if (current_to_name == null || current_to_name.equalsIgnoreCase(addr)) {
					current_to_name = "";
				}
				to_list.add(new InternetAddress(addr, current_to_name));
			}
		} else {
			/* Simple recipient, one address */
			if (to_name == null || to_name.equals(to)) {
				to_name = "";
			} else {
				to_name = (String) to_name;
			}
			to_list.add(new InternetAddress((String) to, (String) to_name));
		}
		if (StringUtils.isNotBlank(bcc)) {
			bcc_list.add(new InternetAddress(bcc));
		}
		recipients_list.put("to", to_list);
		recipients_list.put("bcc", bcc_list);
		to = to_list;

		// Create a Properties object to contain connection configuration information.
		Properties properties = System.getProperties();
		/* Connect with the appropriate configuration */
		if (configuration.get("PS_MAIL_METHOD").equals("2")) {
			if (configuration.get("PS_MAIL_SERVER").isEmpty() || configuration.get("PS_MAIL_SMTP_PORT").isEmpty()) {
				LOGGER.info("Error: invalid SMTP server or SMTP port");
				// Tools::dieOrLog(Tools::displayError('Error: invalid SMTP server or SMTP
				// port'), $die);
				return false;
			}

			// String The SMTP server to connect to.
			properties.setProperty("mail.smtp.host", configuration.get("PS_MAIL_SERVER"));
			// int The SMTP server port to connect to, if the connect() method doesn't
			// explicitly specify one. Defaults
			// to 25.
			properties.setProperty("mail.smtp.port", configuration.get("PS_MAIL_SMTP_PORT"));
			// If true, enables the use of the STARTTLS command (if supported by the server)
			// to switch the connection to
			// a TLS-protected connection before issuing any login commands. Note that an
			// appropriate trust store must
			// configured so that the client will trust the server's certificate. Defaults
			// to false.
			properties.setProperty("mail.smtp.starttls.enable",
					configuration.get("PS_MAIL_SMTP_ENCRYPTION").equalsIgnoreCase("tls") ? "true" : "false");
			// If set to true, use SSL to connect and use the SSL port by default. Defaults
			// to false for the "smtp"
			// protocol and true for the "smtps" protocol.
			properties.setProperty("mail.smtp.ssl.enable",
					configuration.get("PS_MAIL_SMTP_ENCRYPTION").equalsIgnoreCase("ssl") ? "true" : "false");
			// int Socket connection timeout value in milliseconds. This timeout is
			// implemented by java.net.Socket.
			// Default is inullnfinite timeout.
			properties.setProperty("mail.smtp.connectiontimeout", "4000");
			properties.setProperty("mail.transport.protocol", "smtp");
			properties.setProperty("mail.smtp.auth", "true");
		}

		String mailDomain = configService.get("PS_MAIL_DOMAIN");
		if (mailDomain != null && StringUtils.isNotBlank(mailDomain)) {
			properties.setProperty("mail.smtp.auth.ntlm.domain", mailDomain);
		}

		// Create a Session object to represent a mail session with the specified
		// properties.
		Session session = Session.getDefaultInstance(properties);

		// Create a transport.
		Transport transport = session.getTransport();

		// Create a message with the specified information.
		MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
		message.setFrom(new InternetAddress(from, from_name));

		// Set To: header field of the header.
		message.addRecipients(Message.RecipientType.TO, recipients_list.get("to").toArray(new InternetAddress[0]));

		// Set Bcc: header field of the header.
		message.addRecipients(Message.RecipientType.BCC, recipients_list.get("bcc").toArray(new InternetAddress[0]));

		// Set Subject: header field
		message.setSubject(subject, "UTF-8");

		String iso = "fr";
		if (StringUtils.isBlank(iso)) {
			LOGGER.info("Error - No ISO code for email");
			return false;
		}

		// get templatePath
		String iso_template = iso + "/" + template;
		Resource templateHtml = null;
		Resource templateTxt = null;
		templateTxt = resourceLoader.getResource(template_path + iso_template + ".txt.ftl");
		templateHtml = resourceLoader.getResource(template_path + iso_template + ".html.ftl");

		if (templateTxt == null && (Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_BOTH
				|| Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_TEXT)) {
			LOGGER.info("Error - The following e-mail template is missing : " + template_path + iso_template
					+ ".txt.ftl" + die);
			return false;
		} else if (templateHtml == null && (Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_BOTH
				|| Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_HTML)) {
			LOGGER.info("Error - The following e-mail template is missing: " + template_path + iso_template
					+ ".html.ftl" + die);
			return false;
		}
		String logo = null;
		String PS_LOGO_IN_MAIL = configService.get("PS_LOGO_MAIL");
		String PS_LOGO = configService.get("PS_LOGO");
		template_vars.put("shop_logo", "");

		if (PS_LOGO_IN_MAIL != null && PS_LOGO != null && BooleanUtils.toBoolean(Integer.parseInt(PS_LOGO_IN_MAIL))
				&& resourceLoader.getResource(Consts._PS_IMG_DIR_ + PS_LOGO) != null) {
			logo = StringUtils.join(Consts._PS_IMG_DIR_ + PS_LOGO);
		} else if (PS_LOGO != null
				&& resourceLoader.getResource(StringUtils.join(Consts._PS_IMG_DIR_ + PS_LOGO)) != null) {
			logo = StringUtils.join(Consts._PS_IMG_DIR_ + PS_LOGO);
		}

		/* don't attach the logo as */
		if (logo != null) {
			Resource asset = resourceLoader.getResource(logo);
			try {
				template_vars.put("shop_logo", asset.getURL().toExternalForm());
			} catch (IOException e) {
			}
		}

		template_vars.put("shop_name", Tools.safeOutput(configService.get("PS_SHOP_NAME")));
		template_vars.put("color", Tools.safeOutput(configService.get("PS_MAIL_COLOR")));

		if (!template_vars.containsKey("shop_url")) {
			template_vars.put("shop_url", configurationManager.getKarutaURL(null));
		}
		if (!template_vars.containsKey("my_account_url")) {
			template_vars.put("my_account_url", configurationManager.getKarutaURL(null));
		}
		if (!template_vars.containsKey("my_account_url")) {
			template_vars.put("my_account_url", configurationManager.getKarutaURL(null));
		}
		if (!template_vars.containsKey("guest_tracking_url")) {
			template_vars.put("guest_tracking_url", configurationManager.getKarutaURL(null));
		}
		if (!template_vars.containsKey("history_url")) {
			template_vars.put("history_url", configurationManager.getKarutaURL(null));
		}

		String template_html = null, template_txt = null;
		try {
			template_html = processTemplateIntoString(freemarkerConfiguration.getTemplate(iso_template + ".html.ftl"),
					template_vars);
			template_txt = processTemplateIntoString(freemarkerConfiguration.getTemplate(iso_template + ".txt.ftl"),
					template_vars);
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}

		MimeBodyPart messageBodyPart = new MimeBodyPart();
		if (Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_BOTH
				|| Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_TEXT) {
			// Now set the actual message
			messageBodyPart.setContent(template_txt, "text/plain; charset=UTF-8");
		}
		if (Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_BOTH
				|| Integer.parseInt(configuration.get("PS_MAIL_TYPE")) == TYPE_HTML) {
			messageBodyPart.setContent(template_html, "text/html; charset=UTF-8");
		}

		// attach differents parts
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// Multiple attachments?
		if (file_attachment != null && file_attachment.size() > 0) {
			for (File file : file_attachment) {
				MimeBodyPart attachPart = new MimeBodyPart();
				try {
					attachPart.attachFile(file);
					multipart.addBodyPart(attachPart);
				} catch (IOException e) {
					LOGGER.error("Cannot attach file", e);
				}
			}
		}

		// Create email
		message.setContent(multipart);

		// Send the message.
		try {
			LOGGER.info("Sending email...");
			turnedOffSecurity();
			// Connect to SES using the SMTP username and password from DB.
			transport.connect(configuration.get("PS_MAIL_SERVER"), configuration.get("PS_MAIL_USER"),
					configuration.get("PS_MAIL_PASSWD"));

			// Send the email.
			transport.sendMessage(message, message.getAllRecipients());
			LOGGER.info("Email sent!");
		} catch (Exception ex) {
			LOGGER.error("The email was not sent.");
			LOGGER.error("Error message: " + ex.getMessage());
			return false;
		} finally {
			// Close and terminate the connection.
			transport.close();
		}
		return true;
	}

	/**
	 * This method is used to get the translation for email Object. For an object is
	 * forbidden to use htmlentities, we have to return a sentence with accents.
	 *
	 * @param sentence raw sentence (write directly in file)
	 * @return mixed
	 */
	public String getTranslation(String sentence) {
		return LANGMAIL.get(sentence);
	}

	/**
	 * This method is used to get the translation for email Object. For an object is
	 * forbidden to use htmlentities, we have to return a sentence with accents.
	 *
	 * @param sentence raw sentence (write directly in file)
	 * @return mixed
	 */
	public String getTranslation(String sentence, Integer id_lang) {
		return LANGMAIL.get(sentence);
	}

	/**
	 * Create a trust manager that does not validate certificate chains like the
	 * default
	 */
	public void turnedOffSecurity() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				// No need to implement.
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				// No need to implement.
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
