package jmri.jmrix.nce.consist;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.nce.NceTrafficControlScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NceConsistBackupTest {

    private NceTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        NceConsistBackup t = new NceConsistBackup(tcis);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tcis = new NceTrafficControlScaffold();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(NceConsistBackupTest.class.getName());

}
