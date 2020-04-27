package eportfolium.com.karuta.webapp.rest.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("RightGroup")
public class RightGroupController extends AbstractController {

    @Autowired
    private GroupManager groupManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Change the group right associated to a user group. <br>
     * POST /rest/api/RightGroup
     *
     * @param user
     * @param token
     * @param groupId            user group id
     * @param groupRightId       group right
     * @param request
     * @return
     */
    @PostMapping(produces = "application/xml")
    public Response postRightGroup(@CookieValue("user") String user,
                                   @CookieValue("credential") String token,
                                   @RequestParam("group") Long groupId,
                                   @RequestParam("groupRightId") Long groupRightId,
                                   HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            groupManager.changeUserGroup(groupRightId, groupId, ui.userId);
            logger.info("modifié");
            return null;
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(Status.NOT_FOUND, "User group not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}

