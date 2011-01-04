/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.client;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.plexus.rest.resource.error.ErrorMessage;

/**
 * Thrown when a NexusClient cannot connect to a Nexus instance, or the Nexus instance returns a non success response.
 */
public class NexusConnectionException
    extends Exception
{

    /**
     * Errors returned from a Nexus server.
     */
    private List<ErrorMessage> errors = new ArrayList<ErrorMessage>();

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = -5163493126499979929L;

    public NexusConnectionException()
    {
        super();
    }

    public NexusConnectionException( List<ErrorMessage> errors )
    {
        super();
        this.errors = errors;
    }

    public NexusConnectionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public NexusConnectionException( String message, Throwable cause, List<ErrorMessage> errors  )
    {
        super( message, cause );
        this.errors = errors;

    }

    public NexusConnectionException( String message )
    {
        super( message );
    }


    public NexusConnectionException( String message, List<ErrorMessage> errors  )
    {
        super( message );
        this.errors = errors;
    }

    public NexusConnectionException( Throwable cause )
    {
        super( cause );
    }

    public NexusConnectionException( Throwable cause, List<ErrorMessage> errors  )
    {
        super( cause );
        this.errors = errors;
    }

    /**
     * A list of errors returned from the server, if any.  Could be empty or null.
     *
     * @return A List of errors returned from the server.
     */
    public List<ErrorMessage> getErrors()
    {
        return errors;
    }

    @Override
    public String getMessage()
    {
        StringBuffer message = new StringBuffer(super.getMessage());

        if(this.getErrors() != null)
        {
            for ( ErrorMessage error : this.getErrors() )
            {
                message.append( "\n" ).append( error.getMsg() );
            }
        }

        return message.toString();
    }



}
