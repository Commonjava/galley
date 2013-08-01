package org.commonjava.maven.galley.transport.htcli.model;

import org.commonjava.maven.galley.model.Location;

public interface HttpLocation
    extends Location
{

    String USER_PASSWORD = "userPassword";

    String KEY_PASSWORD = "keyPassword";

    String PROXY_PASSWORD = "proxyPassword";

    String getKeyCertPem();

    String getServerCertPem();

    String getHost();

    int getPort();

    String getUser();

    String getProxyHost();

    String getProxyUser();

    int getProxyPort();

}
