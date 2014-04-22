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

import org.commonjava.maven.galley.model.Transfer;

/**
 * ONLY return null if there is an error, otherwise, return the transfer passed 
 * in, and allow the .exists() method to return false if the remote resource 
 * was not found.
 * 
 * @author jdcasey
 */
public interface DownloadJob
    extends TransportJob<Transfer>
{

}
