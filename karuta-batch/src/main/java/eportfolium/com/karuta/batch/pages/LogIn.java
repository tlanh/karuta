package eportfolium.com.karuta.batch.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import eportfolium.com.karuta.batch.base.SimpleBasePage;
import eportfolium.com.karuta.batch.commons.IIntermediatePage;
import eportfolium.com.karuta.batch.state.Visit;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.util.ExceptionUtil;

// To make this page accessible only by HTTPS, annotate it with @Secure and ensure your web server can deliver HTTPS.
// See http://tapestry.apache.org/secure.html .
// @Secure
@Import(stylesheet = "css/theapp/login.css")
public class LogIn extends SimpleBasePage implements IIntermediatePage {

	// The activation context

	@Property
	private String loginId;

	// Screen fields

	@Property
	private String password;

	// Generally useful bits and pieces

	@Persist
	private Link nextPageLink;

	@InjectComponent("login")
	private Form form;

	@InjectComponent("loginId")
	private TextField loginIdField;

	@Inject
	private Logger logger;

	@Inject
	private ComponentResources componentResources;

	@Inject
	private SecurityManager securityFinderService;

	@InjectPage
	private BatchView batchViewPage;

	// The code

	@Override
	public void setNextPageLink(Link nextPageLink) {
		this.nextPageLink = nextPageLink;
	}

	void onActivate(String loginId) {
		this.loginId = loginId;
	}

	String onPassivate() {
		return loginId;
	}

	void onValidateFromLogin() {

		if (form.getHasErrors()) {
			// We get here only if a server-side validator detected an error.
			return;
		}

		try {
			// Authenticate the user
			Credential user = securityFinderService.authenticateUser(loginId, password);

			// Store the user in the Visit

			setVisit(new Visit(user));
			logger.info(user.getLogin() + " has logged in.");
		} catch (BusinessException e) {
			form.recordError(loginIdField, e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Could not log in.  Stack trace follows...");
			logger.error(ExceptionUtil.printStackTrace(e));
			form.recordError(getMessages().get("login_problem"));
		}
	}

	Object onSuccess() {

		if (nextPageLink == null) {
			// return Welcome.class;
			batchViewPage.set(getVisit().getMyUserId());
			return batchViewPage;
		} else {
			componentResources.discardPersistentFieldChanges();
			return nextPageLink;
		}

	}

	Object onGoHome() {
		componentResources.discardPersistentFieldChanges();
		return BatchView.class;
	}

}
