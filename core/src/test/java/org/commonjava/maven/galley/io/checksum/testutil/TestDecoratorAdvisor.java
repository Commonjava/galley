package org.commonjava.maven.galley.io.checksum.testutil;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.NO_DECORATE;

/**
 * Created by jdcasey on 5/5/17.
 */
public class TestDecoratorAdvisor
        implements ChecksummingDecoratorAdvisor
{
    public static final String DO_CHECKSUMS = "doChecksums";

    @Override
    public ChecksumAdvice getDecorationAdvice( final Transfer transfer, final TransferOperation operation,
                                               final EventMetadata eventMetadata )
    {
        ChecksumAdvice advice = (ChecksumAdvice) eventMetadata.get( DO_CHECKSUMS );
        return advice == null ? NO_DECORATE : advice;
    }
}
