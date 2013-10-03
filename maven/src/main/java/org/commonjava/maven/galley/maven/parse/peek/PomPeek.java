package org.commonjava.maven.galley.maven.parse.peek;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.join;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.model.Transfer;

public class PomPeek
{

    private static final String G = "g";

    private static final String A = "a";

    private static final String V = "v";

    private static final String PG = "pg";

    private static final String PA = "pa";

    private static final String PV = "pv";

    private static final Set<String> COORD_KEYS = new HashSet<String>()
    {
        private static final long serialVersionUID = 1L;

        {
            add( G );
            add( A );
            add( V );
        }
    };

    private static final Set<String> PARENT_KEYS = new HashSet<String>()
    {
        private static final long serialVersionUID = 1L;

        {
            add( PG );
            add( PA );
            add( PV );
        }
    };

    private static final Map<String, String> CAPTURED_PATHS = new HashMap<String, String>()
    {
        private static final long serialVersionUID = 1L;

        {
            put( "project:groupId", G );
            put( "project:artifactId", A );
            put( "project:version", V );
            put( "project:parent:groupId", PG );
            put( "project:parent:artifactId", PA );
            put( "project:parent:version", PV );
        }
    };

    private final Set<String> coordKeys;

    private ProjectVersionRef key;

    private final Map<String, String> elementValues = new HashMap<String, String>();

    private ProjectVersionRef parentKey;

    private final boolean parseMainCoord;

    public PomPeek( final Transfer transfer, final boolean parseMainCoord )
        throws GalleyMavenException
    {
        this.parseMainCoord = parseMainCoord;
        coordKeys = new HashSet<>();
        if ( parseMainCoord )
        {
            coordKeys.addAll( COORD_KEYS );
        }

        coordKeys.addAll( PARENT_KEYS );

        parseCoordElements( transfer );

        if ( !createCoordinateInfo() )
        {
            throw new GalleyMavenException( "Could not peek at POM coordinates for: %s. "
                + "This POM will NOT be available as an ancestor to other models during effective-model building.", transfer );
        }
    }

    public ProjectVersionRef getKey()
    {
        return key;
    }

    public ProjectVersionRef getParentRef()
    {
        return parentKey;
    }

    private void parseCoordElements( final Transfer transfer )
        throws GalleyMavenException
    {
        InputStream stream = null;
        XMLStreamReader xml = null;
        try
        {
            stream = transfer.openInputStream( false );
            xml = XMLInputFactory.newFactory()
                                 .createXMLStreamReader( stream );

            final Stack<String> path = new Stack<String>();
            while ( xml.hasNext() )
            {
                final int evt = xml.next();
                switch ( evt )
                {
                    case START_ELEMENT:
                    {
                        final String elem = xml.getLocalName();
                        path.push( elem );
                        if ( captureValue( elem, path, xml ) )
                        {
                            // seems like xml.getElementText() traverses the END_ELEMENT event...
                            path.pop();
                        }
                        break;
                    }
                    case END_ELEMENT:
                    {
                        path.pop();
                        break;
                    }
                    default:
                    {
                    }
                }

                if ( foundAll() )
                {
                    return;
                }
            }
        }
        catch ( final IOException e )
        {
            throw new GalleyMavenException( "Failed to peek at POM coordinates for: %s. Reason: %s\n"
                + "This POM will NOT be available as an ancestor to other models during effective-model building.", e, transfer, e.getMessage() );
        }
        catch ( final XMLStreamException e )
        {
            throw new GalleyMavenException( "Failed to peek at POM coordinates for: %s. Reason: %s\n"
                + "This POM will NOT be available as an ancestor to other models during effective-model building.", e, transfer, e.getMessage() );
        }
        catch ( final FactoryConfigurationError e )
        {
            throw new GalleyMavenException( "Failed to peek at POM coordinates for: %s. Reason: %s\n"
                + "This POM will NOT be available as an ancestor to other models during effective-model building.", e, transfer, e.getMessage() );
        }
        finally
        {
            if ( xml != null )
            {
                try
                {
                    xml.close();
                }
                catch ( final XMLStreamException e )
                {
                }
            }

            closeQuietly( stream );
        }
    }

    private boolean foundAll()
    {
        for ( final String key : coordKeys )
        {
            if ( !elementValues.containsKey( key ) )
            {
                return false;
            }
        }

        return true;
    }

    private boolean captureValue( final String elem, final Stack<String> path, final XMLStreamReader xml )
        throws XMLStreamException
    {
        final String pathStr = join( path, ":" );
        final String key = CAPTURED_PATHS.get( pathStr );
        if ( key != null )
        {
            elementValues.put( key, xml.getElementText()
                                       .trim() );

            return true;
        }

        return false;
    }

    private boolean createCoordinateInfo()
    {
        String v = elementValues.get( V );
        final String pv = elementValues.get( PV );
        if ( isEmpty( v ) )
        {
            v = pv;
        }

        String g = elementValues.get( G );
        final String pg = elementValues.get( PG );
        if ( isEmpty( g ) )
        {
            g = pg;
        }

        final String a = elementValues.get( A );
        final String pa = elementValues.get( PA );

        boolean valid = false;
        if ( isValidArtifactId( a ) && isValidGroupId( g ) && isValidVersion( v ) )
        {
            key = new ProjectVersionRef( g, a, v );
            valid = true;
        }

        if ( isValidArtifactId( pa ) && isValidGroupId( pg ) && isValidVersion( pv ) )
        {
            parentKey = new ProjectVersionRef( pg, pa, pv );
            valid = valid || !parseMainCoord;
        }

        return valid;
    }

    private boolean isValidVersion( final String version )
    {
        if ( isEmpty( version ) )
        {
            return false;
        }

        if ( "version".equals( version ) )
        {
            return false;
        }

        if ( "parentVersion".equals( version ) )
        {
            return false;
        }

        return true;
    }

    private boolean isValidGroupId( final String groupId )
    {
        if ( isEmpty( groupId ) )
        {
            return false;
        }

        if ( groupId.contains( "${" ) )
        {
            return false;
        }

        if ( "parentGroupId".equals( groupId ) )
        {
            return false;
        }

        if ( "groupId".equals( groupId ) )
        {
            return false;
        }

        return true;
    }

    private boolean isValidArtifactId( final String artifactId )
    {
        if ( isEmpty( artifactId ) )
        {
            return false;
        }

        if ( artifactId.contains( "${" ) )
        {
            return false;
        }

        if ( "parentArtifactId".equals( artifactId ) )
        {
            return false;
        }

        if ( "artifactId".equals( artifactId ) )
        {
            return false;
        }

        return true;
    }

}
