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
@Alternative
@Named
public class NoCacheTransferDecorator
                extends AbstractTransferDecorator
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private SpecialPathManager specialPathManager;

    public NoCacheTransferDecorator( SpecialPathManager specialPathManager )
    {
        this.specialPathManager = specialPathManager;
    }

    @Override
    public InputStream decorateRead( final InputStream stream, final Transfer transfer,
                                     final EventMetadata eventMetadata ) throws IOException
    {
        SpecialPathInfo specialPathInfo = specialPathManager.getSpecialPathInfo( transfer, eventMetadata.getPackageType() );

        logger.trace( "SpecialPathInfo for: {} is: {} (cachable? {})", transfer, specialPathInfo,
                      ( specialPathInfo == null ? true : specialPathInfo.isCachable() ) );

        if ( specialPathInfo != null && !specialPathInfo.isCachable() )
        {
            logger.trace( "Decorating read with NoCacheTransferDecorator for: {}", transfer );
            return new NoCacheInputStream( stream, transfer );
        }

        return stream;
    }
}
