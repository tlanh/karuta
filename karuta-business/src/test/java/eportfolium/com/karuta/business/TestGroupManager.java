package eportfolium.com.karuta.business;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupDao;
import eportfolium.com.karuta.model.bean.CredentialGroup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
public class TestGroupManager {

	/**
	 * Pretty-prints XML, supplied as a string.
	 * <p/>
	 * eg. <code>
	 * String formattedXml = new XmlFormatter().format("<tag><nested>hello</nested></tag>");
	 * </code>
	 */
	class XmlFormatter {

		public String format(String xml) {

			try {
				final InputSource src = new InputSource(new StringReader(xml));
				final org.w3c.dom.Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src)
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

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private CredentialGroupDao credentialGroupDao;

	@Autowired
	private SecurityManager securityManager;

	@Test
	public void test_creating_everything_one_by_one() throws Exception {

		// Create a credential group with a random name.
		String cgName = RandomStringUtils.randomAlphabetic(10);
		Long credentialGroupId = groupManager.addCredentialGroup(cgName);

		String newCgName = RandomStringUtils.randomAlphabetic(10);
		boolean renameResult = groupManager.renameCredentialGroup(credentialGroupId, newCgName);
		Assert.assertTrue(renameResult);

		CredentialGroup crg = groupManager.getCredentialGroupByName(newCgName);
		Assert.assertNotNull(crg);
		Assert.assertEquals(newCgName, crg.getLabel());

		boolean addUserInCredentialGroupsResult = securityManager.addUserInCredentialGroups(1L,
				Arrays.asList(credentialGroupId));
		Assert.assertTrue(addUserInCredentialGroupsResult);

		String cgGpByUser = groupManager.getCredentialGroupByUser(1L);
		Assert.assertNotEquals("<groups></groups>", cgGpByUser);
		System.out.println(new XmlFormatter().format(cgGpByUser));

		String cgList = groupManager.getCredentialGroupList();
		Assert.assertNotEquals("<groups></groups>", cgList);
		System.out.println(new XmlFormatter().format(cgList));

		String generatedGroup = RandomStringUtils.randomAlphabetic(10);
		// Create a group with random name.
		groupManager.addGroup(generatedGroup);

		String xmlGroup = "<group grid=\"25\" owner=\"1\" label=\"test\"></group>";
		// add user to a group with informations formatted in XML.
		String addUserGroupResult = groupManager.addUserGroup(xmlGroup, 1L);
		Assert.assertEquals(xmlGroup, addUserGroupResult);

		String label = "root";
		String right = "add";
		String nodeUuid = "26fb4189-e58c-41c8-8fd1-38157da80510";
		String portfolioUuid = "89f2ffd2-db6e-4bb3-bf68-188a18d05656";

		boolean grResults = groupManager.addGroupRights(label, nodeUuid, right, portfolioUuid, 1L);
		Assert.assertTrue(grResults);

		// Restore UserGroup.
		groupManager.changeUserGroup(3L, 3L, 1L);
		groupManager.changeUserGroup(4L, 4L, 1L);

		String getGroupRightsResults = groupManager.getGroupRights(1L, 3L);
		Assert.assertNotEquals("<groupRights></groupRights>", getGroupRightsResults);
		System.out.println(new XmlFormatter().format(getGroupRightsResults));

		boolean changeNotifyRolesResults = groupManager.changeNotifyRoles(1L, portfolioUuid, nodeUuid, "designer");
		Assert.assertTrue(changeNotifyRolesResults);

		// Swap UserGroup.
		groupManager.changeUserGroup(3L, 4L, 1L);
		groupManager.changeUserGroup(4L, 3L, 1L);

		String userGroupsList = groupManager.getUserGroups(1L);
		Assert.assertNotEquals("<groups></groups>", userGroupsList);
		System.out.println(new XmlFormatter().format(userGroupsList));

		String groupList = groupManager.getGroupsByRole(portfolioUuid, "root");
		Assert.assertNotEquals("<groups></groups>", groupList);
		System.out.println(new XmlFormatter().format(groupList));

		boolean setPublicState = groupManager.setPublicState(1L, portfolioUuid, true);
		Assert.assertTrue(setPublicState);
	}

	@Test
	public void test_removing_with_a_composition_relationship() throws Exception {
		List<CredentialGroup> cgList = credentialGroupDao.findAll();
		groupManager.removeCredentialGroup(cgList.get(cgList.size() - 1).getId());
	}

}
