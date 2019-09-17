package eportfolium.com.karuta.business.impl;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.CredentialGroupMembersDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.CredentialGroupMembers;
import eportfolium.com.karuta.model.bean.GroupInfo;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.exception.GenericBusinessException;

@Service
public class GroupManagerImpl implements GroupManager {

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private GroupInfoDao groupInfoDao;

	@Autowired
	private GroupRightsDao groupRightsDao;

	@Autowired
	private CredentialGroupMembersDao credentialGroupMembersDao;

	public String createGroup(String name) {
		Long retval = 0L;
		try {
			GroupRightInfo gri = new GroupRightInfo();
			gri.setOwner(1);
			gri.setLabel(name);
			groupRightInfoDao.persist(gri);
			retval = gri.getId();

			GroupInfo groupInfo = new GroupInfo();
			groupInfo.setGroupRightInfo(gri);
			groupInfo.setOwner(1);
			groupInfo.setLabel(name);
			groupInfoDao.persist(groupInfo);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return String.valueOf(retval);
	}

	public String getGroupByUser(Long userId) {
		List<CredentialGroupMembers> cgmList = credentialGroupMembersDao.getGroupByUser(userId);
		Iterator<CredentialGroupMembers> it = cgmList.iterator();
		String result = "<groups>";
		CredentialGroupMembers cgm = null;
		while (it.hasNext()) {
			cgm = it.next();
			result += "<group ";
			result += DomUtils.getXmlAttributeOutput("id", "" + cgm.getCredentialGroup().getId()) + " ";
			result += ">";
			result += "<label>" + cgm.getCredentialGroup().getLabel() + "</label>";
			result += "</group>";
		}
		result += "</groups>";
		return result;
	}

	public String getGroupsByUser(Long id) {
		return null;
	}

	public boolean postNotifyRoles(Long userId, String portfolioId, String uuid, String notify)
			throws GenericBusinessException {

		boolean ret = false;
		if (!credentialDao.isAdmin(userId))
			throw new GenericBusinessException("No admin right");
		List<GroupRights> grList = groupRightsDao.getRightsByPortfolio(uuid, portfolioId);

		try {
			GroupRights gr = null;
			for (Iterator<GroupRights> it = grList.iterator(); it.hasNext();) {
				gr = it.next();
				gr.setNotifyRoles(notify);
				groupRightsDao.merge(gr);
			}
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;
	}

}
