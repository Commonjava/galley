/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.galley.io;

import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.model.TransferOperation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.CALCULATE_AND_WRITE;
import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.CALCULATE_NO_WRITE;
import static org.commonjava.maven.galley.io.checksum.ChecksummingDecoratorAdvisor.ChecksumAdvice.NO_DECORATE;

/**
 * Support deprecated constructors in {@link ChecksummingTransferDecorator} which used old boolean-and-set style to
 * determine when checksums should be calculated vs written. This class adapts that old style to the new style based on
 * {@link ChecksummingDecoratorAdvisor}.
 *
 * Created by jdcasey on 5/5/17.
 */
@Deprecated
public class DeprecatedChecksummingFilter
        implements ChecksummingDecoratorAdvisor
{
    private final boolean enabled;

    private final Set<TransferOperation> writeOperations;

    /**
     * Create a new filter instance using the old style of enabled + allowable-write-operations. If the operation for
     * a given transfer is in writeOperations, this signals that checksums should be calculated AND written to
     * checksum files.
     *
     * @param enabled Whether checksumming is enabled at all for this type of operation (file read / write)
     * @param writeOperations Set of {@link TransferOperation}'s for which checksum-file writing is enabled for this
     * filter
     */
    public DeprecatedChecksummingFilter( final boolean enabled,
                                         final Set<TransferOperation> writeOperations )
    {
        this.enabled = enabled;
        this.writeOperations = writeOperations == null ? Collections.<TransferOperation> emptySet() : writeOperations;
    }

    /**
     * If the filter is not enabled, return {@link ChecksumAdvice#NO_DECORATE}. Otherwise:
     * <ul>
     *     <li>If the operation matches one of the {@link TransferOperation}'s specifically enabled for this
     *     type of decorator (reader vs. writer), return (@link FilterAdvice#CALCULATE_AND_WRITE}</li>
     *     <li>Otherwise, return {@link ChecksumAdvice#CALCULATE_NO_WRITE}</li>
     * </ul>
     */
    @Override
    public ChecksumAdvice getDecorationAdvice( final Transfer transfer, final TransferOperation operation,
                                               final EventMetadata eventMetadata )
    {
        if ( !enabled )
        {
            return NO_DECORATE;
        }

        if ( writeOperations.contains( operation ) )
        {
            return CALCULATE_AND_WRITE;
        }

        return CALCULATE_NO_WRITE;
    }

    public static Set<TransferOperation> calculateWriteOperations( final Set<TransferOperation> enabledForDecorator,
                                                 final TransferOperation... enabledForFilter)
    {
        Set<TransferOperation> writeOperations = enabledForDecorator == null ?
                new HashSet<TransferOperation>() :
                new HashSet<TransferOperation>( enabledForDecorator );

        writeOperations.retainAll( Arrays.asList( enabledForFilter ) );

        return writeOperations;
    }
}
