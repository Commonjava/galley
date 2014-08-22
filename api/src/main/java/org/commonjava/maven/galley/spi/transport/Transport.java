/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.spi.transport;

import java.io.InputStream;

import org.commonjava.maven.galley.TransferException;
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
     * @return NEVER NULL
     */
    DownloadJob createDownloadJob( ConcreteResource resource, Transfer target, int timeoutSeconds )
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
    ExistenceJob createExistenceJob( ConcreteResource resource, int timeoutSeconds )
        throws TransferException;

    boolean handles( Location location );

}
