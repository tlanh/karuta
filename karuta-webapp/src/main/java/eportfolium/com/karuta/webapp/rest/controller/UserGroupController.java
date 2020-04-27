package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

/**
 * Managing and listing user groups
 *
 * @author mlengagne
 *
 */
@RestController
@RequestMapping("/groupsUsers")
public class UserGroupController extends AbstractController {

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
     * @param userId             userId
     * @param request
     * @return <ok/>
     */
    @PostMapping(produces = "application/xml")
    public String postGroupsUsers(@CookieValue("user") String user,
                                  @CookieValue("credential") String token,
                                  @RequestParam("group") long groupId,
                                  @RequestParam("userId") long userId,
                                  HttpServletRequest request) {

        UserInfo ui = checkCredential(request, user, token, null);

        try {
            securityManager.addUserToGroup(ui.userId, userId, groupId);
            return "<ok/>";
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Response.Status.FORBIDDEN, ex.getMessage());
        } catch (RestWebApplicationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}

