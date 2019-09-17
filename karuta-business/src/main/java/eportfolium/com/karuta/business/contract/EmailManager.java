package eportfolium.com.karuta.business.contract;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

public interface EmailManager {

	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to)
			throws UnsupportedEncodingException, MessagingException;

	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to,
			Object to_name) throws UnsupportedEncodingException, MessagingException;

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
	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, Object to,
			Object to_name, String from, String from_name, List<File> file_attachment)
			throws UnsupportedEncodingException, MessagingException;

	boolean send(Integer id_lang, String template, String subject, Map<String, String> template_vars, String to,
			Object to_name, String from, String from_name, List<File> file_attachment, Boolean mode_smtp,
			String template_path, boolean die, Integer id_shop) throws UnsupportedEncodingException, MessagingException;

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

	public boolean send(Integer langID, String template, String subject, Map<String, String> template_vars, Object to,
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
