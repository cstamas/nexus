package org.sonatype.nexus.client;

import java.util.List;

import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryResource;

public interface NexusClient
{
    public static final String ROLE = "NexusClient";

    public void connect( String baseUrl, String username, String password ) throws NexusClientException, NexusConnectionException;
    
    public void disconnect() throws NexusClientException, NexusConnectionException;
    
    public RepositoryBaseResource createRepository( RepositoryBaseResource repo ) throws NexusClientException, NexusConnectionException;
    
    public RepositoryBaseResource updateRepository( RepositoryBaseResource repo ) throws NexusClientException, NexusConnectionException;
    
    public RepositoryBaseResource getRepository( String id ) throws NexusClientException, NexusConnectionException;
    
    public void deleteRepository( String id ) throws NexusClientException, NexusConnectionException;
    
    public List<RepositoryListResource> getRespositories() throws NexusClientException, NexusConnectionException;
    
}
