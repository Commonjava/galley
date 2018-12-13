/**
 * Copyright (C) 2013 Red Hat, Inc. (nos-devel@redhat.com)
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
package org.commonjava.maven.galley.config;

import org.commonjava.maven.galley.model.Location;

public interface TransportMetricConfig
{
    boolean isEnabled();

    /**
     * For clustering. Return node prefix. This will be prepended to metric names.
     * @return null if not clustered.
     */
    String getNodePrefix();

    /**
     * Get metric unique name for the given location.
     * @param location from where to download artifacts,
     *                 e.g., maven:remote:test, maven:remote:koji-com.google.guava-guava-parent-14.0.1
     * @return metric unique name for this location. null if not going to measure metric for this location.
     */
    String getMetricUniqueName( Location location );

}
