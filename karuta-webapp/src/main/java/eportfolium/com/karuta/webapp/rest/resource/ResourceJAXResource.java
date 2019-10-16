package eportfolium.com.karuta.webapp.rest.resource;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.json.XML;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;

import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;

/**
 * @author mlengagne
 *
 */
public class ResourceJAXResource extends AbstractResource {

	@Autowired
	private ResourceManager resourceManager;

	@InjectLogger
	private static Logger logger;

	/**
	 * Fetch resource from node uuid GET
	 * /rest/api/resources/resource/{node-parent-id} parameters: - portfoliocode:
	 * mandatory - semtag_parent, code_parent: From a code_parent, find the children
	 * that have semtag_parent - semtag: mandatory, find the semtag under
	 * portfoliocode, or the selection from semtag_parent/code_parent return:
	 **/
	@Path("/resources/resource/{node-parent-id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_XML)
	public String getResource(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("node-parent-id") String nodeParentUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@HeaderParam("Accept") String accept, @QueryParam("user") Integer userId) {
		if (!isUUID(nodeParentUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			String returnValue = resourceManager
					.getResource(MimeTypeUtils.TEXT_XML, nodeParentUuid, ui.getId(), groupId).toString();
			if (accept.equals(MediaType.APPLICATION_JSON))
				returnValue = XML.toJSONObject(returnValue).toString();
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Resource " + nodeParentUuid + " not found");
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Fetch all resource in a portfolio TODO: is it used? GET
	 * /rest/api/resources/portfolios/{portfolio-id} parameters: - portfolio-id
	 * return:
	 **/
	@Path("/resources/portfolios/{portfolio-id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getResources(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@HeaderParam("Accept") String accept, @QueryParam("user") Integer userId) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			String returnValue = resourceManager
					.getResources(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.getId(), groupId).toString();
			if (accept.equals(MediaType.APPLICATION_JSON))
				returnValue = XML.toJSONObject(returnValue).toString();
			return returnValue;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Modify resource content. <br>
	 * PUT /rest/api/resources/resource/{node-parent-uuid}
	 * 
	 * @param xmlResource
	 * @param user
	 * @param token
	 * @param groupId
	 * @param info
	 * @param nodeParentUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@Path("/resources/resource/{node-parent-uuid}")
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public String putResource(String xmlResource, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId, @QueryParam("info") String info,
			@PathParam("node-parent-uuid") String nodeParentUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId) {
		if (!isUUID(nodeParentUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		Credential ui = checkCredential(httpServletRequest, user, token, null);

		Date time = new Date();
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
		String timeFormat = dt.format(time);
		String logformat = "";
		if ("false".equals(info))
			logformat = logFormatShort;
		else
			logformat = logFormat;

		try {
			String returnValue = resourceManager
					.changeResource(MimeTypeUtils.TEXT_XML, nodeParentUuid, xmlResource, ui.getId(), groupId)
					.toString();
			logger.info(String.format(logformat, "OK", nodeParentUuid, "resource", ui.getId(), timeFormat,
					httpServletRequest.getRemoteAddr(), xmlResource));
			return returnValue;
		} catch (DoesNotExistException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Resource " + nodeParentUuid + " not found");
		} catch (BusinessException ex) {
			logger.info(String.format(logformat, "ERR", nodeParentUuid, "resource", ui.getId(), timeFormat,
					httpServletRequest.getRemoteAddr(), xmlResource));
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add a resource (?) POST /rest/api/resources/{node-parent-uuid} parameters:
	 * return:
	 **/
	@Path("/resources/{node-parent-uuid}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postResource(String xmlResource, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId,
			@PathParam("node-parent-uuid") String nodeParentUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId) {
		if (!isUUID(nodeParentUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String returnValue = resourceManager
					.addResource(MimeTypeUtils.TEXT_XML, nodeParentUuid, xmlResource, ui.getId(), groupId).toString();
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
	 * (?) POST /rest/api/resources parameters: return:
	 **/
	@Path("/resources")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postResource(String xmlResource, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("type") Integer type,
			@QueryParam("resource") String resource, @QueryParam("user") Integer userId) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			String returnValue = resourceManager
					.addResource(MimeTypeUtils.TEXT_XML, resource, xmlResource, ui.getId(), groupId).toString();
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
	 * Delete a resource DELETE /rest/api/resources/{resource-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param resourceUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@Path("/resources/{resource-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deleteResource(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("resource-id") String resourceUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Long userId) {
		if (!isUUID(resourceUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			resourceManager.deleteResource(resourceUuid, ui.getId(), groupId);
			return "";
		} catch (DoesNotExistException e) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Resource " + resourceUuid + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
