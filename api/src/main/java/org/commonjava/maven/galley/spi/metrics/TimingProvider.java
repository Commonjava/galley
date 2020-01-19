package org.commonjava.maven.galley.spi.metrics;

public interface TimingProvider
{
    void start( String name );

    long stop();
}
