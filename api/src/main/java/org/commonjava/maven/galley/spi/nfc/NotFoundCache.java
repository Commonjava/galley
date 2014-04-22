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
package org.commonjava.maven.galley.spi.nfc;

import java.util.Map;
import java.util.Set;

import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.ConcreteResource;

public interface NotFoundCache
{

    void addMissing( ConcreteResource resource );

    boolean isMissing( ConcreteResource resource );

    void clearMissing( Location location );

    void clearMissing( ConcreteResource resource );

    void clearAllMissing();

    Map<Location, Set<String>> getAllMissing();

    Set<String> getMissing( Location location );

}
