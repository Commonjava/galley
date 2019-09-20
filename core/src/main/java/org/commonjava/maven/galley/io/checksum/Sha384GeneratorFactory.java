/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.io.checksum;

import com.codahale.metrics.Timer;
import org.commonjava.maven.galley.io.checksum.Sha384GeneratorFactory.Sha384Generator;
import org.commonjava.maven.galley.model.Transfer;

import java.io.IOException;
import java.util.function.Function;

import static org.commonjava.maven.galley.io.checksum.ContentDigest.SHA_384;

public final class Sha384GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Sha384Generator>
{

    public Sha384GeneratorFactory()
    {
    }

    @Override
    protected Sha384Generator newGenerator( final Transfer transfer, final boolean writeChecksumFile,
                                            final Function<String, Timer.Context> timerProvider )
        throws IOException
    {
        return new Sha384Generator( transfer, writeChecksumFile, timerProvider );
    }

    public static final class Sha384Generator
        extends AbstractChecksumGenerator
    {

        protected Sha384Generator( final Transfer transfer, final boolean writeChecksumFile,
                                   final Function<String, Timer.Context> timerProvider )
            throws IOException
        {
            super( transfer, ".sha384", SHA_384, writeChecksumFile, timerProvider );
        }

    }

}
