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
package org.commonjava.maven.galley.filearc.internal;

import java.io.File;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.spi.transport.ExistenceJob;

public class FileExistence
    implements ExistenceJob
{

    private final File src;

    public FileExistence( final File src )
    {
        this.src = src;
    }

    @Override
    public TransferException getError()
    {
        return null;
    }

    @Override
    public Boolean call()
    {
        if ( src.exists() && src.canRead() )
        {
            return true;
        }

        return false;
    }

}
