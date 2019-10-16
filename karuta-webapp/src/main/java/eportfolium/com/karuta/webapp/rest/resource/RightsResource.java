package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/rights")
public class RightsResource extends AbstractResource {

	@Autowired
	private NodeManager nodeManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Change rights for a node. <br>
	 * POST /rest/api/rights
	 * 
	 * @param xmlNode
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postChangeRights(String xmlNode, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {

		UserInfo ui = checkCredential(httpServletRequest, null, null, null);
		String returnValue = "";

		try {
			nodeManager.changeRights(xmlNode, ui.userId, ui.subId, "");
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
