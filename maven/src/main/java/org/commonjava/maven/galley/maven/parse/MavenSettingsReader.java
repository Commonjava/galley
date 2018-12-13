/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DocRef;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.maven.model.view.settings.MavenSettingsView;
import org.w3c.dom.Document;

@ApplicationScoped
public class MavenSettingsReader
{

    //    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    private XPathManager xpath;

    @Inject
    private XMLInfrastructure xml;

    protected MavenSettingsReader()
    {
    }

    public MavenSettingsReader( final XMLInfrastructure xml, final XPathManager xpath )
    {
        this.xml = xml;
        this.xpath = xpath;
    }

    /**
     * Read (and stack by inheritance) one or more settings.xml files, and return a view that can be used to 
     * query various parts of the Maven settings object.<br/>
     * <br/>
     * <b>NOTE:</b> The first file in the list should be the most specific (eg. user-level),
     * followed by ancester files in the inheritance hierarchy (parent, grand-parent, etc.).
     * 
     * @param settingsFiles One or more files to parse, in most-local-first order
     * @return The settings object
     * @throws GalleyMavenException XML parsing failed, or a file could not be read.
     */
    public MavenSettingsView read( final File... settingsFiles )
        throws GalleyMavenException
    {
        final List<DocRef<File>> drs = new ArrayList<>();

        for ( final File f : settingsFiles )
        {
            if ( f == null || !f.exists() )
            {
                continue;
            }

            try
            {
                final Document doc = xml.parse( f );
                drs.add( new DocRef<>( f, f, doc ) );
            }
            catch ( final GalleyMavenXMLException e )
            {
                throw new GalleyMavenException( "Failed to parse settings XML: {}. Reason: {}", e, f, e.getMessage() );
            }
        }

        if ( drs.isEmpty() )
        {
            return null;
        }

        return new MavenSettingsView( drs, xpath, xml );
    }

}
