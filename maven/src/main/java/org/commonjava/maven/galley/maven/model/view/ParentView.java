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
package org.commonjava.maven.galley.maven.model.view;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.w3c.dom.Element;

public class ParentView
    extends MavenGAVView
{

    public ParentView( final MavenPomView pomView, final Element element )
    {
        super( pomView, element );
    }

    public String getRelativePath()
        throws GalleyMavenException
    {
        String val = getValue( "relativePath" );
        if ( val == null )
        {
            val = "../pom.xml";
        }

        return val;
    }

}
