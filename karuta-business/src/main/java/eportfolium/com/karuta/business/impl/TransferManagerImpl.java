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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.ConfigurationManager;
import eportfolium.com.karuta.business.contract.FileManager;
import eportfolium.com.karuta.business.contract.GroupManager;
import eportfolium.com.karuta.business.contract.LogManager;
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.business.contract.PortfolioManager;
import eportfolium.com.karuta.business.contract.ResourceManager;
import eportfolium.com.karuta.business.contract.TransferManager;
import eportfolium.com.karuta.business.contract.UserManager;

/**
 * @author mlengagne
 *
 *         Contains migration scripts. To transfer data from a database to
 *         another.
 */
@Service
@Transactional
public class TransferManagerImpl implements TransferManager {

	@Autowired
	private ResourceManager resourceManager;

	@Autowired
	private PortfolioManager portfolioManager;

	@Autowired
	private NodeManager nodeManager;

	@Autowired
	private LogManager logManager;

	@Autowired
	private UserManager userManager;

	@Autowired
	private GroupManager groupManager;

	@Autowired
	private FileManager fileManager;

	@Autowired
	private ConfigurationManager configurationManager;

	private Connection initConnection() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String url = "jdbc:mysql://localhost:3306/karuta-backend?useUnicode=true&characterEncoding=utf-8";
		Connection con;
		con = DriverManager.getConnection(url, "karuta", "karuta_password");
		return con;
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferDataFromMySQLToMongoDB() throws SQLException {
		Connection con = null;
		Map<String, String> nodeIds = null;
		try {
			con = initConnection(); // ici c'est une base MySQL ... à remplacer selon votre BDD.
			nodeManager.removeAnnotations();
			logManager.removeLogs();
			fileManager.removeData();
			userManager.removeUsers();
			groupManager.removeGroups();
			nodeManager.removeNodes();
			resourceManager.removeResources();
			portfolioManager.removePortfolioGroups();
			portfolioManager.removePortfolios();

			fileManager.transferDataTable(con);
			logManager.transferLogTable(con);
			configurationManager.transferConfigurationTable(con);

			Map<Long, Long> userIds = userManager.transferCredentialTable(con);
			userManager.transferCredentialSubstitutionTable(con, userIds);
			Map<String, String> rtIds = resourceManager.transferResourceTable(con, userIds);
			Map<String, String> portIds = portfolioManager.transferPortfolioTable(con, userIds);

			Map<Long, Long> griIds = groupManager.transferGroupRightInfoTable(con, portIds);
			Map<Long, Long> giIds = groupManager.transferGroupInfoTable(con, griIds);
			groupManager.transferGroupRightsTable(con, griIds);
			groupManager.transferGroupGroupTable(con, giIds);
			Map<Long, Long> cgIds = groupManager.transferCredentialGroupTable(con);
			userManager.transferCredentialGroupMembersTable(con, userIds, cgIds);
			groupManager.transferGroupUserTable(con, giIds, userIds);

			Map<Long, Long> pgIds = portfolioManager.transferPortfolioGroupTable(con);
			portfolioManager.transferParentPortfolioGroup(con, pgIds);
			portfolioManager.transferPortfolioGroupMembersTable(con, portIds, pgIds);

			nodeIds = nodeManager.transferNodeTable(con, rtIds, portIds, userIds);
			nodeManager.transferAnnotationTable(con, nodeIds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.close();
			}
		}
	}

}
