package eportfolium.com.karuta.batch.pages.infra;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;

public class ExceptionReportPage {

	@Property
	@Inject
	private Request request;

	@Property
	@Inject
	@Symbol(SymbolConstants.PRODUCTION_MODE)
	private boolean productionMode;
}
