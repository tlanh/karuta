package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.socialnetwork.Ning;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ning")
public class NingController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    /**
     * Ning related. <br>
     * GET /rest/api/ning/activities
     *
     * @param user
     * @param token
     * @param group
     * @param type
     * @param request
     * @return
     */
    @GetMapping(value = "/activities", produces = "application/xml")
    public String getNingActivities(@CookieValue("user") String user,
                                    @CookieValue("credential") String token,
                                    @CookieValue("group") String group,
                                    @RequestParam("type") Integer type,
                                    HttpServletRequest request) {
        checkCredential(request, user, token, group);

        Ning ning = new Ning();
        return ning.getXhtmlActivites();
    }

}
