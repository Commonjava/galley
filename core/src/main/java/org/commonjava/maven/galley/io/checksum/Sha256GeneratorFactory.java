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
import org.commonjava.maven.galley.io.checksum.Sha256GeneratorFactory.Sha256Generator;
import org.commonjava.maven.galley.model.Transfer;

import java.io.IOException;
import java.util.function.Function;

public final class Sha256GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Sha256Generator>
{

    public Sha256GeneratorFactory()
    {
    }

    @Override
    protected Sha256Generator newGenerator( final Transfer transfer, final boolean writeChecksumFile,
                                            final Function<String, Timer.Context> timerProvider )
        throws IOException
    {
        return new Sha256Generator( transfer, writeChecksumFile, timerProvider );
    }

    public static final class Sha256Generator
        extends AbstractChecksumGenerator
    {

        protected Sha256Generator( final Transfer transfer, final boolean writeChecksumFile,
                                   final Function<String, Timer.Context> timerProvider )
            throws IOException
        {
            super( transfer, ".sha256", ContentDigest.SHA_256, writeChecksumFile, timerProvider );
        }

    }

}
