package org.commonjava.maven.galley.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.spi.transport.LocationExpander;

@Named( "no-op-galley-location-expander" )
@Alternative
public class NoOpLocationExpander
    implements LocationExpander
{

    @SuppressWarnings( "unchecked" )
    @Override
    public <T extends Location> List<Location> expand( final Collection<T> locations )
        throws TransferException
    {
        return locations instanceof List ? (List<Location>) locations : new ArrayList<Location>( locations );
    }

    @Override
    public List<Location> expand( final Location... locations )
        throws TransferException
    {
        return Arrays.asList( locations );
    }

}
