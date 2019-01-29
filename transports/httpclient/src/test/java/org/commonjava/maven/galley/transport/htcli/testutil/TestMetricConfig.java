package org.commonjava.maven.galley.transport.htcli.testutil;

import com.codahale.metrics.MetricRegistry;
import org.commonjava.maven.galley.config.TransportMetricConfig;
import org.commonjava.maven.galley.model.Location;

public class TestMetricConfig
{
    public final static MetricRegistry metricRegistry = new MetricRegistry();

    public final static TransportMetricConfig disabledMetricConfig = new TransportMetricConfig()
    {
        @Override
        public boolean isEnabled()
        {
            return false;
        }

        @Override
        public String getNodePrefix()
        {
            return null;
        }

        @Override
        public String getMetricUniqueName( Location location )
        {
            return null;
        }
    };
}
