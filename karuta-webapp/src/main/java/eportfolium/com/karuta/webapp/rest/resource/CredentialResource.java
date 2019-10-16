package eportfolium.com.karuta.webapp.rest.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.yale.its.tp.cas.client.ServiceTicketValidator;
import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.EmailManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.config.Consts;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.util.StrToTime;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.rest.resource.RestServicePortfolio.UserInfo;
import eportfolium.com.karuta.webapp.util.DomUtils;
import eportfolium.com.karuta.webapp.util.javaUtils;

public class CredentialResource extends AbstractResource {

	@Autowired
	private UserManager userManager;

	@Autowired
	private EmailManager emailManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private ConfigurationManager configurationManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Fetch current user info <br>
	 * GET /rest/api/credential
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @return <user id="uid"> <username></username> <firstname></firstname>
	 *         <lastname></lastname> <email></email> <admin>1/0</admin>
	 *         <designer>1/0</designer> <active>1/0</active>
	 *         <substitute>1/0</substitute> </user>
	 */
	@Path("/credential")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getCredential(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		if (ui.getId() == 0) // Non valid userid
		{
			return Response.status(401).build();
		}

		try {
			String xmluser = userManager.getInfUser(ui.getId());

			/// Add shibboleth info if needed
			HttpSession session = httpServletRequest.getSession(false);
			Integer fromshibe = (Integer) session.getAttribute("fromshibe");
			Integer updatefromshibe = (Integer) session.getAttribute("updatefromshibe");
			String alist = configurationManager.get("shib_attrib");
			HashMap<String, String> updatevals = new HashMap<String, String>();

			if (fromshibe != null && fromshibe == 1 && alist != null) {
				/// Fetch and construct needed data
				String[] attriblist = alist.split(",");
				int lastst = xmluser.lastIndexOf("<");
				StringBuilder shibuilder = new StringBuilder(xmluser.substring(0, lastst));

				for (String attrib : attriblist) {
					String value = (String) httpServletRequest.getAttribute(attrib);
					shibuilder.append("<").append(attrib).append(">").append(value).append("</").append(attrib)
							.append(">");
					/// Pre-process values
					if (1 == updatefromshibe) {
						String colname = configurationManager.get(attrib);
						updatevals.put(colname, value);
					}
				}
				/// Update values
				if (1 == updatefromshibe) {
					String xmlInfUser = String.format(
							"<user id=\"%s\">" + "<firstname>%s</firstname>" + "<lastname>%s</lastname>"
									+ "<email>%s</email>" + "</user>",
							ui.getId(), updatevals.get("display_firstname"), updatevals.get("display_lastname"),
							updatevals.get("email"));
					/// User update its own info automatically
					securityManager.changeUser(ui.getId(), ui.getId(), xmlInfUser);
					/// Consider it done
					session.removeAttribute("updatefromshibe");
				}
				/// Add it as last tag after "moving" the closing tag
				shibuilder.append(xmluser.substring(lastst));
				xmluser = shibuilder.toString();
			}
			return Response.ok(xmluser).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Modify user info <br>
	 * PUT /rest/api/users/user/{user-id} body: <user id="uid">
	 * <username></username> <firstname></firstname> <lastname></lastname>
	 * <admin>1/0</admin> <designer>1/0</designer> <email></email>
	 * <active>1/0</active> <substitute>1/0</substitute> </user>
	 *
	 * @param xmlInfUser
	 * @param user
	 * @param token
	 * @param groupId
	 * @param userid
	 * @param sc
	 * @param httpServletRequest
	 * @return <user id="uid"> <username></username> <firstname></firstname>
	 *         <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
	 *         <email></email> <active>1/0</active> <substitute>1/0</substitute>
	 *         </user>
	 */
	@Path("/users/user/{user-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putUser(String xmlInfUser, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("user-id") long userid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {

			String queryUser = "";
			if (securityManager.isAdmin(ui.getId()) || securityManager.isCreator(ui.getId())) {
				queryUser = securityManager.changeUserInfo(ui.getId(), userid, xmlInfUser);
			} else if (ui.getId() == userid) /// Changing self
			{
				String ip = httpServletRequest.getRemoteAddr();
				logger.info(String.format("[%s] ", ip));
				queryUser = securityManager.changeUser(ui.getId(), userid, xmlInfUser);
			} else
				throw new RestWebApplicationException(Status.FORBIDDEN, "Not authorized");

			return queryUser;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, "Error : " + ex.getMessage());
		}
	}

	/**
	 * Add user to a role (?) <br>
	 * POST /rest/api/roleUser
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param grid
	 * @param userid
	 * @return
	 */
	@Path("/roleUser")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRoleUser(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("grid") long grid,
			@QueryParam("user-id") Long userid) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return securityManager.addUserRole(ui.getId(), grid, userid).toString();
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN,
					"Vous n'avez pas les droits necessaires " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Send login information <br>
	 * PUT /rest/api/credential/login
	 * 
	 * @param xmlCredential
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/credential/login")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response putCredentialFromXml(String xmlCredential, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		return this.postCredentialFromXml(xmlCredential, user, token, 0, sc, httpServletRequest);
	}

	/**
	 * Send login information POST /rest/api/credential/login parameters: return:
	 **/
	@Path("/credential/login")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	public Response postCredentialFromXml(String xmlCredential, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(true);
		KEvent event = new KEvent();
		event.eventType = KEvent.EventType.LOGIN;
		event.inputData = xmlCredential;
		String retVal = "";
		int status = 0;

		String authlog = configurationManager.get("auth_log");
		Log authLog = null;
		try {
			if (!"".equals(authlog) && authlog != null)
				authLog = LogFactory.getLog(authlog);
		} catch (IOException e1) {
			logger.error("Could not create authentification log file");
		}

		try {
			Document doc = DomUtils.xmlString2Document(xmlCredential, new StringBuffer());
			Element credentialElement = doc.getDocumentElement();
			String login = "";
			String password = "";
			String substit = null;
			if (credentialElement.getNodeName().equals("credential")) {
				String[] templogin = DomUtils.getInnerXml(doc.getElementsByTagName("login").item(0)).split("#");
				password = DomUtils.getInnerXml(doc.getElementsByTagName("password").item(0));

				if (templogin.length > 1)
					substit = templogin[1];
				login = templogin[0];
			}

			int dummy = 0;
			String[] resultCredential = securityManager.postCredentialFromXml(dummy, login, password, substit);
			// 0: xml de retour
			// 1,2: username, uid
			// 3,4: substitute name, substitute id
			if (resultCredential == null) {
				event.status = 403;
				retVal = "invalid credential";

				if (authLog != null)
					authLog.info(String.format("Authentication error for user '%s' date '%s'\n", login,
							StrToTime.convert("now")));
			} else if (!"0".equals(resultCredential[2])) {
				// String tokenID = resultCredential[2];

				if (substit != null && !"0".equals(resultCredential[4])) {
					int uid = Integer.parseInt(resultCredential[2]);
					int subid = Integer.parseInt(resultCredential[4]);

					session.setAttribute("user", resultCredential[3]);
					session.setAttribute("uid", subid);
					session.setAttribute("subuser", resultCredential[1]);
					session.setAttribute("subuid", uid);
					if (authLog != null)
						authLog.info(String.format("Authentication success for user '%s' date '%s' (Substitution)\n",
								login, StrToTime.convert("now")));
				} else {
					String login1 = resultCredential[1];
					int userId = Integer.parseInt(resultCredential[2]);

					session.setAttribute("user", login1);
					session.setAttribute("uid", userId);
					session.setAttribute("subuser", "");
					session.setAttribute("subuid", 0);
					if (authLog != null)
						authLog.info(String.format("Authentication success for user '%s' date '%s'\n", login,
								StrToTime.convert("now")));
				}

				event.status = 200;
				retVal = resultCredential[0];
			}
			eventbus.processEvent(event);

			return Response.status(event.status).entity(retVal).type(event.mediaType).build();
		} catch (RestWebApplicationException ex) {
			ex.printStackTrace();
			logger.error(ex.getLocalizedMessage());
			throw new RestWebApplicationException(Status.FORBIDDEN, "invalid Credential or invalid group member");
		} catch (Exception ex) {
			status = 500;
			retVal = ex.getMessage();
			logger.error(ex.getMessage());
		}

		return Response.status(status).entity(retVal).type(MediaType.APPLICATION_XML).build();
	}

	/**
	 * Tell system to forgot your password POST /rest/api/credential/forgot
	 * 
	 * @param xml
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/credential/forgot")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response postForgotCredential(String xml, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		int retVal = 404;
		String retText = "";
		Logger securityLog = null;
		String securitylog = configurationManager.get("security_log");

		if (StringUtils.isNotEmpty(securitylog))
			securityLog = LoggerFactory.getLogger(securitylog);

		String resetEnable = configurationManager.get("enable_password_reset");
		if (resetEnable != null
				&& ("y".equals(resetEnable.toLowerCase()) || "true".equals(resetEnable.toLowerCase()))) {

			try {
				Document doc = DomUtils.xmlString2Document(xml, new StringBuffer());
				Element userInfos = doc.getDocumentElement();

				String username = "";
				if (userInfos.getNodeName().equals("credential")) {
					NodeList children2 = userInfos.getChildNodes();
					for (int y = 0; y < children2.getLength(); y++) {
						if (children2.item(y).getNodeName().equals("login")) {
							username = DomUtils.getInnerXml(children2.item(y));
							break;
						}
					}
				}

				// Vérifier si nous avons cet email enregistré en base
				String email = userManager.getEmailByLogin(username);
				if (email != null && !"".equals(email)) {

					// écrire les changements en base
					String password = securityManager.generatePassword();
					boolean result = securityManager.changePassword(username, password);

					if (result) {
						if (securityLog != null) {
							String ip = httpServletRequest.getRemoteAddr();
							securityLog.info(String.format(
									"[%s] [%s] a demandé la réinitialisation de son mot de passe\n", ip, username));
						}

						final Map<String, String> template_vars = new HashMap<String, String>();
						template_vars.put("firstname", username);
						template_vars.put("lastname", "");
						template_vars.put("email", email);
						template_vars.put("passwd", password);

						String cc_email = configurationManager.get("sys_email");
						// Envoie d'un email
						final Integer langId = Integer.valueOf(configurationManager.get("PS_LANG_DEFAULT"));
						emailManager.send(langId, "employee_password",
								emailManager.getTranslation("Your new password!"), template_vars, email, username, null,
								null, null, null, Consts._PS_MAIL_DIR_, false, null, cc_email, null);

						retVal = 200;
						retText = "sent";
					}
				}
			} catch (BusinessException ex) {
				ex.printStackTrace();
				logger.error(ex.getLocalizedMessage());
				throw new RestWebApplicationException(Status.FORBIDDEN, "invalid Credential or invalid group member");
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				ex.printStackTrace();
				throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
			}
		}
		return Response.status(retVal).entity(retText).build();
	}

	/**
	 * Fetch current user information (CAS) GET /rest/api/credential/login/cas
	 * 
	 * @param content
	 * @param user
	 * @param token
	 * @param groupId
	 * @param ticket
	 * @param redir
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@POST
	@Path("/credential/login/cas")
	public Response postCredentialFromCas(String content, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId,
			@QueryParam("ticket") String ticket, @QueryParam("redir") String redir, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		return getCredentialFromCas(user, token, groupId, ticket, redir, sc, httpServletRequest);
	}

	@Path("/credential/login/cas")
	@GET
	public Response getCredentialFromCas(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @QueryParam("ticket") String ticket, @QueryParam("redir") String redir,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {

		HttpSession session = httpServletRequest.getSession(true); // FIXME
		String xmlResponse = null;
		String userId = null;
		String completeURL;
		StringBuffer requestURL;
		String casUrlValidation = configurationManager.get("casUrlValidation");

		try {
			ServiceTicketValidator sv = new ServiceTicketValidator();

			if (casUrlValidation == null) {
				Response response = null;
				try {
					// formulate the response
					response = Response.status(Status.PRECONDITION_FAILED).entity("CAS URL not defined").build();
				} catch (Exception e) {
					response = Response.status(500).build();
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

			xmlResponse = sv.getResponse();
			if (xmlResponse.contains("cas:authenticationFailure")) {
				System.out.println(String.format("CAS response: %s\n", xmlResponse));
				return Response.status(Status.FORBIDDEN).entity("CAS error").build();
			}

			// <cas:user>vassoilm</cas:user>
			// session.setAttribute("user", sv.getUser());
			// session.setAttribute("uid", dataProvider.getUserId(sv.getUser()));
			userId = String.valueOf(userManager.getUserId(sv.getUser(), null));
			if (userId != null) {
				session.setAttribute("user", sv.getUser()); // FIXME
				session.setAttribute("uid", Integer.parseInt(userId)); // FIXME
			} else {
				return Response.status(403)
						.entity("Login " + sv.getUser() + " not found or bad CAS auth (bad ticket or bad url service : "
								+ completeURL + ") : " + sv.getErrorMessage())
						.build();
			}

			Response response = null;
			try {
				// formulate the response
				response = Response.status(201).header("Location", redir)
						.entity("<script>document.location.replace('" + redir + "')</script>").build();
			} catch (Exception e) {
				response = Response.status(500).build();
			}
			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.FORBIDDEN,
					"Vous n'avez pas les droits necessaires (ticket ?, casUrlValidation) :" + casUrlValidation);
		}
	}

	/**
	 * Ask to logout, clear session POST /rest/api/credential/logout parameters:
	 * return:
	 **/
	@Path("/credential/logout")
	@POST
	public Response logout(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session != null)
			session.invalidate();
		return Response.ok("logout").build();
	}

	/**
	 * Modify a role PUT /rest/api/roles/role/{role-id} parameters: return:
	 **/
	@Path("/roles/role/{role-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putRole(String xmlRole, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("role-id") int roleId) {

		Credential ui = new Credential(); // FIXME
		try {
			String returnValue = securityManager.addOrUpdateRole(xmlRole, ui.getId()).toString();
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get roles in a portfolio GET /rest/api/credential/group/{portfolio-id}
	 * parameters: return:
	 *
	 **/
	@Path("/credential/group/{portfolio-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUserGroupByPortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = new Credential();
		try {
			String xmlGroups = userManager.getUserGroupByPortfolio(portfolioUuid, ui.getId());

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = null;
			Document document = null;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.newDocument();
			document.setXmlStandalone(true);
			Document doc = documentBuilder.parse(new ByteArrayInputStream(xmlGroups.getBytes("UTF-8")));
			NodeList groups = doc.getElementsByTagName("group");
			if (groups.getLength() == 1) {
				Node groupnode = groups.item(0);
				String gid = groupnode.getAttributes().getNamedItem("id").getNodeValue();
				if (gid != null) {
				}
			} else if (groups.getLength() == 0) // Pas de groupe, on rend invalide le choix
			{
			}

			return xmlGroups;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Delete a role DELETE
	 * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id} parameters:
	 * return:
	 **/
	@Path("/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteRightGroup(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long groupRightInfoId) {
		Credential ui = checkCredential(httpServletRequest, user, token, group);

		try {
			securityManager.removeRole(ui.getId(), groupRightInfoId);
			return "";
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Remove user from a role DELETE
	 * /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}
	 *
	 **/
	@Path("/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}/users/user/{user-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteRightGroupUser(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId,
			@PathParam("user-id") Integer queryuser) {
		Credential ui = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			securityManager.removeUserRole(ui.getId(), rrgId);
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Remove all users from a role DELETE /rest/api/rolerightsgroups/all/users
	 *
	 **/
	@Path("/rolerightsgroups/all/users")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deletePortfolioRightInfo(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("portfolio") String portId) {
		Credential cr = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (portId != null) {
				securityManager.removeUsersFromRole(cr.getId(), portId);
			}
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Change a right in role <br>
	 * PUT /rest/api/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}
	 * 
	 * @param xmlNode
	 * @param user
	 * @param token
	 * @param group
	 * @param sc
	 * @param httpServletRequest
	 * @param rrgId
	 * @return
	 */
	@Path("/rolerightsgroups/rolerightsgroup/{rolerightsgroup-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putRightInfo(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @PathParam("rolerightsgroup-id") Long rrgId) {
		Credential ui = checkCredential(httpServletRequest, user, token, group);

		String returnValue = "";
		try {
			// Retourne le contenu du type
			if (rrgId != null) {
				securityManager.changeRole(ui.getId(), rrgId, xmlNode);
			}
			return returnValue;
		} catch (DoesNotExistException e) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Role with id " + rrgId + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Insert a user in a user group <br>
	 * POST /rest/api/groupsUsers
	 * 
	 * @param user
	 * @param token
	 * @param groupId            group: gid
	 * @param sc
	 * @param httpServletRequest
	 * @param userId             userId
	 * @return <ok/>
	 */
	@Path("/groupsUsers")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postGroupsUsers(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("userId") long userId) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			if (securityManager.addUserToGroup(ui.getId(), userId, groupId)) {
				return "<ok/>";
			} else
				throw new RestWebApplicationException(Status.FORBIDDEN,
						ui.getId() + " ne fait pas parti du groupe " + groupId);
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
