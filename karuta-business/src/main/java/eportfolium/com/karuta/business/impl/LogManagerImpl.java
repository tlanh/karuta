package eportfolium.com.karuta.business.impl;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eportfolium.com.karuta.business.contract.LogManager;
import eportfolium.com.karuta.consumer.contract.dao.LogTableDao;
import eportfolium.com.karuta.model.bean.LogTable;
import eportfolium.com.karuta.util.JavaTimeUtil;

@Service
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

}
