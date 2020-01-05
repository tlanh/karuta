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
