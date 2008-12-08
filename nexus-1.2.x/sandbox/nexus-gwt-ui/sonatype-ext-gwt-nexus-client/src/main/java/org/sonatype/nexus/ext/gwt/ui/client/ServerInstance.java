package org.sonatype.nexus.ext.gwt.ui.client;

import java.util.HashMap;

import org.sonatype.gwt.client.resource.DefaultResource;
import org.sonatype.gwt.client.resource.Resource;
import org.sonatype.nexus.ext.gwt.ui.client.data.ResponseHandler;

public class ServerInstance {
    
    private ServerType serverType;
    
    private String id;

    private String name;
    
    private HashMap<String, String> defaultHeaders = new HashMap<String, String>();
    
    public ServerInstance(ServerType serverType) {
        if (serverType == null) {
            throw new NullPointerException("serverType is null");
        }
        this.serverType = serverType;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void addDefaultHeader(String name, String value) {
        defaultHeaders.put(name, value);
    }
    
    public void removeDefaultHeader(String name) {
        defaultHeaders.remove(name);
    }
    
    public String getServicePath() {
        return serverType.getServicePath() + "/" + getId();
    }
    
    public Resource getResource(String url) {
        String resourcePath = Constants.HOST + getServicePath() + "/" + url;
        resourcePath += (url.indexOf('?') == -1) ? "?" : "&";
        resourcePath += "_dc=" + System.currentTimeMillis();
        
        Resource resource = new DefaultResource(resourcePath);
        
        resource.addHeaders(defaultHeaders);
        
        return resource;
    }
    
    public void checkLogin(String authorizationToken, ResponseHandler handler) {
    }
    
    public void login(String username, String password, ResponseHandler handler) {
    }
    
    public void logout(ResponseHandler handler) {
    }
    
}
