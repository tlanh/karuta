package eportfolium.com.karuta.webapp.rest.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groupRightsInfos")
public class GroupRightsInfoController extends AbstractController {

    @Autowired
    private PortfolioManager portfolioManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Get role list from portfolio from uuid. <br>
     * GET /rest/api/groupRightsInfos
     *
     * @param user
     * @param token
     * @param groupId
     * @param portfolioId        portfolio uuid
     * @param request
     * @return <groupRightsInfos> <groupRightInfo grid="grouprightid">
     *         <label></label> <owner>UID</owner> </groupRightInfo>
     *         </groupRightsInfos>
     */
    @GetMapping(produces = "application/xml")
    public String getGroupRightsInfos(@CookieValue("user") String user,
                                      @CookieValue("credential") String token,
                                      @RequestParam("group") int groupId,
                                      @RequestParam("portfolioId") String portfolioId,
                                      HttpServletRequest request) {
        if (!isUUID(portfolioId)) {
            throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
        }
        UserInfo ui = checkCredential(request, user, token, null);

        try {
            return portfolioManager.getGroupRightsInfos(ui.userId, portfolioId);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}

