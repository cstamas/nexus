package org.sonatype.nexus.mock;

import org.junit.Ignore;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Before;
import org.sonatype.nexus.mock.rest.MockHelper;

@Ignore
public abstract class NexusTestCase {
    private static MockNexusEnvironment env;

    @BeforeClass
    public synchronized static void startNexus() throws Exception {
        if (env == null) {
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            System.out.println(System.getProperty("jettyPort"));
            env = new MockNexusEnvironment(12345, "/nexus");
            env.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        env.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
    }

    @Before
    public void mockSetup() {
        MockHelper.clearMocks();
    }

    @After
    public void mockCleanup() {
        MockHelper.checkAssertions();
    }
}
