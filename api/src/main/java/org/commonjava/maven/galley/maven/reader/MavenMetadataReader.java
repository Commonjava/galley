package org.commonjava.maven.galley.maven.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.ArtifactMetadataManager;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.view.DocRef;
import org.commonjava.maven.galley.maven.view.MavenMetadataView;
import org.commonjava.maven.galley.maven.view.XPathManager;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.util.logging.Logger;

@ApplicationScoped
public class MavenMetadataReader
    extends AbstractMavenXmlReader<ProjectRef>
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private ArtifactMetadataManager metadataManager;

    @Inject
    private XPathManager xpath;

    protected MavenMetadataReader()
    {
    }

    public MavenMetadataReader( final ArtifactMetadataManager metadataManager, final XPathManager xpath )
    {
        this.metadataManager = metadataManager;
        this.xpath = xpath;
    }

    public MavenMetadataView getMetadata( final ProjectRef ref, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectRef>> docs = new ArrayList<>( locations.size() );
        final Map<Location, DocRef<ProjectRef>> cached = getAllCached( ref, locations );

        final List<? extends Location> toRetrieve = new ArrayList<>( locations );
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
            throw new GalleyMavenException( "Failed to resolve metadata for: %s from: %s. Reason: %s", e, ref, locations, e.getMessage() );
        }

        if ( transfers != null && !transfers.isEmpty() )
        {
            for ( final Transfer transfer : transfers )
            {
                final DocRef<ProjectRef> dr = new DocRef<ProjectRef>( ref, transfer.getLocation(), parse( transfer ) );
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

        logger.info( "Got %d metadata documents for: %s", docs.size(), ref );
        return new MavenMetadataView( docs, xpath );
    }

}
