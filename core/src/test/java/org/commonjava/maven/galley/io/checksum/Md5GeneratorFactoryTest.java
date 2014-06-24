package org.commonjava.maven.galley.io.checksum;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.commonjava.maven.galley.io.checksum.Md5GeneratorFactory.Md5Generator;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.SimpleLocation;
import org.commonjava.maven.galley.model.Transfer;
import org.commonjava.maven.galley.testing.core.ApiFixture;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class Md5GeneratorFactoryTest
{

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Rule
    public ApiFixture fixture = new ApiFixture( temp );

    @Before
    public void before()
    {
        fixture.initMissingComponents();
    }

    @Test
    public void verifyWithMessageDigest()
        throws Exception
    {
        final byte[] data = "this is a test".getBytes();

        final Transfer txfr =
            fixture.getCache()
                   .getTransfer( new ConcreteResource( new SimpleLocation( "test:uri" ), "my-path.txt" ) );

        final File target = txfr.getDetachedFile();

        final Md5GeneratorFactory factory = new Md5GeneratorFactory();

        final Md5Generator generator = factory.newGenerator( txfr );
        generator.update( data );
        generator.write();

        final MessageDigest md = MessageDigest.getInstance( "MD5" );
        md.update( data );

        final byte[] digest = md.digest();
        final String digestHex = Hex.encodeHexString( digest );

        final File targetMd5 = new File( target.getParentFile(), target.getName() + ".md5" );
        final String resultHex = FileUtils.readFileToString( targetMd5 );

        assertThat( resultHex, equalTo( digestHex ) );
    }

}
