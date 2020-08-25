package eportfolium.com.karuta.business.security;

import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.consumer.repositories.CredentialRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.parameters.P;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IsAdminOrDesignerOnNodeTest {
    @MockBean
    private CredentialRepository credentialRepository;

    private static final UUID nodeId = UUID.randomUUID();
    private static final Long userId = 0L;

    @Service
    public static class Foo {
        @IsAdminOrDesignerOnNode
        public void doNothing(@P("id") UUID nodeId) { }
    }

    @SpyBean
    private Foo foo;

    @Test(expected = AccessDeniedException.class)
    @WithMockUser
    public void beingNeither() {
        foo.doNothing(nodeId);
    }

    @Test
    @WithMockUser
    public void beingDesigner() {
        doReturn(true)
                .when(credentialRepository)
                .isDesigner(userId, nodeId);

        foo.doNothing(nodeId);
    }

    @Test
    @AsAdmin
    public void beingAdmin() {
        foo.doNothing(nodeId);
    }
}
