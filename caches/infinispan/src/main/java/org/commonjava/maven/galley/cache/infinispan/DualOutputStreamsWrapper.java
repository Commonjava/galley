package org.commonjava.maven.galley.cache.infinispan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A output stream wrapper to let the stream writing to dual output stream. Will also wrap an ISPN cache
 * transaction manager to manage the transaction commit/rollback when stream is closing.
 */
class DualOutputStreamsWrapper
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
            out1.close();
            out2.close();
            cacheTxMgr.commit();
        }
        catch ( SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | IOException e )
        {
            logger.error( "Transaction commit error for nfs cache during file writing.", e );

            try
            {
                cacheTxMgr.rollback();
            }
            catch ( SystemException se )
            {
                logger.error( "Transaction rollback error for nfs cache during file writing.", se );
            }
            if ( e instanceof IOException )
            {
                throw (IOException) e;
            }
        }
    }
}
