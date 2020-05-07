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

import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.model.exception.BusinessException;
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
import java.util.UUID;

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
     * @param nodeParentId
     * @param accept
     * @param request
     * @return
     */
    @GetMapping(value = "/resource/{node-parent-id}", consumes = "application/xml",
            produces = {"application/json", "application/xml"})
    public String getResource(@CookieValue("user") String user,
                              @CookieValue("credential") String token,
                              @RequestParam("group") long groupId,
                              @PathVariable("node-parent-id") UUID nodeParentId,
                              @RequestHeader("Accept") String accept,
                              HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = resourceManager.getResource(MimeTypeUtils.TEXT_XML, nodeParentId, ui.userId, groupId)
                    .toString();
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
     * Fetch all resource in a portfolio. <br>
     * GET /rest/api/resources/portfolios/{portfolio-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param portfolioId      portfolio-id
     * @param accept
     * @param request
     * @return
     */
    @GetMapping(value = "/portfolios/{portfolio-id}", produces = {"application/json", "application/xml"})
    public String getResources(@CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @PathVariable("portfolio-id") UUID portfolioId,
                               @RequestHeader("Accept") String accept,
                               HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = resourceManager.getResources(MimeTypeUtils.TEXT_XML, portfolioId, ui.userId, groupId);
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
     * @param parentNodeId
     * @param request
     * @return
     */
    @PutMapping(value = "/resource/{node-parent-uuid}", produces = "application/xml")
    public String putResource(@RequestBody String xmlResource,
                              @CookieValue("user") String user,
                              @CookieValue("credential") String token,
                              @RequestParam("group") long groupId,
                              @RequestParam("info") String info,
                              @PathVariable("node-parent-uuid") UUID parentNodeId,
                              HttpServletRequest request) throws RestWebApplicationException {

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
                    .changeResource(MimeTypeUtils.TEXT_XML, parentNodeId, xmlResource, ui.userId, groupId).toString();
            logger.info(String.format(logformat, "OK", parentNodeId, "resource", ui.userId, timeFormat,
                    request.getRemoteAddr(), xmlResource));
            return returnValue;
        } catch (BusinessException ex) {
            logger.info(String.format(logformat, "ERR", parentNodeId, "resource", ui.userId, timeFormat,
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
     * @param parentNodeId
     * @param request
     * @return
     */
    @PostMapping(value = "/{node-parent-uuid}", produces = "application/xml")
    public String postResource(@RequestBody String xmlResource,
                               @CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @PathVariable("node-parent-uuid") UUID parentNodeId,
                               HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            String returnValue = resourceManager
                    .addResource(MimeTypeUtils.TEXT_XML, parentNodeId, xmlResource, ui.userId, groupId);
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
    public String postResources(@RequestBody String xmlResource,
                               @CookieValue("user") String user,
                               @CookieValue("credential") String token,
                               @RequestParam("group") long groupId,
                               @RequestParam("resource") UUID resource,
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
     * @param resourceId
     * @param request
     * @return
     */
    @DeleteMapping(value = "/{resource-id}", produces = "application/xml")
    public String deleteResource(@CookieValue("user") String user,
                                 @CookieValue("credential") String token,
                                 @RequestParam("group") long groupId,
                                 @PathVariable("resource-id") UUID resourceId,
                                 HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            resourceManager.removeResource(resourceId, ui.userId, groupId);
            return "";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}
