/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
