/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.maven.galley.maven.parse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.ArtifactMetadataManager;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DocRef;
import org.commonjava.maven.galley.maven.model.view.MavenMetadataView;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MavenMetadataReader
    extends AbstractMavenXmlReader<ProjectRef>
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private ArtifactMetadataManager metadataManager;

    @Inject
    private XPathManager xpath;

    protected MavenMetadataReader()
    {
    }

    public MavenMetadataReader( final XMLInfrastructure xml, final ArtifactMetadataManager metadataManager, final XPathManager xpath )
    {
        super( xml );
        this.metadataManager = metadataManager;
        this.xpath = xpath;
    }

    public MavenMetadataView getMetadata( final ProjectRef ref, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectRef>> docs = new ArrayList<DocRef<ProjectRef>>( locations.size() );
        final Map<Location, DocRef<ProjectRef>> cached = getAllCached( ref, locations );

        final List<? extends Location> toRetrieve = new ArrayList<Location>( locations );
        for ( final Location loc : locations )
        {
            final DocRef<ProjectRef> dr = cached.get( loc );
            if ( dr != null )
            {
                docs.add( dr );
                toRetrieve.remove( loc );
            }
            else
            {
                docs.add( null );
            }
        }

        List<Transfer> transfers;
        try
        {
            transfers = metadataManager.retrieveAll( toRetrieve, ref );
        }
        catch ( final TransferException e )
        {
            throw new GalleyMavenException( "Failed to resolve metadata for: {} from: {}. Reason: {}", e, ref, locations, e.getMessage() );
        }

        if ( transfers != null && !transfers.isEmpty() )
        {
            for ( final Transfer transfer : transfers )
            {
                final DocRef<ProjectRef> dr = new DocRef<ProjectRef>( ref, transfer.getLocation(), xml.parse( transfer ) );
                final int idx = locations.indexOf( transfer.getLocation() );
                docs.set( idx, dr );
            }
        }

        for ( final Iterator<DocRef<ProjectRef>> iterator = docs.iterator(); iterator.hasNext(); )
        {
            final DocRef<ProjectRef> docRef = iterator.next();
            if ( docRef == null )
            {
                iterator.remove();
            }
        }

        logger.debug( "Got {} metadata documents for: {}", docs.size(), ref );
        return new MavenMetadataView( docs, xpath, xml );
    }

}
