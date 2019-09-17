package eportfolium.com.karuta.business.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightInfoDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupDao;
import eportfolium.com.karuta.consumer.contract.dao.PortfolioGroupMembersDao;
import eportfolium.com.karuta.consumer.contract.dao.ResourceTableDao;
import eportfolium.com.karuta.consumer.util.DomUtils;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.Portfolio;
import eportfolium.com.karuta.model.bean.PortfolioGroup;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembers;
import eportfolium.com.karuta.model.bean.PortfolioGroupMembersId;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.model.exception.BusinessException;
import eportfolium.com.karuta.model.exception.DoesNotExistException;
import eportfolium.com.karuta.model.exception.GenericBusinessException;
import eportfolium.com.karuta.util.JavaTimeUtil;
import eportfolium.com.karuta.util.PhpUtil;
import eportfolium.com.karuta.util.ValidateUtil;

@Service
public class PortfolioManagerImpl implements PortfolioManager {

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private PortfolioDao portfolioDao;

	@Autowired
	private PortfolioGroupDao portfolioGroupDao;

	@Autowired
	private PortfolioGroupMembersDao portfolioGroupMembersDao;

	@Autowired
	private NodeDao nodeDao;

	@Autowired
	private CredentialDao credentialDao;

	@Autowired
	private GroupRightsDao groupRightsDao;

	@Autowired
	private GroupRightInfoDao groupRightInfoDao;

	@Autowired
	private ResourceTableDao resourceTableDao;

	public boolean updatePortfolioDate(String fromNodeuuid, String fromPortuuid) {
		boolean hasChanged = false;
		final Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.date_default_timezone));

		try {
			Portfolio portfolio = null;
			if (fromNodeuuid != null) {
				UUID portfolioUUID = nodeDao.getPortfolioIdFromNode(fromNodeuuid);
				portfolio = portfolioDao.findById(portfolioUUID);
				portfolio.setModifDate(now);
				hasChanged = true;
			} else if (fromPortuuid != null) {
				portfolio = portfolioDao.findById(UUID.fromString(fromPortuuid));
				portfolio.setModifDate(now);
				hasChanged = true;
			}
			portfolioDao.merge(portfolio);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return hasChanged;
	}

	public boolean deletePortfolioGroups(Long portfolioGroupId) {
		boolean res = false;
		try {
			PortfolioGroup pg = portfolioGroupDao.findById(portfolioGroupId);
			List<PortfolioGroupMembers> results = portfolioGroupMembersDao.getByPortfolioGroupID(portfolioGroupId);
			Iterator<PortfolioGroupMembers> it = results.iterator();
			while (it.hasNext()) {
				portfolioGroupMembersDao.remove(it.next());
			}
			portfolioGroupDao.remove(pg);
			res = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public boolean deletePortfolioFromPortfolioGroups(String uuid, Long portfolioGroupId) {
		boolean result = false;

		try {
			PortfolioGroupMembersId pgmID = new PortfolioGroupMembersId();
			pgmID.setPortfolio(portfolioDao.findById(UUID.fromString(uuid)));
			pgmID.setPortfolioGroup(portfolioGroupDao.findById(portfolioGroupId));
			PortfolioGroupMembers portfolioGroupMembers = portfolioGroupMembersDao.findById(pgmID);
			portfolioGroupMembersDao.remove(portfolioGroupMembers);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public String getPortfolioByPortfolioGroup(Long portfolioGroupId) {
		StringBuilder result = new StringBuilder();
		result.append("<group id=\"").append(portfolioGroupId).append("\">");
		List<Portfolio> portfolios = portfolioGroupDao.getPortfolioByPortfolioGroup(portfolioGroupId);
		Iterator<Portfolio> it = portfolios.iterator();
		while (it.hasNext()) {
			result.append("<portfolio");
			result.append(" id=\"");
			result.append(it.next().getId().toString());
			result.append("\"");
			result.append(">");
			result.append("</portfolio>");
		}
		result.append("</group>");
		return result.toString();
	}

	public int setPortfolioActive(String portfolioUuid, Boolean active) {
		int result = -1;
		try {
			Portfolio portfolio = portfolioDao.findById(UUID.fromString(portfolioUuid));
			portfolio.setActive(BooleanUtils.toInteger(active));
			portfolioDao.merge(portfolio);
			result = 0;
		} catch (DoesNotExistException e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getPortfolio(MimeType outMimeType, String portfolioUuid, Long userId, Long groupId, String label,
			String resource, String files, long substid, Integer cutoff) {

		Node rootNode = portfolioDao.getPortfolioRootNode(portfolioUuid);
		String header = "";
		String footer = "";

		GroupRights rights = getRightsOnPortfolio(userId, groupId, portfolioUuid);
		if (!rights.isRead()) {
			userId = credentialDao.getPublicUid();
			/// Verifie les droits avec le compte publique (derniere chance)
			GroupRights publicRights = groupRightsDao.getPublicRightsByUserId(userId, rootNode.getId());
			if (!publicRights.isRead())
				return "faux";
		}

		if (outMimeType.getSubType().equals("xml")) {
			Long ownerId = portfolioDao.getOwner(portfolioUuid);
			boolean isOwner = false;
			if (ownerId == userId)
				isOwner = true;

			String headerXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><portfolio code=\"0\" id=\"" + portfolioUuid
					+ "\" owner=\"" + isOwner + "\"><version>4</version>";

//			String data = getLinearXml(portfolioUuid, rootNode.getId(), null, true, null, userId, rights, cutoff); TODO 
			String data = "";

			StringWriter stw = new StringWriter();
			stw.append(headerXML + data + "</portfolio>");

			if (resource != null && files != null) {
				if (resource.equals("true") && files.equals("true")) {
					String adressedufichier = System.getProperty("user.dir") + "/tmp_getPortfolio_" + new Date()
							+ ".xml";
					String adresseduzip = System.getProperty("user.dir") + "/tmp_getPortfolio_" + new Date() + ".zip";

					File file = null;
					PrintWriter ecrire;
					try {
						file = new File(adressedufichier);
						ecrire = new PrintWriter(new FileOutputStream(adressedufichier));
						ecrire.println(stw.toString());
						ecrire.flush();
						ecrire.close();
						System.out.print("fichier cree ");
					} catch (IOException ioe) {
						System.out.print("Erreur : ");
						ioe.printStackTrace();
					}

					try {
						ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(adresseduzip));
						zip.setMethod(ZipOutputStream.DEFLATED);
						zip.setLevel(Deflater.BEST_COMPRESSION);
						File dataDirectories = new File(file.getName());
						FileInputStream fis = new FileInputStream(dataDirectories);
						byte[] bytes = new byte[fis.available()];
						fis.read(bytes);

						ZipEntry entry = new ZipEntry(file.getName());
						entry.setTime(dataDirectories.lastModified());
						zip.putNextEntry(entry);
						zip.write(bytes);
						zip.closeEntry();
						fis.close();
						// zipDirectory(dataDirectories, zip);
						zip.close();

						file.delete();

						return adresseduzip;
					} catch (FileNotFoundException fileNotFound) {
						fileNotFound.printStackTrace();
					} catch (IOException io) {
						io.printStackTrace();
					}
				}
			}

			return stw.toString();
		} else if (outMimeType.getSubType().equals("json")) {
			header = "{\"portfolio\": { \"-xmlns:xsi\": \"http://www.w3.org/2001/XMLSchema-instance\",\"-schemaVersion\": \"1.0\",";
			footer = "}}";
		}

		return header + nodeManager
				.getNode(outMimeType, rootNode.getId().toString(), true, userId, groupId, label, cutoff).toString()
				+ footer;
	}

	public GroupRights getRightsOnPortfolio(Long userId, Long groupId, String portfolioUuid) {
		GroupRights reponse = new GroupRights();

		try {
			/// modif_user_id => current owner
			Portfolio p = portfolioDao.getPortfolio(portfolioUuid);
			if (p != null) {
				if (p.getModifUserId() == userId)
					// Is the owner
					reponse = new GroupRights(true, true, true, true);
				else // General case
					reponse = nodeManager.getRights(userId, groupId, p.getRootNode().getId());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return reponse;
	}

	public String getPortfolioShared(Long userId) {
		StringBuilder out = new StringBuilder();

		List<Map<String, Object>> portfolios = portfolioDao.getPortfolioShared(userId);
		out.append("<portfolios>");
		Iterator<Map<String, Object>> it = portfolios.iterator();
		Map<String, Object> current = null;
		while (it.hasNext()) {
			current = it.next();
			Long gid = MapUtils.getLong(current, "gid");
			UUID portfolioUuid = (UUID) current.get("portfolio");
			out.append("<portfolio gid='" + gid + "' portfolio='" + portfolioUuid.toString() + "'/>");
		}
		out.append("</portfolios>");
		return out.toString();
	}

	public String getPortfolioByCode(MimeType mimeType, String portfolioCode, Long userId, Long groupId,
			String resources, long substid) {
		UUID pid = portfolioDao.getPortfolioUuidByPortfolioCode(portfolioCode);
		Boolean withResources = false;
		String result = "";

		try {
			withResources = Boolean.parseBoolean(resources);
		} catch (Exception ex) {
		}

		if (withResources) {
			try {
				return getPortfolio(new MimeType("text/xml"), pid.toString(), userId, groupId, null, null, null,
						substid, null).toString();
			} catch (MimeTypeParseException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Portfolio p = portfolioDao.findById(pid);
				result += DomUtils.getXmlAttributeOutput("id", p.getId().toString()) + " ";
				result += DomUtils.getXmlAttributeOutput("root_node_id", p.getRootNode().getId().toString()) + " ";
				result += ">";
				result += nodeManager.getNodeXmlOutput(p.getRootNode().getId().toString(), false, "nodeRes", userId,
						groupId, null, false);
				result += "</portfolio>";
			} catch (DoesNotExistException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public GroupRights getPortfolioRight(Long userId, Long groupId, String portfolioUuid, String droit) {
		Portfolio result = null;
		GroupRights reponse = null;

		try {
			result = portfolioDao.findById(UUID.fromString(portfolioUuid));
			if (result.getCredential().equals(new Credential(userId))) {
				// Is the owner
				reponse = new GroupRights(new GroupRightsId(), true, true, true, true, true);
			} else // General case
				reponse = nodeManager.getRights(userId, groupId, result.getRootNode().getId());
		} catch (DoesNotExistException e) {
			e.printStackTrace();
		}
		return reponse;
	}

	public int deletePortfolio(String portfolioUuid, Long userId, Long groupId) throws Exception {
		Integer status = Integer.valueOf(0);
		boolean hasRights = false;

		GroupRights rights = getRightsOnPortfolio(userId, groupId, portfolioUuid);
		if (rights.isDelete() || credentialDao.isAdmin(userId)) {
			hasRights = true;
		}

		if (hasRights) {
			try {
				// S'il y a quelque chose de particulier, on s'assure que tout soit bien nettoyé
				// de façon separée
				List<GroupRightInfo> griList = groupRightInfoDao.getByPortfolioID(portfolioUuid);
				for (java.util.Iterator<GroupRightInfo> it = griList.iterator(); it.hasNext();) {
					groupRightInfoDao.remove(it.next());
				}

				/// Resources
				List<ResourceTable> rtList = resourceTableDao.getResourcesByPortfolioUUID(portfolioUuid);
				for (java.util.Iterator<ResourceTable> it = rtList.iterator(); it.hasNext();) {
					resourceTableDao.remove(it.next());
				}

				rtList = resourceTableDao.getContextResourcesByPortfolioUUID(portfolioUuid);
				for (java.util.Iterator<ResourceTable> it = rtList.iterator(); it.hasNext();) {
					resourceTableDao.remove(it.next());
				}

				rtList = resourceTableDao.getResourcesOfResourceByPortfolioUUID(portfolioUuid);
				for (java.util.Iterator<ResourceTable> it = rtList.iterator(); it.hasNext();) {
					resourceTableDao.remove(it.next());
				}

				/// Nodes
				List<Node> nodes = nodeDao.getNodes(portfolioUuid);
				for (java.util.Iterator<Node> it = nodes.iterator(); it.hasNext();) {
					nodeDao.remove(it.next());
				}

				/// Portfolio
				portfolioDao.removeById(portfolioUuid);

				/// delete portfolio from Group
				List<PortfolioGroupMembers> pgmList = portfolioGroupMembersDao.getByPortfolioID(portfolioUuid);
				for (java.util.Iterator<PortfolioGroupMembers> it = pgmList.iterator(); it.hasNext();) {
					portfolioGroupMembersDao.remove(it.next());
				}
			} catch (Exception e) {
				status = Integer.valueOf(1);
			}
		}
		return status.intValue();
	}

	public boolean isOwner(Long id, String portfolioUuid) {
		return portfolioDao.isOwner(id, portfolioUuid);
	}

	public boolean putPortfolioOwner(String portfolioUuid, long newOwner) {
		return portfolioDao.putPortfolioOwner(portfolioUuid, newOwner);
	}

	public Portfolio putPortfolioConfiguration(String portfolioUuid, Boolean portfolioActive, Long userId)
			throws BusinessException {
		if (!credentialDao.isAdmin(userId)) {
			throw new GenericBusinessException("No admin right");
		}
		return portfolioDao.putPortfolioConfiguration(portfolioUuid, portfolioActive);
	}

	public Object getPortfolios(MimeType outMimeType, long userId, long groupId, Boolean portfolioActive, long substid,
			Boolean portfolioProject, String projectId, Boolean countOnly, String search) {
		return portfolioDao.getPortfolios(userId, substid, portfolioActive);
	}

	public boolean putPortfolio(MimeType inMimeType, MimeType outMimeType, String in, String portfolioUuid, Long userId,
			Boolean portfolioActive, int groupId, UUID portfolioModelId) throws Exception {
		StringBuffer outTrace = new StringBuffer();

		Portfolio resPortfolio = portfolioDao.getPortfolio(portfolioUuid);
		if (resPortfolio != null) {
			// Le portfolio existe donc on regarde si modele ou pas
			portfolioModelId = resPortfolio.getModelId();
		} else {
			resPortfolio = new Portfolio();
		}

		if (PhpUtil.empty(userId) || !ValidateUtil.isUnsignedId(0)) {
			if (resPortfolio != null)
				userId = resPortfolio.getCredential().getId();
		}

		if (in.length() > 0) {
			Document doc = DomUtils.xmlString2Document(in, outTrace);

			org.w3c.dom.Node rootNode = (doc.getElementsByTagName("portfolio")).item(0);
			if (rootNode == null) {
				throw new GenericBusinessException("Root Node (portfolio) not found !");
			} else {
				rootNode = (doc.getElementsByTagName("asmRoot")).item(0);

				String rootNodeUuid = UUID.randomUUID().toString();
				org.w3c.dom.Node idAtt = rootNode.getAttributes().getNamedItem("id");
				if (idAtt != null) {
					String tempId = idAtt.getNodeValue();
					if (tempId.length() > 0) {
						rootNodeUuid = tempId;
					}
				}

				if (resPortfolio.getId() != null) {
					portfolioDao.changePortfolio(resPortfolio);
				} else {
					portfolioDao.addPortfolio(rootNodeUuid, null, userId, resPortfolio);
				}

				nodeManager.writeNode(new Node(UUID.fromString(rootNodeUuid)), portfolioUuid, portfolioModelId, userId,
						0, null, null, 0, 0, true, null, false);
			}
		}

		portfolioDao.putPortfolioConfiguration(portfolioUuid, portfolioActive);

		return true;
	}

}
