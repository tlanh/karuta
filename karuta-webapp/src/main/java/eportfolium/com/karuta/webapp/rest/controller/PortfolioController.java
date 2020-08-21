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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eportfolium.com.karuta.business.contract.*;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.business.security.IsAdmin;
import eportfolium.com.karuta.business.security.IsAdminOrDesigner;
import eportfolium.com.karuta.document.NodeDocument;
import eportfolium.com.karuta.document.PortfolioDocument;
import eportfolium.com.karuta.document.PortfolioList;
import eportfolium.com.karuta.document.ResourceDocument;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController extends AbstractController {

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserManager userManager;

    @Autowired
    private PortfolioManager portfolioManager;

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private ResourceManager resourceManager;

    @InjectLogger
    static private Logger logger;
    
    /**
     * Get a portfolio from uuid.
     *
     * GET /rest/api/portfolios/portfolio/{id}
     *
     * @param files              if set with resource, return a zip file
     * @param export             if set, return XML as a file download
     */
    @GetMapping(value = "/portfolio/{id}")
    public HttpEntity<Object> getPortfolio(@PathVariable UUID id,
                                           @RequestParam(defaultValue = "true") boolean resources,
                                           @RequestParam(required = false) boolean files,
                                           @RequestParam(required = false) boolean export,
                                           @RequestParam(required = false) String lang,
                                           @RequestParam(required = false) Integer level,
                                           @AuthenticationPrincipal UserInfo userInfo,
                                           HttpServletRequest request) throws BusinessException, IOException {

        String contextPath = request.getContextPath();
        String xmlPortfolio = portfolioManager.getPortfolio(id, userInfo.getId(), level);

        if (!export && !files)
          return new HttpEntity<>(xmlPortfolio);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");

        ObjectMapper mapper = new XmlMapper();
        PortfolioDocument portfolio = mapper
  	            .readerFor(PortfolioDocument.class)
  	            .readValue(xmlPortfolio);
        
        String code = portfolio.getCode().replace("_", "");
        String filename = code + "-" + dateFormat.format(new Date());
        
        if (export) {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".xml\"")
                    .body(xmlPortfolio);

        } else if (resources && files) {

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".zip\"")
                    .body(portfolioManager.getZippedPortfolio(portfolio, lang, contextPath).toByteArray());

        } else {
            return new HttpEntity<>(xmlPortfolio);
        }

    }

    /**
     * Return the portfolio from its code.
     *
     * GET /rest/api/portfolios/code/{code}
     */
    @GetMapping(value = "/portfolio/code/{code:.+}")
    public HttpEntity<String> getByCode(@PathVariable String code,
                                                   @RequestParam(required = false) boolean resources,
                                                   @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        String portfolio = portfolioManager
                .getPortfolioByCode(code, userInfo.getId(), resources);

        if (portfolio == null)
            return ResponseEntity.notFound().build();

        return new HttpEntity<>(portfolio);
    }

    /**
     * List portfolios for current user (return also other things, but should be
     * removed).
     *
     * GET /rest/api/portfolios.
     */
    @GetMapping
    public HttpEntity<String> getPortfolios(@RequestParam(defaultValue = "true") boolean active,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) boolean count,
                                            @RequestParam(required = false) Integer userid,
                                            @RequestParam(required = false) String project,
                                            @AuthenticationPrincipal UserInfo userInfo) {

        String portfolioCode = search;
        boolean specialProject = "true".equals(project) || "1".equals(project);

        if (project != null && project.length() > 0)
            portfolioCode = project;

        if (userid != null && securityManager.isAdmin(userInfo.getId())) {
            return new HttpEntity<>(portfolioManager.getPortfolios(userid,
                        active, count, specialProject, portfolioCode));

        } else {
            return new HttpEntity<>(portfolioManager.getPortfolios(userInfo.getId(),
                        active, count, specialProject, portfolioCode));

        }
    }

    /**
     * Reparse portfolio rights.
     *
     * POST /rest/api/portfolios/portfolios/{id}/parserights
     */
    @PostMapping("/portfolio/{id}/parserights")
    @IsAdminOrDesigner
    public ResponseEntity<String> postPortfolio(@PathVariable UUID id,
                                                @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

        portfolioManager.postPortfolioParserights(id, userInfo.getId());

        return ResponseEntity.ok().build();
    }

    /**
     * Change portfolio owner.
     *
     * PUT /rest/api/portfolios/portfolios/{id}/setOwner/{ownerId}
     */
    @PutMapping(value = "/portfolio/{id}/setOwner/{ownerId}")
    public String changeOwner(@PathVariable UUID id,
                               @PathVariable long ownerId,
                               @AuthenticationPrincipal UserInfo userInfo) {

        if (securityManager.isAdmin(userInfo.getId()) || portfolioManager.isOwner(userInfo.getId(), id)) {
            return String.valueOf(portfolioManager.changePortfolioOwner(id, ownerId));
        } else {
            return "false";
        }
    }

    /**
     * Modify some portfolio option.
     *
     * PUT /rest/api/portfolios/portfolios/{portfolio-id}
     */
    @PutMapping("/portfolio/{portfolio}")
    @IsAdmin
    public String putConfiguration(@PathVariable UUID portfolio,
                                   @RequestParam Boolean active) {

        portfolioManager.changePortfolioConfiguration(portfolio, active);

        return "";
    }

    /**
     * From a base portfolio, make an instance with parsed rights in the attributes.
     *
     * POST /rest/api/portfolios/instanciate/{portfolio-id}
     *
     * @param sourcecode         if set, rather than use the provided portfolio
     *                           uuid, search for the portfolio by code
     * @param targetcode         code we want the portfolio to have. If code already
     *                           exists, adds a number after
     * @return instanciated portfolio uuid
     */
    @PostMapping("/instanciate/{id}")
    @IsAdminOrDesigner
    public String instanciate(@PathVariable String id,
                              @RequestParam(required = false) String sourcecode,
                              @RequestParam String targetcode) {

        // Find a suitable code if the provided one already exists.
        String newcode = targetcode;
        int num = 1;

        while (nodeManager.isCodeExist(newcode))
            newcode = targetcode + " (" + num++ + ")";

        // NOTE: We are forced to rely on a String rather than a UUID as for the path
        // variable as, for legacy reasons, when the `sourcecode` parameter is provided,
        // `null` is passed as the id.
        return portfolioManager.instanciatePortfolio(id, sourcecode, newcode);
    }

    /**
     * From a base portfolio, just make a direct copy without rights parsing.
     *
     * POST /rest/api/portfolios/copy/{portfolio-id}
     */
    @PostMapping("/copy/{id}")
    @IsAdminOrDesigner
    public ResponseEntity<String> copyPortfolio(@PathVariable String id,
                                                @RequestParam(required = false) String sourcecode,
                                                @RequestParam String targetcode,
                                                @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        if (nodeManager.isCodeExist(targetcode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("code exist");
        }

        // NOTE: We are forced to rely on a String rather than a UUID as for the path
        // variable as, for legacy reasons, when the `sourcecode` parameter is provided,
        // `null` is passed as the id.
        String returnValue = portfolioManager
                .copyPortfolio(id, sourcecode, targetcode, userInfo.getId())
                .toString();

        return ResponseEntity.ok().body(returnValue);
    }

    /**
     * Return a list of portfolio shared to a user.
     *
     * GET /portfolios/shared/{userid}
     */
    @PostMapping(value = "/shared/{userid}")
    @IsAdmin
    public HttpEntity<PortfolioList> getShared(@PathVariable long userid) {
        return new HttpEntity<>(portfolioManager.getPortfolioShared(userid));
    }

    /**
     * GET /portfolios/zip ? portfolio={}, toujours avec files zip sépares zip des
     * zip Fetching multiple portfolio in a zip.
     *
     * @param portfolios      list of portfolios, separated with ','
     * @return zipped portfolio (with files) inside zip file
     */
    @GetMapping(value = "/zip", consumes = "application/zip")
    public ResponseEntity<byte[]> getZip(@RequestParam String portfolios,
                                         @RequestParam(required = false) String lang,
                                         @AuthenticationPrincipal UserInfo userInfo,
                                         HttpServletRequest request) throws Exception {

        String contextPath = request.getContextPath();

        List<UUID> uuids = Arrays.stream(portfolios.split(","))
                .map(UUID::fromString)
                .collect(Collectors.toList());

        Map<UUID, ByteArrayOutputStream> files = new HashMap<>();

        /// Suppose the first portfolio has the right name to be used
        String name = "";

        /// Create all the zip files
        for (UUID portfolioId : uuids) {
            String xmlportfolio = portfolioManager.getPortfolio(portfolioId, userInfo.getId(), null);

            ObjectMapper mapper = new XmlMapper();

            PortfolioDocument portfolio = mapper
                .readerFor(PortfolioDocument.class)
                .readValue(xmlportfolio);

            // No name yet
            if ("".equals(name)) {
                NodeDocument asmRoot = portfolio.getRoot();

                if (asmRoot != null) {
                    List<ResourceDocument> resources = asmRoot.getResources();

                    if (!resources.isEmpty())
                        name = resources.get(0).getCode();
                }
            }

            files.put(portfolio.getId(), portfolioManager.getZippedPortfolio(portfolio, lang, contextPath));
        }

        // Generate a zip of all the different zips
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(output);

        for (Map.Entry<UUID, ByteArrayOutputStream> file : files.entrySet()) {
            ZipEntry ze = new ZipEntry(file.getKey().toString() + ".zip");

            zos.putNextEntry(ze);
            zos.write(file.getValue().toByteArray());
            zos.closeEntry();
        }

        zos.close();

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(new Date());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = \"" + name + "-" + timeFormat + ".zip\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(output.toByteArray());
    }

    /**
     * As a form, import zip, extract data and put everything into the database.
     *
     * POST /rest/api/portfolios From a zip export of the system
     *
     * @return portfolio uuid
     */
    @PostMapping(value = "/zip", consumes = "multipart/form-data")
    @IsAdminOrDesigner
    public String importZip(@RequestParam MultipartFile uploadfile,
                            @AuthenticationPrincipal UserInfo userInfo,
                            HttpServletRequest request)
            throws BusinessException, IOException {

        String contextPath = request.getServletContext().getContextPath();

        return portfolioManager
                .importPortfolio(uploadfile.getInputStream(), userInfo.getId(), contextPath).toString();
    }

    /**
     * Delete portfolio.
     *
     * DELETE /rest/api/portfolios/portfolio/{id}
     */
    @DeleteMapping(value = "/portfolio/{id}")
    public HttpEntity<String> delete(@PathVariable UUID id,
                         @AuthenticationPrincipal UserInfo userInfo) {

        if (portfolioManager.removePortfolio(id, userInfo.getId())) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * As a form, import xml into the database.
     *
     * POST /rest/api/portfolios
     *
     * @return <portfolios> <portfolio id="uuid"/> </portfolios>
     */
    @PostMapping
    @IsAdminOrDesigner
    public HttpEntity<PortfolioList> postPortfolio(@RequestParam MultipartFile uploadfile,
                                                   @RequestParam(defaultValue = "") String project,
                                                   @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, IOException {

        XmlMapper mapper = new XmlMapper();

        PortfolioDocument document = mapper
            .readerFor(PortfolioDocument.class)
            .readValue(uploadfile.getInputStream());

        return new HttpEntity<>(portfolioManager.addPortfolio(document, userInfo.getId(), project));
    }
}
