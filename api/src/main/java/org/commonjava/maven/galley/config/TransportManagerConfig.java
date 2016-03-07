package org.commonjava.maven.galley.config;

import javax.enterprise.inject.Alternative;

/**
 * Created by jdcasey on 3/7/16.
 */
@Alternative
public class TransportManagerConfig
{
    long DEFAULT_THRESHOLD_WAIT_RETRY_SIZE = 10 * 1024 * 1024; // 10 MB files get retried.

    long DEFAULT_WAIT_RETRY_SCALING_INCREMENT = DEFAULT_THRESHOLD_WAIT_RETRY_SIZE;
            // how many times we retry depends on this.

    float DEFAULT_TIMEOUT_OVEREXTENSION_FACTOR = 1.25f;

    private long thresholdWaitRetrySize;

    private long waitRetryScalingIncrement;

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
