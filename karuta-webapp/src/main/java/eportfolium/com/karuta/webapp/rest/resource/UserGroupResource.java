package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

/**
 * Managing and listing user groups
 * 
 * @author mlengagne
 *
 */
@Path("/groupsUsers")
public class UserGroupResource extends AbstractResource {

	@InjectLogger
	private static Logger logger;

	@Autowired
	private SecurityManager securityManager;

	/**
	 * Insert a user in a user group. <br>
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
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postGroupsUsers(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("userId") long userId) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			securityManager.addUserToGroup(ui.userId, userId, groupId);
			return "<ok/>";
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (RestWebApplicationException ex) {
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
