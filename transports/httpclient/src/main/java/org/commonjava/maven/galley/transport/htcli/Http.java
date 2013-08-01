package org.commonjava.maven.galley.transport.htcli;

import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.commonjava.maven.galley.transport.htcli.model.HttpLocation;

public interface Http
{

    String HTTP_PARAM_LOCATION = "Location-Object";

    void bindCredentialsTo( final HttpLocation location, final HttpRequest request );

    HttpClient getClient();

    void clearBoundCredentials( final HttpLocation location );

    void closeConnection();

}
