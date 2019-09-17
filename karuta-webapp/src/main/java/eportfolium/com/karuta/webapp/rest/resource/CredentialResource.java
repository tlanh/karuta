package eportfolium.com.karuta.webapp.rest.resource;

import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;

public class CredentialResource extends AbstractResource {

	@Autowired
	private UserManager userManager;

	@Autowired
	private SecurityManager securityManager;

	@Autowired
	private ConfigurationManager configurationManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Fetch current user info GET /rest/api/credential parameters: return:
	 * <user id="uid"> <username></username> <firstname></firstname>
	 * <lastname></lastname> <email></email> <admin>1/0</admin>
	 * <designer>1/0</designer> <active>1/0</active> <substitute>1/0</substitute>
	 * </user>
	 **/
	@Path("/credential")
	@GET
	@Produces(javax.ws.rs.core.MediaType.APPLICATION_XML)
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
					securityManager.userChangeInfo(ui.getId(), ui.getId(), xmlInfUser);
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
	 * Modify user info PUT /rest/api/users/user/{user-id} body: <user id="uid">
	 * <username></username> <firstname></firstname> <lastname></lastname>
	 * <admin>1/0</admin> <designer>1/0</designer> <email></email>
	 * <active>1/0</active> <substitute>1/0</substitute> </user>
	 *
	 * parameters:
	 *
	 * return: <user id="uid"> <username></username> <firstname></firstname>
	 * <lastname></lastname> <admin>1/0</admin> <designer>1/0</designer>
	 * <email></email> <active>1/0</active> <substitute>1/0</substitute> </user>
	 **/
	@Path("/users/user/{user-id}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putUser(String xmlInfUser, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("user-id") long userid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {

			String queryuser = "";
			if (securityManager.isAdmin(ui.getId()) || securityManager.isCreator(ui.getId())) {
				queryuser = securityManager.putInfUser(ui.getId(), userid, xmlInfUser);
			} else if (ui.getId() == userid) /// Changing self
			{
				String ip = httpServletRequest.getRemoteAddr();
				logger.info(String.format("[%s] ", ip));
				queryuser = securityManager.userChangeInfo(ui.getId(), userid, xmlInfUser);
			} else
				throw new RestWebApplicationException(Status.FORBIDDEN, "Not authorized");

			return queryuser;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, "Error : " + ex.getMessage());
		}
	}

}
