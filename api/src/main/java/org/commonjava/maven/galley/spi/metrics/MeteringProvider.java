package org.commonjava.maven.galley.spi.metrics;

public interface MeteringProvider
{
    void mark( String name );

    void mark( String name, long count );
}
