package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.business.ServiceTest;
import eportfolium.com.karuta.document.ResourceDocument;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ServiceTest
public class FileManagerTest {
    @SpyBean
    private FileManager manager;

    @MockBean
    private ConfigurationManager configurationManager;

    private ArgumentCaptor<HttpPut> doAnswer(String answer) throws IOException {
        CloseableHttpClient client = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        StatusLine statusLine = mock(StatusLine.class);

        doReturn(client)
                .when(manager)
                .createClient();

        ArgumentCaptor<HttpPut> captor = ArgumentCaptor.forClass(HttpPut.class);

        doReturn(response)
                .when(client)
                .execute(captor.capture());

        doReturn(httpEntity)
                .when(response)
                .getEntity();

        doReturn(new ByteArrayInputStream(answer.getBytes()))
                .when(httpEntity)
                .getContent();

        doReturn(statusLine)
                .when(response)
                .getStatusLine();

        doReturn(200)
                .when(statusLine)
                .getStatusCode();

        return captor;
    }

    @Test
    public void updateResource_WithErrors() throws IOException {
        InputStream input = new ByteArrayInputStream("".getBytes());
        String lang = "fr";
        boolean thumb = true;

        CloseableHttpClient client = mock(CloseableHttpClient.class);

        doReturn(client)
                .when(manager)
                .createClient();

        doThrow(new IOException())
                .when(client)
                .execute(any(HttpPut.class));

        ResourceDocument document = new ResourceDocument();

        String retval = manager.updateResource(document, input, lang, thumb, "");
        assertNull(retval);
    }

    @Test
    public void updateResource_WithoutErrors_WithThumb() throws IOException {
        InputStream input = new ByteArrayInputStream("".getBytes());
        String lang = "fr";
        boolean thumb = true;

        doReturn("")
                .when(configurationManager)
                .get("fileserver");

        String answer = "Hello world";
        ArgumentCaptor<HttpPut> captor = doAnswer(answer);

        ResourceDocument document = mock(ResourceDocument.class);
        when(document.getFileid(lang)).thenReturn("foo");

        String retval = manager.updateResource(document, input, lang, thumb, "");
        assertEquals(answer, retval);

        assertEquals("/foo/thumb", captor.getValue().getURI().getPath());

        verify(configurationManager).get("fileserver");
        verifyNoMoreInteractions(configurationManager);
    }

    @Test
    public void updateResource_WithoutErrors_WithoutThumb() throws IOException {
        InputStream input = new ByteArrayInputStream("".getBytes());
        String lang = "fr";
        boolean thumb = false;

        doReturn("")
                .when(configurationManager)
                .get("fileserver");

        String answer = "foo bar";
        ArgumentCaptor<HttpPut> captor = doAnswer(answer);

        ResourceDocument document = mock(ResourceDocument.class);
        when(document.getFileid(lang)).thenReturn("foo");

        String retval = manager.updateResource(document, input, lang, thumb, "");
        assertEquals(answer, retval);

        assertEquals("/foo", captor.getValue().getURI().getPath());

        verify(configurationManager).get("fileserver");
        verifyNoMoreInteractions(configurationManager);
    }

    @Test
    public void fetchResource_WithErrors() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String lang = "fr";
        boolean thumb = true;

        CloseableHttpClient client = mock(CloseableHttpClient.class);

        doReturn(client)
                .when(manager)
                .createClient();

        doThrow(new IOException())
                .when(client)
                .execute(any(HttpGet.class));

        ResourceDocument document = new ResourceDocument();

        assertFalse(manager.fetchResource(document, output, lang, thumb, ""));

        assertEquals(0, output.size());
    }

    @Test
    public void fetchResource_WithoutErrors() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayInputStream content = new ByteArrayInputStream("hello".getBytes());

        String lang = "fr";
        boolean thumb = true;

        doReturn("")
                .when(configurationManager)
                .get("fileserver");

        CloseableHttpClient client = mock(CloseableHttpClient.class);

        doReturn(client)
                .when(manager)
                .createClient();

        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContent()).thenReturn(content);

        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getEntity()).thenReturn(httpEntity);

        ArgumentCaptor<HttpGet> captor = ArgumentCaptor.forClass(HttpGet.class);

        doReturn(response)
                .when(client)
                .execute(captor.capture());

        ResourceDocument document = mock(ResourceDocument.class);
        when(document.getFileid(lang)).thenReturn("bar");

        assertTrue(manager.fetchResource(document, output, lang, thumb, ""));

        assertEquals("/bar/thumb", captor.getValue().getURI().getPath());
        assertEquals("hello", output.toString("UTF-8"));

        verify(configurationManager).get("fileserver");
        verifyNoMoreInteractions(configurationManager);
    }
}
