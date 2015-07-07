package org.commonjava.maven.galley.filearc.internal;

import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.isJar;

import java.io.File;

import org.commonjava.maven.galley.filearc.internal.util.ZipUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

abstract class AbstractZipOperation
{

    private final Location location;

    private final String path;

    private final File zipFile;

    private final String basePath;

    private final String fullPath;

    protected Transfer transfer;

    protected ConcreteResource resource;

    protected AbstractZipOperation( final Transfer transfer )
    {
        this( transfer.getLocation(), transfer.getPath() );
        this.transfer = transfer;
    }

    protected AbstractZipOperation( final ConcreteResource resource )
    {
        this( resource.getLocation(), resource.getPath() );
        this.resource = resource;
    }

    protected AbstractZipOperation( final Location location, final String path )
    {
        this.location = location;
        this.path = path;

        final String uri = location.getUri();
        this.zipFile = ZipUtils.getArchiveFile( uri );
        String basePath = ZipUtils.getArchivePath( uri );
        if ( basePath == null )
        {
            basePath = "";
        }
        this.basePath = basePath;

        this.fullPath = basePath + path;
    }
    
    protected ConcreteResource getResource()
    {
        return resource;
    }

    protected Transfer getTransfer()
    {
        return transfer;
    }

    protected boolean isJarOperation()
    {
        return isJar( location.getUri() );
    }

    protected String getFullPath()
    {
        return fullPath;
    }

    protected Location getLocation()
    {
        return location;
    }

    protected String getPath()
    {
        return path;
    }

    protected File getZipFile()
    {
        return zipFile;
    }

    protected String getBasePath()
    {
        return basePath;
    }

}
