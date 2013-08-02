package org.commonjava.maven.galley.testutil;

import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.spi.transport.DownloadJob;

public interface TestDownloadJob
    extends DownloadJob
{

    void setTransfer( Transfer transfer );

}
