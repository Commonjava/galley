/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.transport.htcli.testutil;

import org.commonjava.maven.galley.io.SpecialPathConstants;
import org.commonjava.maven.galley.io.SpecialPathSet;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.commonjava.maven.galley.io.SpecialPathConstants.MVN_SP_PATH_SET;
import static org.commonjava.maven.galley.io.SpecialPathConstants.NPM_SP_PATH_SET;

/**
 * Copied from {@Link SpecialPathManagerImpl} in galley-core as there is a cycle dep between these two modules.<br />
 * TODO: Maybe we should consider to move {@Link SpecialPathManagerImpl} to galley-api and remove this one.
 */
public class TestSpecialPathManager implements SpecialPathManager
{

    private List<SpecialPathInfo> stdSpecialPaths;

    private Map<String, SpecialPathSet> pkgtypes;

    public TestSpecialPathManager()
    {
        initPkgPathSets();
    }

    public void initPkgPathSets()
    {
        stdSpecialPaths = new ArrayList<>();
        stdSpecialPaths.addAll( SpecialPathConstants.STANDARD_SPECIAL_PATHS );
        pkgtypes = new ConcurrentHashMap<>(  );
        pkgtypes.put( MVN_SP_PATH_SET.getPackageType(), MVN_SP_PATH_SET );
        pkgtypes.put( NPM_SP_PATH_SET.getPackageType(), NPM_SP_PATH_SET );
    }

    @Override
    public synchronized void registerSpecialPathInfo( SpecialPathInfo pathInfo )
    {
        stdSpecialPaths.add( pathInfo );
    }

    @Override
    public void registerSpecialPathInfo( SpecialPathInfo pathInfo, final String pkgType )
    {
        pkgtypes.get(pkgType).registerSpecialPathInfo( pathInfo );
    }

    @Override
    public synchronized void deregisterSpecialPathInfo( SpecialPathInfo pathInfo )
    {
        stdSpecialPaths.remove( pathInfo );
    }

    @Override
    public void deregisterSpecialPathInfo( SpecialPathInfo pathInfo, String pkgType )
    {
        pkgtypes.get(pkgType).deregisterSpecialPathInfo( pathInfo );
    }

    @Override
    public void registerSpecialPathSet( SpecialPathSet pathSet )
    {
        pkgtypes.put( pathSet.getPackageType(), pathSet );
    }

    @Override
    public SpecialPathSet deregesterSpecialPathSet( SpecialPathSet pathSet )
    {
        return pkgtypes.remove( pathSet.getPackageType() );
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( ConcreteResource resource )
    {
        return null;
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( ConcreteResource resource, String pkgType )
    {
        if ( resource != null )
        {
            return getSpecialPathInfo( resource.getLocation(), resource.getPath(), pkgType );
        }

        return null;
    }

    @Deprecated
    @Override
    public SpecialPathInfo getSpecialPathInfo( Transfer transfer )
    {
        return null;
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( Transfer transfer, String pkgType )
    {
        if ( transfer != null )
        {
            return getSpecialPathInfo( transfer.getLocation(), transfer.getPath(), pkgType );
        }

        return null;
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( Location location, String path, String pkgType )
    {
        SpecialPathInfo info = getPathInfo( location, path, stdSpecialPaths );
        if ( info != null )
        {
            return info;
        }

        if ( pkgtypes.containsKey( pkgType ) )
        {
            info = getPathInfo( location, path, pkgtypes.get( pkgType ).getSpecialPathInfos() );
            return info;
        }
        return null;
    }

    private SpecialPathInfo getPathInfo( Location location, String path, Collection<SpecialPathInfo> from )
    {
        SpecialPathInfo firstHit = null;
        // Location is not used in current SpecialPathMatcher impl classes, so removed the null check.
        if ( path != null )
        {
            for ( SpecialPathInfo info : from )
            {
                if ( info.getMatcher().matches( location, path ) )
                {
                    if ( firstHit != null )
                    {
                        Logger logger = LoggerFactory.getLogger( getClass() );
                        logger.error( "Duplicate special-path registration for: {}:{}. Using: {}", location, path,
                                      firstHit );
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

    @Override
    public SpecialPathInfo getSpecialPathInfo( Location location, String path )
    {
        return null;
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( String path )
    {
        return path == null ? null : getSpecialPathInfo( null, path, SpecialPathConstants.PKG_TYPE_MAVEN );
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( String path, String pkgType )
    {
        return path == null ? null : getSpecialPathInfo( null, path, pkgType );
    }
}
