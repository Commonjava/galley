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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/19/16
 * Time: 1:15 PM
 */
public class TransferMetadata
        implements Externalizable
{
    private Map<ContentDigest, String> digests;

    private Long size;

    public TransferMetadata()
    {
    }

    public TransferMetadata( Map<ContentDigest, String> digests, Long size )
    {
        this.digests = digests;
        this.size = size;
    }

    public Map<ContentDigest, String> getDigests()
    {
        return digests;
    }

    public Long getSize()
    {
        return size;
    }

    @Override
    public void writeExternal( final ObjectOutput objectOutput )
            throws IOException
    {
        objectOutput.writeLong( size );
        objectOutput.writeObject( digests );
    }

    @Override
    public void readExternal( final ObjectInput objectInput )
            throws IOException, ClassNotFoundException
    {
        this.size = objectInput.readLong();
        //noinspection unchecked
        this.digests = (Map<ContentDigest, String>) objectInput.readObject();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "TransferMetadata: {" );
        sb.append( "\n  size: " ).append( size );
        sb.append( "\n  digests: {" );
        for ( Map.Entry<ContentDigest, String> entry : digests.entrySet() )
        {
            sb.append( "\n    " ).append( entry.getKey() ).append( ": " ).append( entry.getValue() );
        }
        sb.append( "\n  }\n}" );
        return sb.toString();
    }
}
