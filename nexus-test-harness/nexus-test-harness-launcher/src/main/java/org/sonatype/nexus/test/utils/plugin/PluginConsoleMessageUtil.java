package org.sonatype.nexus.test.utils.plugin;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoListResponseDTO;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class PluginConsoleMessageUtil
{
    private static final String PLUGIN_INFOS_URL = "service/local/plugin_console/plugin_infos";

    private static XStream xmlXstream;

    private static final Logger LOGGER = Logger.getLogger( PluginConsoleMessageUtil.class );

    static
    {
        xmlXstream = XStreamFactory.getXmlXStream();
    }

    public List<PluginInfoDTO> listPluginInfos()
        throws IOException
    {
        String serviceURI = PLUGIN_INFOS_URL;

        LOGGER.info( "HTTP GET: " + serviceURI );

        Response response = RequestFacade.sendMessage( serviceURI, Method.GET );

        if ( response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();

            LOGGER.debug( "Response Text: \n" + responseText );

            XStreamRepresentation representation = new XStreamRepresentation(
                xmlXstream,
                responseText,
                MediaType.APPLICATION_XML );

            PluginInfoListResponseDTO responseDTO = (PluginInfoListResponseDTO) representation
                .getPayload( new PluginInfoListResponseDTO() );

            return responseDTO.getData();
        }
        else
        {
            LOGGER.warn( "HTTP Error: '" + response.getStatus().getCode() + "'" );

            LOGGER.warn( response.getEntity().getText() );

            return null;
        }
    }

}
