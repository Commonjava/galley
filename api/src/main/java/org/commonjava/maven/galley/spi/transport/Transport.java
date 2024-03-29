/**
 * Copyright (C) 2013 Red Hat, Inc. (https://github.com/Commonjava/galley)
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
package org.commonjava.maven.galley.spi.transport;

import java.io.InputStream;
import java.util.Map;

import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.event.EventMetadata;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

public interface Transport
{

    /**
     * @return NEVER NULL
     */
    ListingJob createListingJob( ConcreteResource resource, Transfer target, int timeoutSeconds )
        throws TransferException;

    /**
     *
     * @param transferSizes
     * @param eventMetadata
     * @return NEVER NULL
     */
    DownloadJob createDownloadJob( ConcreteResource resource, Transfer target, Map<Transfer, Long> transferSizes, int timeoutSeconds,
                                   EventMetadata eventMetadata )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    PublishJob createPublishJob( ConcreteResource resource, InputStream stream, long length, int timeoutSeconds )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    PublishJob createPublishJob( ConcreteResource resource, InputStream stream, long length, String contentType, int timeoutSeconds )
        throws TransferException;

    /**
     * @return NEVER NULL
     */
    ExistenceJob createExistenceJob( ConcreteResource resource, Transfer target, int timeoutSeconds )
        throws TransferException;

    boolean handles( Location location );

    boolean allowsCaching();

}
