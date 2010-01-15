/*
 =================== DO NOT EDIT THIS FILE ====================
 Generated by Modello 1.0.2 on 2010-01-11 12:09:20,
 any modifications will be overwritten.
 ==============================================================
 */

package org.sonatype.nexus.rest.model;

/**
 * Class SmtpSettingsResource.
 * 
 * @version $Revision$ $Date$
 */
public class SmtpSettingsResource
    implements java.io.Serializable
{

      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field testEmail.
     */
    private String testEmail;

    /**
     * Field host.
     */
    private String host;

    /**
     * Field port.
     */
    private int port = 0;

    /**
     * Field username.
     */
    private String username;

    /**
     * Field password.
     */
    private String password;

    /**
     * Field systemEmailAddress.
     */
    private String systemEmailAddress;

    /**
     * Field sslEnabled.
     */
    private boolean sslEnabled = false;

    /**
     * Field tlsEnabled.
     */
    private boolean tlsEnabled = false;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Get the host field.
     * 
     * @return String
     */
    public String getHost()
    {
        return this.host;
    } //-- String getHost()

    /**
     * Get the password field.
     * 
     * @return String
     */
    public String getPassword()
    {
        return this.password;
    } //-- String getPassword()

    /**
     * Get the port field.
     * 
     * @return int
     */
    public int getPort()
    {
        return this.port;
    } //-- int getPort()

    /**
     * Get the systemEmailAddress field.
     * 
     * @return String
     */
    public String getSystemEmailAddress()
    {
        return this.systemEmailAddress;
    } //-- String getSystemEmailAddress()

    /**
     * Get the testEmail field.
     * 
     * @return String
     */
    public String getTestEmail()
    {
        return this.testEmail;
    } //-- String getTestEmail()

    /**
     * Get the username field.
     * 
     * @return String
     */
    public String getUsername()
    {
        return this.username;
    } //-- String getUsername()

    /**
     * Get the sslEnabled field.
     * 
     * @return boolean
     */
    public boolean isSslEnabled()
    {
        return this.sslEnabled;
    } //-- boolean isSslEnabled()

    /**
     * Get the tlsEnabled field.
     * 
     * @return boolean
     */
    public boolean isTlsEnabled()
    {
        return this.tlsEnabled;
    } //-- boolean isTlsEnabled()

    /**
     * Set the host field.
     * 
     * @param host
     */
    public void setHost( String host )
    {
        this.host = host;
    } //-- void setHost( String )

    /**
     * Set the password field.
     * 
     * @param password
     */
    public void setPassword( String password )
    {
        this.password = password;
    } //-- void setPassword( String )

    /**
     * Set the port field.
     * 
     * @param port
     */
    public void setPort( int port )
    {
        this.port = port;
    } //-- void setPort( int )

    /**
     * Set the sslEnabled field.
     * 
     * @param sslEnabled
     */
    public void setSslEnabled( boolean sslEnabled )
    {
        this.sslEnabled = sslEnabled;
    } //-- void setSslEnabled( boolean )

    /**
     * Set the systemEmailAddress field.
     * 
     * @param systemEmailAddress
     */
    public void setSystemEmailAddress( String systemEmailAddress )
    {
        this.systemEmailAddress = systemEmailAddress;
    } //-- void setSystemEmailAddress( String )

    /**
     * Set the testEmail field.
     * 
     * @param testEmail
     */
    public void setTestEmail( String testEmail )
    {
        this.testEmail = testEmail;
    } //-- void setTestEmail( String )

    /**
     * Set the tlsEnabled field.
     * 
     * @param tlsEnabled
     */
    public void setTlsEnabled( boolean tlsEnabled )
    {
        this.tlsEnabled = tlsEnabled;
    } //-- void setTlsEnabled( boolean )

    /**
     * Set the username field.
     * 
     * @param username
     */
    public void setUsername( String username )
    {
        this.username = username;
    } //-- void setUsername( String )


}
