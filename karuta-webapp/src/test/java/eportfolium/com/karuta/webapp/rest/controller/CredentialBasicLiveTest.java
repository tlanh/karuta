package eportfolium.com.karuta.webapp.rest.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class CredentialBasicLiveTest {

	// http://localhost:8080/RESTfulExample/json/product/post
	@Test
	public void registerTest() {

		try {
			URL url = new URL("http://localhost:8080/karuta-webapp/register");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/xml");

			String input = "<users><user><username>test2</username>";
			input += "<firstname>test2</firstname>";
			input += "<lastname>test2</lastname>";
			input += "<admin>1</admin>";
			input += "<designer>1</designer><email>test2@test.com</email><active>1</active>";
			input += "<substitute>0</substitute></user></users>";

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		}

	}

}