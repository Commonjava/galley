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

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

/**
 * Created by jdcasey on 3/7/16.
 */
@Alternative
@Named
public class TransportManagerConfig
{
    final long DEFAULT_THRESHOLD_WAIT_RETRY_SIZE = 10 * 1024 * 1024; // 10 MB files get retried.

    final long DEFAULT_WAIT_RETRY_SCALING_INCREMENT = DEFAULT_THRESHOLD_WAIT_RETRY_SIZE;
            // how many times we retry depends on this.

    final float DEFAULT_TIMEOUT_OVEREXTENSION_FACTOR = 1.25f;

    private final long thresholdWaitRetrySize;

    private final long waitRetryScalingIncrement;

    private float timeoutOverextensionFactor;

    public TransportManagerConfig()
    {
        thresholdWaitRetrySize = DEFAULT_THRESHOLD_WAIT_RETRY_SIZE;
        waitRetryScalingIncrement = DEFAULT_WAIT_RETRY_SCALING_INCREMENT;
        timeoutOverextensionFactor = DEFAULT_TIMEOUT_OVEREXTENSION_FACTOR;
    }

    public TransportManagerConfig( long thresholdWaitRetrySize, long waitRetryScalingIncrement )
    {
        this.thresholdWaitRetrySize = thresholdWaitRetrySize;
        this.waitRetryScalingIncrement = waitRetryScalingIncrement;
    }

    public long getThresholdWaitRetrySize()
    {
        return thresholdWaitRetrySize;
    }

    public long getWaitRetryScalingIncrement()
    {
        return waitRetryScalingIncrement;
    }

    public float getTimeoutOverextensionFactor()
    {
        return timeoutOverextensionFactor;
    }
}
