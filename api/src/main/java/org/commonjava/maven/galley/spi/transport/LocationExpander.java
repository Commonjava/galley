package org.commonjava.maven.galley.spi.transport;

import java.util.Collection;
import java.util.List;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;

public interface LocationExpander
{

    List<Location> expand( Location... locations )
        throws TransferException;

    <T extends Location> List<Location> expand( Collection<T> locations )
        throws TransferException;

}
