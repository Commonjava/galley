/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.maven.parse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.atlas.maven.ident.ref.ProjectRef;
import org.commonjava.atlas.maven.ident.util.JoinString;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.maven.ArtifactMetadataManager;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DocRef;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.maven.model.view.meta.MavenMetadataView;
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

    public MavenMetadataReader( final XMLInfrastructure xml, final LocationExpander locationExpander,
                                final ArtifactMetadataManager metadataManager, final XPathManager xpath )
    {
        super( xml, locationExpander );
        this.metadataManager = metadataManager;
        this.xpath = xpath;
    }

    public MavenMetadataView getMetadata( final ProjectRef ref, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        return getMetadata( ref, locations, new EventMetadata() );
    }

    public MavenMetadataView getMetadata( final ProjectRef ref , final List<? extends Location> locations , final EventMetadata eventMetadata  )
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
            transfers = metadataManager.retrieveAll( toRetrieve, ref, eventMetadata );
        }
        catch ( final TransferException e )
        {
            throw new GalleyMavenException( "Failed to resolve metadata for: {} from: {}. Reason: {}", e, ref,
                                            locations, e.getMessage() );
        }

        logger.debug( "Resolved {} transfers:\n  {}", transfers.size(), new JoinString( "\n  ", transfers ) );

        //noinspection ConstantConditions
        if ( transfers != null && !transfers.isEmpty() )
        {
            for ( final Transfer transfer : transfers )
            {
                final DocRef<ProjectRef> dr =
                    new DocRef<>( ref, transfer.getLocation(), xml.parse( transfer, eventMetadata ) );
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

        docs.removeIf( Objects::isNull );

        logger.debug( "Got {} metadata documents for: {}", docs.size(), ref );
        return new MavenMetadataView( docs, xpath, xml );
    }

    public MavenMetadataView readMetadata( final ProjectRef ref, final List<Transfer> transfers )
        throws GalleyMavenException
    {
        return readMetadata( ref, transfers, new EventMetadata() );
    }

    public MavenMetadataView readMetadata( final ProjectRef ref , final List<Transfer> transfers , final EventMetadata eventMetadata  )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectRef>> docs = new ArrayList<>( transfers.size() );

        //noinspection ConstantConditions
        if ( transfers != null && !transfers.isEmpty() )
        {
            for ( final Transfer transfer : transfers )
            {
                if ( transfer == null )
                {
                    continue;
                }

                final DocRef<ProjectRef> dr =
                    new DocRef<>( ref, transfer.getLocation(), xml.parse( transfer, eventMetadata ) );

                docs.add( dr );
            }
        }

        if ( docs.isEmpty() )
        {
            return null;
        }

        return new MavenMetadataView( docs, xpath, xml );
    }

}
