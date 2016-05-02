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

    @Override
    public SpecialPathInfo getSpecialPathInfo( ConcreteResource resource )
    {
        if ( resource != null )
        {
            return getSpecialPathInfo( resource.getLocation(), resource.getPath() );
        }

        return null;
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( Transfer transfer )
    {
        if ( transfer != null )
        {
            return getSpecialPathInfo( transfer.getLocation(), transfer.getPath() );
        }

        return null;
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( Location location, String path )
    {
        SpecialPathInfo firstHit = null;
        if ( location != null && path != null )
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

        return firstHit;
    }
}
