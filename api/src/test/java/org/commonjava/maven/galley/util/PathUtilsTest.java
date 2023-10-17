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
package org.commonjava.maven.galley.util;

import static org.commonjava.maven.galley.util.LocationUtils.ATTR_PATH_ENCODE;
import static org.commonjava.maven.galley.util.LocationUtils.PATH_ENCODE_BASE64;
import static org.commonjava.maven.galley.util.UrlUtils.buildUrl;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.junit.Test;

public class PathUtilsTest
{
    @Test
    public void normalizeDirectoryWithTrailingSlashAndChildFile()
    {
        final String result = PathUtils.normalize( "dir/", "child.txt" );
        assertThat( result, equalTo( "dir/child.txt" ) );
    }

    @Test
    public void buildUrlTest() throws Exception
    {
        final String uri = "https://www.somesite.com/";
        final String expected = uri + "employ?version=1.0";

        // Prepare location with metadata 'path-encode'
        Location loc = new SimpleLocation( "test", uri );
        loc.setAttribute(ATTR_PATH_ENCODE, PATH_ENCODE_BASE64);

        // Test resource with base64-encoded virtual path
        ConcreteResource resource = new ConcreteResource(loc, "L2VtcGxveT92ZXJzaW9uPTEuMA");
        String url = buildUrl(resource);
        //System.out.println(url);
        assertThat( url, equalTo(expected));
    }
}
