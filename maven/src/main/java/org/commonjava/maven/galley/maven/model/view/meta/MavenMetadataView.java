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
package org.commonjava.maven.galley.maven.model.view.meta;

import java.util.List;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.model.view.DocRef;
import org.commonjava.maven.galley.maven.model.view.MavenXmlView;
import org.commonjava.maven.galley.maven.model.view.XPathManager;
import org.commonjava.maven.galley.maven.parse.XMLInfrastructure;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MavenMetadataView
    extends MavenXmlView<ProjectRef>
{

    public MavenMetadataView( final List<DocRef<ProjectRef>> stack, final XPathManager xpath,
                              final XMLInfrastructure xml )
    {
        super( stack, xpath, xml );
    }

    public VersioningView getVersioning()
    {
        final Node node = resolveXPathToNode( "/metadata/versioning", true, -1 );
        if ( node != null && node.getNodeType() == Node.ELEMENT_NODE )
        {
            return new VersioningView( this, (Element) node );
        }

        return null;
    }

    public String resolveSingleValue( final String path )
        throws GalleyMavenException
    {
        return resolveXPathToRawString( path, true, -1 );
    }

    public List<String> resolveValues( final String path )
        throws GalleyMavenException
    {
        return resolveXPathToAggregatedStringList( path, true, -1 );
    }

}
