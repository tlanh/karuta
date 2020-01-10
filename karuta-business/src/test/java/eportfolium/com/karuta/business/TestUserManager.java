package eportfolium.com.karuta.business;

import java.io.StringReader;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
public class TestUserManager {

	@Autowired
	private UserManager service;

	private final String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";

	/**
	 * Pretty-prints xml, supplied as a string.
	 * <p/>
	 * eg. <code>
	 * String formattedXml = new XmlFormatter().format("<tag><nested>hello</nested></tag>");
	 * </code>
	 */
	class XmlFormatter {

		public String format(String xml) {

			try {
				final InputSource src = new InputSource(new StringReader(xml));
				final Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src)
						.getDocumentElement();
				final Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));

				// May need this:
				// System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");

				final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
				final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
				final LSSerializer writer = impl.createLSSerializer();

				writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the
																							// output needs to be
																							// beautified.
				writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the
																						// declaration is needed to be
																						// outputted.

				return writer.writeToString(document);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	// Tests


	@Test
	public void getUserList() {
		System.out.println("TestUserManager.getUserList()");
		String users = service.getUserList(1L, "root", null, null);
		System.out.println(new XmlFormatter().format(users));
	}

	@Test
	public void getUsersByUserGroup() {
		System.out.println("TestUserManager.getUsersByUserGroup()");
		String usersGps = service.getUsersByCredentialGroup(1L);
		System.out.println(new XmlFormatter().format(usersGps));
	}

	/*********** transfer to securityManager ****/
	@Test
	public void getUserGroupByPortfolio() {
		System.out.println("TestUserManager.getUserGroupByPortfolio()");
		String usersGpPort = service.getUserGroupByPortfolio(portfolioUuid, 1L);
		System.out.println(new XmlFormatter().format(usersGpPort));
	}

	@Test
	public void getUsersByRole() {
		System.out.println("TestUserManager.getUsersByRole()");
		String usersByRole = service.getUsersByRole(1L, portfolioUuid, "all");
		System.out.println(new XmlFormatter().format(usersByRole));
	}

	@Test
	public void getRole() throws BusinessException {
		System.out.println("TestUserManager.getRole()");
		String role = service.getUsersByRole(1L, portfolioUuid, "all");
		System.out.println(new XmlFormatter().format(role));
	}

	@Test
	public void getUserInfos() throws DoesNotExistException {
		System.out.println("TestUserManager.getUserInfos()");
		String userInfos = service.getUserInfos(1L);
		System.out.println(new XmlFormatter().format(userInfos));
	}

	@Test
	public void getUserRolesByUserId() {
		System.out.println("TestUserManager.getUserRolesByUserId()");
		String userRoles = service.getUserRolesByUserId(1L);
		System.out.println(new XmlFormatter().format(userRoles));
	}

	@Test
	public void getPublicUserId() {
		System.out.println("TestUserManager.getPublicUserId()");
		Long publicUserId = service.getPublicUserId();
		Assert.assertEquals(2L, publicUserId.longValue());
	}

	@Test
	public void getEmailByLogin() {
		String rootEmail = service.getEmailByLogin("root");
		Assert.assertEquals("test@test.com", rootEmail);
	}

	@Test
	public void getRoleList() throws BusinessException {
		System.out.println("TestUserManager.getRoleList()");
		String roleList = service.getRoleList(portfolioUuid, null, null);
		System.out.println(new XmlFormatter().format(roleList));
	}

	@Test
	public void findUserRolesByPortfolio() throws Exception {
		System.out.println("TestUserManager.findUserRolesByPortfolio()");
		String userRoles = service.getUserRolesByPortfolio(portfolioUuid, 1L);
		System.out.println(new XmlFormatter().format(userRoles));
	}

	@Test
	public void findUserRole() {
		System.out.println("TestUserManager.findUserRole()");
		String userRole = service.getUserRole(3L);
		System.out.println(new XmlFormatter().format(userRole));
	}

	@Test
	public void getNotificationUserList() {
		final Set<String[]> results = service.getNotificationUserList(1L, 3L, portfolioUuid);
		for (String[] result : results) {
			System.out.println(result);
		}
	}

	@Test
	public void getUser() throws DoesNotExistException {
		System.out.println("TestUserManager.getUser()");
		Credential user = service.getUser(1L);
		System.out.println(user.toString());
	}

}
