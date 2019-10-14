package org.commonjava.maven.galley.cache.pathmapped.model;

import java.util.Date;

public interface Reclaim
{
    String getFileId();

    Date getDeletion();

    String getStorage();
}
