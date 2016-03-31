package org.commonjava.maven.galley.spi.io;

public enum OverriddenBooleanValue
{

    OVERRIDE_TRUE( true, true ),
    OVERRIDE_FALSE( true, false ),
    DEFER( false, null );

    private boolean overrides;

    private Boolean result;


    private OverriddenBooleanValue( final boolean overrides, final Boolean result )
    {
        this.overrides = overrides;
        this.result = result;
    }


    public boolean overrides()
    {
        return overrides;
    }

    public boolean getResult()
    {
        return result;
    }

}
