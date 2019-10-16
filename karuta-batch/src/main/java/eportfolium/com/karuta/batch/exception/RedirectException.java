package eportfolium.com.karuta.batch.exception;

import org.apache.tapestry5.Link;

public class RedirectException extends RuntimeException {
	private static final long serialVersionUID = -2927007059196754228L;

	protected Link pageLink;
	protected Class<?> pageClass;

	public RedirectException(String pageName) {
		super(pageName);
	}

	public RedirectException(Class<?> pageClass) {
		this.pageClass = pageClass;
	}

	public RedirectException(Link link) {
		this.pageLink = link;
	}

	public Link getPageLink() {
		return pageLink;
	}

	public Class<?> getPageClass() {
		return pageClass;
	}
}
