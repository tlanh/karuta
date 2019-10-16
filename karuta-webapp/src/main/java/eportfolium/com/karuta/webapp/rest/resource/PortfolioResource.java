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
import org.apache.http.impl.client.HttpClientBuilder;
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
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.DomUtils;
import eportfolium.com.karuta.webapp.util.javaUtils;

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
	 * Get a portfolio from uuid GET /rest/api/portfolios/portfolio/{portfolio-id}
	 * resources: - files: if set with resource, return a zip file - export: if set,
	 * return xml as a file download return: zip as file download content
	 * 
	 * <?xml version=\"1.0\" encoding=\"UTF-8\"?> <portfolio code=\"0\"
	 * id=\""+portfolioUuid+"\" owner=\""+isOwner+"\"><version>4</version> <asmRoot>
	 * <asm*> <metadata-wad></metadata-wad> <metadata></metadata>
	 * <metadata-epm></metadata-epm> <asmResource xsi_type="nodeRes">
	 * <asmResource xsi_type="context"> <asmResource xsi_type="SPECIFIC TYPE">
	 * </asm*> </asmRoot> </portfolio>
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
	 * @param files
	 * @param export
	 * @param lang
	 * @param cutoff
	 * @return
	 */
	@Path("/portfolios/portfolio/{portfolio-id}")
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

//		Credential ui = checkCredential(httpServletRequest, user, token, null);

		Credential ui = new Credential(); // FIXME

		try {
			String portfolio = portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.getId(), 0L,
					this.label, resource, "", ui.getCredentialSubstitution().getCredentialSubstitutionId(), cutoff)
					.toString();

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

		/// Write xml file to zip
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
	 * Return the portfolio from its code <br>
	 * GET /rest/api/portfolios/code/{code}
	 * 
	 * @see 'content' of "GET /rest/api/portfolios/portfolio/{portfolio-id}"
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
	@Path("/portfolios/portfolio/code/{code}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Object getPortfolioByCode(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("code") String code, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId, @QueryParam("group") Integer group,
			@QueryParam("resources") String resources) {
//		Credential ui = checkCredential(httpServletRequest, user, token, null);
		Credential ui = new Credential(); // FIXME

		try {
			if (ui.getId() == 0) {
				return Response.status(Status.FORBIDDEN).build();
			}

			if (resources == null)
				resources = "false";
			String returnValue = portfolioManager.getPortfolioByCode(MimeTypeUtils.TEXT_XML, code, ui.getId(), groupId,
					resources, ui.getCredentialSubstitution().getCredentialSubstitutionId()).toString();
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
	 * removed) GET /rest/api/portfolios parameters: - active: false/0 (also show
	 * inactive portoflios) - code - n: number of results (10<n<50) - i: index start
	 * + n - userid: for this user (only with root) return: <?xml version=\"1.0\"
	 * encoding=\"UTF-8\"?> <portfolios>
	 * <portfolio id="uuid" root_node_id="uuid" owner="Y/N" ownerid="uid" modified=
	 * "DATE"> <asmRoot id="uuid"> <metadata-wad/> <metadata-epm/> <metadata/>
	 * <code></code> <label/> <description/> <semanticTag/>
	 * <asmResource xsi_type="nodeRes"></asmResource>
	 * <asmResource xsi_type="context"/> </asmRoot> </portfolio> ... </portfolios>
	 **/
	@Path("/portfolios")
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

//		Credential ui = checkCredential(httpServletRequest, user, token, null);

		Credential ui = new Credential(); // FIXME

		try {
			if (portfolioUuid != null) {
				String returnValue = portfolioManager
						.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.getId(), groupId, this.label, null,
								null, ui.getCredentialSubstitution().getCredentialSubstitutionId(), cutoff)
						.toString();
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
					returnValue = portfolioManager.getPortfolioByCode(MimeTypeUtils.TEXT_XML, portfolioCode, ui.getId(),
							groupId, null, ui.getCredentialSubstitution().getCredentialSubstitutionId()).toString();
				} else {
					if (public_var != null) {
						long publicid = userManager.getUserId("public");
						returnValue = portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, publicid, groupId,
								portfolioActive, 0, portfolioProject, portfolioProjectId, countOnly, search).toString();
					} else if (userId != null && securityManager.isAdmin(ui.getId())) {
						returnValue = portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, userId, groupId,
								portfolioActive, ui.getCredentialSubstitution().getCredentialSubstitutionId(),
								portfolioProject, portfolioProjectId, countOnly, search).toString();
					} else /// For user logged in
					{
						returnValue = portfolioManager.getPortfolios(MimeTypeUtils.TEXT_XML, ui.getId(), groupId,
								portfolioActive, ui.getCredentialSubstitution().getCredentialSubstitutionId(),
								portfolioProject, portfolioProjectId, countOnly, search).toString();
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
	 * Rewrite portfolio content PUT /rest/api/portfolios/portfolios/{portfolio-id}
	 * parameters: content see GET /rest/api/portfolios/portfolio/{portfolio-id}
	 * and/or the asm format return:
	 **/
	@Path("/portfolios/portfolio/{portfolio-id}")
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

//		Credential ui = checkCredential(httpServletRequest, user, token, null);
		Credential ui = new Credential(); // FIXME

		Boolean portfolioActive;
		if ("false".equals(active) || "0".equals(active))
			portfolioActive = false;
		else
			portfolioActive = true;

		try {
			portfolioManager.putPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, xmlPortfolio, portfolioUuid,
					ui.getId(), portfolioActive, groupId, null);

			return "";

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Reparse portfolio rights POST
	 * /rest/api/portfolios/portfolios/{portfolio-id}/parserights parameters:
	 * return:
	 **/
	@Path("/portfolios/portfolio/{portfolio-id}/parserights")
	@POST
	public Response postPortfolio(@PathParam("portfolio-id") String portfolioUuid, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
//		Credential ui = checkCredential(httpServletRequest, null, null, null);
		Credential cr = new Credential(); // FIXME

		try {
			if (!securityManager.isAdmin(cr.getId()))
				return Response.status(Status.FORBIDDEN).build();

			portfolioManager.postPortfolioParserights(portfolioUuid, cr.getId());

			return Response.status(Status.OK).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Change portfolio owner PUT
	 * /rest/api/portfolios/portfolios/{portfolio-id}/setOwner/{newOwnerId}
	 * parameters: - portfolio-id - newOwnerId return:
	 **/
	@Path("/portfolios/portfolio/{portfolio-id}/setOwner/{newOwnerId}")
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String putPortfolioOwner(String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @PathParam("portfolio-id") String portfolioUuid,
			@PathParam("newOwnerId") long newOwner, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
//		Credential ui = checkCredential(httpServletRequest, user, token, null);
		Credential cr = new Credential(); // FIXME
		boolean retval = false;

		try {
			// Check if logged user is either admin, or owner of the current portfolio
			if (securityManager.isAdmin(cr.getId()) || portfolioManager.isOwner(cr.getId(), portfolioUuid)) {
				retval = portfolioManager.changePortfolioOwner(portfolioUuid, newOwner);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return Boolean.toString(retval);
	}

	/**
	 * Modify some portfolio option PUT
	 * /rest/api/portfolios/portfolios/{portfolio-id}
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
	@Path("/portfolios")
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

//		Credential ui = checkCredential(httpServletRequest, user, token, null); 
		Credential ui = new Credential(); // FIXME

		try {
			String returnValue = "";
			if (portfolioUuid != null && portfolioActive != null) {
				portfolioManager.changePortfolioConfiguration(portfolioUuid, portfolioActive, ui.getId());
			}
			return returnValue;
		} catch (BusinessException ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * From a base portfolio, make an instance with parsed rights in the attributes
	 * POST /rest/api/portfolios/instanciate/{portfolio-id} parameters: -
	 * sourcecode: if set, rather than use the provided portfolio uuid, search for
	 * the portfolio by code - targetcode: code we want the portfolio to have. If
	 * code already exists, adds a number after - copyshared: y/null Make a copy of
	 * shared nodes, rather than keeping the link to the original data - owner:
	 * true/null Set the current user instanciating the portfolio as owner.
	 * Otherwise keep the one that created it.
	 *
	 * return: instanciated portfolio uuid
	 **/
	@Path("/portfolios/instanciate/{portfolio-id}")
	@POST
	public Object postInstanciatePortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("portfolio-id") String portfolioId, @QueryParam("sourcecode") String srccode,
			@QueryParam("targetcode") String tgtcode, @QueryParam("copyshared") String copy,
			@QueryParam("groupname") String groupname, @QueryParam("owner") String setowner) {

//		Credential ui = checkCredential(httpServletRequest, user, token, null);
		Credential ui = new Credential(); // FIXME

		//// TODO: IF user is creator and has parameter owner -> change ownership
		try {
			if (!securityManager.isAdmin(ui.getId()) && !securityManager.isCreator(ui.getId())) {
				return Response.status(Status.FORBIDDEN).entity("403").build();
			}

			boolean setOwner = false;
			if ("true".equals(setowner))
				setOwner = true;
			boolean copyshared = false;
			if ("y".equalsIgnoreCase(copy))
				copyshared = true;

			/// Check if code exist, find a suitable one otherwise. Eh.
			String newcode = tgtcode;
			int num = 0;
			while (nodeManager.isCodeExist(null, newcode))
				newcode = tgtcode + " (" + num++ + ")";
			tgtcode = newcode;

			String returnValue = portfolioManager.postInstanciatePortfolio(MimeTypeUtils.TEXT_XML, portfolioId, srccode,
					tgtcode, ui.getId(), groupId, copyshared, groupname, setOwner).toString();

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
	 * From a base portfolio, just make a direct copy without rights parsing POST
	 * /rest/api/portfolios/copy/{portfolio-id} parameters: Same as in instanciate
	 * return: Same as in instanciate
	 **/
	@Path("/portfolios/copy/{portfolio-id}")
	@POST
	public Response postCopyPortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("portfolio-id") String portfolioId, @QueryParam("sourcecode") String srccode,
			@QueryParam("targetcode") String tgtcode, @QueryParam("owner") String setowner) {

		String value = "Instanciate: " + portfolioId;

		Credential ui = checkCredential(httpServletRequest, user, token, null);

		//// TODO: IF user is creator and has parameter owner -> change ownership
		try {
			if (!securityManager.isAdmin(ui.getId()) && !securityManager.isCreator(ui.getId())) {
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
					.copyPortfolio(MimeTypeUtils.TEXT_XML, portfolioId, srccode, tgtcode, ui.getId(), setOwner)
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
	 * As a form, import xml into the database POST /rest/api/portfolios parameters:
	 * return:
	 **/
	@Path("/portfolios")
	@POST
//	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
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
	 * As a form, import xml into the database POST /rest/api/portfolios parameters:
	 * - model: another uuid, not sure why it's here - srce: sakai/null Need to be
	 * logged in on sakai first - srceurl: url part of the sakai system to fetch -
	 * xsl: filename when using with sakai source, convert data before importing it
	 * - instance: true/null if as an instance, parse rights. Otherwise just write
	 * nodes xml: ASM format return: <portfolios> <portfolio id="uuid"/>
	 * </portfolios>
	 **/
	@Path("/portfolios")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_XML)
	public String postPortfolio(String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId,
			@QueryParam("model") String modelId, @QueryParam("srce") String srceType,
			@QueryParam("srceurl") String srceUrl, @QueryParam("xsl") String xsl,
			@QueryParam("instance") String instance, @QueryParam("project") String projectName) {

		Credential ui = checkCredential(httpServletRequest, user, token, null); // FIXME

		if ("sakai".equals(srceType)) {
			/// Session Sakai
			HttpSession session = httpServletRequest.getSession(false);
			if (session != null) {
				String sakai_session = (String) session.getAttribute("sakai_session");
				String sakai_server = (String) session.getAttribute("sakai_server"); // Base server
																						// http://localhost:9090
				HttpClient client = HttpClientBuilder.create().build();

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
					InputStream rstream = response.getEntity().getContent();
					String sakaiData = IOUtils.toString(rstream, "UTF-8");

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

		boolean instantiate = false;
		if ("true".equals(instance))
			instantiate = true;

		try {
			String returnValue = portfolioManager.addPortfolio(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML,
					xmlPortfolio, ui.getId(), groupId, modelId,
					ui.getCredentialSubstitution().getCredentialSubstitutionId(), instantiate, projectName).toString();
			return returnValue;
		} catch (RestWebApplicationException ex) {
			logger.debug("status" + ex.getResponse().getStatus() + ex.getCustomMessage());
			throw new RestWebApplicationException(ex.getStatus(), ex.getCustomMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.debug(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Return a list of portfolio shared to a user GET /portfolios/shared/{userid}
	 * parameters: return:
	 **/
	@Path("/portfolios/shared/{userid}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public Response getPortfolioShared(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@PathParam("userid") long userid) {
		Credential uinfo = checkCredential(httpServletRequest, user, token, null);

		try {
			if (securityManager.isAdmin(uinfo.getId())) {
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
	 * GET /portfolios/zip ? portfolio={}, toujours avec files zip separes zip des
	 * zip Fetching multiple portfolio in a zip GET /rest/api/portfolios
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
	@Path("/portfolios/zip")
	@GET
	@Consumes("application/zip") // Envoie donnee brut
	public Object getPortfolioZip(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("portfolios") String portfolioList, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId,
			@QueryParam("model") String modelId, @QueryParam("instance") String instance,
			@QueryParam("lang") String lang) {
		Credential ui = checkCredential(httpServletRequest, user, token, null); // FIXME

		try {
			HttpSession session = httpServletRequest.getSession(false);
			String[] list = portfolioList.split(",");
			File[] files = new File[list.length];

			/// Suppose the first portfolio has the right name to be used
			String name = "";

			/// Create all the zip files
			for (int i = 0; i < list.length; ++i) {
				String portfolioUuid = list[i];
				String portfolio = portfolioManager.getPortfolio(MimeTypeUtils.TEXT_XML, portfolioUuid, ui.getId(), 0L,
						this.label, "true", "", ui.getCredentialSubstitution().getCredentialSubstitutionId(), null)
						.toString();

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

				/// Write xml file to zip
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

			// And the over-arching zip
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
	 * As a form, import zip, extract data and put everything into the database POST
	 * /rest/api/portfolios parameters: zip: From a zip export of the system return:
	 * portfolio uuid
	 **/
	@Path("/portfolios/zip")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Consumes("application/zip")	// Envoie donnee brut
	public String postPortfolioZip(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @FormDataParam("fileupload") InputStream fileInputStream,
			@QueryParam("user") Integer userId, @QueryParam("model") String modelId,
			@FormDataParam("instance") String instance, @FormDataParam("project") String projectName) {
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		javax.servlet.ServletContext servletContext = httpServletRequest.getSession().getServletContext();
		String path = servletContext.getRealPath("/");

		final String userName = ui.getLogin();

		try {
			boolean instantiate = false;
			if ("true".equals(instance))
				instantiate = true;
			String returnValue = portfolioManager.postPortfolioZip(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, path,
					userName, fileInputStream, ui.getId(), groupId, modelId,
					ui.getCredentialSubstitution().getCredentialSubstitutionId(), instantiate, projectName).toString();
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
	 * Delete portfolio DELETE /rest/api/portfolios/portfolio/{portfolio-id}
	 * parameters: return:
	 **/
	@Path("/portfolios/portfolio/{portfolio-id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public String deletePortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("user") Integer userId) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = checkCredential(httpServletRequest, user, token, null); // FIXME
		try {
			portfolioManager.deletePortfolio(portfolioUuid, ui.getId(), groupId);
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

	/******************************
	 * PORTFOLIO GROUP METHODS
	 ******************************/

	/**
	 * Create a new portfolio group POST /rest/api/portfoliogroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupname          Name of the group we are creating
	 * @param type               group/portfolio
	 * @param parent             parentid
	 * @return groupid
	 */
	@Path("/portfoliogroups")
	@POST
	public Response postPortfolioGroup(@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("label") String groupname, @QueryParam("type") String type, @QueryParam("parent") Long parent) {
		Credential ui = checkCredential(httpServletRequest, null, null, null); // FIXME
		Long response = -1L;

		// Check type value
		try {
			response = portfolioManager.createPortfolioGroup(groupname, type, parent, ui.getId());
			logger.debug("Portfolio group " + groupname + " created");

			if (response == -1) {
				return Response.status(Status.NOT_MODIFIED).entity("Error in creation").build();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return Response.ok(Long.toString(response)).build();
	}

	/**
	 * Put a portfolio in portfolio group PUT /rest/api/portfoliogroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param group              group id
	 * @param uuid               portfolio id
	 * @param label
	 * @return Code 200
	 */
	@Path("/portfoliogroups")
	@PUT
	public Response putPortfolioInPortfolioGroup(@Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("group") Long group,
			@QueryParam("uuid") String uuid, @QueryParam("label") String label) {
		Credential ui = checkCredential(httpServletRequest, null, null, null);

		try {
			int response = -1;
			response = portfolioManager.addPortfolioInGroup(uuid, group, label, ui.getId()); // FIXME
			logger.debug("Portfolio added  in group " + label);
			return Response.ok(Integer.toString(response)).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get portfolio by portfoliogroup, or if there's no group id give, give the
	 * list of portfolio group GET /rest/api/portfoliogroups<br>
	 * 
	 * - Without group id <groups> <group id={groupid}> <label>{group name}</label>
	 * </group> ... </groups>
	 *
	 * - With group id <group id={groupid}> <portfolio id={uuid}></portfolio> ...
	 * </group>
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param group              group id
	 * @param portfolioUuid
	 * @param groupLabel         group label
	 * @return group id or empty str if group id not found
	 */
	@Path("/portfoliogroups")
	@GET
	public String getPortfolioByPortfolioGroup(@Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("group") Long group,
			@QueryParam("uuid") String portfolioUuid, @QueryParam("label") String groupLabel) {
		Credential ui = checkCredential(httpServletRequest, null, null, null);
		String xmlUsers = "";

		try {
			if (groupLabel != null) {
				Long groupid = portfolioManager.getPortfolioGroupIdFromLabel(groupLabel, ui.getId());
				if (groupid == -1) {
					throw new RestWebApplicationException(Status.NOT_FOUND, "");
				}
				xmlUsers = Long.toString(groupid);
			} else if (portfolioUuid != null) {
				xmlUsers = portfolioManager.getPortfolioGroupListFromPortfolio(portfolioUuid);
			} else if (group == null)
				xmlUsers = portfolioManager.getPortfolioGroupList();
			else
				xmlUsers = portfolioManager.getPortfolioByPortfolioGroup(group);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return xmlUsers;
	}

	/**
	 * Remove a portfolio from a portfolio group, or remove a portfoliogroup DELETE
	 * /rest/api/portfoliogroups
	 * 
	 * @param sc
	 * @param httpServletRequest
	 * @param groupId            group id
	 * @param uuid               portfolio id
	 * @return Code 200
	 */
	@Path("/portfoliogroups")
	@DELETE
	public String deletePortfolioByPortfolioGroup(@Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("group") long groupId,
			@QueryParam("uuid") String uuid) {
//		checkCredential(httpServletRequest, null, null, null); //FIXME
		boolean response = false;
		try {
			if (uuid == null)
				response = portfolioManager.deletePortfolioGroups(groupId);
			else
				response = portfolioManager.deletePortfolioFromPortfolioGroups(uuid, groupId);

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
		return String.valueOf(response);
	}

	/**
	 * As a form, import xml into the database POST /rest/api/portfolios
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
	@Path("/portfolios")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public String postPortfolio(String xmlPortfolio, @CookieParam("user") String user,
			@CookieParam("credential") String token, @QueryParam("group") int groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Integer userId,
			@QueryParam("model") String modelId, @QueryParam("srce") String srceType,
			@QueryParam("srceurl") String srceUrl, @QueryParam("xsl") String xsl,
			@QueryParam("instance") String instance, @QueryParam("project") String projectName) {

		Credential ui = checkCredential(httpServletRequest, user, token, null); // FIXME

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
					xmlPortfolio, ui.getId(), groupId, modelId,
					ui.getCredentialSubstitution().getCredentialSubstitutionId(), instantiate, projectName).toString();
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
	 * Import zip file POST /rest/api/portfolios/zip parameters: return:
	 **/
	@Path("/portfolios/zip")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String postPortfolioByForm(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @QueryParam("user") Long userId,
			@QueryParam("model") String modelId, @FormDataParam("uploadfile") InputStream uploadedInputStream,
			@FormDataParam("instance") String instance, @FormDataParam("project") String projectName) {
		Credential ui = checkCredential(httpServletRequest, user, token, null); // FIXME
		String returnValue = "";

		boolean instantiate = false;
		if ("true".equals(instance))
			instantiate = true;

		javax.servlet.ServletContext servletContext = httpServletRequest.getSession().getServletContext();
		String path = servletContext.getRealPath("/");

		final Long credentialId = ui.getId();
		final String userName = ui.getLogin();

		try {
			returnValue = portfolioManager
					.postPortfolioZip(MimeTypeUtils.TEXT_XML, MimeTypeUtils.TEXT_XML, path, userName,
							uploadedInputStream, credentialId, groupId, modelId,
							ui.getCredentialSubstitution().getCredentialSubstitutionId(), instantiate, projectName)
					.toString();
		} catch (RestWebApplicationException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnValue;
	}

	/**
	 * Fetch a role in a portfolio <br>
	 * GET /rest/api/roles/portfolio/{portfolio-id} parameters: return:
	 **/
	@Path("/roles/portfolio/{portfolio-id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public String getRolePortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @QueryParam("role") String role,
			@PathParam("portfolio-id") String portfolioId, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept) {
		if (!isUUID(portfolioId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			String returnValue = portfolioManager
					.findRoleByPortfolio(MimeTypeUtils.TEXT_XML, role, portfolioId, ui.getId()).toString();
			return returnValue;
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

	}

	/**
	 * Get roles in a portfolio <br>
	 * GET /rest/api/groups/{portfolio-id}
	 * 
	 * @param user
	 * @param token
	 * @param groupId
	 * @param portfolioUuid
	 * @param sc
	 * @param httpServletRequest
	 * @return
	 */
	@Path("/groups/{portfolio-id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupsPortfolio(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @PathParam("portfolio-id") String portfolioUuid,
			@Context ServletConfig sc, @Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolioUuid)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = checkCredential(httpServletRequest, user, token, null);
		try {
			return portfolioManager.findRolesByPortfolio(portfolioUuid, ui.getId());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Get role list from portfolio from uuid GET /rest/api/groupRightsInfos
	 * parameters: - portfolioId: portfolio uuid return: <groupRightsInfos>
	 * <groupRightInfo grid="grouprightid"> <label></label> <owner>UID</owner>
	 * </groupRightInfo> </groupRightsInfos>
	 **/
	@Path("/groupRightsInfos")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public String getGroupRightsInfos(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") int groupId, @Context ServletConfig sc, @Context HttpServletRequest httpServletRequest,
			@QueryParam("portfolioId") String portfolioId) {
		if (!isUUID(portfolioId)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}
		Credential ui = checkCredential(httpServletRequest, user, token, null);

		try {
			return portfolioManager.getGroupRightsInfos(ui.getId(), portfolioId);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	/**
	 * Add a role in the portfolio <br>
	 * POST /rest/api/rolerightsgroups/{portfolio-id}
	 * 
	 **/
	@Path("/rolerightsgroups/{portfolio-id}")
	@POST
	@Produces(MediaType.APPLICATION_XML)
	public String postRightGroups(String xmlNode, @CookieParam("user") String user,
			@CookieParam("credential") String token, @CookieParam("group") String group,
			@PathParam("portfolio-id") String portfolio, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest) {
		if (!isUUID(portfolio)) {
			throw new RestWebApplicationException(Status.BAD_REQUEST, "Not UUID");
		}

		Credential ui = checkCredential(httpServletRequest, user, token, group);
		String returnValue = "";
		try {
			returnValue = portfolioManager.addRoleInPortfolio(ui.getId(), portfolio, xmlNode);

			if (returnValue == "faux") {
				throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'avez pas les droits d'acces");
			}

			return returnValue;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage() + "\n\n" + javaUtils.getCompleteStackTrace(ex));
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
