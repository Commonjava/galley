/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.VirtualResource;
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

    @Override
    public VirtualResource expand( final Resource resource )
    {
        if ( resource instanceof VirtualResource )
        {
            return (VirtualResource) resource;
        }
        else
        {
            return new VirtualResource( Collections.singletonList( ( (ConcreteResource) resource ).getLocation() ), resource.getPath() );
        }
    }

}
