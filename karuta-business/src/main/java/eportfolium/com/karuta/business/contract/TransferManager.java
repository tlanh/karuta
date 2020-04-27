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

package eportfolium.com.karuta.business.contract;

import java.sql.SQLException;

/**
 * @author mlengagne
 *
 *         Contains migration scripts in order to transfer data from a database
 *         to another.
 */
public interface TransferManager {

	/**
	 * Use to transfer data from MySQL to MongoDB. <br>
	 * Basically, it reads everything from MySQL one element at a time and then
	 * inserts it into MongoDB.
	 * 
	 * @return
	 * 
	 * @throws SQLException
	 */
	void transferDataFromMySQLToMongoDB() throws SQLException;

}
