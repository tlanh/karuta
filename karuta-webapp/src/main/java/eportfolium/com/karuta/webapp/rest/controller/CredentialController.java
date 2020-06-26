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

package eportfolium.com.karuta.webapp.rest.controller;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.LoginDocument;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.yale.its.tp.cas.client.ServiceTicketValidator;
import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.EmailManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.component.AuthenticationProviderInst;

import org.xml.sax.SAXException;

@RestController
@RequestMapping("/credential")
public class CredentialController extends AbstractController {

    @Autowired
    private UserManager userManager;

    @Autowired
    private EmailManager emailManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private AuthenticationProviderInst authenticationProvider;
    
    @InjectLogger
    private static Logger logger;

    /**
     * Fetch current user info. <br>
     * GET /rest/api/credential
     *
     * @return <user id="uid"> <username></username> <firstname></firstname>
     *         <lastname></lastname> <email></email> <admin>1/0</admin>
     *         <designer>1/0</designer> <active>1/0</active>
     *         <substitute>1/0</substitute> </user>
     */
    @GetMapping(produces = "application/xml")
    public HttpEntity<CredentialDocument> getCredential(Authentication authentication, HttpServletRequest request) {
    	HttpSession session = request.getSession(false);
    	CredentialDocument userInfo = null;
    	if( session != null )
    	{
	    	SecurityContext securityContext = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
	    	authentication = securityContext.getAuthentication();
	    	userInfo = (CredentialDocument)authentication.getDetails();
    	}
    	
    	if( userInfo == null )
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        return new HttpEntity<>(userManager.getUserInfos(userInfo.getId()));
    }

    /**
     * Send login information.
     *
     * POST /rest/api/credential/login
     */
    @RequestMapping(value = "/login", consumes = "application/xml", produces = "application/xml",
        method = { RequestMethod.POST, RequestMethod.PUT })
    public HttpEntity<Object> postCredentialFromXml(HttpServletRequest request, @RequestBody LoginDocument credentials) {

        String authlog = configurationManager.get("auth_log");
        Logger authLog = null;

        if (StringUtils.isNotEmpty(authlog)) {
            authLog = LoggerFactory.getLogger(authlog);
        }

        String login = credentials.getLogin();

        CredentialDocument credential = securityManager.login(credentials);

        if (credential == null) {
            if (authLog != null) {
                authLog.info(String.format("Authentication error for user '%s' date '%s'\n", login,
                        new Date()));
            }

            return ResponseEntity
                        .status(403)
                        .body("Invalid credential");

        } else {
            boolean substitute = credential.getSubstitute() == 1;

      			List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(login, credential.getPassword(), grantedAuthorities);
            authReq.setDetails(credential);
						Authentication authentication = authenticationProvider.authenticate(authReq);

				    SecurityContext securityContext = SecurityContextHolder.getContext();
				    securityContext.setAuthentication(authentication);

						HttpSession session = request.getSession(true);
				    session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            if (authLog != null) {

                if (substitute) {
                    authLog.info(String.format("Authentication success for user '%s' date '%s' (Substitution)\n",
                            login, new Date()));
                } else {
                    authLog.info(String.format("Authentication success for user '%s' date '%s'\n", login,
                            new Date()));
                }
            }
        }

        return new HttpEntity<>(credential);
    }

    /**
     * Tell system to forgot your password.
     *
     * POST /rest/api/credential/forgot
     */
    @PostMapping(value = "/forgot", consumes = "application/xml")
    public ResponseEntity<String> postForgotCredential(@RequestBody LoginDocument document,
                                                       HttpServletRequest request) throws Exception {

        String resetEnable = configurationManager.get("enable_password_reset");

        if (!Arrays.asList("y", "true").contains(resetEnable)) {
            return ResponseEntity
                        .notFound()
                        .build();
        }

        String username = document.getLogin();
        String email = userManager.getEmailByLogin(username);

        if (!StringUtils.isNotEmpty(email)) {
            return ResponseEntity
                        .notFound()
                        .build();
        }

        Logger securityLog = null;
        String securitylog = configurationManager.get("security_log");

        if (StringUtils.isNotEmpty(securitylog)) {
            securityLog = LoggerFactory.getLogger(securitylog);
        }

        // Try to write changes to database
        String password = securityManager.generatePassword();
        boolean result = securityManager.changePassword(username, password);

        if (result) {
            if (securityLog != null) {
                String ip = request.getRemoteAddr();
                securityLog.info(String.format(
                        "[%s] [%s] a demandé la réinitialisation de son mot de passe\n", ip, username));
            }

            final Map<String, String> locals = new HashMap<>();

            locals.put("firstname", username);
            locals.put("lastname", "");
            locals.put("email", email);
            locals.put("passwd", password);

            String cc_email = configurationManager.get("sys_email");
            // Envoie d'un email
            emailManager.send("employee_password",
                    "Your new password!",
                    locals, email, username, cc_email);

            return ResponseEntity
                        .ok()
                        .body("sent");
        } else {
            return ResponseEntity
                        .badRequest()
                        .build();
        }
    }

    @RequestMapping(value = "/login/cas", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> getCredentialFromCas(@RequestParam String ticket,
                                                       @RequestParam String redir,
                                                       HttpServletRequest httpServletRequest)
            throws ParserConfigurationException, SAXException, IOException {

        HttpSession session = httpServletRequest.getSession(true); // FIXME
        String userId = null;
        String completeURL;
        StringBuffer requestURL;
        String casUrlValidation = configurationManager.get("casUrlValidation");

        ServiceTicketValidator sv = new ServiceTicketValidator();

        if (casUrlValidation == null) {
            ResponseEntity<String> response = null;
            try {
                // formulate the response
                response = ResponseEntity
                        .status(HttpStatus.PRECONDITION_FAILED)
                        .body("CAS URL not defined");
            } catch (Exception e) {
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            return response;
        }

        sv.setCasValidateUrl(casUrlValidation);

        /// X-Forwarded-Proto is for certain setup, check config file
        /// for some more details
        String proto = httpServletRequest.getHeader("X-Forwarded-Proto");
        requestURL = httpServletRequest.getRequestURL();
        if (proto == null) {
            System.out.println("cas usuel");
            if (redir != null) {
                requestURL.append("?redir=").append(redir);
            }
            completeURL = requestURL.toString();
        } else {
            /// Keep only redir parameter
            if (redir != null) {
                requestURL.append("?redir=").append(redir);
            }
            completeURL = requestURL.replace(0, requestURL.indexOf(":"), proto).toString();
        }
        /// completeURL should be the same provided in the "service" parameter
        // System.out.println(String.format("Service: %s\n", completeURL));

        sv.setService(completeURL);
        sv.setServiceTicket(ticket);
        // sv.setProxyCallbackUrl(urlOfProxyCallbackServlet);
        sv.validate();

        String xmlResponse = sv.getResponse();

        if (xmlResponse.contains("cas:authenticationFailure")) {
            System.out.println(String.format("CAS response: %s\n", xmlResponse));
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("CAS error");
        }

        // <cas:user>vassoilm</cas:user>
        // session.setAttribute("user", sv.getUser());
        // session.setAttribute("uid", dataProvider.getUserId(sv.getUser()));
        userId = String.valueOf(userManager.getUserId(sv.getUser(), null));
        if (userId != null) {
            session.setAttribute("user", sv.getUser()); // FIXME
            session.setAttribute("uid", Integer.parseInt(userId)); // FIXME
        } else {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Login " + sv.getUser() + " not found or bad CAS auth (bad ticket or bad url service : "
                            + completeURL + ") : " + sv.getErrorMessage());
        }

            // formulate the response
        return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .header("Location", redir)
                    .body("<script>document.location.replace('" + redir + "')</script>");
    }

    /**
     * Ask to logout, clear session.
     *
     * POST /rest/api/credential/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null)
            session.invalidate();

        return ResponseEntity.ok("logout");
    }

}

