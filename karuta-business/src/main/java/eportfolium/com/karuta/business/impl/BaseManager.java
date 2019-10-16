package eportfolium.com.karuta.business.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import eportfolium.com.karuta.model.bean.GroupRights;
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
		return getRights(userId, groupId, UUID.fromString(nodeUuid));
	}

	/**
	 * test pour l'affichage des différentes méthodes de Node
	 */
	public GroupRights getRights(Long userId, Long groupId, UUID nodeUuid) {

		// Par defaut accès à rien
		GroupRights rights = new GroupRights();

		if (credentialDao.isAdmin(userId)) {
			rights = new GroupRights(true, true, true, true);
		} else if (credentialDao.isDesigner(userId, nodeUuid)) /// Droits via le partage totale (obsolete) ou si c'est
		{
			rights = new GroupRights(true, true, true, true);
		} else {

			if (PhpUtil.empty(groupId)) {
				rights = groupRightsDao.getRightsByIdAndUser(nodeUuid, userId);
			}

			rights = groupRightsDao.getRightsByUserAndGroup(nodeUuid, userId, groupId);
			rights = groupRightsDao.getSpecificRightsForUser(nodeUuid, userId);
			rights = groupRightsDao.getPublicRightsByGroupId(nodeUuid, groupId);

		} // fin else

		/// Public rights (last chance for rights)
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
		long t_01 = 0;
		long t_02 = 0;
		long t_03 = 0;
		long t_04 = 0;
		long t_05 = 0;
		long t_06 = 0;

		long totalConstruct = 0;
		long totalAggregate = 0;
		long totalParse = 0;
		long totalAdopt = 0;
		long totalReconstruct = 0;

		StringBuilder data = new StringBuilder(256);
		Node node = null;
		GroupRights gr = null;

		if (CollectionUtils.isNotEmpty(nodes)) {
			for (Pair<Node, GroupRights> pair : nodes) {
				node = pair.getLeft();
				gr = pair.getRight();

				data.setLength(0);
				t_01 = System.currentTimeMillis();
				UUID nodeUuid = node.getId();
				if (nodeUuid == null)
					continue; // Cas où on a des droits sur plus de noeuds qui ne sont pas dans le portfolio

				String childsId = node.getChildrenStr();

				String type = node.getAsmType();

				data.append("<");
				data.append(type);
				data.append(" ");

				String xsi_type = node.getXsiType();
				if (null == xsi_type)
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

				ResourceTable res_context_node = node.getContextResource();
				if (res_context_node != null && res_context_node.getId().toString().length() > 0) {
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

				ResourceTable res_node_uuid = node.getResource();
				if (res_node_uuid != null && res_node_uuid.getId().toString().length() > 0) {
					String nodeContent = res_node_uuid.getContent();
					Date resModifdate = res_node_uuid.getModifDate();
					if (nodeContent != null) {
						data.append("<asmResource contextid=\"");
						data.append(nodeUuid);
						data.append("\" id=\"");
						data.append(res_node_uuid);
						data.append("\" last_modif=\"");
						data.append(resModifdate);
						data.append("\" xsi_type=\"");
						data.append(node.getXsiType());
						data.append("\">");
						data.append(nodeContent.trim());
						data.append("</asmResource>");
					}
				}

				t_02 = System.currentTimeMillis();

				String snode = data.toString();

				t_03 = System.currentTimeMillis();

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

				t_06 = System.currentTimeMillis();

				totalConstruct += t_02 - t_01;
				totalAggregate += t_03 - t_02;
				totalParse += t_04 - t_03;
				totalAdopt += t_05 - t_04;
				totalReconstruct += t_06 - t_05; //

				System.out.println("======= Loop =======");
				System.out.println("Retrieve data: " + (t_02 - t_01));
				System.out.println("Aggregate data: " + (t_03 - t_02));
				System.out.println("Parse as XML: " + (t_04 - t_03));
				System.out.println("Adopt XML: " + (t_05 - t_04));
				System.out.println("Store for reconstruction: " + (t_06 - t_05)); //
			}
		}

		System.out.println("======= Total =======");
		System.out.println("Construct: " + totalConstruct);
		System.out.println("Aggregate: " + totalAggregate);
		System.out.println("Parsing: " + totalParse);
		System.out.println("Adopt: " + totalAdopt);
		System.out.println("Reconstruction: " + totalReconstruct); //
	}

	protected void reconstructTree(StringBuilder data, t_tree node, Map<String, t_tree> entries) {
		if (node.childString == null)
			return;

		String[] childsId = node.childString.split(",");
		data.append(node.data);
//		String data = node.data;

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
