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

/**
 * Types of checksums used for content metadata, which may be generated via things like
 * {@link org.commonjava.maven.galley.io.ChecksummingTransferDecorator}.
 */
public enum ContentDigest
{

    MD5,
    SHA_512( "SHA-512" ),
    SHA_384( "SHA-384" ),
    SHA_256( "SHA-256" ),
    SHA_1 ( "SHA-1" );

    private final String digestName;

    ContentDigest()
    {
        this.digestName = name();
    }

    ContentDigest( final String digestName )
    {
        this.digestName = digestName;
    }

    public String digestName()
    {
        return digestName;
    }

}
