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
package org.commonjava.maven.galley.model;

import static org.commonjava.maven.galley.util.PathUtils.ROOT;
import static org.commonjava.maven.galley.util.PathUtils.normalize;
import static org.commonjava.maven.galley.util.PathUtils.parentPath;

public abstract class AbstractResource
    implements Resource
{

    private final String path;

    protected abstract Resource newDerivedResource( String... path );

    public AbstractResource()
    {
        path = ROOT;
    }

    protected AbstractResource( final String... path )
    {
        this.path = normalize( path );
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public boolean isRoot()
    {
        return path == ROOT || ROOT.equals( path );
    }

    @Override
    public Resource getParent()
    {
        if ( isRoot() )
        {
            return null;
        }

        return newDerivedResource( parentPath( path ) );
    }

    @Override
    public Resource getChild( final String file )
    {
        return newDerivedResource( path, file );
    }

}
