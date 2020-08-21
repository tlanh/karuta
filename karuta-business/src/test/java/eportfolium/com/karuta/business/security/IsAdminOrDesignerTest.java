package eportfolium.com.karuta.business.security;

import eportfolium.com.karuta.business.security.test.AsAdmin;
import eportfolium.com.karuta.business.security.test.AsDesigner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IsAdminOrDesignerTest {
    @Service
    public static class Foo {
        @IsAdminOrDesigner
        public void doNothing() { }
    }

    @SpyBean
    private Foo foo;

    @Test(expected = AccessDeniedException.class)
    @WithMockUser
    public void beingNeither() {
        foo.doNothing();
    }

    @Test
    @AsDesigner
    public void beingDesigner() {
        foo.doNothing();
    }

    @Test
    @AsAdmin
    public void beingAdmin() {
        foo.doNothing();
    }
}
