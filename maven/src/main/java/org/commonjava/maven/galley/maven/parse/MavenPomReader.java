/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
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
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.maven.ArtifactManager;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DependencyView;
import org.commonjava.maven.galley.maven.model.view.DocRef;
import org.commonjava.maven.galley.maven.model.view.MavenPomView;
import org.commonjava.maven.galley.maven.model.view.MavenXmlMixin;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.LocationExpander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

@ApplicationScoped
public class MavenPomReader
    extends AbstractMavenXmlReader<ProjectVersionRef>
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private ArtifactManager artifacts;

    @Inject
    private MavenPluginDefaults pluginDefaults;

    @Inject
    private MavenPluginImplications pluginImplications;

    @Inject
    private XPathManager xpath;

    protected MavenPomReader()
    {
    }

    public MavenPomReader( final XMLInfrastructure xml, final LocationExpander locationExpander, final ArtifactManager artifactManager,
                           final XPathManager xpath, final MavenPluginDefaults pluginDefaults, final MavenPluginImplications pluginImplications )
    {
        super( xml, locationExpander );
        this.artifacts = artifactManager;
        this.xpath = xpath;
        this.pluginDefaults = pluginDefaults;
        this.pluginImplications = pluginImplications;
    }

    public MavenPomView read( final ProjectVersionRef ref, final Transfer pom, final List<? extends Location> locations,
                              final String... activeProfileLocations )
        throws GalleyMavenException
    {
        return read( ref, pom, locations, new EventMetadata(), activeProfileLocations );
    }

    public MavenPomView read( final ProjectVersionRef ref, final Transfer pom,
                              final List<? extends Location> locations, final EventMetadata eventMetadata,
                              final String... activeProfileLocations )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectVersionRef>> stack = new ArrayList<>();

        DocRef<ProjectVersionRef> dr;
        try
        {
            dr = getDocRef( ref, pom, false, eventMetadata );
        }
        catch ( final TransferException e )
        {
            throw new GalleyMavenException( "Failed to retrieve POM for: {}, {} levels deep in ancestry stack of: {}. Reason: {}", e, ref,
                                            stack.size(), ref, e.getMessage() );
        }

        stack.add( dr );

        ProjectVersionRef next = xml.getParentRef( dr.getDoc() );
        while ( next != null )
        {
            try
            {
                dr = getDocRef( next, locations, false, eventMetadata );
            }
            catch ( final TransferException e )
            {
                throw new GalleyMavenException( "Failed to retrieve POM for: {}, {} levels deep in ancestry stack of: {}. Reason: {}", e, next,
                                                stack.size(), ref, e.getMessage() );
            }

            if ( dr == null )
            {
                throw new GalleyMavenException( "Cannot resolve {}, {} levels dep in the ancestry stack of: {}", next, stack.size(), ref );
            }

            stack.add( dr );

            next = xml.getParentRef( dr.getDoc() );
        }

        final MavenPomView view = new MavenPomView( ref, stack, xpath, pluginDefaults, pluginImplications, xml, activeProfileLocations );
        assembleImportedInformation( view, locations );

        logStructure( view );

        return view;
    }

    public void logStructure( final MavenPomView view )
    {
        logger.debug( "{}", new Object()
        {
            @Override
            public String toString()
            {
                return printStructure( view );
            }
        } );
    }

    private String printStructure( final MavenPomView view )
    {
        final StringBuilder sb = new StringBuilder();

        final List<DocRef<ProjectVersionRef>> stack = view.getDocRefStack();
        final List<MavenXmlMixin<ProjectVersionRef>> mixins = view.getMixins();

        sb.append( "\n\n" )
          .append( view.getRef() )
          .append( " consists of:\n  " );

        int i = 0;
        for ( final DocRef<ProjectVersionRef> docref : stack )
        {
            sb.append( "\n  D" )
              .append( i++ )
              .append( docref );
        }

        sb.append( "\n\n" );

        if ( mixins != null && !mixins.isEmpty() )
        {
            sb.append( mixins.size() )
              .append( " Mix-ins for " )
              .append( view.getRef() )
              .append( ":\n\n" );

            i = 0;
            for ( final MavenXmlMixin<ProjectVersionRef> mixin : mixins )
            {
                sb.append( 'M' )
                  .append( i++ )
                  .append( mixin )
                  .append( "\n    " );
                sb.append( printStructure( (MavenPomView) mixin.getMixin() ) );
            }

            sb.append( "\n\n" );
        }

        return sb.toString();
    }

    private DocRef<ProjectVersionRef> getDocRef( final ProjectVersionRef ref, final List<? extends Location> locations,
                                                 final boolean cache, final EventMetadata eventMetadata )
        throws TransferException, GalleyMavenException
    {
        DocRef<ProjectVersionRef> dr = getFirstCached( ref, locations );
        if ( dr == null )
        {
            final Transfer transfer = artifacts.retrieveFirst( locations, ref.asPomArtifact(), eventMetadata );

            if ( transfer == null )
            {
                return null;
            }

            final Document doc = xml.parse( transfer, new EventMetadata() );
            dr = new DocRef<>( ref, transfer.getLocation()
                                                             .toString(), doc );

            if ( cache )
            {
                cache( dr );
            }
        }

        return dr;
    }

    private DocRef<ProjectVersionRef> getDocRef( final ProjectVersionRef ref, final Transfer pom, final boolean cache,
                                                 final EventMetadata eventMetadata )
        throws GalleyMavenException, TransferException
    {
        DocRef<ProjectVersionRef> dr = getFirstCached( ref, Collections.singletonList( pom.getLocation() ) );

        if ( dr == null )
        {
            final Document doc = xml.parse( pom, eventMetadata );
            dr = new DocRef<>( ref, pom.getLocation(), doc );
        }

        if ( cache )
        {
            cache( dr );
        }

        return dr;
    }

    public MavenPomView readLocalPom( final ProjectVersionRef ref, final Transfer transfer,
                                      final String... activeProfileIds )
        throws GalleyMavenException
    {
        return readLocalPom( ref, transfer, false, activeProfileIds );
    }

    public MavenPomView readLocalPom( final ProjectVersionRef ref, final Transfer transfer,
                                      final EventMetadata eventMetadata, final String... activeProfileIds )
        throws GalleyMavenException
    {
        return readLocalPom( ref, transfer, false, eventMetadata, activeProfileIds );
    }

    public MavenPomView readLocalPom( final ProjectVersionRef ref, final Transfer transfer, final boolean cache,
                                      final String... activeProfileIds )
        throws GalleyMavenException
    {
        return readLocalPom( ref, transfer, cache, new EventMetadata(), activeProfileIds );
    }

    public MavenPomView readLocalPom( final ProjectVersionRef ref, final Transfer transfer, final boolean cache,
                                      final EventMetadata eventMetadata, final String... activeProfileIds )
        throws GalleyMavenException
    {
        DocRef<ProjectVersionRef> dr;
        try
        {
            dr = getDocRef( ref, transfer, cache, eventMetadata );
        }
        catch ( final TransferException e )
        {
            throw new GalleyMavenException( "Failed to parse POM for: {}. Reason: {}", e, ref, e.getMessage() );
        }

        final MavenPomView view =
            new MavenPomView( ref, Collections.singletonList( dr ), xpath, pluginDefaults, pluginImplications, xml,
                              activeProfileIds );

        logStructure( view );

        return view;
    }

    public MavenPomView read( final ProjectVersionRef ref, final List<? extends Location> locations, final String... activeProfileIds )
        throws GalleyMavenException
    {
        return read( ref, locations, false, activeProfileIds );
    }

    public MavenPomView read( final ProjectVersionRef ref, final List<? extends Location> locations,
                              final EventMetadata eventMetadata, final String... activeProfileIds )
        throws GalleyMavenException
    {
        return read( ref, locations, false, eventMetadata, activeProfileIds );
    }

    public MavenPomView read( final ProjectVersionRef ref, final List<? extends Location> locations,
                              final boolean cache, final String... activeProfileIds )
        throws GalleyMavenException
    {
        return read( ref, locations, cache, new EventMetadata(), activeProfileIds );
    }

    public MavenPomView read( final ProjectVersionRef ref, final List<? extends Location> locations, final boolean cache,
 final EventMetadata eventMetadata,
                              final String... activeProfileIds )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectVersionRef>> stack = new ArrayList<>();

        ProjectVersionRef next = ref;
        do
        {
            DocRef<ProjectVersionRef> dr;
            try
            {
                dr = getDocRef( next, locations, cache, eventMetadata );
            }
            catch ( final TransferException e )
            {
                throw new GalleyMavenException( "Failed to retrieve POM for: {}, {} levels deep in ancestry stack of: {}. Reason: {}", e, next,
                                                stack.size(), ref, e.getMessage() );
            }

            if ( dr == null )
            {
                throw new GalleyMavenException( "Cannot resolve {}, {} levels dep in the ancestry stack of: {}", next, stack.size(), ref );
            }

            stack.add( dr );

            next = xml.getParentRef( dr.getDoc() );
        }
        while ( next != null );

        final MavenPomView view = new MavenPomView( ref, stack, xpath, pluginDefaults, pluginImplications, xml, activeProfileIds );
        assembleImportedInformation( view, locations );

        logStructure( view );

        return view;
    }

    private void assembleImportedInformation( final MavenPomView view, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        final List<DependencyView> md = view.getAllBOMs();
        for ( final DependencyView dv : md )
        {
            final ProjectVersionRef ref = dv.asProjectVersionRef();
            logger.debug( "Found BOM: {} for: {}", ref, view.getRef() );

            // This is a BOM, it's likely to be used in multiple locations...cache this.
            final MavenPomView imp = read( ref, locations, true );

            view.addMixin( new MavenXmlMixin<>( imp, MavenXmlMixin.DEPENDENCY_MIXIN ) );
        }
    }

}
