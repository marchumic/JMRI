package jmri.jmrix.loconet.duplexgroup.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of DuplexGroupInfoPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class DuplexGroupInfoPanelTest {

    @Test
    public void testCtor() {
        DuplexGroupInfoPanel action = new DuplexGroupInfoPanel();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
