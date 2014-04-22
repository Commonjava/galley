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
