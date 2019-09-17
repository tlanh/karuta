package eportfolium.com.karuta.webapp.rest.resource;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.CredentialSubstitutionId;

public abstract class AbstractResource {

	/**
	 * Fetch user session info
	 * 
	 * @param request
	 * @param login
	 * @param token
	 * @param group
	 * @return
	 */
	public Credential checkCredential(HttpServletRequest request, String login, String token, String group) {
		HttpSession session = request.getSession(true);

		Credential ui = new Credential();
		initService(request);
		Long val = (Long) session.getAttribute("uid");
		if (val != null)
			ui.setId(val);
//			ui.groupId = val;
		val = (Long) session.getAttribute("subuid");
		CredentialSubstitution cs = new CredentialSubstitution(new CredentialSubstitutionId());
		if (val != null) {
			cs.setCredentialSubstitutionId(val);
			ui.setCredentialSubstitution(cs);
		}

		ui.setLogin((String) session.getAttribute("user"));
		ui.setSubUser((String) session.getAttribute("subuser"));

		return ui;
	}

	protected boolean isUUID(String uuidstr) {
		try {
			UUID.fromString(uuidstr);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
