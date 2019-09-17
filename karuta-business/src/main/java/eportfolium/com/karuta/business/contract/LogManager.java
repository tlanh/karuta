package eportfolium.com.karuta.business.contract;

public interface LogManager {

	boolean addLog(String url, String method, String headers, String inBody, String outBody, int code);

}
