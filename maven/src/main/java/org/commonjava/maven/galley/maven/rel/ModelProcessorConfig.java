package org.commonjava.maven.galley.maven.rel;

/**
 * Created by jdcasey on 3/21/16.
 */
public class ModelProcessorConfig
{

    private boolean includeManagedDependencies;

    private boolean includeBuildSection;

    private boolean includeManagedPlugins;

    public boolean isIncludeManagedDependencies()
    {
        return includeManagedDependencies;
    }

    public ModelProcessorConfig setIncludeManagedDependencies( boolean includeManagedDependencies )
    {
        this.includeManagedDependencies = includeManagedDependencies;
        return this;
    }

    public boolean isIncludeBuildSection()
    {
        return includeBuildSection;
    }

    public ModelProcessorConfig setIncludeBuildSection( boolean includeBuildSection )
    {
        this.includeBuildSection = includeBuildSection;
        return this;
    }

    public boolean isIncludeManagedPlugins()
    {
        return includeManagedPlugins;
    }

    public ModelProcessorConfig setIncludeManagedPlugins( boolean includeManagedPlugins )
    {
        this.includeManagedPlugins = includeManagedPlugins;
        return this;
    }
}
