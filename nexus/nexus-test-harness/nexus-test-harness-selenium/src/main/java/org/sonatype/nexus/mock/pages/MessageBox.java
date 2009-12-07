package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class MessageBox
    extends Window
{

    public MessageBox( Selenium selenium )
    {
        super( selenium, "window.Sonatype.MessageBox.getDialog()" );
    }

    public MessageBox clickYes()
    {
        selenium.click( "Yes" );

        return this;
    }

    public MessageBox clickNo()
    {
        selenium.click( "No" );

        return this;
    }

    public MessageBox clickOk()
    {
        selenium.click( "OK" );

        return this;
    }

    public String getTitle()
    {
        waitForVisible();

        return getEval( ".title" );
    }

}
