/**
 * Copyright (C) 2012 Red Hat, Inc. (jdcasey@commonjava.org)
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
package org.commonjava.maven.galley.maven.parse;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Node;

public final class JXPathUtils
{

    private JXPathUtils()
    {
    }

    public static JXPathContext newContext( final Node node )
    {
        final JXPathContext ctx = JXPathContext.newContext( node );
        ctx.setLenient( true );
        ctx.setFunctions( new ClassFunctions( ResolveFunctions.class, "ext" ) );

        return ctx;
    }

}
