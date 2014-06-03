package org.commonjava.maven.galley.maven.version;

import java.util.List;

import org.commonjava.maven.atlas.ident.version.SingleVersion;

public interface VersionSelectionStrategy
{

    SingleVersion select( List<SingleVersion> candidates );

}
