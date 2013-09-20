package org.commonjava.maven.galley.maven.reader;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.ArtifactManager;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.peek.PomPeek;
import org.commonjava.maven.galley.maven.view.DocRef;
import org.commonjava.maven.galley.maven.view.MavenPomView;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

@ApplicationScoped
public class MavenPomReader
    extends AbstractMavenXmlReader<ProjectVersionRef>
{

    private static final String PEEK = "peek";

    @Inject
    private ArtifactManager artifacts;

    @Inject
    private MavenPluginDefaults pluginDefaults;

    protected MavenPomReader()
    {
    }

    public MavenPomReader( final ArtifactManager artifactManager, final MavenPluginDefaults pluginDefaults )
    {
        this.artifacts = artifactManager;
        this.pluginDefaults = pluginDefaults;
    }

    public MavenPomView read( final Transfer pom, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectVersionRef>> stack = new ArrayList<>();

        PomPeek peek;
        Transfer transfer = pom;

        peek = new PomPeek( transfer, true );
        final ProjectVersionRef ref = peek.getKey();
        DocRef<ProjectVersionRef> dr = getFirstCached( ref, locations );

        if ( dr == null )
        {
            dr = new DocRef<ProjectVersionRef>( peek.getKey(), transfer.getLocation(), parse( transfer ) );
        }

        dr.setAttribute( PEEK, peek );
        stack.add( dr );

        ProjectVersionRef next = peek.getParentRef();
        while ( next != null && dr != null )
        {
            dr = getFirstCached( next, locations );
            if ( dr == null )
            {
                try
                {
                    transfer = artifacts.retrieveFirst( locations, next.asPomArtifact() );
                }
                catch ( final TransferException e )
                {
                    throw new GalleyMavenException( "Failed to retrieve POM for: %s, %d levels deep in ancestry stack of: %s. Reason: %s", e, next,
                                                    stack.size(), ref, e.getMessage() );
                }

                if ( transfer == null )
                {
                    throw new GalleyMavenException( "Cannot resolve %s, %d levels dep in the ancestry stack of: %s", next, stack.size(), ref );
                }

                dr = new DocRef<ProjectVersionRef>( next, transfer.getLocation(), parse( transfer ) );
                peek = new PomPeek( transfer, false );
                dr.setAttribute( PEEK, peek );

                cache( dr );
            }
            else
            {
                peek = dr.getAttribute( PEEK, PomPeek.class );
            }

            stack.add( dr );

            next = peek.getParentRef();
        }

        return new MavenPomView( stack, pluginDefaults );
    }

    public MavenPomView read( final ProjectVersionRef ref, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectVersionRef>> stack = new ArrayList<>();

        PomPeek peek;
        Transfer transfer;
        ProjectVersionRef next = ref;
        do
        {
            DocRef<ProjectVersionRef> dr = getFirstCached( next, locations );
            if ( dr == null )
            {
                try
                {
                    transfer = artifacts.retrieveFirst( locations, next.asPomArtifact() );
                }
                catch ( final TransferException e )
                {
                    throw new GalleyMavenException( "Failed to retrieve POM for: %s, %d levels deep in ancestry stack of: %s. Reason: %s", e, next,
                                                    stack.size(), ref, e.getMessage() );
                }

                if ( transfer == null )
                {
                    throw new GalleyMavenException( "Cannot resolve %s, %d levels dep in the ancestry stack of: %s", next, stack.size(), ref );
                }

                dr = new DocRef<ProjectVersionRef>( next, transfer.getLocation(), parse( transfer ) );
                peek = new PomPeek( transfer, false );
                dr.setAttribute( PEEK, peek );

                cache( dr );
            }
            else
            {
                peek = dr.getAttribute( PEEK, PomPeek.class );
            }

            stack.add( dr );

            next = peek.getParentRef();
        }
        while ( next != null );

        return new MavenPomView( stack, pluginDefaults );
    }

}
