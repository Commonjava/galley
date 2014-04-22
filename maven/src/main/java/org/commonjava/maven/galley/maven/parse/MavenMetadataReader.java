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
import org.commonjava.maven.galley.spi.transport.LocationExpander;
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

    public MavenMetadataReader( final XMLInfrastructure xml, final LocationExpander locationExpander, final ArtifactMetadataManager metadataManager,
                                final XPathManager xpath )
    {
        super( xml, locationExpander );
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

                // FIXME: This is too clever by half...the if/then here is probably wrong.
                // I'm assuming the index out of bounds problem comes from location expansion...
                //
                // java.lang.ArrayIndexOutOfBoundsException: -1
                //                at java.util.ArrayList.elementData(ArrayList.java:371)
                //                at java.util.ArrayList.set(ArrayList.java:399)
                //                at org.commonjava.maven.galley.maven.parse.MavenMetadataReader.getMetadata(MavenMetadataReader.java:102)
                //                at org.commonjava.maven.galley.maven.parse.MavenMetadataReader$Proxy$_$$_WeldClientProxy.getMetadata(Unknown Source)
                //                at org.commonjava.maven.galley.maven.internal.version.VersionResolverImpl.resolveMulti(VersionResolverImpl.java:115)
                //                at org.commonjava.maven.galley.maven.internal.version.VersionResolverImpl.resolveVariableVersions(VersionResolverImpl.java:65)
                //                at org.commonjava.maven.galley.maven.internal.version.VersionResolverImpl$Proxy$_$$_WeldClientProxy.resolveVariableVersions(Unknown Source)
                //                at org.commonjava.maven.galley.maven.internal.ArtifactManagerImpl.resolveVariableVersion(ArtifactManagerImpl.java:221)
                //                at org.commonjava.aprox.depgraph.discover.AproxProjectGraphDiscoverer.resolveSpecificVersion(AproxProjectGraphDiscoverer.java:167)
                //                at org.commonjava.aprox.depgraph.discover.AproxProjectGraphDiscoverer.discoverRelationships(AproxProjectGraphDiscoverer.java:99)
                //                at org.commonjava.aprox.depgraph.discover.AproxProjectGraphDiscoverer$Proxy$_$$_WeldClientProxy.discoverRelationships(Unknown Source)
                //                at org.commonjava.maven.cartographer.agg.DiscoveryRunnable.run(DiscoveryRunnable.java:80)
                //                at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
                //                at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
                //                at java.lang.Thread.run(Thread.java:722)
                //
                if ( idx > -1 )
                {
                    docs.set( idx, dr );
                }
                else
                {
                    docs.add( dr );
                }
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
