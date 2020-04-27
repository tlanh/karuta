package eportfolium.com.karuta.webapp.rest.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response.Status;

import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;

import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.javaUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
public class RoleController extends AbstractController {

    @InjectLogger
    private static Logger logger;

    @Autowired
    private UserManager userManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PortfolioManager portfolioManager;

    /**
     * Fetch rights in a role. <br>
     * GET /rest/api/roles/role/{role-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param roleId
     * @param request
     * @return
     */
    @GetMapping(value = "/role/{role-id}", produces = {"application/json", "application/xml"})
    public String getRole(@CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") int groupId,
                          @PathVariable("role-id") Long roleId,
                          HttpServletRequest request) {
        // checkCredential(httpServletRequest, user, token, null); FIXME
        try {
            return userManager.getRole(roleId);
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Status.NOT_FOUND, "Role " + roleId + " not found");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    /**
     * Fetch a role in a portfolio. <br>
     * GET /rest/api/roles/portfolio/{portfolio-id}
     *
     * @param user
     * @param token
     * @param groupId
     * @param role
     * @param portfolioId
     * @param request
     * @return
     */
    @GetMapping(value = "/portfolio/{portfolio-id}", produces = {"application/json", "application/xml"})
    public String getRolePortfolio(@CookieValue("user") String user,
                                   @CookieValue("credential") String token,
                                   @RequestParam("group") int groupId,
                                   @RequestParam("role") String role,
                                   @RequestParam("portfolio-id") String portfolioId,
                                   HttpServletRequest request) {
        if (!isUUID(portfolioId)) {
            throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
        }
        UserInfo ui = checkCredential(request, user, token, null);
        try {
            String returnValue = portfolioManager
                    .getRoleByPortfolio(MimeTypeUtils.TEXT_XML, role, portfolioId, ui.userId);
            return returnValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }

    }

    /**
     * Modify a role. <br>
     * PUT /rest/api/roles/role/{role-id}
     *
     * @param xmlRole
     * @param user
     * @param token
     * @param groupId
     * @param roleId
     * @param request
     * @return
     */
    @PutMapping(value = "/role/{role-id}", produces = "application/xml")
    public String putRole(String xmlRole,
                          @CookieValue("user") String user,
                          @CookieValue("credential") String token,
                          @RequestParam("group") int groupId,
                          @PathVariable("role-id") long roleId,
                          HttpServletRequest request) {

        UserInfo ui = checkCredential(request, user, token, null);
        try {
            return securityManager.changeRole(ui.userId, roleId, xmlRole).toString();
        } catch (DoesNotExistException e) {
            throw new RestWebApplicationException(Status.NOT_FOUND, "Role with id " + roleId + " not found");
        } catch (BusinessException ex) {
            throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
            throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
