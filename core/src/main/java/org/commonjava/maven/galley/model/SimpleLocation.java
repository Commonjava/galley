package org.commonjava.maven.galley.model;

public class SimpleLocation
    implements Location
{

    private final boolean allowSnapshots;

    private final boolean allowReleases;

    private final String uri;

    private final int timeoutSeconds;

    public SimpleLocation( final String uri, final boolean allowSnapshots, final boolean allowReleases,
                           final int timeoutSeconds )
    {
        this.uri = uri;
        this.allowSnapshots = allowSnapshots;
        this.allowReleases = allowReleases;
        this.timeoutSeconds = timeoutSeconds;
    }

    public SimpleLocation( final String uri )
    {
        this.uri = uri;
        this.allowReleases = true;
        this.allowSnapshots = false;
        this.timeoutSeconds = -1;
    }

    @Override
    public boolean allowsSnapshots()
    {
        return allowSnapshots;
    }

    @Override
    public boolean allowsReleases()
    {
        return allowReleases;
    }

    @Override
    public String getUri()
    {
        return uri;
    }

    @Override
    public int getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

}
