package org.commonjava.maven.galley.spi.transport;

import java.util.concurrent.Callable;

import org.commonjava.maven.galley.TransferException;

public interface TransportJob<T>
    extends Callable<T>
{

    TransferException getError();

}
