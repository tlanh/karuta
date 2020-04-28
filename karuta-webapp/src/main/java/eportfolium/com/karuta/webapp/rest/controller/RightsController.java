package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/rights")
public class RightsController extends AbstractController {

    @Autowired
    private NodeManager nodeManager;

    @InjectLogger
    private static Logger logger;

    /**
     * Change rights for a node. <br>
     * POST /rest/api/rights
     *
     * @param xmlNode
     * @param request
     * @return
     */
    @PostMapping(produces = "application/xml")
    public String postChangeRights(@RequestBody String xmlNode,
                                   HttpServletRequest request) throws RestWebApplicationException {

        UserInfo ui = checkCredential(request, null, null, null);

        try {
            nodeManager.changeRights(xmlNode, ui.userId, ui.subId, "");
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
