package eportfolium.com.karuta.business.impl;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupMembersDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.CredentialGroupMembersId;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.bean.GroupUserId;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
public class UserManagerImpl implements UserManager {

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private CredentialGroupMembersDao credentialGroupMembersDao;

	@Autowired
	private GroupUserDao groupUserDao;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	public String getUsers(Long userId, String username, String firstname, String lastname) {
		List<Credential> res = credentialDao.getUsers(userId, username, firstname, lastname);
		Iterator<Credential> it = res.iterator();

		String result = "<users>";
		Credential current = null;
		long curUser = 0;
		while (it.hasNext()) {
			current = it.next();
			long userid = current.getId();
			if (curUser != userid) {
				curUser = userid;
				String subs = null;
				final CredentialSubstitution credentialSubstitution = current.getCredentialSubstitution();
				if (credentialSubstitution != null && credentialSubstitution.getId() != null)
					subs = String.valueOf(credentialSubstitution.getId().getId());
				if (subs != null)
					subs = "1";
				else
					subs = "0";

				result += "<user ";
				result += DomUtils.getXmlAttributeOutput("id", String.valueOf(userId)) + " ";
				result += ">";
				result += DomUtils.getXmlElementOutput("label", current.getLogin());
				result += DomUtils.getXmlElementOutput("display_firstname", current.getDisplayFirstname());
				result += DomUtils.getXmlElementOutput("display_lastname", current.getDisplayLastname());
				result += DomUtils.getXmlElementOutput("email", current.getEmail());
				result += DomUtils.getXmlElementOutput("active", String.valueOf(current.getActive()));
				result += DomUtils.getXmlElementOutput("substitute", subs);
				result += "</user>";
			} else {
			}
		}

		result += "</users>";

		return result;
	}

	public String getUserList(Long userId, String username, String firstname, String lastname) {
		List<Credential> users = credentialDao.getUsers(userId, username, firstname, lastname);

		StringBuilder result = new StringBuilder();
		result.append("<users>");
		Long curUser = 0L;
		Credential current = null;
		for (Iterator<Credential> it = users.iterator(); it.hasNext();) {
			current = it.next();
			Long userid = current.getId();
			if (curUser != userid) {
				curUser = userid;
				String subs = null;
				final CredentialSubstitution credentialSubstitution = current.getCredentialSubstitution();
				if (credentialSubstitution != null && credentialSubstitution.getId() != null)
					subs = String.valueOf(credentialSubstitution.getId().getId());
				if (subs != null)
					subs = "1";
				else
					subs = "0";

				result.append("<user id=\"");
				result.append(current.getId());
				result.append("\"><username>");
				result.append(current.getLogin());
				result.append("</username><firstname>");
				result.append(current.getDisplayFirstname());
				result.append("</firstname><lastname>");
				result.append(current.getDisplayLastname());
				result.append("</lastname><admin>");
				result.append(current.getIsAdmin());
				result.append("</admin><designer>");
				result.append(current.getIsDesigner());
				result.append("</designer><email>");
				result.append(current.getEmail());
				result.append("</email><active>");
				result.append(current.getActive());
				result.append("</active><substitute>");
				result.append(subs);
				result.append("</substitute>");
				result.append("<other>").append(current.getOther()).append("</other>");
				result.append("</user>");
			} else {
			}
		}

		result.append("</users>");

		return result.toString();

	}

	public String getUsersByUserGroup(Long userGroupId) {
		String result = "<group id=\"" + userGroupId + "\"><users>";
		final List<CredentialGroupMembers> userGroupList = credentialDao.getUsersByUserGroup(userGroupId);
		final Iterator<CredentialGroupMembers> it = userGroupList.iterator();
		while (it.hasNext()) {
			result += "<user ";
			result += DomUtils.getXmlAttributeOutput("id", "" + it.next().getCredential().getId()) + " ";
			result += ">";
			result += "</user>";
		}
		result += "</users></group>";
		return result;
	}

	public boolean addUserInGroups(Long userId, List<Long> credentialGroupIds) {
		boolean added = true;
		CredentialGroupMembers cgm = null;
		try {
			for (Long credentialGroupId : credentialGroupIds) {
				cgm = new CredentialGroupMembers(
						new CredentialGroupMembersId(new CredentialGroup(credentialGroupId), new Credential(userId)));
				credentialGroupMembersDao.persist(cgm);
			}
		} catch (Exception e) {
			added = false;
		}
		return added;
	}

	public boolean addUserToGroup(Long user, Long userId, Long groupId) throws BusinessException {
		if (!credentialDao.isAdmin(String.valueOf(user)))
			throw new GenericBusinessException("No admin right");

		GroupUser gu = new GroupUser();
		GroupUserId guID = new GroupUserId(new GroupInfo(groupId), new Credential(userId));
		try {
			gu = groupUserDao.findById(guID);
		} catch (DoesNotExistException e) {
			gu.setId(guID);
		}

		try {
			groupUserDao.merge(gu);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public String getUserGroupByPortfolio(String portfolioUuid, Long userId) {
		List<GroupUser> res = groupUserDao.getByPortfolioAndUser(portfolioUuid, userId);
		String result = "<groups>";
		Iterator<GroupUser> it = res.iterator();
		GroupUser gu = null;
		while (it.hasNext()) {
			gu = it.next();
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(gu.getGroupInfo().getId())) + " ";
			result += DomUtils.getXmlAttributeOutput("templateId",
					String.valueOf(gu.getGroupInfo().getGroupRightInfo().getId())) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("label", gu.getGroupInfo().getLabel());
			result += DomUtils.getXmlElementOutput("role", gu.getGroupInfo().getLabel());
			result += DomUtils.getXmlElementOutput("groupid", String.valueOf(gu.getGroupInfo().getId()));
			result += "</group>";

		}
		result += "</groups>";
		return result;
	}

	public String getUsersByRole(Long userId, String portfolioUuid, String role) {
		List<Credential> users = credentialDao.getUsersByRole(userId, portfolioUuid, role);
		String result = "<users>";
		Credential res = null;
		for (Iterator<Credential> it = users.iterator(); it.hasNext();) {
			res = it.next();
			result += "<user ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(res.getId())) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("username", res.getLogin());
			result += DomUtils.getXmlElementOutput("firstname", res.getDisplayFirstname());
			result += DomUtils.getXmlElementOutput("lastname", res.getDisplayLastname());
			result += DomUtils.getXmlElementOutput("email", res.getEmail());
			result += "</user>";
		}

		result += "</users>";

		return result;
	}

	public String getRole(Long groupRightInfoId) throws BusinessException {
		GroupRightInfo res = groupRightInfoDao.findById(groupRightInfoId);
		String result = "<role ";
		result += DomUtils.getXmlAttributeOutput("id", String.valueOf(res.getId())) + " ";
		result += DomUtils.getXmlAttributeOutput("owner", String.valueOf(res.getOwner())) + " ";
		result += ">";
		result += DomUtils.getXmlElementOutput("label", res.getLabel()) + " ";
		result += DomUtils.getXmlElementOutput("portfolio_id", res.getPortfolio().getId().toString());
		result += "</role>";
		return result;
	}

	public String getInfUser(Long userId) throws BusinessException {
		String result = "";

		Credential cr = credentialDao.getInfUser(userId);
		if (cr != null) {
			// traitement de la reponse, renvoie des donnees sous forme d'un xml
			String subs = String.valueOf(cr.getId());
			if (subs != null)
				subs = "1";
			else
				subs = "0";

			result += "<user ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(cr.getId())) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("username", cr.getLogin());
			result += DomUtils.getXmlElementOutput("firstname", cr.getDisplayFirstname());
			result += DomUtils.getXmlElementOutput("lastname", cr.getDisplayLastname());
			result += DomUtils.getXmlElementOutput("email", cr.getEmail());
			result += DomUtils.getXmlElementOutput("admin", String.valueOf(cr.getIsAdmin()));
			result += DomUtils.getXmlElementOutput("designer", String.valueOf(cr.getIsDesigner()));
			result += DomUtils.getXmlElementOutput("active", String.valueOf(cr.getActive()));
			result += DomUtils.getXmlElementOutput("substitute", subs);
			result += DomUtils.getXmlElementOutput("other", cr.getOther());
			result += "</user>";

		} else {
			throw new GenericBusinessException("User " + userId + " not found");
		}

		return result;
	}

	public Long getUserId(String username) {
		return credentialDao.getUserId(username);
	}

	public String getUserRolesByUserId(Long userId) {

		List<GroupUser> groups = groupUserDao.getByUser(userId);
		GroupUser group = null;
		Iterator<GroupUser> it = groups.iterator();

		String result = "<profiles>";
		result += "<profile>";
		while (it.hasNext()) {
			group = it.next();
			result += "<group";
			result += DomUtils.getXmlAttributeOutput("id", group.getGroupInfo().getId().toString()) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("label", group.getGroupInfo().getLabel());
			result += DomUtils.getXmlElementOutput("role", group.getGroupInfo().getGroupRightInfo().getLabel());
			result += "</group>";
		}

		result += "</profile>";
		result += "</profiles>";

		return result;
	}

	public Long getPublicUserId() {
		return credentialDao.getPublicUid();
	}

	public String getEmailByLogin(String username) {
		return credentialDao.getEmailByLogin(username);
	}

	public Long getUserId(String username, String email) {
		return credentialDao.getUserId(username, email);
	}

	public Boolean deleteUsersFromUserGroups(Long userId, Long groupId) {
		return credentialGroupMembersDao.deleteUsersFromUserGroups(userId, groupId);
	}

	public String getRoleList(String portfolio, Long userId, String role) throws BusinessException {

		try {
			boolean bypass = false;
			List<GroupRightInfo> griList = null;
			if (portfolio != null && userId != null) {
				griList = groupRightInfoDao.getByPortfolioAndUser(portfolio, userId);
			} else if (portfolio != null && role != null) {
				GroupRightInfo tmp = groupRightInfoDao.getByPortfolioAndLabel(portfolio, role);
				if (tmp != null) {
					griList = Arrays.asList(tmp);
				} else {
					griList = Arrays.asList();
				}
				bypass = true;
			} else if (portfolio != null) {
				griList = groupRightInfoDao.getByPortfolioID(portfolio);
			} else if (userId != null) {
				griList = groupRightInfoDao.getByUser(userId);
			} else {
				griList = groupRightInfoDao.findAll();
			}

			/// Time to create data
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("rolerightsgroups");
			document.appendChild(root);

			if (bypass) // WAD6 demande un format specifique pour ce type de requete (...)
			{
				if (CollectionUtils.isNotEmpty(griList)) {
					Long id = griList.get(0).getId();
					if (id == null || id == 0)
						return "";
					else
						return id.toString();
				}
			} else
				for (GroupRightInfo gri : griList) {
					Long id = gri.getId();
					if (id == null || id == 0) // Bonne chance que ce soit vide
						continue;

					Element rrg = document.createElement("rolerightsgroup");
					rrg.setAttribute("id", id.toString());
					root.appendChild(rrg);

					String label = gri.getLabel();
					Element labelNode = document.createElement("label");
					rrg.appendChild(labelNode);
					if (label != null)
						labelNode.appendChild(document.createTextNode(label));

					String pid = gri.getPortfolio().getId().toString();
					Element portfolioNode = document.createElement("portfolio");
					portfolioNode.setAttribute("id", pid);
					rrg.appendChild(portfolioNode);
				}

			StringWriter stw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult stream = new StreamResult(stw);
			serializer.transform(source, stream);
			return stw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public String findUserRolesByPortfolio(String portId, Long userId) throws Exception {

		// group_right_info pid:grid -> group_info grid:gid -> group_user gid:userid
		List<GroupUser> guList = groupUserDao.getByPortfolioAndUser(portId, userId);

		/// Time to create data
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		Element root = document.createElement("portfolio");
		root.setAttribute("id", portId);
		document.appendChild(root);

		Element rrgUsers = null;

		long rrg = 0;
		GroupRightInfo gri = null;
		Credential cr = null;
		for (GroupUser gu : guList) {
			if (rrg != gu.getGroupInfo().getGroupRightInfo().getId()) {
				gri = gu.getGroupInfo().getGroupRightInfo();
				rrg = gri.getId();

				Element rrgNode = document.createElement("rrg");
				rrgNode.setAttribute("id", Long.toString(rrg));

				Element rrgLabel = document.createElement("label");
				rrgLabel.setTextContent(gri.getLabel());

				rrgUsers = document.createElement("users");

				rrgNode.appendChild(rrgLabel);
				rrgNode.appendChild(rrgUsers);
				root.appendChild(rrgNode);
			}

			cr = gu.getCredential();
			Long uid = null;
			if (cr != null) {
				uid = cr.getId();
				Element user = document.createElement("user");
				user.setAttribute("id", Long.toString(uid));

				String firstname = cr.getDisplayFirstname();
				Element firstnameNode = document.createElement("display_firstname");
				firstnameNode.setTextContent(firstname);

				String lastname = cr.getDisplayLastname();
				Element lastnameNode = document.createElement("display_lastname");
				lastnameNode.setTextContent(lastname);

				String email = cr.getEmail();
				Element emailNode = document.createElement("email");
				emailNode.setTextContent(email);

				user.appendChild(firstnameNode);
				user.appendChild(lastnameNode);
				user.appendChild(emailNode);
				rrgUsers.appendChild(user);
			}
		}

		StringWriter stw = new StringWriter();
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult stream = new StreamResult(stw);
		serializer.transform(source, stream);
		return stw.toString();
	}

	public String findUserRole(Long userId, Long rrgid) {

		try {
			GroupUser gu = groupUserDao.getByUserAndRole(rrgid, userId);

			/// Time to create data
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("rolerightsgroup");
			root.setAttribute("id", rrgid.toString());
			document.appendChild(root);

			Element usersNode = null;
			if (gu != null) //
			{
				GroupRightInfo gri = gu.getGroupInfo().getGroupRightInfo();

				String label = gri.getLabel();
				String portfolioid = gri.getPortfolio().getId().toString();

				Element labelNode = document.createElement("label");
				labelNode.appendChild(document.createTextNode(label));
				root.appendChild(labelNode);

				Element portfolioNode = document.createElement("portofolio");
				portfolioNode.setAttribute("id", portfolioid);
				root.appendChild(portfolioNode);

				usersNode = document.createElement("users");
				root.appendChild(usersNode);
			} else
				return "";

//			do {
			Credential cr = gu.getCredential();

			Long id = cr.getId();
//				if (id == null) // Bonne chances que ce soit vide
//					continue;

			Element userNode = document.createElement("user");
			userNode.setAttribute("id", id.toString());
			usersNode.appendChild(userNode);

			String login = cr.getLogin();
			Element usernameNode = document.createElement("username");
			userNode.appendChild(usernameNode);
			if (login != null)
				usernameNode.appendChild(document.createTextNode(login));

			String firstname = cr.getDisplayFirstname();
			Element fnNode = document.createElement("firstname");
			userNode.appendChild(fnNode);
			if (firstname != null)
				fnNode.appendChild(document.createTextNode(firstname));

			String lastname = cr.getDisplayLastname();
			Element lnNode = document.createElement("lastname");
			userNode.appendChild(lnNode);
			if (lastname != null)
				lnNode.appendChild(document.createTextNode(lastname));

			String email = cr.getEmail();
			Element eNode = document.createElement("email");
			userNode.appendChild(eNode);
			if (email != null)
				eNode.appendChild(document.createTextNode(email));

//			} while (res.next());

			StringWriter stw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult stream = new StreamResult(stw);
			serializer.transform(source, stream);
			return stw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

}
