package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.socialnetwork.Elgg;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

@RestController
@RequestMapping("/elgg")
public class ElggController extends AbstractController {

    @Autowired
    private ConfigurationManager configurationManager;

    @InjectLogger
    private static Logger logger;

    /**
     * elgg related. <br>
     * GET /rest/api/elgg/site/river_feed
     *
     * @param user
     * @param token
     * @param group
     * @param type
     * @param limit
     * @param request
     * @return
     */
    @GetMapping(value = "/site/river_feed", produces = "text/html")
    public String getElggSiteRiverFeed(@CookieValue("user") String user,
                                       @CookieValue("credential") String token,
                                       @CookieValue("group") String group,
                                       @RequestParam("type") Integer type,
                                       @RequestParam("limit") String limit,
                                       HttpServletRequest request) {
        int iLimit;
        try {
            iLimit = Integer.parseInt(limit);
        } catch (Exception ex) {
            iLimit = 20;
        }
        UserInfo ui = checkCredential(request, user, token, null);
        System.out.println(ui.User);

        // Elgg variables
        String elggDefaultApiUrl = configurationManager.get("elggDefaultApiUrl");
        String elggDefaultSiteUrl = configurationManager.get("elggDefaultSiteUrl");
        String elggApiKey = configurationManager.get("elggApiKey");
        String elggDefaultUserPassword = configurationManager.get("elggDefaultUserPassword");

        try {
            Elgg elgg = new Elgg(elggDefaultApiUrl, elggDefaultSiteUrl, elggApiKey, ui.User, elggDefaultUserPassword);
            return elgg.getSiteRiverFeed(iLimit);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(javaUtils.getCompleteStackTrace(ex) + Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Elgg related. <br>
     * POST /rest/api/elgg/wire
     *
     * @param message
     * @param user
     * @param token
     * @param group
     * @param type
     * @param request
     * @return
     */
    @PostMapping(value = "/wire", produces = "application/xml")
    public String getElggSiteRiverFeed(String message,
                                       @CookieValue("user") String user,
                                       @CookieValue("credential") String token,
                                       @CookieValue("group") String group,
                                       @RequestParam("type") Integer type,
                                       HttpServletRequest request) {
        UserInfo ui = checkCredential(request, user, token, null);

        // Elgg variables
        String elggDefaultApiUrl = configurationManager.get("elggDefaultApiUrl");
        String elggDefaultSiteUrl = configurationManager.get("elggDefaultSiteUrl");
        String elggApiKey = configurationManager.get("elggApiKey");
        String elggDefaultUserPassword = configurationManager.get("elggDefaultUserPassword");

        try {
            Elgg elgg = new Elgg(elggDefaultApiUrl, elggDefaultSiteUrl, elggApiKey, ui.User, elggDefaultUserPassword);
            return elgg.postWire(message);
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(javaUtils.getCompleteStackTrace(ex) + Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            throw new RestWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
