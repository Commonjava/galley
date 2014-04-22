/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
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
                    formatted = String.format( formatted.replaceAll( "\\{\\}", "%s" ), params );
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
