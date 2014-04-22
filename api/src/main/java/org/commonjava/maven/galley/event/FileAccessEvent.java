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
package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Transfer;

public class FileAccessEvent
    extends FileEvent
{

    public FileAccessEvent( final Transfer item )
    {
        super( item );
    }

}
