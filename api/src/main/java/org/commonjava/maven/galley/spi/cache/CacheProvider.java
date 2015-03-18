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
package org.commonjava.maven.galley.spi.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;

public interface CacheProvider
{
    String SUFFIX_TO_DELETE = ".to-delete";

    String SUFFIX_TO_DOWNLOAD = ".to-download";

    Set<String> HIDDEN_SUFFIXES = Collections.unmodifiableSet( new HashSet<String>()
    {
        {
            add( SUFFIX_TO_DELETE );
            add( SUFFIX_TO_DOWNLOAD );
        }

        private static final long serialVersionUID = 1L;

    } );

    boolean isDirectory( ConcreteResource resource );

    boolean isFile( ConcreteResource resource );

    InputStream openInputStream( ConcreteResource resource )
        throws IOException;

    OutputStream openOutputStream( ConcreteResource resource )
        throws IOException;

    boolean exists( ConcreteResource resource );

    void copy( ConcreteResource from, ConcreteResource to )
        throws IOException;

    String getFilePath( ConcreteResource resource );

    boolean delete( ConcreteResource resource )
        throws IOException;

    String[] list( ConcreteResource resource );

    File getDetachedFile( ConcreteResource resource );

    void mkdirs( ConcreteResource resource )
        throws IOException;

    void createFile( ConcreteResource resource )
        throws IOException;

    void createAlias( ConcreteResource from, ConcreteResource to )
        throws IOException;

    Transfer getTransfer( ConcreteResource resource );

    void clearTransferCache();

}
