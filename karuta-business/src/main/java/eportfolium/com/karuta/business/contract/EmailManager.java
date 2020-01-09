package eportfolium.com.karuta.business.contract;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

public interface EmailManager {

	/**
	 * Send Email
	 * 
	 * @param id_lang       Language ID of the email (to translate the template)
	 * @param template      the name of template not be a var but a string !
	 * @param subject       Subject of the email
	 * @param template_vars Template variables for the email
	 * @param to            To email
	 * @return true, if sending was successful. false, otherwise
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to)
			throws UnsupportedEncodingException, MessagingException;

	/**
	 * Send Email
	 * 
	 * @param id_lang       Language ID of the email (to translate the template)
	 * @param template      the name of template not be a var but a string !
	 * @param subject       Subject of the email
	 * @param template_vars Template variables for the email
	 * @param to            To email
	 * @param to_name       To name
	 * @return true, if sending was successful. false, otherwise
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to,
			Object to_name) throws UnsupportedEncodingException, MessagingException;

	/**
	 * Send Email
	 * 
	 * @param id_lang         Language ID of the email (to translate the template)
	 * @param template        the name of template not be a var but a string !
	 * @param subject         Subject of the email
	 * @param template_vars   Template variables for the email
	 * @param to              To email
	 * @param to_name         To name
	 * @param from            From email
	 * @param from_name       From name
	 * @param file_attachment Array with three parameters (content, mime and name).
	 *                        You can use an array of array to attach multiple
	 *                        files.
	 * @return true, if sending was successful. false, otherwise
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, Object to,
			Object to_name, String from, String from_name, List<File> file_attachment)
			throws UnsupportedEncodingException, MessagingException;

	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to,
			Object to_name, String from, String from_name, List<File> file_attachment, Boolean mode_smtp,
			String template_path, boolean die, Integer id_shop) throws UnsupportedEncodingException, MessagingException;

	/**
	 * @param langId          Language ID of the email (to translate the template)
	 * @param template        the name of template not be a var but a string !
	 * @param subject         Subject of the email
	 * @param template_vars   Template variables for the email
	 * @param to              To email
	 * @param to_name         To name
	 * @param from            From email
	 * @param from_name       From name
	 * @param file_attachment Array with three parameters (content, mime and name).
	 *                        You can use an array of array to attach multiple files
	 * @param mode_smtp       SMTP mode (deprecated)
	 * @param template_path   Template path
	 * @param die             Die after error
	 * @param id_shop         Shop ID
	 * @param bcc             Bcc recipient (email address)
	 * @param reply_to        reply_to Email address for setting the Reply-To header
	 * @return true, if sending was successful. false, otherwise
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public boolean send(Integer langId, String template, String subject, Map<String, String> template_vars, Object to,
			Object to_name, String from, String from_name, List<File> file_attachment, Boolean mode_smtp,
			String template_path, boolean die, Integer id_shop, String bcc, String reply_to)
			throws MessagingException, UnsupportedEncodingException;

	/**
	 * Create a trust manager that does not validate certificate chains like the
	 * default
	 */
	public void turnedOffSecurity();

	/**
	 * This method is used to get the translation for email Object. For an object is
	 * forbidden to use htmlentities, we have to return a sentence with accents.
	 *
	 * @param sentence raw sentence (write directly in file)
	 * @return mixed
	 */
	public String getTranslation(String sentence);

	/**
	 * This method is used to get the translation for email Object. For an object is
	 * forbidden to use htmlentities, we have to return a sentence with accents.
	 *
	 * @param sentence raw sentence (write directly in file)
	 * @return mixed
	 */
	public String getTranslation(String sentence, Integer id_lang);

}
