package eportfolium.com.karuta.webapp.rest.controller;

import eportfolium.com.karuta.document.CredentialDocument;
import eportfolium.com.karuta.document.LoginDocument;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.model.bean.CredentialSubstitution;
import eportfolium.com.karuta.webapp.rest.AsUser;
import eportfolium.com.karuta.webapp.rest.ControllerTest;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class CredentialControllerTest extends ControllerTest {

    @Test
    @AsUser
    public void getCredential() throws Exception {
        Credential credential = new Credential();

        credential.setId(42L);
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");
        credential.setEmail("john@doe.com");
        credential.setCredentialSubstitution(new CredentialSubstitution());
        credential.setIsAdmin(1);
        credential.setActive(1);

        doReturn(credential)
                .when(credentialRepository)
                .getUserInfos(userId);

        get("/credential")
                .andExpect(status().isOk())
                .andDo(document("get-credential"));
    }

    @Test
    public void login_WithWrongCredentials() throws Exception {
        String xml = "<credential>" +
                    "<login>foo</login>" +
                    "<password>wRongPass</password>" +
                "</credential>";

        doReturn(null)
                .when(securityManager)
                .login(any(LoginDocument.class));

        post("/credential/login", xml)
                .andExpect(status().isForbidden())
                .andDo(document("login-wrong"));
    }

    @Test
    public void login_WithRightCredentials() throws Exception {
        String xml = "<credential>" +
                "<login>jdoe</login>" +
                "<password>s3cr3t</password>" +
              "</credential>";

        Credential credential = new Credential();
        credential.setLogin("jdoe");
        credential.setDisplayFirstname("John");
        credential.setDisplayLastname("Doe");
        credential.setEmail("john@doe.com");
        credential.setIsAdmin(1);
        credential.setActive(1);

        doReturn(new CredentialDocument(credential))
                .when(securityManager)
                .login(any(LoginDocument.class));

        post("/credential/login", xml)
                .andExpect(status().isOk())
                .andDo(document("login-right"));
    }
}