package org.commonjava.maven.galley;

import java.text.MessageFormat;
import java.util.IllegalFormatException;

public class TransferException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    private final Object[] params;

    private transient String formatted;

    public TransferException( final String format, final Object... params )
    {
        super( format );
        this.params = params;
    }

    public TransferException( final String format, final Throwable error, final Object... params )
    {
        super( format, error );
        this.params = params;
    }

    @Override
    public String getMessage()
    {
        if ( formatted == null )
        {
            formatted = super.getMessage();

            if ( params != null )
            {
                try
                {
                    formatted = String.format( formatted, params );
                }
                catch ( final IllegalFormatException ife )
                {
                    try
                    {
                        formatted = MessageFormat.format( formatted, params );
                    }
                    catch ( final IllegalArgumentException iae )
                    {
                    }
                }
            }
        }

        return formatted;
    }

}
