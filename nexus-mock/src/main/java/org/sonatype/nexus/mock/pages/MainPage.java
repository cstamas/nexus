package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;

public class MainPage {
    private Selenium selenium;

    public MainPage(Selenium selenium) {
        this.selenium = selenium;
        selenium.open("/nexus");
    }

    public boolean loginLinkAvailable() {
        String text = selenium.getText("login-link");

        return "Log In".equals(text);
    }

    public LoginWindow clickLogin() {
        if (loginLinkAvailable()) {
            selenium.click("login-link");
        } else {
            throw new RuntimeException("Login link not found!");
        }

        return new LoginWindow(selenium, this);
    }

    public ViewsPanel viewsPanel() {
        return new ViewsPanel(selenium);
    }

    public AdministrationPanel adminPanel() {
        return new AdministrationPanel(selenium);
    }

    public SecurityPanel securityPanel() {
        return new SecurityPanel(selenium);
    }
}
