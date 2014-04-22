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
package org.commonjava.maven.galley.transport.htcli;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public interface Http
{

    String HTTP_PARAM_LOCATION = "Location-Object";

    void bindCredentialsTo( final HttpLocation location, final HttpRequest request );

    HttpClient getClient();

    void clearBoundCredentials( HttpLocation location );

    void clearAllBoundCredentials();

    void closeConnection();

}
