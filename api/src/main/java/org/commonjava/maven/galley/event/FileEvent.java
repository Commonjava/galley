package org.commonjava.maven.galley.event;

import org.commonjava.maven.galley.model.Transfer;

public class FileEvent
{

    private final Transfer transfer;

    protected FileEvent( final Transfer transfer )
    {
        this.transfer = transfer;
    }

    public Transfer getTransfer()
    {
        return transfer;
    }

    public String getExtraInfo()
    {
        return "";
    }

    @Override
    public String toString()
    {
        return String.format( "%s [%s, transfer=%s]", getClass().getSimpleName(), getExtraInfo(), transfer );
    }

}