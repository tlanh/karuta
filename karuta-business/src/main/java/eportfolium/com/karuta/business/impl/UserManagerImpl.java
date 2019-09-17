package eportfolium.com.karuta.business.impl;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eportfolium.com.karuta.business.contract.UserManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupDao;
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
	private CredentialGroupDao credentialGroupDao;

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

	public String getListUsers(Long userId, String username, String firstname, String lastname) {
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

	public Boolean deleteUsersGroups(Long userGroupId) {
		Boolean isOK = Boolean.TRUE;
		try {
			final List<CredentialGroupMembers> userGroupList = credentialDao.getUsersByUserGroup(userGroupId);
			final Iterator<CredentialGroupMembers> it = userGroupList.iterator();
			while (it.hasNext()) {
				credentialGroupMembersDao.remove(it.next());
			}
			credentialGroupDao.removeById(userGroupId);
		} catch (Exception e) {
			e.printStackTrace();
			isOK = Boolean.FALSE;
		}

		return isOK;

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

	public void addGroups(Long userID, List<Long> groups) {
		CredentialGroupMembers cgm = null;
		for (Long group : groups) {
			cgm = new CredentialGroupMembers(
					new CredentialGroupMembersId(new CredentialGroup(group), new Credential(userID)));
			credentialGroupMembersDao.persist(cgm);
		}
	}

	public String getUserGroups(Long userId) throws Exception {
		List<GroupUser> res = groupUserDao.getUserGroups(userId);
		GroupUser current = null;

		Iterator<GroupUser> it = res.iterator();
		String result = "<groups>";
		while (it.hasNext()) {
			current = it.next();
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(current.getGroupInfo().getId())) + " ";
			result += DomUtils.getXmlAttributeOutput("owner", String.valueOf(current.getGroupInfo().getOwner())) + " ";
			result += DomUtils.getXmlAttributeOutput("templateId",
					String.valueOf(current.getGroupInfo().getGroupRightInfo().getId())) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("label", current.getGroupInfo().getLabel());
			result += "</group>";
		}

		result += "</groups>";

		return result;
	}

	public String getUserGroupList() {
		String result = "<groups>";
		List<CredentialGroup> res = credentialGroupDao.findAll();
		Iterator<CredentialGroup> it = res.iterator();
		CredentialGroup currentCredential;
		while (it.hasNext()) {
			currentCredential = it.next();
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(currentCredential.getId()) + " ");
			result += ">";
			result += DomUtils.getXmlElementOutput("label", currentCredential.getLabel());
			result += "</group>";
		}
		result += "</groups>";

		return result;
	}

	public boolean postGroupsUsers(Long user, Long userId, Long groupId) throws BusinessException {
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
		List<GroupUser> res = groupUserDao.getUserGroupByPortfolioAndUser(portfolioUuid, userId);
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

	public String getRole(Long grid) {
		String result = null;
		try {
			GroupRightInfo res = groupRightInfoDao.findById(grid);
			result = "<role ";
			result += DomUtils.getXmlAttributeOutput("id", String.valueOf(res.getId())) + " ";
			result += DomUtils.getXmlAttributeOutput("owner", String.valueOf(res.getOwner())) + " ";
			result += ">";
			result += DomUtils.getXmlElementOutput("label", res.getLabel()) + " ";
			result += DomUtils.getXmlElementOutput("portfolio_id", res.getPortfolio().getId().toString());
			result += "</role>";
		} catch (Exception ex) {
		}
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

	public Long getUserID(String username) {
		return credentialDao.getUserId(username);
	}

	public String getUserRolesByUserId(Long userId) {

		List<GroupUser> groups = groupUserDao.getUserGroups(userId);
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

}
