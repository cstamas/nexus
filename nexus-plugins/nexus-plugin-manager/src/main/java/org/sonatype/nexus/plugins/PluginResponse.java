package org.sonatype.nexus.plugins;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plugin.metadata.GAVCoordinate;

public class PluginResponse
{
    private final GAVCoordinate pluginCoordinates;

    private final PluginActivationRequest wantedGoal;

    private PluginDescriptor pluginDescriptor;

    private Throwable throwable;

    private PluginActivationResult achievedGoal;

    public PluginResponse( GAVCoordinate pluginCoordinates, PluginActivationRequest action )
    {
        this.pluginCoordinates = pluginCoordinates;

        this.wantedGoal = action;

        this.achievedGoal = null;
    }

    public GAVCoordinate getPluginCoordinates()
    {
        return pluginCoordinates;
    }

    public boolean isSuccessful()
    {
        return wantedGoal.isSucess( achievedGoal );
    }

    public Throwable getThrowable()
    {
        return throwable;
    }

    public void setThrowable( Throwable throwable )
    {
        this.throwable = throwable;

        setAchievedGoal( PluginActivationResult.BROKEN );
    }

    public PluginDescriptor getPluginDescriptor()
    {
        return pluginDescriptor;
    }

    public void setPluginDescriptor( PluginDescriptor pluginDescriptor )
    {
        this.pluginDescriptor = pluginDescriptor;
    }

    public PluginActivationRequest getWantedGoal()
    {
        return wantedGoal;
    }

    public PluginActivationResult getAchievedGoal()
    {
        return achievedGoal;
    }

    public void setAchievedGoal( PluginActivationResult achievedGoal )
    {
        this.achievedGoal = achievedGoal;
    }

    // ==

    public String formatAsString( boolean detailed )
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "... " ).append( getPluginCoordinates().toString() ).append( " :: action=" )
            .append( wantedGoal.toString() ).append( " result=" ).append( achievedGoal.toString() ).append( "\n" );

        if ( !isSuccessful() )
        {
            sb.append( "       Reason: " ).append( getThrowable().getLocalizedMessage() ).append( "\n" );

            if ( detailed )
            {
                StringWriter sw = new StringWriter();

                getThrowable().printStackTrace( new PrintWriter( sw ) );

                sb.append( "Stack trace:\n" ).append( sw.toString() ).append( "\n" );
            }
        }

        if ( detailed && getPluginDescriptor() != null )
        {
            sb.append( "\n" );

            PluginDescriptor pluginDescriptor = getPluginDescriptor();

            sb.append( "       Detailed report about the plugin \"" ).append(
                                                                              pluginDescriptor.getPluginCoordinates()
                                                                                  .toString() ).append( "\":\n\n" );

            sb.append( "         Source: \"" ).append( pluginDescriptor.getSource() ).append( "\":\n" );

            sb.append( "         Plugin defined these components:\n" );

            for ( ComponentDescriptor<?> component : pluginDescriptor.getComponents() )
            {
                sb.append( "         * FQN of Type \"" ).append( component.getRole() );

                if ( StringUtils.isNotBlank( component.getRoleHint() ) )
                {
                    sb.append( "\", named as \"" ).append( component.getRoleHint() );
                }

                sb.append( "\", with implementation \"" ).append( component.getImplementation() ).append( "\"\n" );
            }

            if ( !pluginDescriptor.getPluginRepositoryTypes().isEmpty() )
            {
                sb.append( "\n" );
                sb.append( "         Plugin defined these custom Repository Types:\n" );

                for ( PluginRepositoryType repoType : pluginDescriptor.getPluginRepositoryTypes().values() )
                {
                    sb.append( "         * FQN of Type \"" + repoType.getComponentContract()
                        + "\", to be published at path \"" + repoType.getPathPrefix() + "\"\n" );
                }
            }

            if ( !pluginDescriptor.getPluginStaticResourceModels().isEmpty() )
            {
                sb.append( "\n" );
                sb.append( "         Plugin contributed these static resources:\n" );

                for ( PluginStaticResourceModel model : pluginDescriptor.getPluginStaticResourceModels() )
                {
                    sb.append( "         * Resource path \"" + model.getResourcePath()
                        + "\", to be published at path \"" + model.getPublishedPath() + "\", content type \""
                        + model.getContentType() + "\"\n" );
                }
            }
        }

        return sb.toString();
    }

}
