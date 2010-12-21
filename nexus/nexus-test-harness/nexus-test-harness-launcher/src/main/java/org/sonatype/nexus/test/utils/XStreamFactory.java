/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import org.sonatype.nexus.rest.NexusApplication;
import org.sonatype.nexus.rest.XStreamInitializer;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;
import org.sonatype.plexus.rest.xstream.json.PrimitiveKeyedMapConverter;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.StringConverter;

/**
 * XStream factory for Nexus Core. It gives away a preconfigured XStream to communicate with Core REST Resources.
 * 
 * @author cstamas
 */
public class XStreamFactory
{
    public static XStream getXmlXStream()
    {
        XStream xs = new XStream( new LookAheadXppDriver() );

        initXStream( xs );

        return xs;
    }

    public static XStream getJsonXStream()
    {
        XStream xs = new XStream( new JsonOrgHierarchicalStreamDriver() );

        // for JSON, we use a custom converter for Maps
        xs.registerConverter( new PrimitiveKeyedMapConverter( xs.getMapper() ) );

        initXStream( xs );

        return xs;
    }
    
    private static void initXStream( XStream xstream )
    {

        NexusApplication napp = new NexusApplication();

        napp.doConfigureXstream( xstream );

        XStreamInitializer.init( xstream );
        
        // Nexus replaces the String converter with one that escape HTML, we do NOT want that on the IT client.
        xstream.registerConverter( new StringConverter() );
    }

}
