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
