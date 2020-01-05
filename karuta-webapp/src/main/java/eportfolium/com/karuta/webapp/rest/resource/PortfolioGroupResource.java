package eportfolium.com.karuta.webapp.rest.resource;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

/**
 * Managing and listing portfolio groups.
 * 
 * @author mlengagne
 *
 */
@Path("/portfoliogroups")
public class PortfolioGroupResource extends AbstractResource {

	@InjectLogger
	private static Logger logger;

	@Autowired
	private PortfolioManager portfolioManager;

	/**
	 * Create a new portfolio group. <br>
	 * POST /rest/api/portfoliogroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupname          Name of the group we are creating
	 * @param type               group/portfolio
	 * @param parent             parentid
	 * @return groupid
	 */

	@POST
	public Response postPortfolioGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("label") String groupname, @QueryParam("type") String type, @QueryParam("parent") Long parent) {
		UserInfo ui = checkCredential(httpServletRequest, null, null, null); // FIXME
		Long response = -1L;

		// Check type value
		try {
			response = portfolioManager.addPortfolioGroup(groupname, type, parent, ui.userId);
			logger.debug("Portfolio group " + groupname + " created");

			if (response == -1) {
				return Response.status(Status.NOT_MODIFIED).entity("Error in creation").build();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return Response.ok(Long.toString(response)).build();
	}

	/**
	 * Put a portfolio in portfolio group. <br>
	 * PUT /rest/api/portfoliogroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param group              group id
	 * @param uuid               portfolio id
	 * @param label
	 * @return Code 200
	 */
	@PUT
	public Response putPortfolioInPortfolioGroup(@Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("group") Long group,
			@QueryParam("uuid") String uuid, @QueryParam("label") String label) {
		UserInfo ui = checkCredential(httpServletRequest, null, null, null);

		try {
			int response = -1;
			response = portfolioManager.addPortfolioInGroup(uuid, group, label, ui.userId); // FIXME
			logger.debug("Portfolio added  in group " + label);
			return Response.ok(Integer.toString(response)).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get portfolio by portfoliogroup, or if there's no group id give, give the
	 * list of portfolio group GET /rest/api/portfoliogroups<br>
	 * 
	 * - Without group id <groups> <group id={groupid}> <label>{group name}</label>
	 * </group> ... </groups>
	 *
	 * - With group id <group id={groupid}> <portfolio id={uuid}></portfolio> ...
	 * </group>
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param group              group id
	 * @param portfolioUuid
	 * @param groupLabel         group label
	 * @return group id or empty str if group id not found
	 */
	@GET
	public String getPortfolioByPortfolioGroup(@Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("group") Long group,
			@QueryParam("uuid") String portfolioUuid, @QueryParam("label") String groupLabel) {
		UserInfo ui = checkCredential(httpServletRequest, null, null, null);
		String xmlUsers = "";

		try {
			if (groupLabel != null) {
				Long groupid = portfolioManager.getPortfolioGroupIdFromLabel(groupLabel, ui.userId);
				if (groupid == -1) {
					throw new RestWebApplicationException(Status.NOT_FOUND, "");
				}
				xmlUsers = Long.toString(groupid);
			} else if (portfolioUuid != null) {
				xmlUsers = portfolioManager.getPortfolioGroupListFromPortfolio(portfolioUuid);
			} else if (group == null)
				xmlUsers = portfolioManager.getPortfolioGroupList();
			else
				xmlUsers = portfolioManager.getPortfoliosByPortfolioGroup(group);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return xmlUsers;
	}

	/**
	 * Remove a portfolio from a portfolio group, or remove a portfoliogroup. <br>
	 * DELETE /rest/api/portfoliogroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupId            group id
	 * @param uuid               portfolio id
	 * @return Code 200
	 */
	@DELETE
	public String deletePortfolioByPortfolioGroup(@Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("group") long groupId,
			@QueryParam("uuid") String uuid) {
//		checkCredential(httpServletRequest, null, null, null); //FIXME
		boolean response = false;
		try {
			if (uuid == null)
				response = portfolioManager.removePortfolioGroups(groupId);
			else
				response = portfolioManager.removePortfolioFromPortfolioGroups(uuid, groupId);

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		return String.valueOf(response);
	}

}
