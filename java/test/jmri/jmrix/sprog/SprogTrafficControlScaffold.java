/**
 * SprogInterfaceScaffold.java
 *
 * Description:	Stands in for the SprogTrafficController class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.sprog;

import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SprogTrafficControlScaffold extends SprogTrafficController {

    public SprogTrafficControlScaffold(SprogSystemConnectionMemo memo) {
       super(memo);
    }

    // override some SprogTrafficController methods for test purposes
    @Override
    public boolean status() {
        return true;
    }

    /**
     * record messages sent, provide access for making sure they are OK
     */
    public Vector<SprogMessage> outbound = new Vector<SprogMessage>();  // public OK here, so long as this is a test class

    @Override
    public void sendSprogMessage(SprogMessage m) {
        if (log.isDebugEnabled()) {
            log.debug("sendSprogMessage [" + m + "]");
        }
        // save a copy
        outbound.addElement(m);
        // we don't return an echo so that the processing before the echo can be
        // separately tested
    }

    // test control member functions
    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestMessage(SprogMessage m, SprogListener l) {
        // forward a test message to NceListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestMessage    [" + m + "]");
        }
        notifyMessage(m, l);
        return;
    }

    /**
     * forward a message to the listeners, e.g. test receipt
     */
    protected void sendTestReply(SprogReply m) {
        // forward a test message to NceListeners
        if (log.isDebugEnabled()) {
            log.debug("sendTestReply [" + m + "]");
        }
        notifyReply(m);
        return;
    }

    /*
     * Check number of listeners, used for testing dispose()
     */
    public int numListeners() {
        return cmdListeners.size();
    }

    private final static Logger log = LoggerFactory.getLogger(SprogTrafficControlScaffold.class.getName());

}
