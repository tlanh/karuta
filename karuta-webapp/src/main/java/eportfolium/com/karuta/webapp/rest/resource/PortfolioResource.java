package eportfolium.com.karuta.webapp.rest.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimeType;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.XML;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.webapp.annotation.InjectLogger;
import eportfolium.com.karuta.webapp.rest.provider.mapper.exception.RestWebApplicationException;
import eportfolium.com.karuta.webapp.util.DomUtils;

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

	@InjectLogger
	private static Logger logger;

	/**
	 * Get a portfolio from uuid GET /rest/api/portfolios/portfolio/{portfolio-id}
	 * parameters: - resources: - files: if set with resource, return a zip file -
	 * export: if set, return xml as a file download return: zip as file download
	 * content <?xml version=\"1.0\" encoding=\"UTF-8\"?> <portfolio code=\"0\"
	 * id=\""+portfolioUuid+"\" owner=\""+isOwner+"\"><version>4</version> <asmRoot>
	 * <asm*> <metadata-wad></metadata-wad> <metadata></metadata>
	 * <metadata-epm></metadata-epm> <asmResource xsi_type="nodeRes">
	 * <asmResource xsi_type="context"> <asmResource xsi_type="SPECIFIC TYPE">
	 * </asm*> </asmRoot> </portfolio>
	 **/
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

//		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		Credential ui = new Credential(); // FIXME

		Response response = null;
		try {
			String portfolio = portfolioManager.getPortfolio(new MimeType("text/xml"), portfolioUuid, ui.getId(), 0L,
					this.label, resource, "", ui.getCredentialSubstitution().getCredentialSubstitutionId(), cutoff)
					.toString();

			if ("faux".equals(portfolio)) {
				response = Response.status(403).build();
			}

			if (response == null) {
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
					response = Response.ok(portfolio).header("content-disposition",
							"attachment; filename = \"" + code + "-" + timeFormat + ".xml\"").build();
				} else if (resource != null && files != null) {
					//// Cas du renvoi d'un ZIP
					HttpSession session = httpServletRequest.getSession(true);
					File tempZip = getZipFile(portfolioUuid, portfolio, lang, doc, session);

					/// Return zip file
					RandomAccessFile f = new RandomAccessFile(tempZip.getAbsoluteFile(), "r");
					byte[] b = new byte[(int) f.length()];
					f.read(b);
					f.close();

					response = Response.ok(b, MediaType.APPLICATION_OCTET_STREAM).header("content-disposition",
							"attachment; filename = \"" + code + "-" + timeFormat + ".zip").build();

					// Temp file cleanup
					tempZip.delete();
				} else {
					if (portfolio.equals("faux")) {

						throw new RestWebApplicationException(Status.FORBIDDEN,
								"Vous n'avez pas les droits necessaires");
					}

					if (accept.equals(MediaType.APPLICATION_JSON)) {
						portfolio = XML.toJSONObject(portfolio).toString();
						response = Response.ok(portfolio).type(MediaType.APPLICATION_JSON).build();
					} else
						response = Response.ok(portfolio).type(MediaType.APPLICATION_XML).build();
				}
			}
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
		} catch (SQLException ex) {
			logger.info("Portfolio " + portfolioUuid + " not found");
			throw new RestWebApplicationException(Status.NOT_FOUND, "Portfolio " + portfolioUuid + " not found");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return response;
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
	 * Return the portfolio from its code GET /rest/api/portfolios/code/{code}
	 * parameters: return: see 'content' of "GET
	 * /rest/api/portfolios/portfolio/{portfolio-id}"
	 **/
	@Path("/portfolios/portfolio/code/{code}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Object getPortfolioByCode(@CookieParam("user") String user, @CookieParam("credential") String token,
			@QueryParam("group") long groupId, @PathParam("code") String code, @Context ServletConfig sc,
			@Context HttpServletRequest httpServletRequest, @HeaderParam("Accept") String accept,
			@QueryParam("user") Integer userId, @QueryParam("group") Integer group,
			@QueryParam("resources") String resources) {
//		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		Credential ui = new Credential(); // FIXME

		try {
			if (ui.getId() == 0) {
				return Response.status(Status.FORBIDDEN).build();
			}

			if (resources == null)
				resources = "false";
			String returnValue = portfolioManager.getPortfolioByCode(new MimeType("text/xml"), code, ui.getId(),
					groupId, resources, ui.getCredentialSubstitution().getCredentialSubstitutionId()).toString();
			if ("faux".equals(returnValue)) {
				throw new RestWebApplicationException(Status.FORBIDDEN, "Vous n'avez pas les droits necessaires");
			}
			if ("".equals(returnValue)) {
				return Response.status(Status.NOT_FOUND).entity("").build();
			}
			if (MediaType.APPLICATION_JSON.equals(accept)) // Not really used
				returnValue = XML.toJSONObject(returnValue).toString();

			return returnValue;
		} catch (RestWebApplicationException ex) {
			throw new RestWebApplicationException(Status.FORBIDDEN, ex.getResponse().getEntity().toString());
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

//		UserInfo ui = checkCredential(httpServletRequest, user, token, null);

		Credential ui = new Credential(); // FIXME
		final MimeType mimeType = new MimeType("text/xml");
		try {
			if (portfolioUuid != null) {
				String returnValue = portfolioManager.getPortfolio(mimeType, portfolioUuid, ui.getId(), groupId,
						this.label, null, null, ui.getCredentialSubstitution().getCredentialSubstitutionId(), cutoff)
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
					returnValue = portfolioManager.getPortfolioByCode(mimeType, portfolioCode, ui.getId(), groupId,
							null, ui.getCredentialSubstitution().getCredentialSubstitutionId()).toString();
				} else {
					if (public_var != null) {
						long publicid = userManager.getUserID("public");
						returnValue = portfolioManager.getPortfolios(mimeType, publicid, groupId, portfolioActive, 0,
								portfolioProject, portfolioProjectId, countOnly, search).toString();
					} else if (userId != null && securityManager.isAdmin(ui.getId())) {
						returnValue = portfolioManager.getPortfolios(mimeType, userId, groupId, portfolioActive,
								ui.getCredentialSubstitution().getCredentialSubstitutionId(), portfolioProject,
								portfolioProjectId, countOnly, search).toString();
					} else /// For user logged in
					{
						returnValue = portfolioManager.getPortfolios(mimeType, ui.getId(), groupId, portfolioActive,
								ui.getCredentialSubstitution().getCredentialSubstitutionId(), portfolioProject,
								portfolioProjectId, countOnly, search).toString();
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

//		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		Credential ui = new Credential(); // FIXME

		try {
			Boolean portfolioActive;
			if ("false".equals(active) || "0".equals(active))
				portfolioActive = false;
			else
				portfolioActive = true;

			final MimeType mimeType = new MimeType("text/xml");
			portfolioManager.putPortfolio(mimeType, mimeType, xmlPortfolio, portfolioUuid, ui.getId(), portfolioActive,
					groupId, null);

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
//		UserInfo ui = checkCredential(httpServletRequest, null, null, null);
		Credential cr = null; // FIXME

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
//		UserInfo ui = checkCredential(httpServletRequest, user, token, null);
		Credential cr = new Credential(); // FIXME
		boolean retval = false;

		try {
			// Check if logged user is either admin, or owner of the current portfolio
			if (securityManager.isAdmin(cr.getId()) || portfolioManager.isOwner(cr.getId(), portfolioUuid)) {
				retval = portfolioManager.putPortfolioOwner(portfolioUuid, newOwner);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}

		return Boolean.toString(retval);
	}

	/**
	 * Modify some portfolio option PUT
	 * /rest/api/portfolios/portfolios/{portfolio-id} parameters: - portfolio: uuid
	 * - active: 0/1, true/false return:
	 **/
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

//		UserInfo ui = checkCredential(httpServletRequest, user, token, null); 
		Credential ui = new Credential(); // FIXME

		try {
			String returnValue = "";
			if (portfolioUuid != null && portfolioActive != null) {
				portfolioManager.putPortfolioConfiguration(portfolioUuid, portfolioActive, ui.getId());
			}
			return returnValue;
		} catch (BusinessException ex) {
			ex.printStackTrace();
			throw new RestWebApplicationException(Status.INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

}
