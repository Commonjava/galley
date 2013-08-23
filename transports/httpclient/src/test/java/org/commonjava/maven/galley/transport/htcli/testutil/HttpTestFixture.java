package org.commonjava.maven.galley.transport.htcli.testutil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.log4j.Level;
import org.commonjava.maven.galley.auth.PasswordEntry;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;
import org.commonjava.util.logging.Log4jUtil;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class HttpTestFixture
    extends ExternalResource
{

    public final TemporaryFolder folder = new TemporaryFolder();

    public final TestHttpServer server = new TestHttpServer( "download-basic" );

    private final FileEventManager events;

    private final TransferDecorator decorator;

    private final TestCacheProvider cache;

    private final TestPasswordManager passwords;

    private final Http http;

    static
    {
        Log4jUtil.configure( Level.DEBUG );
    }

    public HttpTestFixture()
    {
        try
        {
            folder.create();
        }
        catch ( final IOException e )
        {
            throw new RuntimeException( "Failed to setup temp folder.", e );
        }

        events = new NoOpFileEventManager();

        decorator = new NoOpTransferDecorator();

        cache = new TestCacheProvider( folder.newFolder( "cache" ), events, decorator );

        passwords = new TestPasswordManager();

        http = new HttpImpl( passwords );
    }

    @Override
    protected void after()
    {
        server.after();
        folder.delete();
        super.after();
    }

    @Override
    protected void before()
        throws Throwable
    {
        super.before();
        server.before();
    }

    public TemporaryFolder getFolder()
    {
        return folder;
    }

    public TestHttpServer getServer()
    {
        return server;
    }

    public FileEventManager getEvents()
    {
        return events;
    }

    public TransferDecorator getDecorator()
    {
        return decorator;
    }

    public TestCacheProvider getCache()
    {
        return cache;
    }

    public TestPasswordManager getPasswords()
    {
        return passwords;
    }

    public Http getHttp()
    {
        return http;
    }

    public File newFile( final String fileName )
        throws IOException
    {
        return folder.newFile( fileName );
    }

    public File newFile()
        throws IOException
    {
        return folder.newFile();
    }

    public File newFolder( final String... folderNames )
    {
        return folder.newFolder( folderNames );
    }

    public File newFolder()
        throws IOException
    {
        return folder.newFolder();
    }

    public File getRoot()
    {
        return folder.getRoot();
    }

    public Transfer getCacheReference( final Resource resource )
    {
        return cache.getCacheReference( resource );
    }

    public Transfer writeClasspathResourceToCache( final Resource resource, final String cpResource )
        throws IOException
    {
        return cache.writeClasspathResourceToCache( resource, cpResource );
    }

    public Transfer writeToCache( final Resource resource, final String content )
        throws IOException
    {
        return cache.writeToCache( resource, content );
    }

    public String getPassword( final PasswordEntry id )
    {
        return passwords.getPassword( id );
    }

    public void setPassword( final PasswordEntry id, final String password )
    {
        passwords.setPassword( id, password );
    }

    public int getPort()
    {
        return server.getPort();
    }

    public Map<String, Integer> getAccessesByPath()
    {
        return server.getAccessesByPath();
    }

    public String formatUrl( final String subpath )
    {
        return server.formatUrl( subpath );
    }

    public String getBaseUri()
    {
        return server.getBaseUri();
    }

    public String getUrlPath( final String url )
        throws MalformedURLException
    {
        return server.getUrlPath( url );
    }

}
