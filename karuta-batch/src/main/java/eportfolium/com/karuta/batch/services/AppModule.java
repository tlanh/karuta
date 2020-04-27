/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.batch.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.PropertyOverridesImpl;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.DisplayBlockContribution;
import org.apache.tapestry5.services.EditBlockContribution;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestExceptionHandler;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.upload.services.UploadSymbols;
import org.slf4j.Logger;

import eportfolium.com.karuta.batch.exception.RedirectException;
import eportfolium.com.karuta.util.JavaTimeUtil;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions. See
 * http://tapestry.apache.org/5.3.4/tapestry-ioc/module.html
 */
public class AppModule {

	@Property(write = false)
	private static boolean productionMode = true;

	@Property(write = false)
	private String iso;

	@Inject
	private Logger logger;

	@Inject
	private ApplicationStateManager sessionStateManager;

	@Inject
	private Cookies cookies;

	@Inject
	private Request request;

	@Inject
	private HttpServletRequest servletRequest;

	// Add 2 services to those provided by Tapestry.
	// - CountryNames, and SelectIdModelFactory are used by pages which ask Tapestry
	// to @Inject them.

	public static void bind(ServiceBinder binder) {
		binder.bind(SelectIdModelFactory.class, SelectIdModelFactoryImpl.class);

		// This next line addresses an issue affecting GlassFish and JBoss - see
		// http://blog.progs.be/?p=52
		javassist.runtime.Desc.useContextClassLoader = true;
	}

	// Tell Tapestry about our custom translators, validators, and their message
	// files.
	// We do this by contributing configuration to Tapestry's
	// TranslatorAlternatesSource service, FieldValidatorSource
	// service, and ComponentMessagesSource service.

	@SuppressWarnings("rawtypes")
	public static void contributeTranslatorAlternatesSource(MappedConfiguration<String, Translator> configuration,
			ThreadLocale threadLocale) {
//		configuration.add("yesno", new YesNoTranslator("yesno"));
//		configuration.add("money2", new MoneyTranslator("money2", 2, threadLocale));
	}

	public void contributeComponentMessagesSource(OrderedConfiguration<String> configuration) {
		configuration.add("myTranslationMessages", "dulocaldansnosassiettes/web/translators/TranslationMessages");
		configuration.add("myValidationMessages", "dulocaldansnosassiettes/web/validators/ValidationMessages");
	}

	// Tell Tapestry about our custom ValueEncoders.
	// We do this by contributing configuration to Tapestry's ValueEncoderSource
	// service.

	// @SuppressWarnings("rawtypes")
	// public static void contributeValueEncoderSource(MappedConfiguration<Class,
	// Object> configuration) {
	// configuration.addInstance(Person.class, PersonEncoder.class);
	// }

	// Tell Tapestry which locales we support, and tell Tapestry5jQuery not to
	// suppress Tapestry's built-in Prototype
	// and Scriptaculous (see the JQuery example for more information).
	// We do this by contributing configuration to Tapestry's ApplicationDefaults
	// service.

	public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration) {
		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en_US,en_GB,fr");
		configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
	}

	// Tell Tapestry how to block access to WEB-INF/, META-INF/, and assets that are
	// not in our assets "whitelist".
	// We do this by contributing a custom RequestFilter to Tapestry's
	// RequestHandler service.
	// - This is necessary due to https://issues.apache.org/jira/browse/TAP5-815 .
	// - RequestHandler is shown in
	// http://tapestry.apache.org/request-processing.html#RequestProcessing-Overview
	// .
	// - RequestHandler is described in
	// http://tapestry.apache.org/request-processing.html
	// - Based on an entry in the Tapestry Users mailing list by martijn.list on 15
	// Aug 09.

	public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,
			PageRenderLinkSource pageRenderLinkSource, @InjectService("TimingFilter") final RequestFilter timingFilter,
			@InjectService("Utf8Filter") final RequestFilter utf8Filter) {
		final HashSet<String> ASSETS_WHITE_LIST = new HashSet<String>(Arrays.asList("json", "jpg", "jpeg", "png", "gif",
				"js", "css", "ico", ".woff", "eot", "otf", "svg", "ttf", "woff", "woff2", "pdf"));
		configuration.add("AssetProtectionFilter", new AssetProtectionFilter(ASSETS_WHITE_LIST, pageRenderLinkSource),
				"before:*");
		configuration.add("Utf8Filter", utf8Filter); // handle UTF-8
		// configuration.add("Timing", timingFilter);
	}

	// Tell Tapestry how to detect and protect pages that require security.
	// We do this by contributing a custom ComponentRequestFilter to Tapestry's
	// ComponentRequestHandler service.
	// - ComponentRequestHandler is shown in
	// http://tapestry.apache.org/request-processing.html#RequestProcessing-Overview
	// - Based on
	// http://tapestryjava.blogspot.com/2009/12/securing-tapestry-pages-with.html

	public void contributeComponentRequestHandler(OrderedConfiguration<ComponentRequestFilter> configuration) {
		configuration.addInstance("PageProtectionFilter", PageProtectionFilter.class);
	}

	// Tell Tapestry how to handle WildFly 11's classpath URLs - WildFly uses a
	// "virtual file system".
	// Tell Tapestry how to handle JBoss 7's classpath URLs - JBoss uses a "virtual
	// file system".
	// We do this by overriding Tapestry's ClasspathURLConverter service.
	// See "Running Tapestry on JBoss" in
	// http://wiki.apache.org/tapestry/Tapestry5HowTos .
	// Tell Tapestry how to handle WildFly 11's classpath URLs - WildFly uses a
	// "virtual file system".
	// We do this by overriding Tapestry's ClasspathURLConverter service.
	// See "Running Tapestry on JBoss" (sic) in
	// http://wiki.apache.org/tapestry/Tapestry5HowTos .

	@SuppressWarnings("rawtypes")
	public static void contributeServiceOverride(MappedConfiguration<Class, Object> configuration) {
		// configuration.add(ClasspathURLConverter.class, new
		// ClasspathURLConverterJBoss7());
		configuration.add(ClasspathURLConverter.class, new ClasspathURLConverterWildFly11());
	}

	// Tell Tapestry how to handle @Inject in page and component classes.
	// We do this by contributing configuration to Tapestry's
	// ComponentClassTransformWorker service.
	// - Based on http://wiki.apache.org/tapestry/JEE-Annotation.

	@Primary
	public static void contributeComponentClassTransformWorker(
			OrderedConfiguration<ComponentClassTransformWorker2> configuration) {
	}

//	public void contributeMetaDataLocator(MappedConfiguration<String, String> configuration) {
//		ssl = BooleanUtils.toBoolean(Integer.parseInt(configurationService.get("PS_SSL_ENABLED")))
//				&& BooleanUtils.toBoolean(Integer.parseInt(configurationService.get("PS_SSL_ENABLED_EVERYWHERE")));
//		if (ssl) {
//			configuration.add(MetaDataConstants.SECURE_PAGE, "true");
//		}
//	}

	// Tell Tapestry how to handle pages annotated with @WhitelistAccessOnly, eg.
	// Tapestry's ServiceStatus and
	// PageCatalog.
	// The default WhitelistAnalyzer allows localhost only and only in
	// non-production mode.
	// Our aim is to make the servicestatus page available to ALL clients when not
	// in production mode.
	// We do this by contributing our own WhitelistAnalyzer to Tapestry's
	// ClientWhitelist service.

	public static void contributeClientWhitelist(OrderedConfiguration<WhitelistAnalyzer> configuration) {
		if (!productionMode) {
			configuration.add("NonProductionWhitelistAnalyzer", new WhitelistAnalyzer() {
				@Override
				public boolean isRequestOnWhitelist(Request request) {
					if (request.getPath().startsWith("/core/servicestatus")) {
						return true;
					} else {
						// This is copied from
						// org.apache.tapestry5.internal.services.security.LocalhostOnly
						String remoteHost = request.getRemoteHost();
						return remoteHost.equals("localhost") || remoteHost.equals("127.0.0.1")
								|| remoteHost.equals("0:0:0:0:0:0:0:1%0") || remoteHost.equals("0:0:0:0:0:0:0:1");
					}
				}
			}, "before:*");
		}
	}

	// Tell Tapestry how to build our Filer service (used in the FileUpload
	// example).
	// Annotate it with EagerLoad to force resolution of symbols at startup rather
	// than when it is first used.

	@EagerLoad
	public static IFiler buildFiler(Logger logger,
			@Inject @Symbol(UploadSymbols.REPOSITORY_LOCATION) final String uploadsPath,
			@Symbol(UploadSymbols.FILESIZE_MAX) final long fileSizeMax) {
		return new Filer(logger, UploadSymbols.REPOSITORY_LOCATION, uploadsPath, UploadSymbols.FILESIZE_MAX,
				fileSizeMax);
	}

	// Tell Tapestry how to coerce Joda Time types to and from Java Date types for
	// the TypeCoercers example.
	// We do this by contributing configuration to Tapestry's TypeCoercer service.
	// - Based on http://tapestry.apache.org/typecoercer-service.html

	@SuppressWarnings("rawtypes")
	public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration) {

		configuration.add(CoercionTuple.create(ComponentResources.class, PropertyOverrides.class,
				new Coercion<ComponentResources, PropertyOverrides>() {
					public PropertyOverrides coerce(ComponentResources input) {
						return new PropertyOverridesImpl(input);
					}
				}));

		// From java.util.Date to LocalDate

		Coercion<java.util.Date, LocalDate> toLocalDate = new Coercion<java.util.Date, LocalDate>() {
			public LocalDate coerce(java.util.Date input) {
				return JavaTimeUtil.toLocalDate(input);
			}
		};

		configuration.add(new CoercionTuple<>(java.util.Date.class, LocalDate.class, toLocalDate));

		// From LocalDate to java.util.Date

		Coercion<LocalDate, java.util.Date> fromLocalDate = new Coercion<LocalDate, java.util.Date>() {
			public java.util.Date coerce(LocalDate input) {
				return JavaTimeUtil.toJavaDate(input);
			}
		};

		configuration.add(new CoercionTuple<>(LocalDate.class, java.util.Date.class, fromLocalDate));
	}

	// Tell Tapestry how its BeanDisplay and BeanEditor can handle the JodaTime
	// types.
	// We do this by contributing configuration to Tapestry's
	// DefaultDataTypeAnalyzer and BeanBlockSource services.
	// - Based on http://tapestry.apache.org/beaneditform-guide.html .

	public static void contributeDefaultDataTypeAnalyzer(
			@SuppressWarnings("rawtypes") MappedConfiguration<Class, String> configuration) {
		configuration.add(ZonedDateTime.class, "zonedDateTime");
		configuration.add(LocalDateTime.class, "localDateTime");
		configuration.add(LocalDate.class, "localDate");
		configuration.add(LocalTime.class, "localTime");
	}

	public static void contributeBeanBlockSource(Configuration<BeanBlockContribution> configuration) {
		configuration.add(new DisplayBlockContribution("dateTime", "infra/AppPropertyDisplayBlocks", "dateTime"));
		configuration
				.add(new DisplayBlockContribution("localDateTime", "infra/AppPropertyDisplayBlocks", "localDateTime"));
		configuration.add(new DisplayBlockContribution("localDate", "infra/AppPropertyDisplayBlocks", "localDate"));
		configuration.add(new DisplayBlockContribution("localTime", "infra/AppPropertyDisplayBlocks", "localTime"));
		configuration.add(new EditBlockContribution("localDate", "infra/AppPropertyEditBlocks", "localDate"));

	}

	public RequestFilter buildUtf8Filter(@InjectService("RequestGlobals") final RequestGlobals requestGlobals) {
		return new RequestFilter() {
			public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
				requestGlobals.getHTTPServletRequest().setCharacterEncoding("UTF-8");
				requestGlobals.getHTTPServletResponse().setCharacterEncoding("UTF-8");
				return handler.service(request, response);
			}
		};
	}

	/**
	 * This is a service definition, the service will be named "TimingFilter". The
	 * interface, RequestFilter, is used within the RequestHandler service pipeline,
	 * which is built from the RequestHandler service configuration. Tapestry IoC is
	 * responsible for passing in an appropriate Logger instance. Requests for
	 * static resources are handled at a higher level, so this filter will only be
	 * invoked for Tapestry related requests.
	 *
	 *
	 * Service builder methods are useful when the implementation is inline as an
	 * inner class (as here) or require some other kind of special initialization.
	 * In most cases, use the static bind() method instead.
	 *
	 *
	 * If this method was named "build", then the service id would be taken from the
	 * service interface and would be "RequestFilter". Since Tapestry already
	 * defines a service named "RequestFilter" we use an explicit service id that we
	 * can reference inside the contribution method.
	 */
	public RequestFilter buildTimingFilter(final Logger log) {
		return new RequestFilter() {
			public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
				long startTime = System.currentTimeMillis();

				try {
					// The responsibility of a filter is to invoke the corresponding method
					// in the handler. When you chain multiple filters together, each filter
					// received a handler that is a bridge to the next filter.

					return handler.service(request, response);
				} finally {
					long elapsed = System.currentTimeMillis() - startTime;

					log.info("Request time: {} ms", elapsed);
				}
			}
		};
	}

	// handle RedirectException
	public static RequestExceptionHandler decorateRequestExceptionHandler(final Object delegate,
			final Response response, final RequestPageCache requestPageCache, final PageRenderLinkSource linkFactory,
			final ComponentClassResolver resolver) {
		return new RequestExceptionHandler() {
			public void handleRequestException(Throwable exception) throws IOException {
				// check if wrapped
				Throwable cause = exception;
				if (exception.getCause() instanceof RedirectException) {
					cause = exception.getCause();
				}

				// Better way to check if the cause is RedirectException. Sometimes it's wrapped
				// pretty deep..
				int i = 0;
				while (true) {
					if (cause == null || cause instanceof RedirectException || i > 1000) {
						break;
					}
					i++;
					cause = cause.getCause();
				}

				// check for redirect
				if (cause instanceof RedirectException) {
					// check for class and string
					RedirectException redirect = (RedirectException) cause;
					org.apache.tapestry5.Link pageLink = redirect.getPageLink();
					if (pageLink == null) {
						// handle Class (see ClassResultProcessor)
						String pageName = redirect.getMessage();
						Class<?> pageClass = redirect.getPageClass();
						if (pageClass != null) {
							pageName = resolver.resolvePageClassNameToPageName(pageClass.getName());
						}

						// handle String (see StringResultProcessor)
						pageLink = linkFactory.createPageRenderLinkWithContext(pageName, false);
					}

					// handle Link redirect
					if (pageLink != null) {
						response.sendRedirect(pageLink.toRedirectURI());
						return;
					}
				}

				// no redirect so pass on the exception
				((RequestExceptionHandler) delegate).handleRequestException(exception);
			}
		};
	}

}
