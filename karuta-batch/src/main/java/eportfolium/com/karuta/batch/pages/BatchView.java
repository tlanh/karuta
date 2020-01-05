package eportfolium.com.karuta.batch.pages;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;

import eportfolium.com.karuta.batch.annotation.ProtectedPage;
import eportfolium.com.karuta.batch.base.SimpleBasePage;

@ProtectedPage
@Import(stylesheet = "css/examples/olive.css")
public class BatchView extends SimpleBasePage {

	@Property
	private Long userId;
	// Screen fields

	@Property
	private int count;

	// The code
	// onActivate() is called by Tapestry to pass in the activation context from the
	// URL.

	void onActivate(int count) {
		this.count = count;
	}

	// onPassivate() is called by Tapestry to get the activation context to put in
	// the URL.

	int onPassivate() {
		return count;
	}

	void onActionFromAddX(int amount) {
		count += amount;
	}

	void onActionFromAddY(int amount) {
		count += amount;
	}

	void onActionFromClear() {
		count = 0;
	}

	// The code
	public void set(Long userId) {
		this.userId = userId;
	}

}