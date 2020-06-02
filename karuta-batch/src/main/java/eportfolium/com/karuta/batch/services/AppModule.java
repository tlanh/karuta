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

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.PropertyOverridesImpl;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.Cookies;
import org.apache.tapestry5.services.DisplayBlockContribution;
import org.apache.tapestry5.services.EditBlockContribution;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.slf4j.Logger;

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
		configuration.add("Utf8Filter", utf8Filter); // handle UTF-8
		// configuration.add("Timing", timingFilter);
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

	// Tell Tapestry how to handle @Inject in page and component classes.
	// We do this by contributing configuration to Tapestry's
	// ComponentClassTransformWorker service.
	// - Based on http://wiki.apache.org/tapestry/JEE-Annotation.

	@Primary
	public static void contributeComponentClassTransformWorker(
			OrderedConfiguration<ComponentClassTransformWorker2> configuration) {
	}

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

}
