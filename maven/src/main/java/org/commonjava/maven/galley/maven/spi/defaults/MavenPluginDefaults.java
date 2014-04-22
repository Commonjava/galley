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
package org.commonjava.maven.galley.maven.spi.defaults;

import org.commonjava.maven.atlas.ident.ref.ProjectRef;

public interface MavenPluginDefaults
{

    public String getDefaultGroupId( String artifactId );

    public String getDefaultVersion( String groupId, String artifactId );

    public String getDefaultVersion( ProjectRef ref );

}
