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
package org.commonjava.maven.galley.transport.htcli.model;

import org.commonjava.maven.galley.model.Location;

public interface HttpLocation
    extends Location
{

    String getKeyCertPem();

    String getServerCertPem();

    String getHost();

    int getPort();

    String getUser();

    String getProxyHost();

    String getProxyUser();

    int getProxyPort();

}
