package eportfolium.com.karuta.business.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eportfolium.com.karuta.consumer.contract.dao.CredentialDao;
import eportfolium.com.karuta.consumer.contract.dao.GroupRightsDao;
import eportfolium.com.karuta.consumer.contract.dao.NodeDao;
import eportfolium.com.karuta.model.bean.GroupRightInfo;
import eportfolium.com.karuta.model.bean.GroupRights;
import eportfolium.com.karuta.model.bean.GroupRightsId;
import eportfolium.com.karuta.model.bean.Node;
import eportfolium.com.karuta.model.bean.ResourceTable;
import eportfolium.com.karuta.util.PhpUtil;

public abstract class BaseManager {

	@Autowired
	protected CredentialDao credentialDao;

	@Autowired
	protected NodeDao nodeDao;

	@Autowired
	protected GroupRightsDao groupRightsDao;

	// Help reconstruct tree
	protected class t_tree {
		String data = "";
		String type = "";
		String childString = "";
	};

	/**
	 * test pour l'affichage des différentes méthodes de Node
	 */
	public GroupRights getRights(Long userId, Long groupId, String nodeUuid) {

		GroupRights rights = null;

		if (credentialDao.isAdmin(userId)) {
			rights = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true, true, true, true, false);
		} else if (credentialDao.isDesigner(userId, nodeUuid)) /// Droits via le partage totale (obsolete) ou si c'est
		{
			rights = new GroupRights(new GroupRightsId(new GroupRightInfo(), null), true, true, true, true, false);
		} else {
			if (PhpUtil.empty(groupId)) {
				rights = groupRightsDao.getRightsByIdAndUser(nodeUuid, userId);
			}

			rights = groupRightsDao.getRightsByUserAndGroup(nodeUuid, userId, groupId);
			rights = groupRightsDao.getSpecificRightsForUser(nodeUuid, userId);
			rights = groupRightsDao.getPublicRightsByGroupId(nodeUuid, groupId);

		}

		// Si null alors par défaut défaut accès à rien
		if (rights == null) {
			rights = new GroupRights();
			rights.setId(new GroupRightsId(new GroupRightInfo(), null));
		}

		// Dernière chance pour les droits avec les droits publics.
		if (nodeDao.isPublic(nodeUuid)) {
			rights.setRead(true);
		}
		return rights;
	}

	protected boolean hasRight(Long userId, Long groupId, String nodeUuid, String right) {
		GroupRights rights = getRights(userId, groupId, nodeUuid);
		if (right.equals(GroupRights.READ))
			return rights.isRead();
		else if (right.equals(GroupRights.WRITE))
			return rights.isWrite();
		else if (right.equals(GroupRights.SUBMIT))
			return rights.isSubmit();
		else if (right.equals(GroupRights.DELETE))
			return rights.isDelete();
		else
			return false;
	}

	protected void processQuery(List<Pair<Node, GroupRights>> nodes, Map<String, Object[]> resolve,
			Map<String, t_tree> entries, Document document, DocumentBuilder documentBuilder, String role)
			throws UnsupportedEncodingException, DOMException, SQLException, SAXException, IOException {

		StringBuilder data = new StringBuilder(256);
		Node node = null;
		GroupRights gr = null;

		if (CollectionUtils.isNotEmpty(nodes)) {
			for (Pair<Node, GroupRights> pair : nodes) {
				node = pair.getLeft();
				gr = pair.getRight();

				data.setLength(0);
				final UUID nodeUuidObj = node.getId();
				String nodeUuid = null;
				if (nodeUuidObj == null)
					continue; // Cas où on a des droits sur plus de noeuds qui ne sont pas dans le portfolio
				else
					nodeUuid = nodeUuidObj.toString();

				String childsId = node.getChildrenStr();

				String type = node.getAsmType();

				data.append("<");
				data.append(type);
				data.append(" ");

				String xsi_type = node.getXsiType();
				if (xsi_type == null)
					xsi_type = "";

				String readRight = gr.isRead() ? "Y" : "N";
				String writeRight = gr.isWrite() ? "Y" : "N";
				String submitRight = gr.isSubmit() ? "Y" : "N";
				String deleteRight = gr.isDelete() ? "Y" : "N";
				String macro = gr.getRulesId();
				Date nodeDate = node.getModifDate();

				if (macro != null) {
					data.append("action=\"");
					data.append(macro);
					data.append("\" ");
				} else
					macro = "";

				data.append("delete=\"");
				data.append(deleteRight);
				data.append("\" id=\"");
				data.append(nodeUuid);
				data.append("\" read=\"");
				data.append(readRight);
				data.append("\" role=\"");
				data.append(role);
				data.append("\" submit=\"");
				data.append(submitRight);
				data.append("\" write=\"");
				data.append(writeRight);
				data.append("\" last_modif=\"");
				data.append(nodeDate);
				data.append("\" xsi_type=\"");
				data.append(xsi_type);
				data.append("\">");

				String attr = node.getMetadataWad();
				if (attr != null && !"".equals(attr)) /// Attributes exists
				{
					data.append("<metadata-wad ");
					data.append(attr);
					data.append("/>");
				} else {
					data.append("<metadata-wad/>");
				}

				attr = node.getMetadataEpm();
				if (attr != null && !"".equals(attr)) /// Attributes exists
				{
					data.append("<metadata-epm ");
					data.append(attr);
					data.append("/>");
				} else {
					data.append("<metadata-epm/>");
				}

				attr = node.getMetadata();
				if (attr != null && !"".equals(attr)) /// Attributes exists
				{
					data.append("<metadata ");
					data.append(attr);
					data.append("/>");
				} else {
					data.append("<metadata/>");
				}

				ResourceTable res_res_node = node.getResResource();
				if (res_res_node != null && res_res_node.getId().toString().length() > 0) {
					try
					{
						String nodeContent = res_res_node.getContent();
						Date resModifdate = res_res_node.getModifDate();
						if (nodeContent != null) {
							data.append("<asmResource contextid=\"");
							data.append(nodeUuid);
							data.append("\" id=\"");
							data.append(res_res_node.getId().toString());
							data.append("\" last_modif=\"");
							data.append(resModifdate);
							data.append("\" xsi_type=\"nodeRes\">");
							data.append(nodeContent.trim());
							data.append("</asmResource>");
						}
					}
					catch(EntityNotFoundException e){}
				}

				ResourceTable res_context_node = node.getContextResource();
				if (res_context_node != null && res_context_node.getId().toString().length() > 0) {
					try
					{
						String nodeContent = res_context_node.getContent();
						Date resModifdate = res_context_node.getModifDate();
						if (nodeContent != null) {
							data.append("<asmResource contextid=\"");
							data.append(nodeUuid);
							data.append("\" id=\"");
							data.append(res_context_node.getId().toString());
							data.append("\" last_modif=\"");
							data.append(resModifdate);
							data.append("\" xsi_type=\"context\">");
							data.append(nodeContent.trim());
							data.append("</asmResource>");
						} else {
							data.append("<asmResource contextid=\"");
							data.append(nodeUuid);
							data.append("\" id=\"");
							data.append(res_context_node.getId().toString());
							data.append("\" xsi_type=\"context\"/>");
						}
					}
					catch(EntityNotFoundException e){}
				}

				ResourceTable res_node = node.getResource();
				if (res_node != null && res_node.getId().toString().length() > 0) {
					try
					{
						String nodeContent = res_node.getContent();
						Date resModifdate = res_node.getModifDate();
						if (nodeContent != null) {
							data.append("<asmResource contextid=\"");
							data.append(nodeUuid);
							data.append("\" id=\"");
							data.append(res_node.getId().toString());
							data.append("\" last_modif=\"");
							data.append(resModifdate);
							data.append("\" xsi_type=\"");
							data.append(res_node.getXsiType());
							data.append("\">");
							data.append(nodeContent.trim());
							data.append("</asmResource>");
						}
					}
					catch(EntityNotFoundException e){}
				}

				String snode = data.toString();

				/// Prepare data to reconstruct tree
				t_tree entry = new t_tree();
				entry.type = type;
				entry.data = snode;
				Object[] nodeData = { snode, type };
				resolve.put(nodeUuid.toString(), nodeData);
				if (!"".equals(childsId) && childsId != null) {
					entry.childString = childsId;
				}
				entries.put(nodeUuid.toString(), entry);

			}
		}

	}

	protected void reconstructTree(StringBuilder data, t_tree node, Map<String, t_tree> entries) {
		if (node.childString == null)
			return;

		String[] childsId = node.childString.split(",");
		data.append(node.data);

		for (int i = 0; i < childsId.length; ++i) {
			String cid = childsId[i];
			if ("".equals(cid))
				continue;

			t_tree c = entries.remove(cid); // Help converge a bit faster
			if (c != null) {
				reconstructTree(data, c, entries);
			} else {
				// Node missing from query, can be related to security safe to ignore
			}
		}

		data.append("</").append(node.type).append(">");
	}

}
