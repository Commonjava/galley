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

import org.commonjava.maven.galley.io.checksum.Md5GeneratorFactory.Md5Generator;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.metrics.TimingProvider;

import java.io.IOException;
import java.util.function.Function;

public final class Md5GeneratorFactory
    extends AbstractChecksumGeneratorFactory<Md5Generator>
{

    public Md5GeneratorFactory()
    {
    }

    @Override
    protected Md5Generator newGenerator( final Transfer transfer, final boolean writeChecksumFile,
                                         final Function<String, TimingProvider> timerProvider )
        throws IOException
    {
        return new Md5Generator( transfer, writeChecksumFile, timerProvider );
    }

    public static final class Md5Generator
        extends AbstractChecksumGenerator
    {

        protected Md5Generator( final Transfer transfer, final boolean writeChecksumFile,
                                final Function<String, TimingProvider> timerProvider )
            throws IOException
        {
            super( transfer, ChecksumAlgorithm.MD5.getExtension(), ContentDigest.MD5, writeChecksumFile, timerProvider );
        }

    }

}
