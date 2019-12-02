package org.commonjava.maven.galley.io.checksum;

public enum ChecksumAlgorithm
{
    MD5( ".md5" ), SHA1( ".sha1" ), SHA256( ".sha256" ), SHA384( ".sha384" ), SHA512( ".sha512" );

    private String extension;

    ChecksumAlgorithm( String extension )
    {
        this.extension = extension;
    }

    public String getExtension()
    {
        return extension;
    }
}
