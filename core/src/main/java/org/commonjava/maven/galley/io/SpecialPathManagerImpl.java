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
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.SpecialPathMatcher;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang.StringUtils.join;
import static org.commonjava.maven.galley.io.SpecialPathConstants.MVN_SP_PATH_SET;
import static org.commonjava.maven.galley.io.SpecialPathConstants.NPM_SP_PATH_SET;
import static org.commonjava.maven.galley.io.SpecialPathConstants.PKG_TYPE_MAVEN;

/**
 * Created by jdcasey on 1/27/16.
 */
@ApplicationScoped
public class SpecialPathManagerImpl
        implements SpecialPathManager
{
    private static final Logger logger = LoggerFactory.getLogger( SpecialPathManagerImpl.class );

    private List<SpecialPathInfo> stdSpecialPaths;

    private Map<String, SpecialPathSet> pkgtypes;

    public SpecialPathManagerImpl()
    {
        initPkgPathSets();
    }

    @PostConstruct
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
        if ( pkgtypes.containsKey( pathSet.getPackageType() ) )
        {
            logger.warn(
                    "[Galley] The package types already contains the path set for this package type {}, will override it",
                    pathSet.getPackageType() );
        }

        pkgtypes.put( pathSet.getPackageType(), pathSet );

        if ( logger.isTraceEnabled() )
        {
            final List<SpecialPathMatcher> pathMatchers = new ArrayList<>();
            for ( SpecialPathInfo info : pathSet.getSpecialPathInfos() )
            {
                pathMatchers.add( info.getMatcher() );
            }

            logger.trace( "Enabling special paths for package: '{}'\n  - {}\n\nCalled from: {}", pathSet.getPackageType(),
                          join( pathMatchers, "\n  - " ), Thread.currentThread().getStackTrace()[1] );
        }
    }

    @Override
    public SpecialPathSet deregesterSpecialPathSet( SpecialPathSet pathSet )
    {
        if ( !pkgtypes.containsKey( pathSet.getPackageType() ) )
        {
            logger.warn(
                    "[Galley] The package does not contain the path set for this package type {}, no deregister operation there",
                    pathSet.getPackageType() );
        }

        return pkgtypes.remove( pathSet.getPackageType() );
    }

    @Deprecated
    @Override
    public SpecialPathInfo getSpecialPathInfo( ConcreteResource resource )
    {
        return getSpecialPathInfo( resource, PKG_TYPE_MAVEN );
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( ConcreteResource resource, String pkgType )
    {
        if ( resource != null )
        {
            return getSpecialPathInfo( resource.getLocation(), resource.getPath(), pkgType );
        }

        // TODO: Return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR or something non-null?
        return null;
    }

    @Deprecated
    @Override
    public SpecialPathInfo getSpecialPathInfo( Transfer transfer )
    {
        return getSpecialPathInfo( transfer, PKG_TYPE_MAVEN );
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( Transfer transfer, String pkgType )
    {
        if ( transfer != null )
        {
            return getSpecialPathInfo( transfer.getLocation(), transfer.getStoragePath(), pkgType );
        }

        // TODO: Return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR or something non-null?
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
            if ( info != null )
            {
                return info;
            }
        }
        // TODO: Return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR or something non-null?
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

        // TODO: Return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR based on path if firstHit is null!
        return firstHit;
    }

    @Deprecated
    @Override
    public SpecialPathInfo getSpecialPathInfo( Location location, String path )
    {
        return getSpecialPathInfo( location, path, SpecialPathConstants.PKG_TYPE_MAVEN );
    }

    @Override
    public SpecialPathInfo getSpecialPathInfo( String path )
    {
        // TODO: seems that all SpecialPathMatcher impl classes does not use the Location, so we should consider to remove the Location arg next step.
        // TODO: When path is null, return SpecialPathConstants.DEFAULT_FILE or SpecialPathConstants.DEFAULT_DIR or something non-null?
        return path == null ? null : getSpecialPathInfo( null, path, SpecialPathConstants.PKG_TYPE_MAVEN );
    }
}
