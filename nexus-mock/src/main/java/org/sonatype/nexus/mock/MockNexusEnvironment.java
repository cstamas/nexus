package org.sonatype.nexus.mock;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.util.FileUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class MockNexusEnvironment
{
    private Server server;

    private PlexusContainer plexusContainer;

    public static void main(String[] args) throws Exception {
        System.setProperty("plexus-index.template.file", "templates/index-debug.vm");
        MockNexusEnvironment e = new MockNexusEnvironment(12345, "/nexus");
        e.start();
    }

    public static Server createSimpleJettyServer( int port )
    {
        System.out.println("MockNexusEnvironment.createSimpleJettyServer");
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();

        connector.setHost(null);

        connector.setPort( port );

        server.addConnector( connector );

        return server;
    }

    public MockNexusEnvironment( int port, String contextPath )
        throws Exception
    {
        this( createSimpleJettyServer( port ), contextPath );
    }

    public MockNexusEnvironment( Server server, String contextPath )
        throws Exception
    {
        this.server = server;

        addNexus( server, contextPath );
    }

    public Server getServer()
    {
        return server;
    }

    public PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    public PlexusContainer createPlexusContainer()
        throws Exception
    {
        System.out.println("MockNexusEnvironment.createPlexusContainer");
        if ( plexusContainer == null )
        {
            ClassWorld cw = new ClassWorld( "default", Thread.currentThread().getContextClassLoader() );

            ClassRealm realm = cw.newRealm( "nexus-war", new URLClassLoader( new URL[] { new File(
                "target/nexus-ui/WEB-INF/classes" ).toURI().toURL() }, cw.getRealm( "default" ) ) );

            realm.setParentRealm( cw.getRealm( "default" ) );

            // create one
            ContainerConfiguration cc = new DefaultContainerConfiguration()
                .setClassWorld( cw ).setContainerConfigurationURL( Class.class.getResource( "/plexus/plexus.xml" ) )
                .setContext( createContainerContext() ).addComponentDiscoveryListener(
                    new InhibitingComponentDiscovererListener() );

            plexusContainer = new DefaultPlexusContainer( cc );
        }

        return plexusContainer;
    }

    protected Map<Object, Object> createContainerContext()
    {
        System.out.println("MockNexusEnvironment.createContainerContext");
        Map<Object, Object> containerContext = new HashMap<Object, Object>();

        containerContext.put( "basedir", new File( "" ).getAbsolutePath() );

        containerContext.put( "nexus-work", new File( "target/nexus-work" ).getAbsolutePath() );

        containerContext.put( "security-xml-file", new File( "target/nexus-work/conf/security.xml" ).getAbsolutePath() );

        File webappRoot = new File("../nexus-webapp/src/main/webapp");
        if (webappRoot.exists()) {
            containerContext.put("index.template.file", "templates/index-debug.vm");
        }

        // for EHCache component
        System.setProperty( "nexus.home", new File( "target/nexus-work" ).getAbsolutePath() );

        return containerContext;
    }

    public void start()
        throws Exception
    {
        System.out.println("MockNexusEnvironment.start");
        getServer().start();
    }

    public void stop()
        throws Exception
    {
        getServer().stop();

        getPlexusContainer().dispose();

        plexusContainer = null;
    }

    public void addNexus( Server server, String contextPath )
        throws Exception
    {
        System.out.println("MockNexusEnvironment.addNexus");
        // prepare config
        FileUtils.copyFile( new File("src/test/resources/nexus-1.xml"), new File( "target/nexus-work/conf/nexus.xml" ) );
        FileUtils.copyFile( new File("src/test/resources/security-1.xml"), new File( "target/nexus-work/conf/security.xml" ) );

        // create plexus
        createPlexusContainer();

        // add mock nexus
        ContextHandlerCollection ctxHandler = new ContextHandlerCollection();

        File webappRoot = new File("../nexus-webapp/src/main/webapp");
        if (!webappRoot.exists()) {
            webappRoot = new File("target/nexus-ui");
        }

        WebAppContext webapp = new WebAppContext(
            ctxHandler,
            webappRoot.getAbsolutePath(),
            contextPath );

        // spoof in our simplified web.xml
        webapp.setDescriptor( new File( "target/test-classes/nexus-ui/WEB-INF/web.xml" ).getAbsolutePath() );

        // Put the container for the application into the servlet context

        webapp.setAttribute( PlexusConstants.PLEXUS_KEY, getPlexusContainer() );

        webapp.setClassLoader( getPlexusContainer().getContainerRealm() );

        ctxHandler.mapContexts();

        getServer().addHandler( ctxHandler );
    }
}
