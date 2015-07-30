package org.commonjava.maven.galley;

public class TransferTimeoutException
    extends TransferException
{

    private static final long serialVersionUID = 1L;

    public TransferTimeoutException( final String format, final Object... params )
    {
        super( format, params );
    }

    public TransferTimeoutException( final String format, final Throwable error, final Object... params )
    {
        super( format, error, params );
    }

}
