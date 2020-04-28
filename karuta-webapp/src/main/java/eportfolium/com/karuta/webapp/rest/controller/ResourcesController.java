package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.json.XML;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mlengagne
 *
 */
@RestController
@RequestMapping("/resources")
public class ResourcesController extends AbstractController {

    @Autowired
    private ResourceManager resourceManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Fetch resource from node uuid. <br>
     * GET /rest/api/resources/resource/{node-parent-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param nodeParentUuid
     * @param accept
     * @param request
     * @return
     */
    @GetMapping(value = "/resource/{node-parent-id}", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String getResource(@CookieValue("user") String user,
                              @CookieValue("credential") String token,
                              @RequestParam("group") long groupId,
                              @PathVariable("node-parent-id") String nodeParentUuid,
                              @RequestHeader("Accept") String accept,
                              HttpServletRequest request) throws RestWebApplicationException {
        if (!isUUID(nodeParentUuid)) {
            throw new RestWebApplicationException(HttpStatus.BAD_REQUEST, "Not UUID");
        }

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = resourceManager.getResource(MimeTypeUtils.TEXT_XML, nodeParentUuid, ui.userId, groupId)
                    .toString();
            if (accept.equals("application/json"))
                returnValue = XML.toJSONObject(returnValue).toString();
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Resource " + nodeParentUuid + " not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch all resource in a portfolio. <br>
     * GET /rest/api/resources/portfolios/{portfolio-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param portfolioUuid      portfolio-id
     * @param accept
     * @param request
     * @return
     */
    @GetMapping(value = "/portfolios/{portfolio-id}", produces = {"application/json", "application/xml"})
    public String getResources(@CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @PathVariable("portfolio-id") String portfolioUuid,
                               @RequestHeader("Accept") String accept,
                               HttpServletRequest request) throws RestWebApplicationException {
        if (!isUUID(portfolioUuid)) {
            throw new RestWebApplicationException(HttpStatus.BAD_REQUEST, "Not UUID");
        }

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = resourceManager.getResources(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.userId, groupId);
            if (accept.equals("application/json"))
                returnValue = XML.toJSONObject(returnValue).toString();
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
     * @param request
     * @return
     */
    @PutMapping(value = "/resource/{node-parent-uuid}", produces = "application/xml")
    public String putResource(String xmlResource,
                              @CookieValue("user") String user,
                              @CookieValue("credential") String token,
                              @RequestParam("group") long groupId,
                              @RequestParam("info") String info,
                              @PathVariable("node-parent-uuid") String nodeParentUuid,
                              HttpServletRequest request) throws RestWebApplicationException {
        if (!isUUID(nodeParentUuid)) {
            throw new RestWebApplicationException(HttpStatus.BAD_REQUEST, "Not UUID");
        }

        UserInfo ui = checkCredential(request, user, token, null);

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
                    .changeResource(MimeTypeUtils.TEXT_XML, nodeParentUuid, xmlResource, ui.userId, groupId).toString();
            logger.info(String.format(logformat, "OK", nodeParentUuid, "resource", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlResource));
            return returnValue;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Resource " + nodeParentUuid + " not found");
        } catch (BusinessException ex) {
            logger.info(String.format(logformat, "ERR", nodeParentUuid, "resource", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlResource));
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Add a resource (?). <br>
     * POST /rest/api/resources/{node-parent-uuid}
     *
     * @param xmlResource
     * @param user
     * @param token
     * @param groupId
     * @param nodeParentUuid
     * @param request
     * @return
     */
    @PostMapping(value = "/{node-parent-uuid}", produces = "application/xml")
    public String postResource(String xmlResource,
                               @CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @PathVariable("node-parent-uuid") String nodeParentUuid,
                               HttpServletRequest request) throws RestWebApplicationException {
        if (!isUUID(nodeParentUuid)) {
            throw new RestWebApplicationException(HttpStatus.BAD_REQUEST, "Not UUID");
        }

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = resourceManager
                    .addResource(MimeTypeUtils.TEXT_XML, nodeParentUuid, xmlResource, ui.userId, groupId).toString();
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * (?) POST /rest/api/resources
     *
     * @param xmlResource
     * @param user
     * @param token
     * @param groupId
     * @param resource
     * @param request
     * @return
     */
    @PostMapping(produces = "application/xml")
    public String postResources(String xmlResource,
                               @CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @RequestParam("resource") String resource,
                               HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = resourceManager
                    .addResource(MimeTypeUtils.TEXT_XML, resource, xmlResource, ui.userId, groupId).toString();
            return returnValue;
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Delete a resource <br>
     * DELETE /rest/api/resources/{resource-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param resourceUuid
     * @param request
     * @return
     */
    @DeleteMapping(value = "/{resource-id}", produces = "application/xml")
    public String deleteResource(@CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @RequestParam("group") long groupId,
                                 @PathVariable("resource-id") String resourceUuid,
                                 HttpServletRequest request) throws RestWebApplicationException {
        if (!isUUID(resourceUuid)) {
            throw new RestWebApplicationException(HttpStatus.BAD_REQUEST, "Not UUID");
        }
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            resourceManager.removeResource(resourceUuid, ui.userId, groupId);
            return "";
        } catch (DoesNotExistException e) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "Resource " + resourceUuid + " not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}

