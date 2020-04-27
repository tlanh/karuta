package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/groups")
public class GroupsController extends AbstractController {

    @Autowired
    private GroupManager groupManager;

    @Autowired
    private PortfolioManager portfolioManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Get groups from a user id <br>
     * GET /rest/api/groups
     *
     * @param user
     * @param token
     * @param groupId            group id
     * @param request
     * @return <groups> <group id="gid" owner="uid" templateId="rrgid">GROUP
     *         LABEL</group> ... </groups>
     */
    @GetMapping(produces = "application/xml")
    public String getGroups(@CookieValue("user") String user,
                            @CookieValue("credential") String token,
                            @RequestParam("group") int groupId,
                            HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, null);
        try {
            return groupManager.getUserGroups(ui.userId);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Get roles in a portfolio <br>
     * GET /rest/api/groups/{portfolio-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param portfolioUuid
     * @param request
     * @return
     */
    @GetMapping(value = "/{portfolio-id}", produces = "application/xml")
    public String getGroupsPortfolio(@CookieValue("user") String user,
                                     @CookieValue("credential") String token,
                                     @RequestParam("group") int groupId,
                                     @PathVariable("portfolio-id") String portfolioUuid,
                                     HttpServletRequest request) {
        if (!isUUID(portfolioUuid)) {
            throw new RestWebApplicationException(Response.Status.BAD_REQUEST, "Not UUID");
        }
        UserInfo ui = checkCredential(request, user, token, null);
        try {
            return portfolioManager.getRolesByPortfolio(portfolioUuid, ui.userId);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
