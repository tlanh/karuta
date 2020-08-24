package eportfolium.com.karuta.business.security;

import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.consumer.repositories.NodeRepository;
import eportfolium.com.karuta.model.bean.GroupRights;
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
@WithMockUser
public class RightsAnnotationsTest {
    @SpyBean
    private NodeManager nodeManager;

    @MockBean
    private NodeRepository nodeRepository;

    @SpyBean
    private Foo foo;

    private static final UUID nodeId = UUID.randomUUID();
    private static final Long userId = 0L;

    @Service
    public static class Foo {
        @CanReadOrPublic
        public void doSomethingOnPublic(@P("id") UUID nodeId) { }

        @CanRead
        public void readSomething(@P("id") UUID nodeId) { }

        @CanDelete
        public void deleteSomething(@P("id") UUID nodeId) { }
    }

    @Test(expected = AccessDeniedException.class)
    public void doSomething_NotPublic() {
        doReturn(false)
                .when(nodeRepository)
                .isPublic(nodeId);

        foo.doSomethingOnPublic(nodeId);
    }

    @Test
    public void doSomething_Public() {
        doReturn(true)
                .when(nodeRepository)
                .isPublic(nodeId);

        foo.doSomethingOnPublic(nodeId);
    }


    @Test(expected = AccessDeniedException.class)
    public void canRead_WithoutRight() {

        doReturn(new GroupRights())
                .when(nodeManager)
                .getRights(userId, nodeId);

        foo.readSomething(nodeId);
    }

    @Test
    public void canRead_WithRight() {
        GroupRights groupRights = new GroupRights();
        groupRights.setRead(true);

        doReturn(groupRights)
                .when(nodeManager)
                .getRights(userId, nodeId);

        foo.readSomething(nodeId);
    }

    @Test(expected = AccessDeniedException.class)
    public void canDelete_WithoutRight() {
        doReturn(new GroupRights())
                .when(nodeManager)
                .getRights(userId, nodeId);

        foo.readSomething(nodeId);
    }

    @Test
    public void canDelete_WithRight() {
        GroupRights groupRights = new GroupRights();
        groupRights.setDelete(true);

        doReturn(groupRights)
                .when(nodeManager)
                .getRights(userId, nodeId);

        foo.deleteSomething(nodeId);
    }
}
