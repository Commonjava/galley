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
