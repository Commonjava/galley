package org.commonjava.maven.galley.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.commonjava.maven.galley.model.TransferOperation;

public interface TransferDecorator
{

    OutputStream decorateWrite( OutputStream stream, TransferOperation op )
        throws IOException;

    InputStream decorateRead( InputStream stream )
        throws IOException;

}
