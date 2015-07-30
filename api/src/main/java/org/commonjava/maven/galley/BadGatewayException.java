package org.commonjava.maven.galley;


public class BadGatewayException
    extends TransferException
{

    private static final long serialVersionUID = 1L;

    private final int statusCode;

    public BadGatewayException( final int code, final String format, final Object... params )
    {
        super( format, params );
        this.statusCode = code;
    }

    public BadGatewayException( final int code, final String format, final Throwable error, final Object... params )
    {
        super( format, error, params );
        this.statusCode = code;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

}
