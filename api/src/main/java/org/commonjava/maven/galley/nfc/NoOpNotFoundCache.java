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
package org.commonjava.maven.galley.nfc;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.spi.nfc.NotFoundCache;

@Named( "no-op-galley-nfc" )
@Alternative
public class NoOpNotFoundCache
    implements NotFoundCache
{

    @Override
    public void addMissing( final ConcreteResource resource )
    {
    }

    @Override
    public boolean isMissing( final ConcreteResource resource )
    {
        return false;
    }

    @Override
    public void clearMissing( final Location location )
    {
    }

    @Override
    public void clearMissing( final ConcreteResource resource )
    {
    }

    @Override
    public void clearAllMissing()
    {
    }

    @Override
    public Map<Location, Set<String>> getAllMissing()
    {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getMissing( final Location location )
    {
        return Collections.emptySet();
    }

}
