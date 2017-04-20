/**
 * Copyright (C) 2011 Red Hat, Inc. (jdcasey@commonjava.org)
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

import java.util.Map;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/19/16
 * Time: 1:15 PM
 */
public class TransferMetadata
{
    private final Map<ContentDigest, String> digests;
    private final Long size;

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
}
