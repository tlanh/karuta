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

package eportfolium.com.karuta.business.impl;

import java.io.StringWriter;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import eportfolium.com.karuta.consumer.repositories.CredentialGroupMembersRepository;
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import eportfolium.com.karuta.consumer.repositories.GroupRightInfoRepository;
import eportfolium.com.karuta.consumer.repositories.GroupUserRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupUser;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;

@Service
@Transactional
public class UserManagerImpl implements UserManager {

	@Autowired
	private CredentialRepository credentialRepository;

	@Autowired
	private CredentialGroupMembersRepository credentialGroupMembersRepository;

	@Autowired
	private GroupUserRepository groupUserRepository;

	@Autowired
	private GroupRightInfoRepository groupRightInfoRepository;

	public String getUsers(Long userId, String username, String firstname, String lastname) {
		List<Credential> res = credentialRepository.getUsers(username, firstname, lastname);

		String result = "<users>";
		long curUser = 0;

		for (Credential current : res) {

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
			}
		}

		result += "</users>";

		return result;
	}

	public String getUserList(Long userId, String username, String firstname, String lastname) {
		List<Credential> users = credentialRepository.getUsers(username, firstname, lastname);

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
		final List<CredentialGroupMembers> userGroupList = credentialGroupMembersRepository.findByGroup(userGroupId);

		for (CredentialGroupMembers cgm : userGroupList) {
			result += "<user ";
			result += DomUtils.getXmlAttributeOutput("id", "" + cgm.getCredential().getId()) + " ";
			result += ">";
			result += "</user>";
		}
		result += "</users></group>";
		return result;
	}

	public String getUserGroupByPortfolio(String portfolioUuid, Long userId) {
		List<GroupUser> res = groupUserRepository.getByPortfolioAndUser(UUID.fromString(portfolioUuid), userId);

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
		List<Credential> users = credentialRepository.getUsersByRole(UUID.fromString(portfolioUuid), role);
		String result = "<users>";

		for (Credential res : users) {
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
		GroupRightInfo res = groupRightInfoRepository.findById(groupRightInfoId)
								.orElseThrow(() -> new DoesNotExistException(GroupRightInfo.class, groupRightInfoId));
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

		Credential cr = credentialRepository.getUserInfos(userId);

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
		return credentialRepository.getIdByLogin(userLogin);
	}

	public String getUserRolesByUserId(Long userId) {
		List<GroupUser> groups = groupUserRepository.getByUser(userId);

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
		return credentialRepository.getPublicId();
	}

	public String getEmailByLogin(String userLogin) {
		return credentialRepository.getEmailByLogin(userLogin);
	}

	public Long getUserId(String userLogin, String email) {
		return credentialRepository.getIdByLoginAndEmail(userLogin, email);
	}

	public String getRoleList(String portfolio, Long userId, String role) throws BusinessException {

		try {
			boolean bypass = false;
			List<GroupRightInfo> griList = new ArrayList<>();
			UUID portfolioId = UUID.fromString(portfolio);

			if (portfolio != null && userId != null) {
				griList = groupRightInfoRepository.getByPortfolioAndUser(portfolioId, userId);
			} else if (portfolio != null && role != null) {
				GroupRightInfo tmp = groupRightInfoRepository.getByPortfolioAndLabel(portfolioId, role);
				if (tmp != null) {
					griList = Arrays.asList(tmp);
				} else {
					griList = Arrays.asList();
				}
				bypass = true;
			} else if (portfolio != null) {
				griList = groupRightInfoRepository.getByPortfolioID(portfolioId);
			} else if (userId != null) {
				griList = groupRightInfoRepository.getByUser(userId);
			} else {
				CollectionUtils.addAll(griList, groupRightInfoRepository.findAll());
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
		List<GroupUser> guList = groupUserRepository.getByPortfolioAndUser(UUID.fromString(portId), userId);

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
			List<GroupUser> guList = groupUserRepository.getByRole(rrgid);
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

			Map<String, Object> rolesToBeNotified = groupRightInfoRepository.getRolesToBeNotified(groupId, userId, UUID.fromString(uuid));
			if (rolesToBeNotified != null) {
				portfolio = (UUID) rolesToBeNotified.get("portfolioUUID");
				roles = MapUtils.getString(rolesToBeNotified, "notifyRoles");
			}

			if (StringUtils.isEmpty(roles))
				return retval;

			String[] roleArray = roles.split(",");
			Set<String> roleSet = new HashSet<String>(Arrays.asList(roleArray));

			/// Fetch all users/role associated with this portfolio.
			List<GroupRightInfo> griList = groupRightInfoRepository.getByPortfolioID(portfolio);

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
		return credentialRepository.findById(userId)
				.orElseThrow(() -> new DoesNotExistException(Credential.class, userId));
	}

}
