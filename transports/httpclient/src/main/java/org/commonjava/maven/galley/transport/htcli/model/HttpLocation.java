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
