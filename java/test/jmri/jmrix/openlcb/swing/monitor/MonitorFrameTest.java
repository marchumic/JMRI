package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class MonitorFrameTest {

    private String testFormatted;
    private String testRaw;

    private TrafficControllerScaffold tcs = null; 
    private CanSystemConnectionMemo memo = null;

    @Test
    public void testFormatMsg() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        MonitorPane f = new MonitorPane() {

            @Override
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        f.initComponents(memo);

        jmri.jmrix.can.CanMessage msg
                = new jmri.jmrix.can.CanMessage(
                        new int[]{1, 2}, 0x12345678);
        msg.setExtended(true);

        f.message(msg);

        Assert.assertEquals("formatted", "S: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        f.dispose();
        
        // accept WARN message due to defect in code as written - see #3091
        jmri.util.JUnitAppender.assertWarnMessage("No User Preferences Manager, not saving format"); 
    }

    @Test
    public void testFormatReply() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        MonitorPane f = new MonitorPane() {
            @Override
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        f.initComponents(memo);

        jmri.jmrix.can.CanReply msg
                = new jmri.jmrix.can.CanReply(
                        new int[]{1, 2});
        msg.setExtended(true);
        msg.setHeader(0x12345678);

        f.reply(msg);

        Assert.assertEquals("formatted", "R: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        f.dispose();
        
        // accept WARN message due to defect in code as written - see #3091
        jmri.util.JUnitAppender.assertWarnMessage("No User Preferences Manager, not saving format"); 
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tcs = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
    }

    @After
    public void tearDown() {
        memo.dispose();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
