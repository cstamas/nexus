package org.sonatype.nexus.integrationtests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.appbooter.ForkedAppBooterException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.test.utils.DeployUtils;

/**
 * TODO: add REST restart to make tests faster
 * 
 * 
curl --user admin:admin123 --request PUT http://localhost:8081/nexus/service/local/status/command --data STOP
 
curl --user admin:admin123 --request PUT http://localhost:8081/nexus/service/local/status/command --data START
 
curl --user admin:admin123 --request PUT http://localhost:8081/nexus/service/local/status/command --data RESTART
 *
 */
public abstract class AbstractNexusIntegrationTest
{

    private PlexusContainer container;

    private Map<String, Object> context;

    private String basedir;

    private static boolean NEEDS_INIT = false;

    private String nexusBaseDir;

    public static final String RELATIVE_CONF_DIR = "runtime/apps/nexus/conf";

    protected AbstractNexusIntegrationTest()
    {
        this.setupContainer();
        
        // we also need to setup a couple fields, that need to be pulled out of a bundle
        ResourceBundle rb = ResourceBundle.getBundle( "baseTest" );
        
        this.nexusBaseDir = rb.getString( "nexus.base.dir" );
    }

    /**
     * To me this seems like a bad hack around this problem. I don't have any other thoughts though. <BR/>If you see
     * this and think: "Wow, why did he to that instead of XYZ, please let me know." <BR/> The issue is that we want to
     * init the tests once (to start/stop the app) and the <code>@BeforeClass</code> is static, so we don't have access to the package name of the running tests. We are going to
     *              use the package name to find resources for additional setup. NOTE: With this setup running multiple
     *              Test at the same time is not possible.
     * @throws Exception
     */
    @Before
    public void oncePerClassSetUp()
        throws Exception
    {
        synchronized ( AbstractNexusIntegrationTest.class )
        {
            if ( NEEDS_INIT )
            {
                // copy nexus config
                this.copyNexusConfig();

                // start nexus
                this.startNexus();

                // deploy artifacts
                this.deployArtifacts();

                NEEDS_INIT = false;
            }
        }
    }

    private void deployArtifacts()
        throws IOException, XmlPullParserException, ConnectionException, AuthenticationException,
        TransferFailedException, ResourceDoesNotExistException, AuthorizationException, ComponentLookupException
    {
        // test the test directory
        File projectsDir = this.getTestFile( "projects" );
        System.out.println( "projectsDir: " + projectsDir );

        // if null there is nothing to deploy...
        if ( projectsDir != null )
        {

            // we have the parent dir, for each child (one level) we need to grab the pom.xml out of it and parse it,
            // and then deploy the artifact, sounds like fun, right!

            File[] projectFolders = projectsDir.listFiles( new FileFilter()
            {

                public boolean accept( File pathname )
                {
                    return ( !pathname.getName().endsWith( ".svn" ) && pathname.isDirectory() && new File( pathname,
                                                                                                           "pom.xml" ).exists() );
                }
            } );

            for ( int ii = 0; ii < projectFolders.length; ii++ )
            {
                File project = projectFolders[ii];

                // we already check if the pom.xml was in here.
                File pom = new File( project, "pom.xml" );

                MavenXpp3Reader reader = new MavenXpp3Reader();
                FileInputStream fis = new FileInputStream( pom );
                Model model = reader.read( new FileInputStream( pom ) );
                fis.close();

                // a helpful note so you don't need to dig into the code to much.
                if ( model.getDistributionManagement() == null
                    || model.getDistributionManagement().getRepository() == null )
                {
                    Assert.fail( "The test artifact is either missing or has an invalid Distribution Management section." );
                }
                String deployUrl = model.getDistributionManagement().getRepository().getUrl();

                // FIXME, this needs to be fluffed up a little, should add the classifier, etc.
                String artifactFileName = model.getArtifactId() + "." + model.getPackaging();
                File artifactFile = new File( project, artifactFileName );

                System.out.println( "wow, this is working: " + artifactFile );

                // deploy pom
                DeployUtils.deployWithWagon( this.container, "http", deployUrl, pom,
                                             this.getRelitiveArtifactPath( model.getGroupId(), model.getArtifactId(),
                                                                           model.getVersion(), "pom" ) );
                // deploy artifact
                DeployUtils.deployWithWagon( this.container, "http", deployUrl, artifactFile,
                                             this.getRelitiveArtifactPath( model.getGroupId(), model.getArtifactId(),
                                                                           model.getVersion(), model.getPackaging() ) );

            }

        }
    }

    @After
    public void afterTest()
        throws Exception
    {
        // stop nexus
        this.stopNexus();
    }

    private void startNexus()
        throws Exception
    {
        ForkedAppBooter appBooter = (ForkedAppBooter) this.lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
        System.out.println( "getPlatformFile: " + appBooter.getPlatformFile() );
        appBooter.start();
    }

    private void stopNexus()
        throws Exception
    {
        ForkedAppBooter appBooter = (ForkedAppBooter) this.lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );

        try
        {
            appBooter.stop();
        }
        catch ( ForkedAppBooterException e )
        {
            Assert.fail( "Test: "
                + this.getClass().getSimpleName()
                + " failed to stop a forked JVM, so, it was either (most likely) not running or an orphaned process that you will need to kill." );
        }
    }

    private void copyNexusConfig()
        throws IOException
    {
        // the test can override the test config.
        File testNexusConfig = this.getTestFile( "test-config/nexus.xml" );

        // if the tests doesn't have a different config then use the default.
        // we need to replace every time to make sure no one changes it.
        if ( testNexusConfig == null || !testNexusConfig.exists() )
        {
            testNexusConfig = this.getResource( "default-config/nexus.xml" );
        }
        else
        {
            System.out.println( "This test is using its own nexus.xml: "+ testNexusConfig );
        }

        System.out.println( "copying nexus.xml to:  "+ new File( this.nexusBaseDir + "/" + RELATIVE_CONF_DIR, "nexus.xml" ) );
        
        FileUtils.copyFile( testNexusConfig, new File( this.nexusBaseDir + "/" + RELATIVE_CONF_DIR, "nexus.xml" ) );
    }

    public File getTestFile( String relativePath )
    {
        String packageName = this.getClass().getPackage().getName();
        String testId = packageName.substring( packageName.lastIndexOf( '.' ) + 1, packageName.length() );

        String resource = testId + "/" + relativePath;
        return this.getResource( resource );
    }

    public File getResource( String resource )
    {
        System.out.println( "Looking for resource: " + resource );
        URL classURL = Thread.currentThread().getContextClassLoader().getResource( resource );
        System.out.println( "found: " + classURL );
        return classURL == null ? null : new File( classURL.getFile() );
    }

    /**
     * See oncePerClassSetUp.
     */
    @BeforeClass
    public static void staticOncePerClassSetUp()
    {
        NEEDS_INIT = true;
    }

    @AfterClass
    public static void oncePerClassTearDown()
    {

    }

    private void setupContainer()
    {
        // ----------------------------------------------------------------------------
        // Context Setup
        // ----------------------------------------------------------------------------

        context = new HashMap<String, Object>();

        context.put( "basedir", basedir );

        boolean hasPlexusHome = context.containsKey( "plexus.home" );

        if ( !hasPlexusHome )
        {
            File f = new File( basedir, "target/plexus-home" );

            if ( !f.isDirectory() )
            {
                f.mkdir();
            }

            context.put( "plexus.home", f.getAbsolutePath() );
        }

        // ----------------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------------

        ContainerConfiguration containerConfiguration =
            new DefaultContainerConfiguration().setName( "test" ).setContext( context ).setContainerConfiguration(
                                                                                                                   getClass().getName().replace(
                                                                                                                                                 '.',
                                                                                                                                                 '/' )
                                                                                                                       + ".xml" );

        try
        {
            container = new DefaultPlexusContainer( containerConfiguration );
        }
        catch ( PlexusContainerException e )
        {
            e.printStackTrace();
            fail( "Failed to create plexus container." );
        }
    }

    protected Object lookup( String componentKey )
        throws Exception
    {
        return container.lookup( componentKey );
    }

    protected Object lookup( String role, String id )
        throws Exception
    {
        return container.lookup( role, id );
    }

    protected String getRelitiveArtifactPath( Gav gav )
        throws FileNotFoundException
    {
        return this.getRelitiveArtifactPath( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
                                             gav.getExtension() );
    }

    protected String getRelitiveArtifactPath( String groupId, String artifactId, String version, String extension )
        throws FileNotFoundException
    {
        return groupId.replace( '.', '/' ) + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + "."
            + extension;
    }

}
