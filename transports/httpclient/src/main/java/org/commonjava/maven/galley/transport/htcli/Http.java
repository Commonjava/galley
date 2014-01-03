/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
