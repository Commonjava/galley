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
package org.commonjava.maven.galley.transport.htcli.model;

import org.commonjava.maven.galley.model.Location;

public interface HttpLocation
    extends Location
{

    String getKeyCertPem();

    String getServerCertPem();

    LocationTrustType getTrustType();

    String getHost();

    int getPort();

    String getUser();

    String getProxyHost();

    String getProxyUser();

    int getProxyPort();

    boolean isIgnoreHostnameVerification();
}
