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

package eportfolium.com.karuta.webapp.rest.resource;

import java.io.ByteArrayInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/group")
public class GroupResource extends AbstractResource {

	@Autowired
	private UserManager userManager;

	@Autowired
	private GroupManager groupManager;

	@InjectLogger
	private static Logger logger;

	private GroupResource() {
	}

	/**
	 * Add a user group <br>
	 * POST /rest/api/credential/group/{group-id}
	 *
	 * @param xmlgroup           <group grid="" owner="" label=""></group>
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @return <group grid="" owner="" label=""></group>
	 */

	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postGroup(String xmlgroup, @CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			return groupManager.addUserGroup(xmlgroup, ui.userId);
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get roles in a portfolio. <br>
	 * GET /rest/api/credential/group/{portfolio-id}
	 *
	 * @param user
	 * @param token
	 * @param groupId
	 * @param portfolioUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/{portfolio-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getUserGroupByPortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String xmlGroups = userManager.getUserGroupByPortfolio(portfolioUuid, ui.userId);

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

}
