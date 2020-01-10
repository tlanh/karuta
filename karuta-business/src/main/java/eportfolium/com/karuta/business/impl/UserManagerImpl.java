package eportfolium.com.karuta.business.impl;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupMembersDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialSubstitutionDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupUserDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroup;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.CredentialGroupMembersId;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.CredentialSubstitutionId;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

@Service
@Transactional
public class UserManagerImpl implements UserManager {

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private CredentialGroupMembersDao credentialGroupMembersDao;

	@Autowired
	private GroupUserDao groupUserDao;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private CredentialSubstitutionDao credentialSubstitutionDao;

	public String getUsers(Long userId, String username, String firstname, String lastname) {
		List<Credential> res = credentialDao.getUsers(username, firstname, lastname);
		Iterator<Credential> it = res.iterator();

		String result = "<users>";
		Credential current = null;
		long curUser = 0;
		while (it.hasNext()) {
			current = it.next();
			Long tmpUserId = current.getId();
			if (curUser != tmpUserId) {
				curUser = tmpUserId;
				String subs = null;
				final CredentialSubstitution credentialSubstitution = current.getCredentialSubstitution();
				if (credentialSubstitution != null && credentialSubstitution.getId() != null)
					subs = String.valueOf(credentialSubstitution.getId().getId());
				if (subs != null)
					subs = "1";
				else
					subs = "0";

				result += "<user ";
				result += DomUtils.getXmlAttributeOutput("id", String.valueOf(tmpUserId)) + " ";
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
		List<Credential> users = credentialDao.getUsers(username, firstname, lastname);

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
				result.append(userid.toString());
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

	public String getUsersByCredentialGroup(Long userGroupId) {
		String result = "<group id=\"" + userGroupId + "\"><users>";
		final List<CredentialGroupMembers> userGroupList = credentialGroupMembersDao.getByGroup(userGroupId);
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

	public String getUserInfos(Long userId) throws DoesNotExistException {
		String result = "";

		Credential cr = credentialDao.getUserInfos(userId);
		if (cr != null) {
			// traitement de la réponse, renvoie les données sous forme d'un XML.
			String subs = "0";
			boolean canSubstitute = cr.getCredentialSubstitution() != null;
			if (canSubstitute) {
				subs = "1";
			}

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
			throw new DoesNotExistException(Credential.class, userId);
		}

		return result;
	}

	public Long getUserId(String userLogin) {
		return credentialDao.getUserId(userLogin);
	}

	public String getUserRolesByUserId(Long userId) {
		List<GroupUser> groups = groupUserDao.getByUser(userId);
		GroupUser group = null;
		Iterator<GroupUser> it = groups.iterator();

		String result = "<profiles>";
		result += "<profile>";
		while (it.hasNext()) {
			group = it.next();
			result += "<group ";
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

	public String getEmailByLogin(String userLogin) {
		return credentialDao.getEmailByLogin(userLogin);
	}

	public Long getUserId(String userLogin, String email) {
		return credentialDao.getUserId(userLogin, email);
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

			if (bypass) // WAD6 demande un format spécifique pour ce type de requête (...)
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

	public String getUserRolesByPortfolio(String portId, Long userId) throws Exception {

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

	public String getUserRole(Long rrgid) {

		try {
			List<GroupUser> guList = groupUserDao.getByRole(rrgid);
			GroupUser gu = null;

			// Il est temps de créer des données
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			Element root = document.createElement("rolerightsgroup");
			root.setAttribute("id", rrgid.toString());
			document.appendChild(root);

			Element usersNode = null;
			Iterator<GroupUser> it = guList.iterator();
			if (it.hasNext()) //
			{
				gu = it.next();
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

			do {
				Credential cr = gu.getCredential();
				Long id = cr.getId();
				if (id == null) // Bonnes chances que ce soit vide
					continue;

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

				try {
					gu = it.next();
				} catch (NoSuchElementException e) {
					break;
				}
			} while (true);

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

	public Set<String[]> getNotificationUserList(Long userId, Long groupId, String uuid) {
		Set<String[]> retval = new HashSet<String[]>();

		try {
			String roles = "";
			UUID portfolio = null;

			Map<String, Object> rolesToBeNotified = groupRightInfoDao.getRolesToBeNotified(groupId, userId, uuid);
			if (rolesToBeNotified != null) {
				portfolio = (UUID) rolesToBeNotified.get("portfolioUUID");
				roles = MapUtils.getString(rolesToBeNotified, "notifyRoles");
			}

			if (StringUtils.isEmpty(roles))
				return retval;

			String[] roleArray = roles.split(",");
			Set<String> roleSet = new HashSet<String>(Arrays.asList(roleArray));

			/// Fetch all users/role associated with this portfolio.
			List<GroupRightInfo> griList = groupRightInfoDao.getByPortfolioID(portfolio);

			/// Filter those we don't care
			String label = null, login = null, lastname = null;
			for (GroupRightInfo gri : griList) {
				if (gri.getGroupInfo() != null) {
					Set<GroupUser> guSet = gri.getGroupInfo().getGroupUser();
					if (CollectionUtils.isNotEmpty(guSet)) {
						label = gri.getGroupInfo().getLabel();
						for (GroupUser gu : guSet) {
							login = gu.getCredential().getLogin();
							lastname = gu.getCredential().getDisplayLastname();
							if (roleSet.contains(label)) {
								String val[] = { login, lastname };
								retval.add(val);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return retval;
	}

	public Credential getUser(Long userId) throws DoesNotExistException {
		return credentialDao.findById(userId);
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public Map<Long, Long> transferCredentialTable(Connection con) {

		Map<Long, Long> userIds = new HashMap<Long, Long>();
		ResultSet res = credentialDao.getMysqlUsers(con, null, null, null);
		Credential cr = null;
		try {
			long curUser = 0L;
			while (res.next()) {
				cr = new Credential();
				long userid = res.getLong("userid");
				if (curUser != userid) {
					curUser = userid;
					int canSubstitute = 0;
					String subs = res.getString("id");
					if (subs != null)
						canSubstitute = 1;

					cr.setLogin(res.getString("login"));
					cr.setDisplayFirstname(res.getString("display_firstname"));
					cr.setDisplayLastname(res.getString("display_lastname"));
					cr.setIsAdmin(res.getInt("is_admin"));
					cr.setIsDesigner(res.getInt("is_designer"));
					cr.setEmail(res.getString("email"));
					cr.setActive(res.getInt("active"));
					cr.setCanSubstitute(canSubstitute);
					// cr.setCredentialSubstitution(new CredentialSubstitution(new ));
					cr.setOther(res.getString("other"));
					credentialDao.persist(cr);
					userIds.put(userid, cr.getId());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userIds;

	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	@Override
	public void transferCredentialSubstitutionTable(Connection con, Map<Long, Long> userIds) throws SQLException {
		ResultSet res = credentialSubstitutionDao.findAll("credential_substitution", con);
		CredentialSubstitution cs = null;
		Credential cr = null;
		while (res.next()) {
			try {
				final Long crId = userIds.get(res.getLong("userid"));
				cr = credentialDao.findById(crId);
				cs = new CredentialSubstitution();
				// userid id type
				cs.setId(new CredentialSubstitutionId(new Credential(crId), res.getLong("id"), res.getString("type")));
				credentialSubstitutionDao.persist(cs);
				cr.setCredentialSubstitution(cs);
				cr = credentialDao.merge(cr);
			} catch (DoesNotExistException e) {
				e.printStackTrace();
			}
		}
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferCredentialGroupMembersTable(Connection con, Map<Long, Long> userIds, Map<Long, Long> cgIds)
			throws SQLException {
		ResultSet res = credentialGroupMembersDao.findAll("credential_group_members", con);
		CredentialGroupMembers cgm = null;
		while (res.next()) {
			cgm = new CredentialGroupMembers(
					new CredentialGroupMembersId(new CredentialGroup(cgIds.get(res.getLong("cg"))),
							new Credential(userIds.get(res.getLong("userid")))));
			credentialGroupMembersDao.merge(cgm);
		}
	}

	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void removeUsers() {
		credentialSubstitutionDao.removeAll();
		credentialGroupMembersDao.removeAll();
		credentialDao.removeAll();
	}

}
