package org.sonatype.nexus.integrationtests.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

public class ReportWriter
{

    private File sourceDir;

    public ReportWriter( File sourceDir )
    {
        this.sourceDir = sourceDir;
    }

    public void writeReport()
    {

        // parse the java doc
        JavaDocBuilder builder = new JavaDocBuilder();
        builder.addSourceTree( this.sourceDir );
        // all parsed

        // now find all of the test classes
        List<JavaClass> testClasses = new ArrayList<JavaClass>();
        JavaClass[] classes = builder.getClasses();

        for ( int ii = 0; ii < classes.length; ii++ )
        {
            JavaClass javaClass = classes[ii];
            if ( classHasMethodWithTestAnnotation( javaClass ) )
            {
                testClasses.add( javaClass );
            }
        }

        List<ReportBean> beans = new ArrayList<ReportBean>();

        for ( JavaClass javaClass : testClasses )
        {
            ReportBean bean = new ReportBean();
            bean.setJavaClass( javaClass );
            bean.setTestId( this.getTestId( javaClass ) );

            // add to the collection
            beans.add( bean );
        }

        // sort the beans.
        Collections.sort( beans );

        // now this would be nice to be configurable, but move the sample tests to the top of the list
        this.fudgeOrder( beans );

        // now write the report // TODO: get from container
        new ConsoleWikiReport().writeReport( beans );

    }

    private void fudgeOrder( List<ReportBean> beans )
    {
        ReportBean nexus166Sample = this.removeBeanFromList( beans, "NEXUS-166" );
        ReportBean nexus262SampleProxy = this.removeBeanFromList( beans, "NEXUS-262" );

        beans.add( 0, nexus262SampleProxy );
        beans.add( 0, nexus166Sample );
    }

    /**
     * Looks for any class that contains a Junit 4 annotation <code>@Test</code>.
     * @param javaClass
     * @return
     */
    private static boolean classHasMethodWithTestAnnotation( JavaClass javaClass )
    {
        JavaMethod[] methods = javaClass.getMethods();
        for ( JavaMethod javaMethod : methods )
        {
            List<Annotation> annotations = Arrays.asList( javaMethod.getAnnotations() );

            for ( Iterator<Annotation> iter = annotations.iterator(); iter.hasNext(); )
            {
                Annotation annotation = iter.next();

                if ( annotation.getType().getValue().equals( Test.class.getName() ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    private String getTestId( JavaClass javaClass )
    {
        String packageName = javaClass.getPackage();
        String testId = packageName.substring( packageName.lastIndexOf( '.' ) + 1, packageName.length() ).toLowerCase();

        if ( testId.startsWith( "nexus" ) )
        {
            testId = testId.replace( "nexus", "NEXUS-" );
        }
        else
        {
            throw new RuntimeException(
                                        "The class: "
                                            + javaClass.getName()
                                            + " is not using the correct format for package.  It sould be something like: <org.sonatype.nexus.inegrationtests>.nexusXXX.NexusXXXDescription. it was: "
                                            + javaClass.getPackage() + "." + javaClass.getName() );
        }
        return testId;
    }

    private ReportBean removeBeanFromList( List<ReportBean> beans, String testId )
    {
        for ( ReportBean bean : beans )
        {
            if ( testId.equals( bean.getTestId() ) )
            {

                beans.remove( bean );
                return bean;
            }
        }
        return null;
    }

    public static void main( String[] args )
    {
        File currentDir = new File( "." );

        File sourceDir = null;

        File parentFile = currentDir.getAbsoluteFile();
        while ( parentFile != null )
        {

            if ( parentFile.getName().equals( "nexus-test-harness-launcher" ) )
            {
                sourceDir = new File( parentFile, "/src/test/java" );
                // we are done with this loop
                break;
            }
            // change the parent
            parentFile = parentFile.getParentFile();
        }

        // make sure we have something
        if( sourceDir == null )
        {
            System.err.println( "Could not figre out the source dir: nexus-test-harness-launcher/src/test/java" );
        }
        
        // now we can write the report
        new ReportWriter( sourceDir ).writeReport();
        
    }

}
