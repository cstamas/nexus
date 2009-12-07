package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Window
    extends Component
{
    public Window( Selenium selenium )
    {
        super( selenium );
    }

    public Window( Selenium selenium, String expression )
    {
        super( selenium, expression );
    }

    public void close()
    {
        selenium.click( getXPath() + "//div[contains(@class, 'x-tool-close')]" );
    }

    public void waitFor()
    {
        selenium.runScript( "window.Ext.Msg.getDialog()" );

        try
        {
            waitEvalTrue( "window.Ext.Msg.isVisible() == false" );
        }
        catch ( RuntimeException e )
        {
            // ok no problem window is not present, go go go
        }
    }
}
