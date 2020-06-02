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
import eportfolium.com.karuta.document.NodeDocument;
import eportfolium.com.karuta.document.PortfolioDocument;
import eportfolium.com.karuta.document.PortfolioList;
import eportfolium.com.karuta.document.ResourceDocument;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController extends AbstractController {

    private String label;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private ConfigurationManager configurationManager;

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
     * GET /rest/api/portfolios/portfolio/{portfolio-id}
     *
     * @param files              if set with resource, return a zip file
     * @param export             if set, return XML as a file download
     * @return zip as file download content. <br>
     *         <?xml version=\"1.0\" encoding=\"UTF-8\"?> <portfolio code=\"0\"
     *         id=\""+portfolioUuid+"\" owner=\""+isOwner+"\"><version>4</version>
     *         <asmRoot> <asm*> <metadata-wad></metadata-wad> <metadata></metadata>
     *         <metadata-epm></metadata-epm> <asmResource xsi_type="nodeRes">
     *         <asmResource xsi_type="context">
     *         <asmResource xsi_type="SPECIFIC TYPE"> </asm*> </asmRoot>
     *         </portfolio>
     */
    @GetMapping(value = "/portfolio/{portfolio-id}", produces = {"application/xml", "application/json",
            "application/zip", "application/octet-stream"})
    public HttpEntity<Object> getPortfolio(@PathVariable("portfolio-id") UUID portfolioId,
                                           @RequestParam("resources") boolean resources,
                                           @RequestParam("files") boolean files,
                                           @RequestParam("export") String export,
                                           @RequestParam("lang") String lang,
                                           @RequestParam("level") Integer cutoff,
                                           HttpServletRequest request) throws BusinessException, IOException {

        UserInfo ui = checkCredential(request);

        PortfolioDocument portfolio = portfolioManager.getPortfolio(portfolioId, ui.userId, 0L,
                this.label, resources, false, ui.subId, cutoff);

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
            File tempZip = getZipFile(portfolio, lang, ui.userId);

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

    private File getZipFile(PortfolioDocument portfolio, final String lang, Long userId)
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
     * @see #putPortfolio(PortfolioDocument, UUID, boolean, HttpServletRequest)
     */
    @GetMapping(value = "/portfolio/code/{code}", produces = {"application/json", "application/xml"})
    public HttpEntity<PortfolioDocument> getPortfolioByCode(@RequestParam("group") long groupId,
                                                            @PathVariable("code") String code,
                                                            @RequestParam("resources") boolean resources,
                                                            HttpServletRequest request)
            throws BusinessException, JsonProcessingException {
        UserInfo ui = checkCredential(request);

        if (ui.userId == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return new HttpEntity<>(portfolioManager
                .getPortfolioByCode(code, ui.userId, groupId, resources, ui.subId));
    }

    /**
     * List portfolios for current user (return also other things, but should be
     * removed).
     *
     * GET /rest/api/portfolios.
     *
     * @param active             false (also show inactive portoflios)
     * @param userId             for this user (only with root)
     * @return <?xml version=\"1.0\" encoding=\"UTF-8\"?> <portfolios>
     *         <portfolio id="uuid" root_node_id="uuid" owner="Y/N" ownerid="uid"
     *         modified= "DATE"> <asmRoot id="uuid"> <metadata-wad/> <metadata-epm/>
     *         <metadata/> <code></code> <label/> <description/> <semanticTag/>
     *         <asmResource xsi_type="nodeRes"></asmResource>
     *         <asmResource xsi_type="context"/> </asmRoot> </portfolio> ...
     *         </portfolios>
     */
    @GetMapping(consumes = "application/xml", produces = {"application/json", "application/xml"})
    public HttpEntity<Object> getPortfolios(@RequestParam("group") long groupId,
                                            @RequestParam("active") boolean active,
                                            @RequestParam("userid") Integer userId,
                                            @RequestParam("code") String code,
                                            @RequestParam("portfolio") UUID portfolioId,
                                            @RequestParam("level") Integer cutoff,
                                            @RequestParam("public") String public_var,
                                            @RequestParam("project") boolean project,
                                            HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        if (portfolioId != null) {
            return new HttpEntity<>(portfolioManager.getPortfolio(portfolioId, ui.userId,
                    groupId, this.label, false, false, ui.subId, cutoff));

        } else if (code != null) {
            return new HttpEntity<>(portfolioManager.getPortfolioByCode(code, ui.userId,
                        groupId, false, ui.subId));

        } else if (public_var != null) {
            long publicid = userManager.getUserId("public");

            return new HttpEntity<>(portfolioManager.getPortfolios(publicid,
                        active, 0, project));

        } else if (userId != null && securityManager.isAdmin(ui.userId)) {
            return new HttpEntity<>(portfolioManager.getPortfolios(userId,
                        active, ui.subId, project));

        } else {
            return new HttpEntity<>(portfolioManager.getPortfolios(ui.userId,
                        active, ui.subId, project));
        }
    }

    /**
     * Rewrite portfolio content.
     *
     * PUT /rest/api/portfolios/portfolios/{portfolio-id}
     *
     * @param portfolio       GET /rest/api/portfolios/portfolio/{portfolio-id}
     *                           and/or the asm format
     */
    @PutMapping(value = "/portfolio/{portfolio-id}", consumes = "application/xml", produces = "application/xml")
    public String putPortfolio(@RequestBody PortfolioDocument portfolio,
                               @PathVariable("portfolio-id") UUID portfolioId,
                               @RequestParam("active") boolean active,
                               HttpServletRequest request) throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        portfolioManager.rewritePortfolioContent(portfolio, portfolioId, ui.userId, active);

        return "";
    }

    /**
     * Reparse portfolio rights.
     *
     * POST /rest/api/portfolios/portfolios/{portfolio-id}/parserights
     */
    @PostMapping("/portfolio/{portfolio-id}/parserights")
    public ResponseEntity<String> postPortfolio(@PathVariable("portfolio-id") UUID portfolioId,
                                                HttpServletRequest request)
            throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        if (!securityManager.isAdmin(ui.userId))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        portfolioManager.postPortfolioParserights(portfolioId, ui.userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Change portfolio owner.
     *
     * PUT /rest/api/portfolios/portfolios/{portfolio-id}/setOwner/{newOwnerId}
     */
    @PutMapping(value = "/portfolio/{portfolio-id}/setOwner/{newOwnerId}", consumes = "application/xml",
            produces = "application/xml")
    public Boolean putPortfolioOwner(@PathVariable("portfolio-id") UUID portfolioId,
                                     @PathVariable("newOwnerId") long newOwner,
                                     HttpServletRequest request) {

        UserInfo ui = checkCredential(request);

        // Vérifie si l'utilisateur connecté est administrateur ou propriétaire du
        // portfolio actuel.
        if (securityManager.isAdmin(ui.userId) || portfolioManager.isOwner(ui.userId, portfolioId)) {
            return portfolioManager.changePortfolioOwner(portfolioId, newOwner);
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
    public String putPortfolioConfiguration(@RequestParam("portfolio") UUID portfolioId,
                                            @RequestParam("active") Boolean portfolioActive,
                                            HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        if (portfolioId != null && portfolioActive != null) {
            portfolioManager.changePortfolioConfiguration(portfolioId, portfolioActive, ui.userId);
        }

        return "";
    }

    /**
     * From a base portfolio, make an instance with parsed rights in the attributes.
     *
     * POST /rest/api/portfolios/instanciate/{portfolio-id}
     *
     * @param srccode            if set, rather than use the provided portfolio
     *                           uuid, search for the portfolio by code
     * @param tgtcode            code we want the portfolio to have. If code already
     *                           exists, adds a number after
     * @param copyshared         y/null Make a copy of shared nodes, rather than
     *                           keeping the link to the original data
     * @param setOwner           true/null Set the current user instanciating the
     *                           portfolio as owner. Otherwise keep the one that
     *                           created it.
     * @return instanciated portfolio uuid
     */
    @PostMapping("/instanciate/{portfolio-id}")
    public ResponseEntity<String> postInstanciatePortfolio(@RequestParam("group") int groupId,
                                                           @PathVariable("portfolio-id") String portfolioId,
                                                           @RequestParam("sourcecode") String srccode,
                                                           @RequestParam("targetcode") String tgtcode,
                                                           @RequestParam("copyshared") boolean copyshared,
                                                           @RequestParam("groupname") String groupname,
                                                           @RequestParam("owner") boolean setOwner,
                                                           HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        //// TODO: IF user is creator and has parameter owner -> change ownership
        if (!securityManager.isAdmin(ui.userId) && !securityManager.isCreator(ui.userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("403");
        }

        /// Vérifiez si le code existe, trouvez-en un qui convient, sinon. Eh.
        String newcode = tgtcode;
        int num = 0;
        while (nodeManager.isCodeExist(newcode))
            newcode = tgtcode + " (" + num++ + ")";
        tgtcode = newcode;

        String returnValue = portfolioManager.instanciatePortfolio(portfolioId, srccode,
                tgtcode, ui.userId, groupId, copyshared, groupname, setOwner);

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
     * @see #postInstanciatePortfolio(int, String,
     *      String, String, boolean, String, boolean, HttpServletRequest)
     */
    @PostMapping("/copy/{portfolio-id}")
    public ResponseEntity<String> postCopyPortfolio(@PathVariable("portfolio-id") UUID portfolioId,
                                                    @RequestParam("sourcecode") String srccode,
                                                    @RequestParam("targetcode") String tgtcode,
                                                    @RequestParam("owner") boolean setowner,
                                                    HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        //// TODO: Si l'utilisateur est créateur et est le propriétaire -> changer la
        //// propriété
        if (!securityManager.isAdmin(ui.userId) && !securityManager.isCreator(ui.userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("403");
        }

        /// Check if code exist, find a suitable one otherwise. Eh.
        // FIXME : Check original Karuta version ; no new code found.
        if (nodeManager.isCodeExist(tgtcode)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("code exist");
        }

        String returnValue = portfolioManager
                .copyPortfolio(portfolioId, srccode, tgtcode, ui.userId, setowner)
                .toString();

        return ResponseEntity.ok().body(returnValue);
    }

    /**
     * Return a list of portfolio shared to a user.
     *
     * GET /portfolios/shared/{userid}
     */
    @PostMapping(value = "/shared/{userid}", produces = "application/xml")
    public HttpEntity<PortfolioList> getPortfolioShared(@PathVariable("userid") long userid,
                                                        HttpServletRequest request) {
        UserInfo ui = checkCredential(request);

        if (securityManager.isAdmin(ui.userId)) {
            return new HttpEntity<>(portfolioManager.getPortfolioShared(userid));
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    /**
     * GET /portfolios/zip ? portfolio={}, toujours avec files zip sépares zip des
     * zip Fetching multiple portfolio in a zip. <br>
     * GET /rest/api/portfolios
     *
     * @param portfolioList      list of portfolios, separated with ','
     * @return zipped portfolio (with files) inside zip file
     */
    @GetMapping(value = "/zip", consumes = "application/zip")
    public Object getPortfolioZip(@RequestParam("portfolios") String portfolioList,
                                  @RequestParam("lang") String lang,
                                  HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);

        List<UUID> uuids = Arrays.asList(portfolioList.split(","))
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<File> files = new ArrayList<>();

        /// Suppose the first portfolio has the right name to be used
        String name = "";

        /// Create all the zip files
        for (UUID portfolioId : uuids) {
            PortfolioDocument portfolio = portfolioManager.getPortfolio(portfolioId, ui.userId, 0L,
                    this.label, true, false, ui.subId, null);

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

            files.add(getZipFile(portfolio, lang, ui.userId));
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
    public String postPortfolioZip(@RequestParam("group") long groupId,
                                   @RequestParam("fileupload") InputStream fileInputStream,
                                   @RequestParam("model") String modelId,
                                   @RequestParam("instance") String instance,
                                   @RequestParam("project") String projectName,
                                   HttpServletRequest request)
            throws BusinessException, IOException {

        UserInfo ui = checkCredential(request);
        javax.servlet.ServletContext servletContext = request.getSession().getServletContext();
        String path = servletContext.getRealPath("/");

        final String userName = ui.User;

        boolean instantiate = false;
        if ("true".equals(instance))
            instantiate = true;

        return portfolioManager
                .importZippedPortfolio(path, userName, fileInputStream, ui.userId, groupId, modelId,
                        ui.subId, instantiate, projectName);
    }

    /**
     * Delete portfolio.
     *
     * DELETE /rest/api/portfolios/portfolio/{portfolio-id}
     */
    @DeleteMapping(value = "/portfolio/{portfolio-id}", produces = "application/xml")
    public String deletePortfolio(@RequestParam("group") long groupId,
                                  @PathVariable("portfolio-id") UUID portfolioId,
                                  HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        portfolioManager.removePortfolio(portfolioId, ui.userId, groupId);

        return "";
    }

    /**
     * As a form, import xml into the database.
     *
     * POST /rest/api/portfolios
     *
     * @param modelId            another uuid, not sure why it's here
     * @param instance           true/null if as an instance, parse rights.
     *                           Otherwise just write nodes xml: ASM format
     * @return <portfolios> <portfolio id="uuid"/> </portfolios>
     */
    @PostMapping(consumes = {"multipart/form-data", "application/xml"}, produces = "application/xml")
    public HttpEntity<PortfolioList> postPortfolio(@RequestBody PortfolioDocument portfolio,
                                                   @RequestParam("group") int groupId,
                                                   @RequestParam("model") UUID modelId,
                                                   @RequestParam("instance") boolean instance,
                                                   @RequestParam("project") String projectName,
                                                   HttpServletRequest request) throws BusinessException, JsonProcessingException {

        UserInfo ui = checkCredential(request);

        return new HttpEntity<>(portfolioManager.addPortfolio(portfolio, ui.userId, groupId, modelId,
                ui.subId, instance, projectName));
    }

    /**
     * Import zip file.
     *
     * POST /rest/api/portfolios/zip
     */
    @PostMapping(value = "/zip2", consumes = "multipart/form-data", produces = "text/plain")
    public String postPortfolioByForm(@RequestParam("group") long groupId,
                                      @RequestParam("model") String modelId,
                                      @RequestParam("uploadfile") InputStream uploadedInputStream,
                                      @RequestParam("instance") String instance,
                                      @RequestParam("project") String projectName,
                                      HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        boolean instantiate = false;
        if ("true".equals(instance))
            instantiate = true;

        javax.servlet.ServletContext servletContext = request.getSession().getServletContext();
        String path = servletContext.getRealPath("/");

        final Long credentialId = ui.userId;
        final String userName = ui.User;

        return portfolioManager
                .importZippedPortfolio(path, userName, uploadedInputStream, credentialId, groupId, modelId,
                        ui.subId, instantiate, projectName);
    }
}
