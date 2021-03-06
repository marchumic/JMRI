package jmri.jmrix.lenz.li100;

import org.junit.After;
import org.junit.Before;

/**
 * <p>
 * Title: LI100XNetPacketizerTest </p>
 * <p>
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class LI100XNetPacketizerTest extends jmri.jmrix.lenz.XNetPacketizerTest {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new LI100XNetPacketizer(new jmri.jmrix.lenz.LenzCommandStation()) {
            @Override
            protected void handleTimeout(jmri.jmrix.AbstractMRMessage msg, jmri.jmrix.AbstractMRListener l) {
            }
        };
    }

    @After
    @Override
    public void tearDown() {
        tc = null;
        apps.tests.Log4JFixture.tearDown();
    }

}
