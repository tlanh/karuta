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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
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
import eportfolium.com.karuta.model.exception.GenericBusinessException;
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

    @Autowired
    private HttpServletRequest httpServletRequest;
    
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
                                           @RequestParam(defaultValue = "false") boolean files,
                                           @RequestParam(required = false) boolean export,
                                           @RequestParam(required = false) String lang,
                                           @RequestParam(required = false) Integer level,
                                           @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, IOException {

    	String contextPath = httpServletRequest.getContextPath();
    	String xmlPortfolio = portfolioManager.getPortfolio(id, userInfo.getId(), level);

        if( !export && !files )
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

            File tempZip = portfolioManager.getZippedPortfolio(portfolio, lang, contextPath);
            byte[] zipContent = Files.readAllBytes(tempZip.toPath());

            // Temp file cleanup
            tempZip.delete();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + ".zip\"")
                    .body(zipContent);
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
     *
     * @param active             false (also show inactive portoflios)
     * @param userid             for this user (only with root)
     */
    @GetMapping
    public HttpEntity<Object> getPortfolios(@RequestParam(defaultValue = "true") boolean active,
                                            @RequestParam(required = false) Integer userid,
                                            @RequestParam(required = false) String code,
                                            @RequestParam(required = false) UUID portfolio,
                                            @RequestParam(required = false) Integer level,
                                            @RequestParam(name="public", required = false) String public_var,
                                            @RequestParam(required = false) String project,
                                            @AuthenticationPrincipal UserInfo userInfo)
            throws BusinessException, JsonProcessingException {

    	String portfolioCode = null;
    	Boolean specialProject = null;
    	if("false".equals(project) || "0".equals(project)) specialProject = false;
    	else if("true".equals(project) || "1".equals(project)) specialProject = true;
			else if(project != null && project.length()>0) portfolioCode = project;

        if (portfolio != null) {
            return new HttpEntity<>(portfolioManager.getPortfolio(portfolio, userInfo.getId(), level));

        } else if (code != null) {
            return new HttpEntity<>(portfolioManager.getPortfolioByCode(code, userInfo.getId(), false));

        } else if (public_var != null) {
            long publicid = userManager.getUserId("public");

            return new HttpEntity<>(portfolioManager.getPortfolios(publicid,
                        active, 0, specialProject, portfolioCode));

        } else if (userid != null && securityManager.isAdmin(userInfo.getId())) {
            return new HttpEntity<>(portfolioManager.getPortfolios(userid,
                        active, userInfo.getSubstituteId(), specialProject, portfolioCode));

        } else {
            return new HttpEntity<>(portfolioManager.getPortfolios(userInfo.getId(),
                        active, userInfo.getSubstituteId(), specialProject, portfolioCode));
        }
    }

    /**
     * Rewrite portfolio content.
     *
     * PUT /rest/api/portfolios/portfolios/{id}
     *
     * @param portfolio       GET /rest/api/portfolios/portfolio/{id}
     *                           and/or the asm format
     */
    /*
    @PutMapping(value = "/portfolio/{id}", consumes = "application/xml", produces = "application/xml")
    public String putPortfolio(@RequestBody PortfolioDocument portfolio,
                               @PathVariable UUID id,
                               @RequestParam boolean active,
                               @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, JsonProcessingException {

        portfolioManager.rewritePortfolioContent(portfolio, id, userInfo.getId(), active);

        return "";
    }
    //*/

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
    public Boolean changeOwner(@PathVariable UUID id,
                               @PathVariable long ownerId,
                               @AuthenticationPrincipal UserInfo userInfo) {

        // Vérifie si l'utilisateur connecté est administrateur ou propriétaire du
        // portfolio actuel.
        if (securityManager.isAdmin(userInfo.getId()) || portfolioManager.isOwner(userInfo.getId(), id)) {
            return portfolioManager.changePortfolioOwner(id, ownerId);
        } else {
            return false;
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
     * @param copyshared         y/null Make a copy of shared nodes, rather than
     *                           keeping the link to the original data
     * @param owner              true/null Set the current user instanciating the
     *                           portfolio as owner. Otherwise keep the one that
     *                           created it.
     * @return instanciated portfolio uuid
     */
    @PostMapping("/instanciate/{id}")
    @IsAdminOrDesigner
    public ResponseEntity<String> instanciate(@PathVariable UUID id,
                                              @RequestParam(required = false) String sourcecode,
                                              @RequestParam String targetcode,
                                              @RequestParam(defaultValue = "false") boolean copyshared,
                                              @RequestParam(required = false) String groupname,
                                              @RequestParam(defaultValue = "false") boolean owner,
                                              @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        /// Vérifiez si le code existe, trouvez-en un qui convient, sinon. Eh.
        String newcode = targetcode;
        int num = 0;

        while (nodeManager.isCodeExist(newcode))
            newcode = targetcode + " (" + num++ + ")";
        targetcode = newcode;

        String returnValue = portfolioManager.instanciatePortfolio(id, sourcecode,
                targetcode, userInfo.getId(), copyshared, groupname, owner);

        if (returnValue.startsWith("no rights"))
            throw new GenericBusinessException(returnValue);
        else if (returnValue.startsWith("erreur"))
            throw new GenericBusinessException(returnValue);
        else if ("".equals(returnValue)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok()
                .body(returnValue);
    }

    /**
     * From a base portfolio, just make a direct copy without rights parsing.
     *
     * POST /rest/api/portfolios/copy/{portfolio-id}
     */
    @PostMapping("/copy/{id}")
    @IsAdminOrDesigner
    public ResponseEntity<String> copyPortfolio(@PathVariable UUID id,
                                                @RequestParam (required = false)String sourcecode,
                                                @RequestParam String targetcode,
                                                @RequestParam boolean owner,
                                                @AuthenticationPrincipal UserInfo userInfo) throws BusinessException {

        /// Check if code exist, find a suitable one otherwise. Eh.
        // FIXME : Check original Karuta version ; no new code found.
        if (nodeManager.isCodeExist(targetcode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("code exist");
        }

        String returnValue = portfolioManager
                .copyPortfolio(id, sourcecode, targetcode, userInfo.getId(), owner)
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
    public Object getZip(@RequestParam String portfolios,
                         @RequestParam String lang,
                         @AuthenticationPrincipal UserInfo userInfo) throws Exception {

    	String contextPath = httpServletRequest.getContextPath();
        List<UUID> uuids = Arrays.stream(portfolios.split(","))
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<File> files = new ArrayList<>();

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

            files.add(portfolioManager.getZippedPortfolio(portfolio, lang, contextPath));
        }

        // Make a big zip of it
        File tempDir = new File(System.getProperty("java.io.tmpdir", null));
        File bigZip = File.createTempFile("project_", ".zip", tempDir);

        // Add content to it
        FileOutputStream fos = new FileOutputStream(bigZip);
        ZipOutputStream zos = new ZipOutputStream(fos);

        byte[] buffer = new byte[0x1000];

        for (File file : files) {
            FileInputStream fis = new FileInputStream(file);
            String filename = file.getName();

            /// Write XML file to zip
            ZipEntry ze = new ZipEntry(filename + ".zip");
            zos.putNextEntry(ze);
            int read = 1;
            while (read > 0) {
                read = fis.read(buffer);
                zos.write(buffer);
            }
            fis.close();
            zos.closeEntry();
        }
        zos.close();

        /// Return zip file
        RandomAccessFile f = new RandomAccessFile(bigZip.getAbsoluteFile(), "r");
        byte[] b = new byte[(int) f.length()];
        f.read(b);
        f.close();

        Date time = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(time);

        ResponseEntity response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = \"" + name + "-" + timeFormat + ".zip\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(b);

        // Delete all zipped file
        files.forEach(File::delete);

        // And the over-arching zip.
        bigZip.delete();

        return response;
    }

    /**
     * As a form, import zip, extract data and put everything into the database.
     *
     * POST /rest/api/portfolios From a zip export of the system
     *
     * @return portfolio uuid
     */
    @PostMapping(value = "/zip", consumes = "multipart/form-data")
    public String importZip(@RequestParam MultipartFile uploadfile,
                            @RequestParam(defaultValue = "false") boolean instance,
                            @RequestParam(required = false) String project,
                            @AuthenticationPrincipal UserInfo userInfo,
                            HttpServletRequest request)
            throws BusinessException, IOException {

        javax.servlet.ServletContext servletContext = request.getSession().getServletContext();
        String contextPath = servletContext.getContextPath();
        String path = servletContext.getRealPath("/");

        return portfolioManager
                .importPortfolio(path, uploadfile.getInputStream(), userInfo.getId(), instance, project, contextPath);
    }

    /**
     * Delete portfolio.
     *
     * DELETE /rest/api/portfolios/portfolio/{id}
     */
    @DeleteMapping(value = "/portfolio/{id}")
    public String delete(@PathVariable UUID id,
                         @AuthenticationPrincipal UserInfo userInfo) {

        portfolioManager.removePortfolio(id, userInfo.getId());

        return "";
    }

    /**
     * As a form, import xml into the database.
     *
     * POST /rest/api/portfolios
     *
     * @param model              another uuid, not sure why it's here
     * @param instance           true/null if as an instance, parse rights.
     *                           Otherwise just write nodes xml: ASM format
     * @return <portfolios> <portfolio id="uuid"/> </portfolios>
     */
    @PostMapping
    public HttpEntity<PortfolioList> postPortfolio(@RequestParam MultipartFile uploadfile,
                                                   @RequestParam(required = false) UUID model,
                                                   @RequestParam(defaultValue = "false") boolean instance,
                                                   @RequestParam(defaultValue = "") String project,
                                                   @AuthenticationPrincipal UserInfo userInfo) throws BusinessException, JsonProcessingException {
    	 
    	StringBuilder sbuilder = new StringBuilder();
    	String content = "";
    	
        try {
            InputStream input = uploadfile.getInputStream();
            byte[] buffer = new byte[2048];
            int length;

            while ((length = input.read(buffer)) != -1) {
                sbuilder.append(new String(buffer, 0, length));
            }

            input.close();

            content = sbuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new XmlMapper();

        PortfolioDocument document = mapper
            .readerFor(PortfolioDocument.class)
            .readValue(content);

      return new HttpEntity<>(portfolioManager.addPortfolio(document, userInfo.getId(), model,
               instance, project));
    }
}
