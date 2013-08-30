package org.commonjava.maven.galley.testing.core;

import java.util.concurrent.Executors;

import org.commonjava.maven.galley.ArtifactManagerImpl;
import org.commonjava.maven.galley.TransferManagerImpl;
import org.commonjava.maven.galley.transport.TransportManagerImpl;
import org.junit.rules.TemporaryFolder;

public class CoreFixture
    extends ApiFixture
{

    public CoreFixture()
    {
        super();
    }

    public CoreFixture( final TemporaryFolder temp )
    {
        super( temp );
    }

    @Override
    public void initMissingComponents()
    {
        super.initMissingComponents();

        if ( getTransports() == null )
        {
            setTransports( new TransportManagerImpl( getTransport() ) );
        }

        if ( getTransfers() == null )
        {
            setTransfers( new TransferManagerImpl( getTransports(), getCache(), getNfc(), getEvents(), getDecorator(),
                                                   Executors.newFixedThreadPool( 2 ) ) );
        }

        if ( getArtifacts() == null )
        {
            setArtifacts( new ArtifactManagerImpl( getTransfers(), getLocations(), getMapper() ) );
        }
    }

}
