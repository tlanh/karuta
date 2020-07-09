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

package eportfolium.com.karuta.webapp.rest.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.imsglobal.lti.launch.LtiOauthVerifier;
import org.imsglobal.lti.launch.LtiVerificationException;
import org.imsglobal.lti.launch.LtiVerificationResult;
import org.imsglobal.lti.launch.LtiVerifier;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eportfolium.com.karuta.webapp.annotation.InjectLogger;

@RestController
public class LTIController {

  @InjectLogger
  static private Logger logger;

	@PostMapping("/lti")
	public String postLTI( HttpServletRequest request, HttpServletResponse response, ModelMap map ) {
		
		LtiVerifier ltiVerifier = new LtiOauthVerifier();
		String key = request.getParameter("oauth_consumer_key");
		String secret = "testkey";
		LtiVerificationResult ltiResult = null;
		try
		{
			ltiResult = ltiVerifier.verify(request, secret);
			
		}
		catch( LtiVerificationException e )
		{
			e.printStackTrace();
		}
		
    if( ltiResult == null || !ltiResult.getSuccess() ){
    	String message = String.format("LTI error: %s\nMessage: %s\n", ltiResult.getError(), ltiResult.getMessage());
      response.setStatus(HttpStatus.FORBIDDEN.value());
      logger.error(message);
      return message;
	  } else {
	  	String message = String.format("LTI OK: %s", ltiResult.getLtiLaunchResult().getUser().getId());
	
	  	/// Login around here
	  	
	      Map<String, String> params = new HashMap<>();
	      for (String param: Collections.list(request.getParameterNames())) {
	          params.put(param, request.getParameter(param));
	      }
	      map.put("params", params);
	      map.put("launch", ltiResult.getLtiLaunchResult());
	      logger.error(message);
	      return message;
	  }
  }
	
}

