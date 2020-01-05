package eportfolium.com.karuta.batch.state;

import java.io.Serializable;

import eportfolium.com.karuta.model.bean.Credential;

public class Visit implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long myUserId = null;
	private String myLoginId = null;
	private String myEmail = null;

	public Visit(Credential user) {
		myUserId = user.getId();
		cacheUsefulStuff(user);
	}

	public void noteChanges(Credential user) {
		if (user == null) {
			throw new IllegalArgumentException();
		} else if (user.getId().equals(myUserId)) {
			cacheUsefulStuff(user);
		}
	}

	private void cacheUsefulStuff(Credential user) {
		myLoginId = user.getLogin();
		myEmail = user.getEmail();
	}

	public Long getMyUserId() {
		return myUserId;
	}

	public String getMyLoginId() {
		return myLoginId;
	}

	public String getMyEmail() {
		return myEmail;
	}

	public String getDateInputPattern() {
		return "dd/MM/yy";
	}

	public String getDateViewPattern() {
		return "dd MMM yyyy";
	}

	public String getDateListPattern() {
		return "yyyy-MM-dd";
	}

}
