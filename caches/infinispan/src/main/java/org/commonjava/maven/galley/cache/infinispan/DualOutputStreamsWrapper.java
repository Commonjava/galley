package org.commonjava.maven.galley.cache.infinispan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A output stream wrapper to let the stream writing to dual output stream
 */
public class DualOutputStreamsWrapper
        extends OutputStream
{

    private OutputStream out1;

    private OutputStream out2;

    private TransactionManager cacheTxMgr;

    public DualOutputStreamsWrapper( final OutputStream out1, final OutputStream out2,
                                     final TransactionManager cacheTxMgr )
    {
        this.out1 = out1;
        this.out2 = out2;
        this.cacheTxMgr = cacheTxMgr;
    }

    @Override
    public void write( int b )
            throws IOException
    {
        out1.write( b );
        out2.write( b );
    }

    public void write( byte b[] )
            throws IOException
    {
        write( b, 0, b.length );
    }

    @Override
    public void write( byte b[], int off, int len )
            throws IOException
    {
        out1.write( b, off, len );
        out2.write( b, off, len );
    }

    public void flush()
            throws IOException
    {
        out1.flush();
        out2.flush();
    }

    public void close()
            throws IOException
    {
        final Logger logger = LoggerFactory.getLogger( getClass() );

        try
        {
            if ( cacheTxMgr == null || cacheTxMgr.getStatus() == Status.STATUS_NO_TRANSACTION )
            {
                throw new IllegalStateException(
                        "[galley] ISPN transaction not started correctly. May be it is not set correctly, please have a check. " );
            }
            out1.close();
            out2.close();
            cacheTxMgr.commit();
        }
        catch ( SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | IOException e )
        {
            logger.error( "[galley] Transaction commit error for nfs cache during file writing.", e );

            try
            {
                cacheTxMgr.rollback();
            }
            catch ( SystemException se )
            {
                final String errorMsg = "[galley] Transaction rollback error for nfs cache during file writing.";
                logger.error( errorMsg, se );
                throw new IllegalStateException( errorMsg, se );
            }
            if ( e instanceof IOException )
            {
                throw (IOException) e;
            }
        }
    }
}
