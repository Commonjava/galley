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

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.nocache.NoCacheInputStream;
import org.commonjava.maven.galley.model.SpecialPathInfo;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.io.SpecialPathManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ruhan on 4/25/17.
 */
@SuppressWarnings( "unused" )
@Alternative
@Named
public class NoCacheTransferDecorator
                extends AbstractTransferDecorator
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final SpecialPathManager specialPathManager;

    public NoCacheTransferDecorator( SpecialPathManager specialPathManager )
    {
        this.specialPathManager = specialPathManager;
    }

    @SuppressWarnings( "RedundantThrows" )
    @Override
    public InputStream decorateRead( final InputStream stream, final Transfer transfer,
                                     final EventMetadata eventMetadata ) throws IOException
    {
        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer, eventMetadata.getPackageType() );

        logger.trace( "SpecialPathInfo for: {} is: {} (cachable? {})", transfer, specialPathInfo,
                      ( specialPathInfo == null || specialPathInfo.isCachable() ) );

        if ( specialPathInfo != null && !specialPathInfo.isCachable() )
        {
            logger.trace( "Decorating read with NoCacheTransferDecorator for: {}", transfer );
            return new NoCacheInputStream( stream, transfer );
        }

        return stream;
    }
}
