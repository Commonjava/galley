package org.commonjava.maven.galley.transport.htcli.testutil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.commonjava.maven.galley.auth.PasswordEntry;
import org.commonjava.maven.galley.event.NoOpFileEventManager;
import org.commonjava.maven.galley.io.NoOpTransferDecorator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.auth.PasswordManager;
import org.commonjava.maven.galley.spi.event.FileEventManager;
import org.commonjava.maven.galley.spi.io.TransferDecorator;
import org.commonjava.maven.galley.transport.htcli.Http;
import org.commonjava.maven.galley.transport.htcli.HttpImpl;
import org.commonjava.util.logging.Log4jUtil;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public class HttpTestFixture
    extends ExternalResource
    implements PasswordManager
{

    public final TemporaryFolder folder = new TemporaryFolder();

    public final TestHttpServer server;

    private final FileEventManager events;

    private final TransferDecorator decorator;

    private final TestCacheProvider cache;

    private final Map<PasswordEntry, String> passwords = new HashMap<PasswordEntry, String>();

    private final Http http;

    private final String baseResource;

    static
    {
        Log4jUtil.configure( Level.DEBUG );
    }

    public HttpTestFixture( final String baseResource )
    {
        String br = baseResource;
        if ( br.startsWith( "/" ) )
        {
            br = br.substring( 1 );
        }
        if ( br.endsWith( "/" ) )
        {
            br = br.substring( 0, br.length() - 1 );
        }

        this.baseResource = br;
        server = new TestHttpServer( baseResource );

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

        http = new HttpImpl( this );
    }

    @Override
    protected void after()
    {
        server.shutdown();
        folder.delete();
        super.after();
    }

    @Override
    protected void before()
        throws Throwable
    {
        super.before();
        server.start();
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

    public Transfer getTransfer( final ConcreteResource resource )
    {
        return cache.getTransfer( resource );
    }

    public Transfer writeClasspathResourceToCache( final ConcreteResource resource, final String cpResource )
        throws IOException
    {
        final StringBuilder path = new StringBuilder();
        if ( !cpResource.startsWith( baseResource ) )
        {
            path.append( baseResource );
        }

        if ( !cpResource.startsWith( "/" ) )
        {
            path.append( '/' );
        }

        path.append( cpResource );

        return cache.writeClasspathResourceToCache( resource, path.toString() );
    }

    public Transfer writeToCache( final ConcreteResource resource, final String content )
        throws IOException
    {
        return cache.writeToCache( resource, content );
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

    public void registerException( final String url, final String error )
    {
        server.registerException( url, error );
    }

    public Map<String, String> getRegisteredErrors()
    {
        return server.getRegisteredErrors();
    }

    @Override
    public String getPassword( final PasswordEntry id )
    {
        return passwords.get( id );
    }

    public void setPassword( final PasswordEntry id, final String password )
    {
        passwords.put( id, password );
    }
}
