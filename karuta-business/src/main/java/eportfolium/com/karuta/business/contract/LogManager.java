package eportfolium.com.karuta.business.contract;

import java.sql.Connection;
import java.sql.SQLException;

public interface LogManager {

	boolean addLog(String url, String method, String headers, String inBody, String outBody, int code);

	void transferLogTable(Connection con) throws SQLException;

	void removeLogs();

}
