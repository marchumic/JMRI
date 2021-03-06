package jmri.jmrix.lenz;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XNetInitializationManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.XNetInitializationManager class
 *
 * @author	Paul Bender
 */
public class XNetInitializationManagerTest extends TestCase {

    public void testCtor() {

// infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetListenerScaffold l = new XNetListenerScaffold();

        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(t);

        XNetInitializationManager m = new XNetInitializationManager(memo) {
            @Override
            protected int getInitTimeout() {
                return 50;   // shorten, because this will fail & delay test
            }
        };
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
        jmri.util.JUnitAppender.assertWarnMessage("Command Station disconnected, or powered down assuming LZ100/LZV100 V3.x");
    }

    // from here down is testing infrastructure
    public XNetInitializationManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetInitializationManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetInitializationManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
