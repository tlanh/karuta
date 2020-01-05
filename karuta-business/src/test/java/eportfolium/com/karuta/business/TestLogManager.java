package eportfolium.com.karuta.business;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eportfolium.com.karuta.business.contract.LogManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:application-context-mysql-test.xml")
public class TestLogManager {

	@Autowired
	private LogManager logManager;

	@Test
	public void addLog() {
		logManager.addLog("testUrl", "testMethod", "testHeader", "testInBody", "testOutBody", 0);
	}

}
