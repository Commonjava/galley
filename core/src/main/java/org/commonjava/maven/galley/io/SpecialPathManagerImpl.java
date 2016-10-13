/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.GalleyException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.FilePatternMatcher;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.PathPatternMatcher;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.SpecialPathMatcher;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jdcasey on 1/27/16.
 */
@ApplicationScoped
public class SpecialPathManagerImpl
        implements SpecialPathManager
{
    private List<SpecialPathInfo> specialPaths;

    public SpecialPathManagerImpl()
    {
        specialPaths = new ArrayList<>();
        specialPaths.addAll( SpecialPathConstants.STANDARD_SPECIAL_PATHS );
    }

    @Override
    public synchronized void registerSpecialPathInfo( SpecialPathInfo pathInfo )
    {
        specialPaths.add( pathInfo );
    }

    @Override
    public synchronized void deregisterSpecialPathInfo( SpecialPathInfo pathInfo )
    {
        specialPaths.remove( pathInfo );
    }

    @Deprecated
    @Override
    public SpecialPathInfo getSpecialPathInfo( ConcreteResource resource )
    {
        if ( resource != null )
        {
            return getSpecialPathInfo(resource.getLocation(), resource.getPath() );
        }

        // TODO: Return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR or something non-null?
        return null;
    }

    @Deprecated
    @Override
    public SpecialPathInfo getSpecialPathInfo( Transfer transfer )
    {
        if ( transfer != null )
        {
            return getSpecialPathInfo(transfer.getLocation(), transfer.getPath() );
        }

        // TODO: Return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR or something non-null?
        return null;
    }

    @Deprecated
    @Override
    public SpecialPathInfo getSpecialPathInfo( Location location, String path )
    {
        SpecialPathInfo firstHit = null;
        // Location is not used in current SpecialPathMatcher impl classes, so removed the null check.
        if ( path != null )
        {
            for ( SpecialPathInfo info : specialPaths )
            {
                if ( info.getMatcher().matches( location, path ) )
                {
                    if ( firstHit != null )
                    {
                        Logger logger = LoggerFactory.getLogger( getClass() );
                        logger.error( "Duplicate special-path registration for: {}:{}. Using: {}", location, path, firstHit );
                    }
                    else
                    {
                        firstHit = info;
                    }
                }
            }
        }

        // TODO: Return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR based on path if firstHit is null!

        return firstHit;
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( String path )
    {
        // TODO: seems that all SpecialPathMatcher impl classes does not use the Location, so we should consider to remove the Location arg next step.
        // TODO: When path is null, return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR or something non-null?
        return path == null ? null : getSpecialPathInfo( null, path );
    }
}
