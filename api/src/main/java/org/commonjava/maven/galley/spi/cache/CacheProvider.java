/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.spi.cache;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Transfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface CacheProvider
{
    String SUFFIX_TO_DELETE = ".to-delete";

    String SUFFIX_TO_WRITE = ".to-write";

    @Deprecated
    String STORAGE_PATH = "storage-path";

    String STORE_HTTP_HEADERS = "store-http-headers";

    Set<String> HIDDEN_SUFFIXES = Collections.unmodifiableSet( new HashSet<String>()
    {
        {
            add( SUFFIX_TO_DELETE );
            add( SUFFIX_TO_WRITE );
        }

        private static final long serialVersionUID = 1L;

    } );

    void startReporting();

    void stopReporting();

    void cleanupCurrentThread();

    boolean isDirectory( ConcreteResource resource );

    boolean isFile( ConcreteResource resource );

    InputStream openInputStream( ConcreteResource resource )
        throws IOException;

    OutputStream openOutputStream( ConcreteResource resource )
        throws IOException;

    boolean exists( ConcreteResource resource );

    void copy( ConcreteResource from, ConcreteResource to )
        throws IOException;

    default void move( ConcreteResource from, ConcreteResource to ) throws IOException
    {
        copy( from, to );
        delete( from );
    }

    String getFilePath( ConcreteResource resource );

    default String getStoragePath( ConcreteResource resource )
    {
        return resource.getPath();
    }

    boolean delete( ConcreteResource resource )
        throws IOException;

    String[] list( ConcreteResource resource );

    void mkdirs( ConcreteResource resource )
        throws IOException;

    @Deprecated
    void createFile( ConcreteResource resource )
        throws IOException;

    @Deprecated
    void createAlias( ConcreteResource from, ConcreteResource to )
        throws IOException;

    Transfer getTransfer( ConcreteResource resource );

    void clearTransferCache();

    long length( ConcreteResource resource );

    long lastModified( ConcreteResource resource );

    boolean isReadLocked( ConcreteResource resource );

    boolean isWriteLocked( ConcreteResource resource );

    void unlockRead( ConcreteResource resource );

    void unlockWrite( ConcreteResource resource );

    void lockRead( ConcreteResource resource );

    void lockWrite( ConcreteResource resource );

    void waitForWriteUnlock( ConcreteResource resource );

    void waitForReadUnlock( ConcreteResource resource );

    AdminView asAdminView ();

    interface AdminView extends CacheProvider
    {
        boolean isFileBased();

        default File getDetachedFile( ConcreteResource resource ) { return null; }

        /**
         * Expose GC in case the caller wants to run it directly, especially in functional test
         */
        default void gc() {}
    }
}
