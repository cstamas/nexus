package org.sonatype.nexus.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.appbooter.ctl.AppBooterServiceException;

@Component( role = ForkedAppBooter.class, hint = "TestUnforkedAppBooter", instantiationStrategy = "per-lookup" )
public class UnforkedAppBooter
    implements ForkedAppBooter
{

    @Configuration( value = "${basedir}/src/main/plexus/plexus.xml" )
    private File configuration;

    @Configuration( value = "${basedir}" )
    private File basedir;

    @Configuration( value = "${basedir}/target/appbooter.tmp" )
    private File tempDir;

    @Configuration( value = "" )
    private Map<String, String> systemProperties;

    private Object container;

    public boolean isShutdown()
    {
        return container == null;
    }

    public boolean isStopped()
    {
        return container == null;
    }

    public void shutdown()
        throws AppBooterServiceException
    {
        try
        {
            Method method = container.getClass().getMethod( "dispose" );
            method.invoke( container );
            
            container = null;
        }
        catch ( InvocationTargetException e )
        {
            throw new AppBooterServiceException( e.getTargetException() );
        }
        catch ( Exception e )
        {
            throw new AppBooterServiceException( e );
        }
    }

    public void start()
        throws AppBooterServiceException
    {
        try
        {
            // not 100%, but is it good enough?
            ClassLoader extCL = ClassLoader.getSystemClassLoader().getParent();

            ArrayList<URL> urls = getClasspath();

            ClassLoader origCL = Thread.currentThread().getContextClassLoader();

            ClassLoader cl = new URLClassLoader( urls.toArray( new URL[urls.size()] ), extCL );

            Thread.currentThread().setContextClassLoader( cl );
            try
            {
                doStart( cl );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( origCL );
            }
        }
        catch ( InvocationTargetException e )
        {
            throw new AppBooterServiceException( e.getTargetException() );
        }
        catch ( Exception e )
        {
            throw new AppBooterServiceException( e );
        }
    }

    private ArrayList<URL> getClasspath()
        throws FileNotFoundException,
            IOException,
            MalformedURLException
    {
        String conf = System.getProperty( "classpath.conf" );

        ArrayList<URL> urls = new ArrayList<URL>();
        BufferedReader r = new BufferedReader( new InputStreamReader( new FileInputStream( conf ) ) );
        try
        {
            String str;
            while ( ( str = r.readLine() ) != null )
            {
                urls.add( new File( str ).toURI().toURL() );
            }
        }
        finally
        {
            r.close();
        }
        return urls;
    }

    private void doStart( ClassLoader cl )
        throws Exception
    {

        Class<?> classWorld = cl.loadClass( "org.codehaus.plexus.classworlds.ClassWorld" );

        Object world = classWorld.newInstance();
        newClassRealm( world, "plexus.core", cl );

        Object cfg = newConfiguration( cl, world );

        Class<?> plxClass = cl.loadClass( "org.codehaus.plexus.DefaultPlexusContainer" );
        Class<?> cfgClass = cl.loadClass( "org.codehaus.plexus.ContainerConfiguration" );
        Constructor<?> plxConst = plxClass.getConstructor( cfgClass );
        container = plxConst.newInstance( cfg );
    }

    private Object newConfiguration( ClassLoader cl, Object world )
        throws Exception
    {
        /*
         * ContainerConfiguration cc = new DefaultContainerConfiguration() .setClassWorld( world )
         * .setContainerConfiguration( configuration.getAbsolutePath() ) .setContext( createContainerContext() )
         * .setDevMode( Boolean.getBoolean( DEV_MODE ) );
         */
        Class<?> cfgClass = cl.loadClass( "org.codehaus.plexus.DefaultContainerConfiguration" );

        Object cfg = cfgClass.newInstance();

        Method method;

        // .setClassWorld( world )
        method = cfgClass.getMethod( "setClassWorld", world.getClass() );
        method.invoke( cfg, world );

        // setContainerConfiguration
        method = cfgClass.getMethod( "setContainerConfiguration", String.class );
        method.invoke( cfg, configuration.getAbsolutePath() );

        // setContext
        method = cfgClass.getMethod( "setContext", Map.class );
        method.invoke( cfg, getContext() );

        return cfg;
    }

    private Map<String, String> getContext()
    {
        Map<String, String> context = new LinkedHashMap<String, String>();
        for ( Map.Entry<String, String> e : systemProperties.entrySet() )
        {
            String key = e.getKey();
            if ( key.startsWith( "plexus." ) ) {
                key = key.substring( "plexus.".length() );
            }
            context.put( key, e.getValue() );
        }
        return context;
    }

    private Object newClassRealm( Object world, String name, ClassLoader cl )
        throws Exception
    {
        Method method = world.getClass().getMethod( "newRealm", String.class, ClassLoader.class );
        return method.invoke( world, name, cl );
    }

    public void stop()
        throws AppBooterServiceException
    {
        shutdown();
    }

}
