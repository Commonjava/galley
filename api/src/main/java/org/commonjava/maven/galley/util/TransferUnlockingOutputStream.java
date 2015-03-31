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
package org.commonjava.maven.galley.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.Transfer.TransferUnlocker;

public class TransferUnlockingOutputStream
    extends FilterOutputStream
{

    private final TransferUnlocker unlocker;

    public TransferUnlockingOutputStream( final OutputStream out, final TransferUnlocker unlocker )
    {
        super( out );
        this.unlocker = unlocker;
    }

    @Override
    public void close()
        throws IOException
    {
        super.close();
        unlocker.unlock();
    }

}
