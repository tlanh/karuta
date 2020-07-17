package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.business.ServiceTest;
import eportfolium.com.karuta.consumer.repositories.ConfigurationRepository;
import eportfolium.com.karuta.model.bean.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class ConfigurationManagerTest {
    @MockBean
    private ConfigurationRepository configurationRepository;

    @SpyBean
    private ConfigurationManager manager;

    @Before
    public void setup() {
        manager.clear();

        Configuration configuration = new Configuration();

        configuration.setName("foo");
        configuration.setValue("bar");

        doReturn(Collections.singletonList(configuration))
                .when(configurationRepository)
                .findAll();
    }

    @Test
    public void loadConfiguration() {
        manager.loadConfiguration();

        assertEquals("bar", manager.get("foo"));
    }

    @Test
    public void get() {
        String result = manager.get("foo");

        assertEquals("bar", result);

        verify(manager).loadConfiguration();
        verify(configurationRepository).findAll();

        manager.get("foo");

        verifyNoMoreInteractions(configurationRepository);
    }

    @Test
    public void getMultiple() {
        Map<String, String> result = manager.getMultiple(Arrays.asList("foo", "test"));

        assertEquals("bar", result.get("foo"));
        assertEquals("", result.get("test"));
    }

    @Test
    public void getKarutaURL_WithSSLEnabled() {
        doReturn("1")
                .when(manager)
                .get("ssl_enabled");

        doReturn("my-super-site.com")
                .when(manager)
                .get("domain");

        assertEquals("https://my-super-site.com", manager.getKarutaURL());
    }

    @Test
    public void getKarutaURL_WithSSLDisabled() {
        doReturn("0")
                .when(manager)
                .get("ssl_enabled");

        doReturn("my-super-site.com")
                .when(manager)
                .get("domain");

        assertEquals("http://my-super-site.com", manager.getKarutaURL());
    }
}