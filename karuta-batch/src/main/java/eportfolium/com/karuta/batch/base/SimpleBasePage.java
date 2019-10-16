package eportfolium.com.karuta.batch.base;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

import eportfolium.com.karuta.batch.state.Visit;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.CannotDeleteIsReferencedException;
import eportfolium.com.karuta.model.exception.DuplicateAlternateKeyException;
import eportfolium.com.karuta.model.exception.DuplicatePrimaryKeyException;
import eportfolium.com.karuta.model.exception.OptimisticLockException;
import eportfolium.com.karuta.model.interpreter.BusinessServiceExceptionInterpreter;

public class SimpleBasePage {

	// @SessionState is explained in http://tapestry.apache.org/session-storage.html
	@SessionState
	private Visit visit;
	private boolean visitExists;

	private BusinessServiceExceptionInterpreter businessServiceExceptionInterpreter = new BusinessServiceExceptionInterpreter();

	@Inject
	private Messages messages;

	protected Messages getMessages() {
		return messages;
	}

	public Visit getVisit() {
		return visit;
	}

	protected void setVisit(Visit visit) {
		this.visit = visit;
	}

	public boolean isVisitExists() {
		return visitExists;
	}

	public String getDateInputPattern() {
		return visitExists ? visit.getDateInputPattern() : Credential.defaultDateInputPattern;
	}

	public String getDateViewPattern() {
		return visitExists ? visit.getDateViewPattern() : Credential.defaultDateViewPattern;
	}

	public String getDateListPattern() {
		return visitExists ? visit.getDateListPattern() : Credential.defaultDateListPattern;
	}

	public DateFormat getDateInputFormat() {
		// If you want to make this static or move it into Visit, first read
		// http://thread.gmane.org/gmane.comp.java.tapestry.user/20925
		return new SimpleDateFormat(visit.getDateInputPattern());
	}

	public DateFormat getDateViewFormat() {
		// If you want to make this static or move it into Visit, first read
		// http://thread.gmane.org/gmane.comp.java.tapestry.user/20925
		return new SimpleDateFormat(visit.getDateViewPattern());
	}

	public DateFormat getDateListFormat() {
		// If you want to make this static or move it into Visit, first read
		// http://thread.gmane.org/gmane.comp.java.tapestry.user/20925
		return new SimpleDateFormat(visit.getDateListPattern());
	}

	protected String interpretBusinessServicesExceptionForCreate(Exception e) {
		String message = "";
		BusinessException x = businessServiceExceptionInterpreter.interpret(e);

		if (x instanceof DuplicatePrimaryKeyException) {
			message = getMessages().get("create_failed_duplicate_primary_key");
		} else if (x instanceof DuplicateAlternateKeyException) {
			DuplicateAlternateKeyException d = (DuplicateAlternateKeyException) x;
			message = getMessages().format("create_failed_duplicate_alternate_key", d.getTechnicalMessageText());
		} else {
			message = x.getMessage();
		}
		return message;
	}

	protected BusinessException interpretBusinessServicesException(Exception e) {
		return businessServiceExceptionInterpreter.interpret(e);
	}

	protected String interpretBusinessServicesExceptionForAdd(Exception e) {
		String message = "";
		BusinessException x = businessServiceExceptionInterpreter.interpret(e);

		if (x instanceof OptimisticLockException) {
			message = getMessages().get("add_failed_optimistic_lock");
		} else if (x instanceof DuplicatePrimaryKeyException) {
			message = getMessages().get("add_failed_duplicate_primary_key");
		} else if (x instanceof DuplicateAlternateKeyException) {
			DuplicateAlternateKeyException d = (DuplicateAlternateKeyException) x;
			message = getMessages().format("add_failed_duplicate_alternate_key", d.getTechnicalMessageText());
		} else {
			message = x.getMessage();
		}
		return message;
	}

	protected String interpretBusinessServicesExceptionForChange(Exception e) {
		String message = "";
		BusinessException x = businessServiceExceptionInterpreter.interpret(e);

		if (x instanceof OptimisticLockException) {
			message = getMessages().get("change_failed_optimistic_lock");
		} else if (x instanceof DuplicateAlternateKeyException) {
			DuplicateAlternateKeyException d = (DuplicateAlternateKeyException) x;
			message = getMessages().format("change_failed_duplicate_alternate_key", d.getTechnicalMessageText());
		} else {
			message = x.getMessage();
		}
		return message;
	}

	protected String interpretBusinessServicesExceptionForRemove(Exception e) {
		String message = "";
		BusinessException x = businessServiceExceptionInterpreter.interpret(e);

		if (x instanceof OptimisticLockException) {
			message = getMessages().get("remove_failed_optimistic_lock");
		} else if (x instanceof CannotDeleteIsReferencedException) {
			CannotDeleteIsReferencedException c = (CannotDeleteIsReferencedException) x;
			message = getMessages().format("remove_failed_is_referenced",
					new Object[] { c.getReferencedByEntityName() });
		} else {
			message = x.getMessage();
		}
		return message;
	}

	protected String interpretBusinessServicesExceptionForDelete(Exception e) {
		String message = "";
		BusinessException x = businessServiceExceptionInterpreter.interpret(e);

		if (x instanceof OptimisticLockException) {
			message = getMessages().get("delete_failed_optimistic_lock");
		} else if (x instanceof CannotDeleteIsReferencedException) {
			CannotDeleteIsReferencedException c = (CannotDeleteIsReferencedException) x;
			message = getMessages().format("delete_failed_is_referenced",
					new Object[] { c.getReferencedByEntityName() });
		} else {
			message = x.getMessage();
		}
		return message;
	}

}
