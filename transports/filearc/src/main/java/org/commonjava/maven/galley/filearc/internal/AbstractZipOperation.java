/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.filearc.internal;

import static org.commonjava.maven.galley.filearc.internal.util.ZipUtils.isJar;

import java.io.File;

import org.commonjava.maven.galley.filearc.internal.util.ZipUtils;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractZipOperation
{

    protected final Logger logger = LoggerFactory.getLogger( getClass() );

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
        String fp = PathUtils.normalize( basePath, path );
        if ( fp.startsWith( "/" ) )
        {
            fp = fp.substring( 1 );
        }
        this.fullPath = fp;

        logger.debug( "Got archive reference with the following:\n  File: {}\n  Base-Path: {}\n  Full-Path: {}\n",
                      zipFile, basePath, fullPath );
    }

    @Override
    public String toString()
    {
        return String.format( "%s [file: %s, base-path: %s, full-path: %s]", getClass().getSimpleName(), zipFile,
                              basePath, fullPath );
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
