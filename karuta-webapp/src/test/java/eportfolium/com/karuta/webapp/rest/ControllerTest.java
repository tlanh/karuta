package eportfolium.com.karuta.webapp.rest;

import eportfolium.com.karuta.business.contract.*;
import eportfolium.com.karuta.business.contract.SecurityManager;
import eportfolium.com.karuta.consumer.repositories.*;
import eportfolium.com.karuta.model.bean.Credential;
import eportfolium.com.karuta.webapp.component.SecurityConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RestApplication.class, SecurityConfiguration.class})
public abstract class ControllerTest {

    @MockBean
    protected GroupInfoRepository groupInfoRepository;

    @MockBean
    protected NodeRepository nodeRepository;

    @MockBean
    protected CredentialSubstitutionRepository credentialSubstitutionRepository;

    @MockBean
    protected CredentialGroupRepository credentialGroupRepository;

    @MockBean
    protected GroupRightInfoRepository groupRightInfoRepository;

    @MockBean
    protected PortfolioGroupRepository portfolioGroupRepository;

    @MockBean
    protected CredentialRepository credentialRepository;

    @MockBean
    protected ConfigurationRepository configurationRepository;

    @MockBean
    protected GroupUserRepository groupUserRepository;

    @MockBean
    protected PortfolioRepository portfolioRepository;

    @MockBean
    protected CredentialGroupMembersRepository credentialGroupMembersRepository;

    @MockBean
    protected GroupRightsRepository groupRightsRepository;

    @MockBean
    protected ResourceRepository resourceRepository;

    @MockBean
    protected PortfolioGroupMembersRepository portfolioGroupMembersRepository;




    @SpyBean
    protected ConfigurationManager configurationManager;

    @MockBean
    protected EmailManager emailManager;

    @SpyBean
    protected FileManager fileManager;

    @SpyBean
    protected GroupManager groupManager;

    @SpyBean
    @Lazy
    protected PortfolioManager portfolioManager;

    @SpyBean
    protected NodeManager nodeManager;

    @SpyBean
    protected ResourceManager resourceManager;

    @SpyBean
    protected SecurityManager securityManager;

    @SpyBean
    protected UserManager userManager;


    protected PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();


    protected MockMvc mvc;

    @Autowired
    protected WebApplicationContext context;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    public CustomUserDetailsService customUserDetailsService;

    protected final Long userId = 42L;

    @Before
    public void setup() {
      // Configure how save behave, otherwise it returns null
      when(credentialRepository.save(any(Credential.class))).thenReturn(new Credential());

        this.mvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation)
                            .operationPreprocessors()
                            .withResponseDefaults(prettyPrint())
                            .withRequestDefaults(prettyPrint())
                        .and()
                            .uris()
                            .withHost("localhost:8080/rest/api")
                            .withPort(80))
                .apply(springSecurity())
                .build();
    }

    public ResultActions get(String endpoint) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(endpoint));
    }

    public ResultActions post(String endpoint, String content) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_XML)
                .content(content));
    }

    public MockHttpServletRequestBuilder postBuilder(String endpoint) {
        return MockMvcRequestBuilders.post(endpoint);
    }

    public MockHttpServletRequestBuilder putBuilder(String endpoint) {
        return MockMvcRequestBuilders.put(endpoint).contentType(MediaType.APPLICATION_XML);
    }

    public MockHttpServletRequestBuilder deleteBuilder(String endpoint) {
        return MockMvcRequestBuilders.delete(endpoint);
    }
}
