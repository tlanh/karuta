package eportfolium.com.karuta.business.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eportfolium.com.karuta.business.contract.LogManager;
import eportfolium.com.karuta.consumer.contract.dao.LogTableDao;
import eportfolium.com.karuta.model.bean.LogTable;
import eportfolium.com.karuta.util.JavaTimeUtil;

@Service
@Transactional
public class LogManagerImpl implements LogManager {

	@Autowired
	private LogTableDao logTableDao;

	public boolean addLog(String url, String method, String headers, String inBody, String outBody, int code) {
		final Date now = JavaTimeUtil.toJavaDate(LocalDateTime.now(JavaTimeUtil.date_default_timezone));
		LogTable logTable = new LogTable(null, now, url, method, headers, inBody, outBody, code);
		boolean result = false;
		try {
			logTableDao.persist(logTable);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;

	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void transferLogTable(Connection con) throws SQLException {
		ResultSet res = logTableDao.findAll("log_table", con);
		LogTable lg = null;
		while (res.next()) {
			lg = new LogTable();
			lg.setId(res.getInt("log_id"));
			lg.setLogDate(res.getDate("log_date"));
			lg.setLogUrl(res.getString("log_url"));
			lg.setLogMethod(res.getString("log_method"));
			lg.setLogHeaders(res.getString("log_headers"));
			lg.setLogInBody(res.getString("log_in_body"));
			lg.setLogOutBody(res.getString("log_out_body"));
			lg.setLogCode(res.getInt("log_code"));
			logTableDao.merge(lg);
		}
	}

	@Override
	@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW)
	public void removeLogs() {
		logTableDao.removeAll();
	}

}
