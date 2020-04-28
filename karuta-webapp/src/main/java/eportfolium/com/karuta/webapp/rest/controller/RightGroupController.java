package eportfolium.com.karuta.webapp.rest.controller;

import javax.servlet.http.HttpServletRequest;

import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> postRightGroup(@CookieValue("user") String user,
                                                 @CookieValue("credential") String token,
                                                 @RequestParam("group") Long groupId,
                                                 @RequestParam("groupRightId") Long groupRightId,
                                                 HttpServletRequest request) throws RestWebApplicationException {
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            groupManager.changeUserGroup(groupRightId, groupId, ui.userId);
            logger.info("modifié");
            return ResponseEntity.ok().build();
        } catch (DoesNotExistException ex) {
            throw new RestWebApplicationException(HttpStatus.NOT_FOUND, "User group not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}
