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
package org.commonjava.maven.galley.filearc;

import java.io.File;

import org.commonjava.maven.galley.spi.io.PathGenerator;

public class FileTransportConfig
{

    private final File pubDir;

    private final PathGenerator generator;

    public FileTransportConfig( final File pubDir, final PathGenerator generator )
    {
        this.pubDir = pubDir;
        this.generator = generator;
    }

    public FileTransportConfig()
    {
        // read-only file transport config.
        this( null, null );
    }

    public File getPubDir()
    {
        return pubDir;
    }

    public PathGenerator getGenerator()
    {
        return generator;
    }

}
