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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.join;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.commonjava.maven.atlas.ident.ref.SimpleProjectVersionRef;
import org.commonjava.maven.galley.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PomPeek
{
    private static final String G = "g";

    private static final String A = "a";

    private static final String V = "v";

    private static final String PG = "pg";

    private static final String PA = "pa";

    private static final String PV = "pv";

    private static final String PKG = "pkg";

    private static final String PRP = "prp";

    private static final String[] COORD_KEYS = { G, A, V, PG, PA, PV, PKG, PRP };

    /**
     * Used to search for Maven modules / module blocks.
     */
    private static final String MODULES_ELEM = "modules";

    private static final String PLUGINS_ELEM = "plugins";

    private static final Map<String, String> CAPTURED_PATHS = new HashMap<String, String>()
    {
        private static final long serialVersionUID = 1L;

        {
            put( "project:groupId", G );
            put( "project:artifactId", A );
            put( "project:version", V );
            put( "project:packaging", PKG );
            put( "project:parent:groupId", PG );
            put( "project:parent:artifactId", PA );
            put( "project:parent:version", PV );
            put( "project:parent:relativePath", PRP );
        }
    };

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private ProjectVersionRef key;

    private final Map<String, String> elementValues = new HashMap<>();

    private final Set<String> modules = new HashSet<>();

    private final File pom;

    private ProjectVersionRef parentKey;

    /**
     * Denotes if this represents the top level peeked POM.
     */
    private boolean inheritanceRoot = false;

    private final Transfer transfer;

    private final boolean captureModules;

    private final InputStream stream;

    public PomPeek( final File pom )
    {
        this.pom = pom;
        this.transfer = null;
        this.stream = null;
        captureModules = true;
        init();
    }

    public PomPeek( final Transfer transfer )
    {
        this.pom = null;
        this.transfer = transfer;
        this.stream = null;
        captureModules = true;
        init();
    }

    public PomPeek( final String content )
    {
        this.pom = null;
        this.transfer = null;
        this.stream = new ByteArrayInputStream( content.getBytes() );
        captureModules = true;
        init();
    }

    public PomPeek( final InputStream stream )
    {
        this.pom = null;
        this.transfer = null;
        this.stream = stream;
        captureModules = true;
        init();
    }

    public PomPeek( final File pom, final boolean captureModules )
    {
        this.pom = pom;
        this.transfer = null;
        this.stream = null;
        this.captureModules = captureModules;
        init();
    }

    public PomPeek( final Transfer transfer, final boolean captureModules )
    {
        this.pom = null;
        this.transfer = transfer;
        this.stream = null;
        this.captureModules = captureModules;
        init();
    }

    public PomPeek( final String content, final boolean captureModules )
    {
        this.pom = null;
        this.transfer = null;
        this.stream = new ByteArrayInputStream( content.getBytes() );
        this.captureModules = captureModules;
        init();
    }

    public PomPeek( final InputStream stream, final boolean captureModules )
    {
        this.pom = null;
        this.transfer = null;
        this.stream = stream;
        this.captureModules = captureModules;
        init();
    }

    private void init()
    {
        parseCoordElements();

        if ( !createCoordinateInfo() )
        {
            logger.warn( "Could not peek at POM coordinate for: " + pom
                + "\nThis POM will NOT be available as an ancestor to other models during effective-model building." );
        }
    }

    public String getParentRelativePath()
    {
        return elementValues.get( PRP );
    }

    public Set<String> getModules()
    {
        return modules;
    }

    public File getPom()
    {
        return pom;
    }

    public ProjectVersionRef getKey()
    {
        return key;
    }

    public ProjectVersionRef getParentKey()
    {
        return parentKey;
    }

    private void parseCoordElements()
    {
        InputStream in = null;
        XMLStreamReader xml = null;
        try
        {
            if ( pom != null )
            {
                in = new FileInputStream( pom );
            }
            else if ( transfer != null )
            {
                in = transfer.openInputStream( false );
            }
            else
            {
                in = stream;
            }

            xml = XMLInputFactory.newFactory()
                                 .createXMLStreamReader( in );

            final Stack<String> path = new Stack<>();
            while ( xml.hasNext() )
            {
                final int evt = xml.next();
                switch ( evt )
                {
                    case START_ELEMENT:
                    {
                        final String elem = xml.getLocalName();
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
        catch ( final IOException | FactoryConfigurationError | XMLStreamException e )
        {
            logger.warn( "Failed to peek at POM coordinate for: " + pom + " Reason: " + e.getMessage()
                + "\nThis POM will NOT be available as an ancestor to other models during effective-model building.", e );
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
                    logger.warn( "Failed to close XMLStreamReader: " + e.getMessage(), e );
                }
            }

            closeQuietly( in );
        }
    }

    private boolean foundAll()
    {
        for ( final String key : COORD_KEYS )
        {
            if ( !elementValues.containsKey( key ) )
            {
                return false;
            }
        }

        // the only reason we need to parse the full doc is to capture modules.
        return !( captureModules && "pom".equals( elementValues.get( PKG ) ) );

    }

    private boolean captureValue( final String elem, final Stack<String> path, final XMLStreamReader xml )
        throws XMLStreamException
    {
        final boolean isModule = path.contains(MODULES_ELEM) && !path.contains(PLUGINS_ELEM);

        path.push( elem );

        final String pathStr = join(path, ":");
        final String key = CAPTURED_PATHS.get(pathStr);

        if ( key != null )
        {
            elementValues.put( key, xml.getElementText()
                                       .trim() );

            return true;
        }
        else if ( captureModules && isModule )
        {
            modules.add( xml.getElementText()
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
            key = new SimpleProjectVersionRef( g, a, v );
            valid = true;
        }

        if ( isValidArtifactId( pa ) && isValidGroupId( pg ) && isValidVersion( pv ) )
        {
            parentKey = new SimpleProjectVersionRef( pg, pa, pv );
        }

        return valid;
    }

    private boolean isValidVersion( final String version )
    {
        return !isEmpty( version ) && !"version".equals( version ) && !"parentVersion".equals( version );

    }

    private boolean isValidGroupId( final String groupId )
    {
        return !isEmpty( groupId ) && !groupId.contains( "${" ) && !"parentGroupId".equals( groupId )
                && !"groupId".equals( groupId );

    }

    private boolean isValidArtifactId( final String artifactId )
    {
        return !isEmpty( artifactId ) && !artifactId.contains( "${" ) && !"parentArtifactId".equals( artifactId )
                && !"artifactId".equals( artifactId );

    }


    /**
     * Represents either the top level pom in a project or a standalone pom i.e. a child
     * module that does not inherit the parent.
     * @param b
     */
    public void setInheritanceRoot( final boolean b )
    {
        inheritanceRoot = b;
    }

    public boolean isInheritanceRoot()
    {
        return inheritanceRoot;
    }
}
