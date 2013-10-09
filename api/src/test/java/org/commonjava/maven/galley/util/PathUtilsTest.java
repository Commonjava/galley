package org.commonjava.maven.galley.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PathUtilsTest
{

    @Test
    public void normalizeDirectoryWithTrailingSlashAndChildFile()
    {
        final String result = PathUtils.normalize( "dir/", "child.txt" );
        assertThat( result, equalTo( "dir/child.txt" ) );
    }

}
