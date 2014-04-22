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
package org.commonjava.maven.galley.spi.transport;

import java.util.Collection;
import java.util.List;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Resource;
import org.commonjava.maven.galley.model.VirtualResource;

public interface LocationExpander
{

    List<Location> expand( Location... locations )
        throws TransferException;

    <T extends Location> List<Location> expand( Collection<T> locations )
        throws TransferException;

    VirtualResource expand( Resource resource )
        throws TransferException;

}
