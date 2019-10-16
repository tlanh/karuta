package eportfolium.com.karuta.webapp.rest.resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.XML;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MimeTypeUtils;
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
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.DomUtils;
import eportfolium.com.karuta.webapp.util.javaUtils;

@Path("/portfolios")
public class PortfolioResource extends AbstractResource {

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
	private static Logger logger;

	/**
	 * Get a portfolio from uuid. <br>
	 * GET /rest/api/portfolios/portfolio/{portfolio-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param portfolioUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @param group
	 * @param resource
	 * @param files              if set with resource, return a zip file
	 * @param export             if set, return XML as a file download
	 * @param lang
	 * @param cutoff
	 * @return zip as file download content. <br>
	 *         <?xml version=\"1.0\" encoding=\"UTF-8\"?> <portfolio code=\"0\"
	 *         id=\""+portfolioUuid+"\" owner=\""+isOwner+"\"><version>4</version>
	 *         <asmRoot> <asm*> <metadata-wad></metadata-wad> <metadata></metadata>
	 *         <metadata-epm></metadata-epm> <asmResource xsi_type="nodeRes">
	 *         <asmResource xsi_type="context">
	 *         <asmResource xsi_type="SPECIFIC TYPE"> </asm*> </asmRoot>
	 *         </portfolio>
	 */

	@Path("/portfolio/{portfolio-id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/zip",
			MediaType.APPLICATION_OCTET_STREAM })
	public Object getPortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@HeaderParam("Accept") String accept, @QueryParam("user") Integer userId,
			@QueryParam("group") Integer group, @QueryParam("resources") String resource,
			@QueryParam("files") String files, @QueryParam("export") String export, @QueryParam("lang") String lang,
			@QueryParam("level") Integer cutoff) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			String portfolio = portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.userId, 0L,
					this.label, resource, "", ui.subId, cutoff).toString();

			/// Finding back code. Not really pretty
			Date time = new Date();
			Response response = null;
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
				response = Response.ok(portfolio)
						.header("content-disposition", "attachment; filename = \"" + code + "-" + timeFormat + ".xml\"")
						.build();
			} else if (resource != null && files != null) {
				//// Cas du renvoi d'un ZIP
				HttpSession session = httpServletRequest.getSession(true);
				File tempZip = getZipFile(portfolioUuid, portfolio, lang, doc, session);

				/// Return zip file
				RandomAccessFile f = new RandomAccessFile(tempZip.getAbsoluteFile(), "r");
				byte[] b = new byte[(int) f.length()];
				f.read(b);
				f.close();

				response = Response.ok(b, MediaType.APPLICATION_OCTET_STREAM)
						.header("content-disposition", "attachment; filename = \"" + code + "-" + timeFormat + ".zip")
						.build();

				// Temp file cleanup
				tempZip.delete();
			} else {
				if (accept.equals(MediaType.APPLICATION_JSON)) {
					portfolio = XML.toJSONObject(portfolio).toString();
					response = Response.ok(portfolio).type(MediaType.APPLICATION_JSON).build();
				} else
					response = Response.ok(portfolio).type(MediaType.APPLICATION_XML).build();
			}
			return response;
		} catch (DoesNotExistException ex) {
			logger.info("Portfolio " + portfolioUuid + " not found");
			throw new RestWebApplicationException(Status.NOT_FOUND, "Portfolio " + portfolioUuid + " not found");
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

	}

	private File getZipFile(String portfolioUuid, String portfolioContent, String lang, Document doc,
			HttpSession session) throws IOException, XPathExpressionException {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		/// Temp file in temp directory
		File tempDir = new File(System.getProperty("java.io.tmpdir", null));
		File tempZip = File.createTempFile(portfolioUuid, ".zip", tempDir);

		FileOutputStream fos = new FileOutputStream(tempZip);
		ZipOutputStream zos = new ZipOutputStream(fos);

		/// Write XML file to zip
		ZipEntry ze = new ZipEntry(portfolioUuid + ".xml");
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
	 * @see #putPortfolio(String, String, String, int, String, ServletConfig,
	 *      HttpServletRequest, String, Integer)
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param code
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param userId
	 * @param group
	 * @param resources
	 * @return
	 */
	@Path("/portfolio/code/{code}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Object getPortfolioByCode(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("code") String code, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId, @QueryParam("group") Integer group,
			@QueryParam("resources") String resources) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			if (ui.userId == 0) {
				return Response.status(Status.FORBIDDEN).build();
			}

			if (resources == null)
				resources = "false";
			String returnValue = portfolioManager
					.getPortfolioByCode(MimeTypeUtils.TEXT_XML, code, ui.userId, groupId, resources, ui.subId)
					.toString();
			if ("".equals(returnValue)) {
				return Response.status(Status.NOT_FOUND).entity("").build();
			}
			if (MediaType.APPLICATION_JSON.equals(accept)) // Not really used
				returnValue = XML.toJSONObject(returnValue).toString();

			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * List portfolios for current user (return also other things, but should be
	 * removed). <br>
	 * GET /rest/api/portfolios.
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param accept
	 * @param active             false/0 (also show inactive portoflios)
	 * @param userId             for this user (only with root)
	 * @param code
	 * @param portfolioUuid
	 * @param index              start + n
	 * @param numResult          number of results (10<n<50)
	 * @param cutoff
	 * @param public_var
	 * @param project
	 * @param count
	 * @param search
	 * @return <?xml version=\"1.0\" encoding=\"UTF-8\"?> <portfolios>
	 *         <portfolio id="uuid" root_node_id="uuid" owner="Y/N" ownerid="uid"
	 *         modified= "DATE"> <asmRoot id="uuid"> <metadata-wad/> <metadata-epm/>
	 *         <metadata/> <code></code> <label/> <description/> <semanticTag/>
	 *         <asmResource xsi_type="nodeRes"></asmResource>
	 *         <asmResource xsi_type="context"/> </asmRoot> </portfolio> ...
	 *         </portfolios>
	 */
	@GET
	@Consumes(MediaType.APPLICATION_XML)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getPortfolios(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("active") String active, @QueryParam("userid") Integer userId, @QueryParam("code") String code,
			@QueryParam("portfolio") String portfolioUuid, @QueryParam("i") String index,
			@QueryParam("n") String numResult, @QueryParam("level") Integer cutoff,
			@QueryParam("public") String public_var, @QueryParam("project") String project,
			@QueryParam("count") String count, @QueryParam("search") String search) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			if (portfolioUuid != null) {
				String returnValue = portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.userId,
						groupId, this.label, null, null, ui.subId, cutoff).toString();
				if (accept.equals(MediaType.APPLICATION_JSON))
					returnValue = XML.toJSONObject(returnValue).toString();

				return returnValue;

			} else {
				String portfolioCode = null;
				String returnValue = "";
				Boolean countOnly = false;
				Boolean portfolioActive;
				Boolean portfolioProject = null;
				String portfolioProjectId = null;

				try {
					if (active.equals("false") || active.equals("0"))
						portfolioActive = false;
					else
						portfolioActive = true;
				} catch (Exception ex) {
					portfolioActive = true;
				}

				try {
					if (project.equals("false") || project.equals("0"))
						portfolioProject = false;
					else if (project.equals("true") || project.equals("1"))
						portfolioProject = true;
					else if (project.length() > 0)
						portfolioProjectId = project;
				} catch (Exception ex) {
					portfolioProject = null;
				}

				try {
					if (count.equals("true") || count.equals("1"))
						countOnly = true;
					else
						countOnly = false;
				} catch (Exception ex) {
					countOnly = false;
				}

				try {
					portfolioCode = code;
				} catch (Exception ex) {
				}
				if (portfolioCode != null) {
					returnValue = portfolioManager.getPortfolioByCode(MimeTypeUtils.TEXT_XML, portfolioCode, ui.userId,
							groupId, null, ui.subId).toString();
				} else {
					if (public_var != null) {
						long publicid = userManager.getUserId("public");
						returnValue = portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, publicid, groupId,
								portfolioActive, 0, portfolioProject, portfolioProjectId, countOnly, search).toString();
					} else if (userId != null && securityManager.isAdmin(ui.userId)) {
						returnValue = portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, userId, groupId,
								portfolioActive, ui.subId, portfolioProject, portfolioProjectId, countOnly, search)
								.toString();
					} else /// For user logged in
					{
						returnValue = portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, ui.userId, groupId,
								portfolioActive, ui.subId, portfolioProject, portfolioProjectId, countOnly, search)
								.toString();
					}

					if (accept.equals(MediaType.APPLICATION_JSON))
						returnValue = XML.toJSONObject(returnValue).toString();
				}
				return returnValue;
			}
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (SQLException ex) {
			throw new RestWebApplicationException(Status.NOT_FOUND, "Portfolios  not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Rewrite portfolio content. <br>
	 * PUT /rest/api/portfolios/portfolios/{portfolio-id}
	 * 
	 * @param xmlPortfolio       GET /rest/api/portfolios/portfolio/{portfolio-id}
	 *                           and/or the asm format
	 * @param user
	 * @param token
	 * @param groupId
	 * @param portfolioUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param active
	 * @param userId
	 * @return
	 */
	@Path("/portfolio/{portfolio-id}")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putPortfolio(String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId,
			@PathParam("portfolio-id") String portfolioUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("active") String active,
			@QueryParam("user") Integer userId) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		Boolean portfolioActive;
		if ("false".equals(active) || "0".equals(active))
			portfolioActive = false;
		else
			portfolioActive = true;

		try {
			portfolioManager.putPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, xmlPortfolio, portfolioUuid,
					ui.userId, portfolioActive, groupId, null);

			return "";

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Reparse portfolio rights. <br>
	 * POST /rest/api/portfolios/portfolios/{portfolio-id}/parserights
	 * 
	 * @param portfolioUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/portfolio/{portfolio-id}/parserights")
	@POST
	public Response postPortfolio(@PathParam("portfolio-id") String portfolioUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {

		UserInfo ui = checkCredential(httpServletRequest, null, null, null);

		try {
			if (!securityManager.isAdmin(ui.userId))
				return Response.status(Status.FORBIDDEN).build();

			portfolioManager.postPortfolioParserights(portfolioUuid, ui.userId);

			return Response.status(Status.OK).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Change portfolio owner. <br>
	 * PUT /rest/api/portfolios/portfolios/{portfolio-id}/setOwner/{newOwnerId}
	 * 
	 * @param xmlPortfolio
	 * @param user
	 * @param token
	 * @param portfolioUuid      portfolio-id
	 * @param newOwner           newOwnerId
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/portfolio/{portfolio-id}/setOwner/{newOwnerId}")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putPortfolioOwner(String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @PathParam("portfolio-id") String portfolioUuid,
			@PathParam("newOwnerId") long newOwner, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		boolean retval = false;

		try {
			// Vérifie si l'utilisateur connecté est administrateur ou propriétaire du
			// portfolio actuel.
			if (securityManager.isAdmin(ui.userId) || portfolioManager.isOwner(ui.userId, portfolioUuid)) {
				retval = portfolioManager.changePortfolioOwner(portfolioUuid, newOwner);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return Boolean.toString(retval);
	}

	/**
	 * Modify some portfolio option. <br>
	 * PUT /rest/api/portfolios/portfolios/{portfolio-id}
	 * 
	 * @param xmlPortfolio
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param portfolioUuid
	 * @param portfolioActive    0/1, true/false
	 * @return
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putPortfolioConfiguration(String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("portfolio") String portfolioUuid,
			@QueryParam("active") Boolean portfolioActive) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			String returnValue = "";
			if (portfolioUuid != null && portfolioActive != null) {
				portfolioManager.changePortfolioConfiguration(portfolioUuid, portfolioActive, ui.userId);
			}
			return returnValue;
		} catch (BusinessException ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * From a base portfolio, make an instance with parsed rights in the attributes.
	 * POST /rest/api/portfolios/instanciate/{portfolio-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
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
	 * @return instanciated portfolio uuid
	 */
	@Path("/instanciate/{portfolio-id}")
	@POST
	public Object postInstanciatePortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("portfolio-id") String portfolioId, @QueryParam("sourcecode") String srccode,
			@QueryParam("targetcode") String tgtcode, @QueryParam("copyshared") String copy,
			@QueryParam("groupname") String groupname, @QueryParam("owner") String setowner) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		//// TODO: IF user is creator and has parameter owner -> change ownership
		try {
			if (!securityManager.isAdmin(ui.userId) && !securityManager.isCreator(ui.userId)) {
				return Response.status(Status.FORBIDDEN).entity("403").build();
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
			while (nodeManager.isCodeExist(null, newcode))
				newcode = tgtcode + " (" + num++ + ")";
			tgtcode = newcode;

			String returnValue = portfolioManager.postInstanciatePortfolio(MimeTypeUtils.TEXT_XML, portfolioId, srccode,
					tgtcode, ui.userId, groupId, copyshared, groupname, setOwner).toString();

			if (returnValue.startsWith("no rights"))
				throw new RestWebApplicationException(Status.FORBIDDEN, returnValue);
			else if (returnValue.startsWith("erreur"))
				throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, returnValue);
			else if ("".equals(returnValue)) {
				return Response.status(Status.NOT_FOUND).build();
			}

			return returnValue;
		} catch (RestWebApplicationException rwe) {
			throw rwe;
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * From a base portfolio, just make a direct copy without rights parsing. <br>
	 * POST /rest/api/portfolios/copy/{portfolio-id}
	 * 
	 * @see #postInstanciatePortfolio(String, String, int, ServletConfig,
	 *      HttpServletRequest, String, String, String, String, String, String)
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param portfolioId
	 * @param srccode
	 * @param tgtcode
	 * @param setowner
	 * @return
	 */
	@Path("/copy/{portfolio-id}")
	@POST
	public Response postCopyPortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("portfolio-id") String portfolioId, @QueryParam("sourcecode") String srccode,
			@QueryParam("targetcode") String tgtcode, @QueryParam("owner") String setowner) {

		String value = "Instanciate: " + portfolioId;

		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		//// TODO: IF user is creator and has parameter owner -> change ownership
		try {
			if (!securityManager.isAdmin(ui.userId) && !securityManager.isCreator(ui.userId)) {
				return Response.status(Status.FORBIDDEN).entity("403").build();
			}

			/// Check if code exist, find a suitable one otherwise. Eh.
			String newcode = tgtcode;
			if (nodeManager.isCodeExist(newcode, null)) {
				return Response.status(Status.CONFLICT).entity("code exist").build();
			}

			boolean setOwner = false;
			if ("true".equals(setowner))
				setOwner = true;
			tgtcode = newcode;

			String returnValue = portfolioManager
					.copyPortfolio(MimeTypeUtils.TEXT_XML, portfolioId, srccode, tgtcode, ui.userId, setOwner)
					.toString();
			logger.debug("Status " + Status.OK.getStatusCode() + " : " + value + " to: " + returnValue);
			return Response.status(Status.OK).entity(returnValue).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(value + " --> Error", ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));

			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * As a form, import xml into the database. <br>
	 * POST /rest/api/portfolios
	 * 
	 * @param xmlPortfolio
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @param modelId
	 * @param srceType
	 * @param srceUrl
	 * @param xsl
	 * @param instance
	 * @param projectName
	 * @return
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	public String postFormPortfolio(@FormDataParam("uploadfile") String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId,
			@QueryParam("model") String modelId, @QueryParam("srce") String srceType,
			@QueryParam("srceurl") String srceUrl, @QueryParam("xsl") String xsl,
			@FormDataParam("instance") String instance, @FormDataParam("project") String projectName) {
		return postPortfolio(xmlPortfolio, user, token, groupId, sc, httpServletRequest, userId, modelId, srceType,
				srceUrl, xsl, instance, projectName);
	}


	/**
	 * Return a list of portfolio shared to a user. <br>
	 * GET /portfolios/shared/{userid}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param userid
	 * @return
	 */
	@Path("/shared/{userid}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response getPortfolioShared(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("userid") long userid) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		try {
			if (securityManager.isAdmin(ui.userId)) {
				String res = portfolioManager.getPortfolioShared(userid);
				return Response.ok(res).build();
			} else {
				return Response.status(403).build();
			}
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * GET /portfolios/zip ? portfolio={}, toujours avec files zip sépares zip des
	 * zip Fetching multiple portfolio in a zip. <br>
	 * GET /rest/api/portfolios
	 * 
	 * @param user
	 * @param token
	 * @param portfolioList      list of portfolios, separated with ','
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @param modelId
	 * @param instance
	 * @param lang
	 * @return zipped portfolio (with files) inside zip file
	 */
	@Path("/zip")
	@GET
	@Consumes("application/zip") // Envoie des données brutes.
	public Object getPortfolioZip(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("portfolios") String portfolioList, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId,
			@QueryParam("model") String modelId, @QueryParam("instance") String instance,
			@QueryParam("lang") String lang) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null); // FIXME

		try {
			HttpSession session = httpServletRequest.getSession(false);
			String[] list = portfolioList.split(",");
			File[] files = new File[list.length];

			/// Suppose the first portfolio has the right name to be used
			String name = "";

			/// Create all the zip files
			for (int i = 0; i < list.length; ++i) {
				String portfolioUuid = list[i];
				String portfolio = portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.userId, 0L,
						this.label, "true", "", ui.subId, null).toString();

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

				files[i] = getZipFile(portfolioUuid, portfolio, lang, doc, session);

			}

			// Make a big zip of it
			File tempDir = new File(System.getProperty("java.io.tmpdir", null));
			File bigZip = File.createTempFile("project_", ".zip", tempDir);

			// Add content to it
			FileOutputStream fos = new FileOutputStream(bigZip);
			ZipOutputStream zos = new ZipOutputStream(fos);

			byte[] buffer = new byte[0x1000];

			for (int i = 0; i < files.length; ++i) {
				File file = files[i];
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

			Response response = Response.ok(b, MediaType.APPLICATION_OCTET_STREAM)
					.header("content-disposition", "attachment; filename = \"" + name + "-" + timeFormat + ".zip\"")
					.build();

			// Delete all zipped file
			for (int i = 0; i < files.length; ++i)
				files[i].delete();

			// And the over-arching zip.
			bigZip.delete();

			return response;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex), modelId,
					Status.INTERNAL_SERVER_ERROR.getStatusCode());

			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * As a form, import zip, extract data and put everything into the database.
	 * <br>
	 * POST /rest/api/portfolios From a zip export of the system
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param fileInputStream
	 * @param userId
	 * @param modelId
	 * @param instance
	 * @param projectName
	 * @return portfolio uuid
	 */
	@Path("/zip")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	// Envoie des données brutes.
	public String postPortfolioZip(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @FormDataParam("fileupload") InputStream fileInputStream,
			@QueryParam("user") Integer userId, @QueryParam("model") String modelId,
			@FormDataParam("instance") String instance, @FormDataParam("project") String projectName) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		javax.servlet.ServletContext servletContext = httpServletRequest.getSession().getServletContext();
		String path = servletContext.getRealPath("/");

		final String userName = ui.User;

		try {
			boolean instantiate = false;
			if ("true".equals(instance))
				instantiate = true;
			String returnValue = portfolioManager
					.importZippedPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, path, userName,
							fileInputStream, ui.userId, groupId, modelId, ui.subId, instantiate, projectName)
					.toString();
			return returnValue;
		} catch (RestWebApplicationException e) {
			throw e;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Delete portfolio. <br>
	 * DELETE /rest/api/portfolios/portfolio/{portfolio-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param portfolioUuid
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @return
	 */
	@Path("/portfolio/{portfolio-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deletePortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("user") Integer userId) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		try {
			portfolioManager.deletePortfolio(portfolioUuid, ui.userId, groupId);
			logger.debug("Portfolio " + portfolioUuid + " found");
			return "";
		} catch (BusinessException ex) {
			logger.debug("Portfolio " + portfolioUuid + " not found");
			throw new RestWebApplicationException(Status.NOT_FOUND, "Portfolio " + portfolioUuid + " not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.debug(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * As a form, import xml into the database. <br>
	 * POST /rest/api/portfolios
	 * 
	 * @param xmlPortfolio
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @param modelId            another uuid, not sure why it's here
	 * @param srceType           sakai/null Need to be logged in on sakai first
	 * @param srceUrl            url part of the sakai system to fetch
	 * @param xsl                filename when using with sakai source, convert data
	 *                           before importing it
	 * @param instance           true/null if as an instance, parse rights.
	 *                           Otherwise just write nodes xml: ASM format
	 * @param projectName
	 * @return <portfolios> <portfolio id="uuid"/> </portfolios>
	 */

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postPortfolio(String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId,
			@QueryParam("model") String modelId, @QueryParam("srce") String srceType,
			@QueryParam("srceurl") String srceUrl, @QueryParam("xsl") String xsl,
			@QueryParam("instance") String instance, @QueryParam("project") String projectName) {

		UserInfo ui = checkCredential(httpServletRequest, user, token, null); // FIXME

		if ("sakai".equals(srceType)) {
			/// Session Sakai
			HttpSession session = httpServletRequest.getSession(false);
			if (session != null) {
				String sakai_session = (String) session.getAttribute("sakai_session");
				String sakai_server = (String) session.getAttribute("sakai_server");
				// Base server - http://localhost:9090
				HttpClient client = HttpClients.createDefault();

				/// Fetch page
				HttpGet get = new HttpGet(sakai_server + "/" + srceUrl);
				Header header = new BasicHeader("JSESSIONID", sakai_session);
				get.addHeader(header);

				try {
					HttpResponse response = client.execute(get);
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() != HttpStatus.SC_OK) {
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
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			boolean instantiate = false;
			if ("true".equals(instance))
				instantiate = true;
			String returnValue = portfolioManager.addPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML,
					xmlPortfolio, ui.userId, groupId, modelId, ui.subId, instantiate, projectName).toString();
			return returnValue;
		} catch (BusinessException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Import zip file. <br>
	 * POST /rest/api/portfolios/zip
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param sc
	 * @param httpServletRequest
	 * @param userId
	 * @param modelId
	 * @param uploadedInputStream
	 * @param instance
	 * @param projectName
	 * @return
	 */
	@Path("/zip2")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String postPortfolioByForm(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Long userId,
			@QueryParam("model") String modelId, @FormDataParam("uploadfile") InputStream uploadedInputStream,
			@FormDataParam("instance") String instance, @FormDataParam("project") String projectName) {
		UserInfo ui = checkCredential(httpServletRequest, user, token, null); // FIXME
		String returnValue = "";

		boolean instantiate = false;
		if ("true".equals(instance))
			instantiate = true;

		javax.servlet.ServletContext servletContext = httpServletRequest.getSession().getServletContext();
		String path = servletContext.getRealPath("/");

		final Long credentialId = ui.userId;
		final String userName = ui.User;

		try {
			returnValue = portfolioManager
					.importZippedPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, path, userName,
							uploadedInputStream, credentialId, groupId, modelId, ui.subId, instantiate, projectName)
					.toString();
		} catch (RestWebApplicationException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnValue;
	}

}
