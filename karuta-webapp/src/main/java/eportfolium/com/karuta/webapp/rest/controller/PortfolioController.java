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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eportfolium.com.karuta.business.contract.*;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.document.NodeDocument;
import eportfolium.com.karuta.document.PortfolioDocument;
import eportfolium.com.karuta.document.PortfolioList;
import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;

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
     * @return zip as file download content.
     */
    @GetMapping(value = "/portfolio/{id}", produces = {"application/xml", "application/json",
            "application/zip", "application/octet-stream"})
    public HttpEntity<Object> getPortfolio(@PathVariable UUID id,
                                           @RequestParam boolean resources,
                                           @RequestParam boolean files,
                                           @RequestParam String export,
                                           @RequestParam String lang,
                                           @RequestParam Integer level,
                                           Authentication authentication) throws BusinessException, IOException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        PortfolioDocument portfolio = portfolioManager.getPortfolio(id, userInfo.getId(), 0L, level);

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(new Date());

        String code = portfolio.getCode().replace("_", "");

        if (export != null) {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = \"" + code + "-" + timeFormat + ".xml\"")
                    .body(portfolio);
        } else if (resources && files) {
            // TODO: Rely on PortfolioManager#getZippedPortfolio

            //// Cas du renvoi d'un ZIP
            File tempZip = getZipFile(portfolio, lang);

            /// Return zip file
            RandomAccessFile f = new RandomAccessFile(tempZip.getAbsoluteFile(), "r");
            byte[] b = new byte[(int) f.length()];
            f.read(b);
            f.close();

            // Temp file cleanup
            tempZip.delete();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = \"" + code + "-" + timeFormat + ".zip")
                    .body(b);
        } else {
            return new HttpEntity<>(portfolio);
        }

    }

    private File getZipFile(PortfolioDocument portfolio, final String lang)
            throws IOException {

        File tempZip = File.createTempFile(portfolio.getId().toString(), ".zip");

        FileOutputStream fos = new FileOutputStream(tempZip);
        ZipOutputStream zos = new ZipOutputStream(fos);

        /// Write XML file to zip
        ZipEntry ze = new ZipEntry(portfolio.getId().toString() + ".xml");
        zos.putNextEntry(ze);

        byte[] bytes = new XmlMapper()
                                .writeValueAsString(portfolio)
                                .getBytes(StandardCharsets.UTF_8);
        zos.write(bytes);
        zos.closeEntry();

        List<NodeDocument> nodesWithFiles = portfolio
                .getNodes()
                .stream()
                .filter(n -> n.getResources()
                        .stream()
                        .anyMatch(r -> r.getLang() == lang && r.getFileid() != null))
                .collect(Collectors.toList());

        // Loop through the different nodes that have file to
        // fetch them.
        nodesWithFiles.forEach(node -> {
            ResourceDocument resource = node.getResources()
                                                .stream()
                                                .filter(r -> r.getFileid() != null)
                                                .findFirst()
                                                .get();

            String filename = resource.getFilename();
            String resourceLang = resource.getLang() != null ? resource.getLang() : "fr";

            if (filename.equals(""))
                return;

            String fullname = String.format("%s_%s.%s",
                    node.getId().toString(),
                    resourceLang,
                    filename.substring(filename.lastIndexOf(".") + 1));

            // Save entry to zip file
            try {
                // TODO: Properly fetch the resource ; looks like it hasn't
                //  been imported from original code
                // String resourceDocument = resourceManager.getResource(node.getId(), userId, 0L);

                InputStream content = null;
                ZipEntry entry = new ZipEntry(fullname);

                zos.putNextEntry(entry);
                int inByte;
                byte[] buf = new byte[4096];

                while ((inByte = content.read(buf)) != -1) {
                    zos.write(buf, 0, inByte);
                }

                content.close();
                zos.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        zos.close();
        fos.close();

        return tempZip;
    }

    /**
     * Return the portfolio from its code.
     *
     * GET /rest/api/portfolios/code/{code}
     *
     * @see #putPortfolio(PortfolioDocument, UUID, boolean, Authentication)
     */
    @GetMapping(value = "/portfolio/code/{code}", produces = {"application/json", "application/xml"})
    public HttpEntity<PortfolioDocument> getByCode(@RequestParam long group,
                                                   @PathVariable String code,
                                                   @RequestParam boolean resources,
                                                   Authentication authentication)
            throws BusinessException, JsonProcessingException {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(portfolioManager
                .getPortfolioByCode(code, userInfo.getId(), group, resources));
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
    @GetMapping(consumes = "application/xml", produces = {"application/json", "application/xml"})
    public HttpEntity<Object> getPortfolios(@RequestParam long group,
                                            @RequestParam boolean active,
                                            @RequestParam Integer userid,
                                            @RequestParam String code,
                                            @RequestParam UUID portfolio,
                                            @RequestParam Integer level,
                                            @RequestParam("public") String public_var,
                                            @RequestParam boolean project,
                                            Authentication authentication)
            throws BusinessException, JsonProcessingException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        if (portfolio != null) {
            return new HttpEntity<>(portfolioManager.getPortfolio(portfolio, userInfo.getId(),
                    group, level));

        } else if (code != null) {
            return new HttpEntity<>(portfolioManager.getPortfolioByCode(code, userInfo.getId(),
                        group, false));

        } else if (public_var != null) {
            long publicid = userManager.getUserId("public");

            return new HttpEntity<>(portfolioManager.getPortfolios(publicid,
                        active, 0, project));

        } else if (userid != null && securityManager.isAdmin(userInfo.getId())) {
            return new HttpEntity<>(portfolioManager.getPortfolios(userid,
                        active, userInfo.getSubstituteId(), project));

        } else {
            return new HttpEntity<>(portfolioManager.getPortfolios(userInfo.getId(),
                        active, userInfo.getSubstituteId(), project));
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
    @PutMapping(value = "/portfolio/{id}", consumes = "application/xml", produces = "application/xml")
    public String putPortfolio(@RequestBody PortfolioDocument portfolio,
                               @PathVariable UUID id,
                               @RequestParam boolean active,
                               Authentication authentication) throws BusinessException, JsonProcessingException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        portfolioManager.rewritePortfolioContent(portfolio, id, userInfo.getId(), active);

        return "";
    }

    /**
     * Reparse portfolio rights.
     *
     * POST /rest/api/portfolios/portfolios/{id}/parserights
     */
    @PostMapping("/portfolio/{id}/parserights")
    @PreAuthorize("hasRole('admin') or hasRole('designer')")
    public ResponseEntity<String> postPortfolio(@PathVariable UUID id,
                                                Authentication authentication)
            throws BusinessException, JsonProcessingException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        portfolioManager.postPortfolioParserights(id, userInfo.getId());

        return ResponseEntity.ok().build();
    }

    /**
     * Change portfolio owner.
     *
     * PUT /rest/api/portfolios/portfolios/{id}/setOwner/{ownerId}
     */
    @PutMapping(value = "/portfolio/{id}/setOwner/{ownerId}", consumes = "application/xml",
            produces = "application/xml")
    public Boolean changeOwner(@PathVariable UUID id,
                               @PathVariable long ownerId,
                               Authentication authentication) {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

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
    @PutMapping(consumes = "application/xml", produces = "application/xml")
    public String changeConfiguration(@RequestParam UUID portfolio,
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
    @PreAuthorize("hasRole('admin') or hasRole('designer')")
    public ResponseEntity<String> instanciate(@RequestParam int group,
                                              @PathVariable String id,
                                              @RequestParam String sourcecode,
                                              @RequestParam String targetcode,
                                              @RequestParam boolean copyshared,
                                              @RequestParam String groupname,
                                              @RequestParam boolean owner,
                                              Authentication authentication) throws BusinessException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        /// Vérifiez si le code existe, trouvez-en un qui convient, sinon. Eh.
        String newcode = targetcode;
        int num = 0;
        while (nodeManager.isCodeExist(newcode))
            newcode = targetcode + " (" + num++ + ")";
        targetcode = newcode;

        String returnValue = portfolioManager.instanciatePortfolio(id, sourcecode,
                targetcode, userInfo.getId(), group, copyshared, groupname, owner);

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
     *
     * @see #instanciate(int, String,
     *      String, String, boolean, String, boolean, Authentication)
     */
    @PostMapping("/copy/{id}")
    @PreAuthorize("hasRole('admin') or hasRole('designer')")
    public ResponseEntity<String> copyPortfolio(@PathVariable UUID id,
                                                @RequestParam String sourcecode,
                                                @RequestParam String targetcode,
                                                @RequestParam boolean owner,
                                                Authentication authentication) {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

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
    @PostMapping(value = "/shared/{userid}", produces = "application/xml")
    @PreAuthorize("hasRole('admin')")
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
                                  Authentication authentication) throws Exception {
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        List<UUID> uuids = Arrays.stream(portfolios.split(","))
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<File> files = new ArrayList<>();

        /// Suppose the first portfolio has the right name to be used
        String name = "";

        /// Create all the zip files
        for (UUID portfolioId : uuids) {
            PortfolioDocument portfolio = portfolioManager.getPortfolio(portfolioId, userInfo.getId(), 0L, null);

            // No name yet
            if ("".equals(name)) {
                Optional<NodeDocument> nodeDocument = portfolio
                        .getNodes()
                        .stream()
                        .filter(n -> n.getType().equals("asmRoot"))
                        .findFirst();

                if (nodeDocument.isPresent()) {
                    List<ResourceDocument> resources = nodeDocument.get().getResources();

                    if (!resources.isEmpty())
                        name = resources.get(0).getCode();
                }
            }

            files.add(getZipFile(portfolio, lang));
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
    public String importZip(@RequestParam(defaultValue = "-1") long group,
                            @RequestParam MultipartFile fileupload,
                            @RequestParam(required = false) String model,
                            @RequestParam(defaultValue = "false") boolean instance,
                            @RequestParam(required = false) String project,
                            Authentication authentication,
                            HttpServletRequest request)
            throws BusinessException, IOException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();
        javax.servlet.ServletContext servletContext = request.getSession().getServletContext();
        String path = servletContext.getRealPath("/");

        return portfolioManager
                .importZippedPortfolio(path, userInfo.getUsername(), fileupload.getInputStream(), userInfo.getId(), group, model,
                        userInfo.getSubstituteId(), instance, project);
    }

    /**
     * Delete portfolio.
     *
     * DELETE /rest/api/portfolios/portfolio/{id}
     */
    @DeleteMapping(value = "/portfolio/{id}", produces = "application/xml")
    public String delete(@RequestParam long group,
                         @PathVariable UUID id,
                         Authentication authentication) throws Exception {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        portfolioManager.removePortfolio(id, userInfo.getId(), group);

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
    @PostMapping(consumes = {"multipart/form-data", "application/xml"}, produces = "application/xml")
    public HttpEntity<PortfolioList> postPortfolio(@RequestBody PortfolioDocument portfolio,
                                                   @RequestParam int group,
                                                   @RequestParam UUID model,
                                                   @RequestParam boolean instance,
                                                   @RequestParam String project,
                                                   Authentication authentication) throws BusinessException, JsonProcessingException {

        UserInfo userInfo = (UserInfo)authentication.getPrincipal();

        return new HttpEntity<>(portfolioManager.addPortfolio(portfolio, userInfo.getId(), group, model,
                instance, project));
    }
}
