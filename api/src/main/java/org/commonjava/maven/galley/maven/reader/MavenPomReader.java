package org.commonjava.maven.galley.maven.reader;

import static org.apache.commons.lang.StringUtils.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.atlas.ident.DependencyScope;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.ArtifactManager;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.peek.PomPeek;
import org.commonjava.maven.galley.maven.view.DependencyView;
import org.commonjava.maven.galley.maven.view.DocRef;
import org.commonjava.maven.galley.maven.view.MavenPomView;
import org.commonjava.maven.galley.maven.view.MavenXmlMixin;
import org.commonjava.maven.galley.maven.view.XPathManager;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.util.logging.Logger;

@ApplicationScoped
public class MavenPomReader
    extends AbstractMavenXmlReader<ProjectVersionRef>
{

    private static final String PEEK = "peek";

    private final Logger logger = new Logger( getClass() );

    @Inject
    private ArtifactManager artifacts;

    @Inject
    private MavenPluginDefaults pluginDefaults;

    @Inject
    private XPathManager xpath;

    protected MavenPomReader()
    {
    }

    public MavenPomReader( final ArtifactManager artifactManager, final XPathManager xpath, final MavenPluginDefaults pluginDefaults )
    {
        this.artifacts = artifactManager;
        this.xpath = xpath;
        this.pluginDefaults = pluginDefaults;
    }

    public MavenPomView read( final Transfer pom, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectVersionRef>> stack = new ArrayList<>();

        DocRef<ProjectVersionRef> dr = getDocRef( pom, locations, false );
        stack.add( dr );

        final ProjectVersionRef ref = dr.getAttribute( PEEK, PomPeek.class )
                                        .getKey();
        ProjectVersionRef next = dr.getAttribute( PEEK, PomPeek.class )
                                   .getParentRef();
        while ( next != null && dr != null )
        {
            try
            {
                dr = getDocRef( next, locations, false );
            }
            catch ( final TransferException e )
            {
                throw new GalleyMavenException( "Failed to retrieve POM for: %s, %d levels deep in ancestry stack of: %s. Reason: %s", e, next,
                                                stack.size(), ref, e.getMessage() );
            }

            if ( dr == null )
            {
                throw new GalleyMavenException( "Cannot resolve %s, %d levels dep in the ancestry stack of: %s", next, stack.size(), ref );
            }

            stack.add( dr );

            next = dr.getAttribute( PEEK, PomPeek.class )
                     .getParentRef();
        }

        final MavenPomView view = new MavenPomView( ref, stack, xpath, pluginDefaults );
        assembleImportedInformation( view, locations );

        logStructure( view );

        return view;
    }

    public void logStructure( final MavenPomView view )
    {
        logger.info( printStructure( view ) );
    }

    private String printStructure( final MavenPomView view )
    {
        final StringBuilder sb = new StringBuilder();

        final List<DocRef<ProjectVersionRef>> stack = view.getDocRefStack();
        final List<MavenXmlMixin<ProjectVersionRef>> mixins = view.getMixins();

        sb.append( "\n\n" )
          .append( view.getRef() )
          .append( " consists of:\n  " )
          .append( join( stack, "\n  " ) )
          .append( "\n\n" );
        if ( mixins != null && !mixins.isEmpty() )
        {
            sb.append( "Mix-ins for " )
              .append( view.getRef() )
              .append( ":\n\n" );
            for ( final MavenXmlMixin<ProjectVersionRef> mixin : mixins )
            {
                sb.append( mixin )
                  .append( "\n    " );
                sb.append( printStructure( (MavenPomView) mixin.getMixin() ) );
            }
        }

        return sb.toString();
    }

    private DocRef<ProjectVersionRef> getDocRef( final ProjectVersionRef ref, final List<? extends Location> locations, final boolean cache )
        throws TransferException
    {
        DocRef<ProjectVersionRef> dr = getFirstCached( ref, locations );
        if ( dr == null )
        {
            Transfer transfer = null;
            transfer = artifacts.retrieveFirst( locations, ref.asPomArtifact() );

            if ( transfer == null )
            {
                return null;
            }

            dr = new DocRef<ProjectVersionRef>( ref, transfer.getLocation(), parse( transfer ) );
            final PomPeek peek = new PomPeek( transfer, false );
            dr.setAttribute( PEEK, peek );

            if ( cache )
            {
                cache( dr );
            }
        }

        return dr;
    }

    private DocRef<ProjectVersionRef> getDocRef( final Transfer pom, final List<? extends Location> locations, final boolean cache )
    {
        PomPeek peek;
        final Transfer transfer = pom;

        peek = new PomPeek( transfer, true );
        final ProjectVersionRef ref = peek.getKey();
        DocRef<ProjectVersionRef> dr = getFirstCached( ref, Arrays.asList( pom.getLocation() ) );

        if ( dr == null )
        {
            dr = new DocRef<ProjectVersionRef>( peek.getKey(), transfer.getLocation(), parse( transfer ) );
        }

        dr.setAttribute( PEEK, peek );

        if ( cache )
        {
            cache( dr );
        }

        return dr;
    }

    public MavenPomView read( final ProjectVersionRef ref, final List<? extends Location> locations )
        throws GalleyMavenException
    {
        return read( ref, locations, false );
    }

    public MavenPomView read( final ProjectVersionRef ref, final List<? extends Location> locations, final boolean cache )
        throws GalleyMavenException
    {
        final List<DocRef<ProjectVersionRef>> stack = new ArrayList<>();

        PomPeek peek;
        ProjectVersionRef next = ref;
        do
        {
            DocRef<ProjectVersionRef> dr;
            try
            {
                dr = getDocRef( next, locations, cache );
            }
            catch ( final TransferException e )
            {
                throw new GalleyMavenException( "Failed to retrieve POM for: %s, %d levels deep in ancestry stack of: %s. Reason: %s", e, next,
                                                stack.size(), ref, e.getMessage() );
            }

            if ( dr == null )
            {
                throw new GalleyMavenException( "Cannot resolve %s, %d levels dep in the ancestry stack of: %s", next, stack.size(), ref );
            }

            peek = dr.getAttribute( PEEK, PomPeek.class );

            stack.add( dr );

            next = peek.getParentRef();
        }
        while ( next != null );

        final MavenPomView view = new MavenPomView( ref, stack, xpath, pluginDefaults );
        assembleImportedInformation( view, locations );

        logStructure( view );

        return view;
    }

    private void assembleImportedInformation( final MavenPomView view, final List<? extends Location> locations )
    {
        final List<DependencyView> md = view.getAllManagedDependencies();
        for ( final DependencyView dv : md )
        {
            if ( DependencyScope._import == dv.getScope() && "pom".equals( dv.getType() ) )
            {
                final ProjectVersionRef ref = dv.asProjectVersionRef();
                logger.info( "Found BOM: %s for: %s", ref, view.getRef() );

                // This is a BOM, it's likely to be used in multiple locations...cache this.
                final MavenPomView imp = read( ref, locations, true );

                view.addMixin( new MavenXmlMixin<ProjectVersionRef>( imp, MavenXmlMixin.DEPENDENCY_MIXIN ) );
            }
        }
    }

}
