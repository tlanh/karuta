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


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.webapp.util.UserInfo;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.util.DomUtils;

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

    @InjectLogger
    static private Logger logger;

    /**
     * Get a portfolio from uuid. <br>
     * GET /rest/api/portfolios/portfolio/{portfolio-id}
     *
     * @param portfolioId
     * @param resource
     * @param files              if set with resource, return a zip file
     * @param export             if set, return XML as a file download
     * @param lang
     * @param cutoff
     * @param request
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
    public Object getPortfolio(@PathVariable("portfolio-id") UUID portfolioId,
                               @RequestParam("resources") String resource,
                               @RequestParam("files") String files,
                               @RequestParam("export") String export,
                               @RequestParam("lang") String lang,
                               @RequestParam("level") Integer cutoff,
                               HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        String portfolio = portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioId, ui.userId, 0L,
                this.label, resource, "", ui.subId, cutoff);

        /// Finding back code. Not really pretty
        Date time = new Date();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HHmmss");
        String timeFormat = dt.format(time);
        Document doc = DomUtils.xmlString2Document(portfolio, new StringBuffer());
        NodeList codes = doc.getDocumentElement().getElementsByTagName("code");
        // Le premier c'est celui du root
        Node codenode = codes.item(0);
        String code = "";
        if (codenode != null)
            code = codenode.getTextContent();
        // Sanitize code
        code = code.replace("_", "");

        if (export != null) {
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename = \"" + code + "-" + timeFormat + ".xml\"")
                    .body(portfolio);
        } else if (resource != null && files != null) {
            //// Cas du renvoi d'un ZIP
            HttpSession session = request.getSession(true);
            File tempZip = getZipFile(portfolioId, portfolio, lang, doc, session);

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
                    .build();
        } else {
            return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                        .body(portfolio);
        }

    }

    private File getZipFile(UUID portfolioId, String portfolioContent, String lang, Document doc,
                            HttpSession session) throws IOException, XPathExpressionException {

        /// Temp file in temp directory
        File tempDir = new File(System.getProperty("java.io.tmpdir", null));
        File tempZip = File.createTempFile(portfolioId.toString(), ".zip", tempDir);

        FileOutputStream fos = new FileOutputStream(tempZip);
        ZipOutputStream zos = new ZipOutputStream(fos);

        /// Write XML file to zip
        ZipEntry ze = new ZipEntry(portfolioId.toString() + ".xml");
        zos.putNextEntry(ze);

        byte[] bytes = portfolioContent.getBytes("UTF-8");
        zos.write(bytes);

        zos.closeEntry();

        /// Find all fileid/filename
        XPath xPath = XPathFactory.newInstance().newXPath();
        String filterRes = "//*[local-name()='asmResource']/*[local-name()='fileid' and text()]";
        NodeList nodelist = (NodeList) xPath.compile(filterRes).evaluate(doc, XPathConstants.NODESET);

        /// Fetch all files
        for (int i = 0; i < nodelist.getLength(); ++i) {
            Node res = nodelist.item(i);
            /// Check if fileid has a lang
            Node langAtt = res.getAttributes().getNamedItem("lang");
            String filterName = "";
            if (langAtt != null) {
                lang = langAtt.getNodeValue();
                filterName = ".//*[local-name()='filename' and @lang='" + lang + "' and text()]";
            } else {
                filterName = ".//*[local-name()='filename' and @lang and text()]";
            }

            Node p = res.getParentNode(); // fileid -> resource
            Node gp = p.getParentNode(); // resource -> context
            Node uuidNode = gp.getAttributes().getNamedItem("id");
            String uuid = uuidNode.getTextContent();

            NodeList textList = (NodeList) xPath.compile(filterName).evaluate(p, XPathConstants.NODESET);
            String filename = "";
            if (textList.getLength() != 0) {
                Element fileNode = (Element) textList.item(0);
                filename = fileNode.getTextContent();
                lang = fileNode.getAttribute("lang"); // In case it's a general fileid, fetch first filename (which can
                // break things if nodes are not clean)
                if ("".equals(lang))
                    lang = "fr";
            }

            String backend = configurationManager.get("backendserver");
            String url = backend + "/resources/resource/file/" + uuid + "?lang=" + lang;
            HttpGet get = new HttpGet(url);

            // Transfer sessionid so that local request still get security checked
            get.addHeader("Cookie", "JSESSIONID=" + session.getId());

            // Send request
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse ret = client.execute(get);
            HttpEntity entity = ret.getEntity();

            // Put specific name for later recovery
            if ("".equals(filename))
                continue;
            int lastDot = filename.lastIndexOf(".");
            if (lastDot < 0)
                lastDot = 0;
            String filenameext = filename.substring(0); /// find extension
            int extindex = filenameext.lastIndexOf(".") + 1;
            filenameext = uuid + "_" + lang + "." + filenameext.substring(extindex);

            // Save it to zip file
            InputStream content = entity.getContent();
            ze = new ZipEntry(filenameext);
            try {
                int totalread = 0;
                zos.putNextEntry(ze);
                int inByte;
                byte[] buf = new byte[4096];
                while ((inByte = content.read(buf)) != -1) {
                    totalread += inByte;
                    zos.write(buf, 0, inByte);
                }
                System.out.println("FILE: " + filenameext + " -> " + totalread);
                content.close();
                zos.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            }
            EntityUtils.consume(entity);
            ret.close();
            client.close();
        }

        zos.close();
        fos.close();

        return tempZip;
    }

    /**
     * Return the portfolio from its code. <br>
     * GET /rest/api/portfolios/code/{code}
     *
     * @see #putPortfolio(String, UUID, String, HttpServletRequest)
     *
     * @param groupId
     * @param code
     * @param resources
     * @param request
     * @return
     */
    @GetMapping(value = "/portfolio/code/{code}", produces = {"application/json", "application/xml"})
    public Object getPortfolioByCode(@RequestParam("group") long groupId,
                                     @PathVariable("code") String code,
                                     @RequestParam("resources") String resources,
                                     HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);

        if (ui.userId == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (resources == null)
            resources = "false";

        String returnValue = portfolioManager
                .getPortfolioByCode(MimeTypeUtils.TEXT_XML, code, ui.userId, groupId, resources, ui.subId);

        if ("".equals(returnValue)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("");
        }

        return returnValue;
    }

    /**
     * List portfolios for current user (return also other things, but should be
     * removed). <br>
     * GET /rest/api/portfolios.
     *
     * @param groupId
     * @param active             false/0 (also show inactive portoflios)
     * @param userId             for this user (only with root)
     * @param code
     * @param portfolioId
     * @param cutoff
     * @param public_var
     * @param project
     * @param count
     * @param search
     * @param request
     * @return <?xml version=\"1.0\" encoding=\"UTF-8\"?> <portfolios>
     *         <portfolio id="uuid" root_node_id="uuid" owner="Y/N" ownerid="uid"
     *         modified= "DATE"> <asmRoot id="uuid"> <metadata-wad/> <metadata-epm/>
     *         <metadata/> <code></code> <label/> <description/> <semanticTag/>
     *         <asmResource xsi_type="nodeRes"></asmResource>
     *         <asmResource xsi_type="context"/> </asmRoot> </portfolio> ...
     *         </portfolios>
     */
    @GetMapping(consumes = "application/xml", produces = {"application/json", "application/xml"})
    public String getPortfolios(@RequestParam("group") long groupId,
                                @RequestParam("active") String active,
                                @RequestParam("userid") Integer userId,
                                @RequestParam("code") String code,
                                @RequestParam("portfolio") UUID portfolioId,
                                @RequestParam("level") Integer cutoff,
                                @RequestParam("public") String public_var,
                                @RequestParam("project") String project,
                                @RequestParam("count") String count,
                                @RequestParam("search") String search,
                                HttpServletRequest request) throws ParserConfigurationException, BusinessException {

        UserInfo ui = checkCredential(request);

        if (portfolioId != null) {
            return portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioId, ui.userId,
                    groupId, this.label, null, null, ui.subId, cutoff);
        } else {
            String portfolioCode = null;
            Boolean countOnly = false;
            Boolean portfolioActive;
            Boolean portfolioProject = null;
            String portfolioProjectId = null;


            if (active != null && (active.equals("false") || active.equals("0")))
                portfolioActive = false;
            else
                portfolioActive = true;

            if (project != null && (project.equals("false") || project.equals("0")))
                portfolioProject = false;
            else if (project != null && (project.equals("true") || project.equals("1")))
                portfolioProject = true;
            else if (project != null && project.length() > 0)
                portfolioProjectId = project;
            else
                portfolioProject = null;


            if (count != null && (count.equals("true") || count.equals("1")))
                countOnly = true;
            else
                countOnly = false;


            portfolioCode = code;

            if (portfolioCode != null) {
                return portfolioManager.getPortfolioByCode(MimeTypeUtils.TEXT_XML, portfolioCode, ui.userId,
                        groupId, null, ui.subId).toString();
            } else {
                if (public_var != null) {
                    long publicid = userManager.getUserId("public");

                    return portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, publicid, groupId,
                            portfolioActive, 0, portfolioProject, portfolioProjectId, countOnly, search);

                } else if (userId != null && securityManager.isAdmin(ui.userId)) {
                    return portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, userId, groupId,
                            portfolioActive, ui.subId, portfolioProject, portfolioProjectId, countOnly, search);

                } else { /// For user logged in
                    return portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, ui.userId, groupId,
                            portfolioActive, ui.subId, portfolioProject, portfolioProjectId, countOnly, search);
                }
            }
        }
    }

    /**
     * Rewrite portfolio content. <br>
     * PUT /rest/api/portfolios/portfolios/{portfolio-id}
     *
     * @param xmlPortfolio       GET /rest/api/portfolios/portfolio/{portfolio-id}
     *                           and/or the asm format
     * @param portfolioId
     * @param active
     * @param request
     * @return
     */
    @PutMapping(value = "/portfolio/{portfolio-id}", consumes = "application/xml", produces = "application/xml")
    public String putPortfolio(@RequestBody String xmlPortfolio,
                               @PathVariable("portfolio-id") UUID portfolioId,
                               @RequestParam("active") String active,
                               HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        Boolean portfolioActive;

        if ("false".equals(active) || "0".equals(active))
            portfolioActive = false;
        else
            portfolioActive = true;


        portfolioManager.rewritePortfolioContent(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, xmlPortfolio,
                portfolioId, ui.userId, portfolioActive);

        return "";
    }

    /**
     * Reparse portfolio rights. <br>
     * POST /rest/api/portfolios/portfolios/{portfolio-id}/parserights
     *
     * @param portfolioId
     * @param request
     * @return
     */
    @PostMapping("/portfolio/{portfolio-id}/parserights")
    public ResponseEntity<String> postPortfolio(@PathVariable("portfolio-id") UUID portfolioId,
                                                HttpServletRequest request) {

        UserInfo ui = checkCredential(request);

        if (!securityManager.isAdmin(ui.userId))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        portfolioManager.postPortfolioParserights(portfolioId, ui.userId);

        return ResponseEntity.ok().build();
    }

    /**
     * Change portfolio owner. <br>
     * PUT /rest/api/portfolios/portfolios/{portfolio-id}/setOwner/{newOwnerId}
     *
     * @param portfolioId      portfolio-id
     * @param newOwner           newOwnerId
     * @param request
     * @return
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
     * Modify some portfolio option. <br>
     * PUT /rest/api/portfolios/portfolios/{portfolio-id}
     *
     * @param portfolioId
     * @param portfolioActive    0/1, true/false
     * @param request
     * @return
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
     * POST /rest/api/portfolios/instanciate/{portfolio-id}
     *
     * @param groupId
     * @param portfolioId
     * @param srccode            if set, rather than use the provided portfolio
     *                           uuid, search for the portfolio by code
     * @param tgtcode            code we want the portfolio to have. If code already
     *                           exists, adds a number after
     * @param copy               y/null Make a copy of shared nodes, rather than
     *                           keeping the link to the original data
     * @param groupname
     * @param setowner           true/null Set the current user instanciating the
     *                           portfolio as owner. Otherwise keep the one that
     *                           created it.
     * @param request
     * @return instanciated portfolio uuid
     */
    @PostMapping("/instanciate/{portfolio-id}")
    public ResponseEntity<String> postInstanciatePortfolio(@RequestParam("group") int groupId,
                                                           @PathVariable("portfolio-id") String portfolioId,
                                                           @RequestParam("sourcecode") String srccode,
                                                           @RequestParam("targetcode") String tgtcode,
                                                           @RequestParam("copyshared") String copy,
                                                           @RequestParam("groupname") String groupname,
                                                           @RequestParam("owner") String setowner,
                                                           HttpServletRequest request) throws BusinessException {

        UserInfo ui = checkCredential(request);

        //// TODO: IF user is creator and has parameter owner -> change ownership
        if (!securityManager.isAdmin(ui.userId) && !securityManager.isCreator(ui.userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("403");
        }

        boolean setOwner = false;
        if ("true".equals(setowner))
            setOwner = true;
        boolean copyshared = false;
        if ("y".equalsIgnoreCase(copy))
            copyshared = true;

        /// Vérifiez si le code existe, trouvez-en un qui convient, sinon. Eh.
        String newcode = tgtcode;
        int num = 0;
        while (nodeManager.isCodeExist(newcode))
            newcode = tgtcode + " (" + num++ + ")";
        tgtcode = newcode;

        String returnValue = portfolioManager.instanciatePortfolio(MimeTypeUtils.TEXT_XML, portfolioId, srccode,
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
     * From a base portfolio, just make a direct copy without rights parsing. <br>
     * POST /rest/api/portfolios/copy/{portfolio-id}
     *
     * @see #postInstanciatePortfolio(int, String,
     *      String, String, String, String, String, HttpServletRequest)
     *
     * @param portfolioId
     * @param srccode
     * @param tgtcode
     * @param setowner
     * @param request
     * @return
     */
    @PostMapping("/copy/{portfolio-id}")
    public ResponseEntity<String> postCopyPortfolio(@PathVariable("portfolio-id") UUID portfolioId,
                                      @RequestParam("sourcecode") String srccode,
                                      @RequestParam("targetcode") String tgtcode,
                                      @RequestParam("owner") String setowner,
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

        boolean setOwner = false;

        if ("true".equals(setowner))
            setOwner = true;

        String returnValue = portfolioManager
                .copyPortfolio(MimeTypeUtils.TEXT_XML, portfolioId, srccode, tgtcode, ui.userId, setOwner)
                .toString();

        return ResponseEntity.ok().body(returnValue);
    }

    /**
     * As a form, import xml into the database. <br>
     * POST /rest/api/portfolios
     *
     * @param xmlPortfolio
     * @param groupId
     * @param modelId
     * @param srceType
     * @param srceUrl
     * @param xsl
     * @param instance
     * @param projectName
     * @param request
     * @return
     */
    @PostMapping(consumes = "multipart/form-data", produces = "application/xml")
    public String postFormPortfolio(@RequestParam("uploadfile") String xmlPortfolio,
                                    @RequestParam("group") int groupId,
                                    @RequestParam("model") UUID modelId,
                                    @RequestParam("srce") String srceType,
                                    @RequestParam("srceurl") String srceUrl,
                                    @RequestParam("xsl") String xsl,
                                    @RequestParam("instance") String instance,
                                    @RequestParam("project") String projectName,
                                    ServletConfig sc,
                                    HttpServletRequest request) throws Exception {
        return postPortfolio(xmlPortfolio, groupId, modelId, srceType,
                srceUrl, xsl, instance, projectName, sc, request);
    }

    /**
     * Return a list of portfolio shared to a user. <br>
     * GET /portfolios/shared/{userid}
     *
     * @param userid
     * @param request
     * @return
     */
    @PostMapping(value = "/shared/{userid}", produces = "application/xml")
    public ResponseEntity<String> getPortfolioShared(@PathVariable("userid") long userid,
                                                     HttpServletRequest request) {
        UserInfo ui = checkCredential(request);

        if (securityManager.isAdmin(ui.userId)) {
            return ResponseEntity.ok()
                    .body(portfolioManager.getPortfolioShared(userid));
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
     * @param lang
     * @param request
     * @return zipped portfolio (with files) inside zip file
     */
    @GetMapping(value = "/zip", consumes = "application/zip")
    public Object getPortfolioZip(@RequestParam("portfolios") String portfolioList,
                                  @RequestParam("lang") String lang,
                                  HttpServletRequest request) throws Exception {
        UserInfo ui = checkCredential(request);

        HttpSession session = request.getSession(false);

        List<UUID> uuids = Arrays.asList(portfolioList.split(","))
                            .stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList());

        List<File> files = new ArrayList<>();

        /// Suppose the first portfolio has the right name to be used
        String name = "";

        /// Create all the zip files
        for (UUID portfolioId : uuids) {
            String portfolio = portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioId, ui.userId, 0L,
                    this.label, "true", "", ui.subId, null);

            // No name yet
            if ("".equals(name)) {
                StringBuffer outTrace = new StringBuffer();
                Document doc = DomUtils.xmlString2Document(portfolio, outTrace);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String filterRes = "//*[local-name()='asmRoot']/*[local-name()='asmResource']/*[local-name()='code']";
                NodeList nodelist = (NodeList) xPath.compile(filterRes).evaluate(doc, XPathConstants.NODESET);

                if (nodelist.getLength() > 0)
                    name = nodelist.item(0).getTextContent();
            }

            Document doc = DomUtils.xmlString2Document(portfolio, new StringBuffer());

            files.add(getZipFile(portfolioId, portfolio, lang, doc, session));
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
     * <br>
     * POST /rest/api/portfolios From a zip export of the system
     *
     * @param groupId
     * @param fileInputStream
     * @param modelId
     * @param instance
     * @param projectName
     * @param request
     * @return portfolio uuid
     */
    @PostMapping(value = "/zip", consumes = "multipart/form-data")
    public String postPortfolioZip(@RequestParam("group") long groupId,
                                   @RequestParam("fileupload") InputStream fileInputStream,
                                   @RequestParam("model") String modelId,
                                   @RequestParam("instance") String instance,
                                   @RequestParam("project") String projectName,
                                   HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);
        javax.servlet.ServletContext servletContext = request.getSession().getServletContext();
        String path = servletContext.getRealPath("/");

        final String userName = ui.User;

        boolean instantiate = false;
        if ("true".equals(instance))
            instantiate = true;

        return portfolioManager
                .importZippedPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, path, userName,
                        fileInputStream, ui.userId, groupId, modelId, ui.subId, instantiate, projectName);
    }

    /**
     * Delete portfolio. <br>
     * DELETE /rest/api/portfolios/portfolio/{portfolio-id}
     *
     * @param groupId
     * @param portfolioId
     * @param request
     * @return
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
     * As a form, import xml into the database. <br>
     * POST /rest/api/portfolios
     *
     * @param xmlPortfolio
     * @param groupId
     * @param modelId            another uuid, not sure why it's here
     * @param srceType           sakai/null Need to be logged in on sakai first
     * @param srceUrl            url part of the sakai system to fetch
     * @param xsl                filename when using with sakai source, convert data
     *                           before importing it
     * @param instance           true/null if as an instance, parse rights.
     *                           Otherwise just write nodes xml: ASM format
     * @param projectName
     * @param sc
     * @param request
     * @return <portfolios> <portfolio id="uuid"/> </portfolios>
     */
    @PostMapping(consumes = "application/xml", produces = "application/xml")
    public String postPortfolio(@RequestBody String xmlPortfolio,
                                @RequestParam("group") int groupId,
                                @RequestParam("model") UUID modelId,
                                @RequestParam("srce") String srceType,
                                @RequestParam("srceurl") String srceUrl,
                                @RequestParam("xsl") String xsl,
                                @RequestParam("instance") String instance,
                                @RequestParam("project") String projectName,
                                ServletConfig sc,
                                HttpServletRequest request) throws Exception {

        UserInfo ui = checkCredential(request);

        if ("sakai".equals(srceType)) {
            /// Session Sakai
            HttpSession session = request.getSession(false);
            if (session != null) {
                String sakai_session = (String) session.getAttribute("sakai_session");
                String sakai_server = (String) session.getAttribute("sakai_server");
                // Base server - http://localhost:9090
                HttpClient client = HttpClients.createDefault();

                /// Fetch page
                HttpGet get = new HttpGet(sakai_server + "/" + srceUrl);
                Header header = new BasicHeader("JSESSIONID", sakai_session);
                get.addHeader(header);


                HttpResponse response = client.execute(get);
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != 200) {
                    System.err.println("Method failed: " + status.getStatusCode());
                }

                // Retrieve data
                InputStream retrieve = response.getEntity().getContent();
                String sakaiData = IOUtils.toString(retrieve, "UTF-8");

                //// Convert it via XSL
                /// Path to XSL
                String servletDir = sc.getServletContext().getRealPath("/");
                int last = servletDir.lastIndexOf(File.separator);
                last = servletDir.lastIndexOf(File.separator, last - 1);
                String baseDir = servletDir.substring(0, last);

                String basepath = xsl.substring(0, xsl.indexOf(File.separator));
                String firstStage = baseDir + File.separator + basepath + File.separator + "karuta" + File.separator
                        + "xsl" + File.separator + "html2xml.xsl";
                System.out.println("FIRST: " + firstStage);

                /// Storing transformed data
                StringWriter dataTransformed = new StringWriter();

                /// Apply change
                Source xsltSrc1 = new StreamSource(new File(firstStage));
                TransformerFactory transFactory = TransformerFactory.newInstance();
                Transformer transformer1 = transFactory.newTransformer(xsltSrc1);
                StreamSource stageSource = new StreamSource(new ByteArrayInputStream(sakaiData.getBytes()));
                Result stageRes = new StreamResult(dataTransformed);
                transformer1.transform(stageSource, stageRes);

                /// Result as portfolio data to be imported
                xmlPortfolio = dataTransformed.toString();
            }
        }

        boolean instantiate = false;
        if ("true".equals(instance))
            instantiate = true;

        return portfolioManager.addPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML,
                xmlPortfolio, ui.userId, groupId, modelId, ui.subId, instantiate, projectName);
    }

    /**
     * Import zip file. <br>
     * POST /rest/api/portfolios/zip
     *
     * @param groupId
     * @param modelId
     * @param uploadedInputStream
     * @param instance
     * @param projectName
     * @param request
     * @return
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
                    .importZippedPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, path, userName,
                            uploadedInputStream, credentialId, groupId, modelId, ui.subId, instantiate, projectName);
    }
}
