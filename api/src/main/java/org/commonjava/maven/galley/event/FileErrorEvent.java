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

public class FileErrorEvent
    extends FileEvent
{
    private final Throwable error;

    public FileErrorEvent( final Transfer transfer, final Throwable error )
    {
        super( transfer );
        this.error = error;
    }

    public Throwable getError()
    {
        return error;
    }

}
