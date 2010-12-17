// NceConsistEngines.java

package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Routine to synchronize operation's engines with NCE consist memory.
 * 
 * NCE Consists are stored in Command Station (CS) memory starting at address
 * xF500 and ending xFAFF. NCE supports up to 127 consists, numbered 1 to 127.
 * They track the lead loco, rear loco, and four mid locos in the consist file.
 * Consist lead locos are stored in memory locations xF500 through xF5FF.
 * Consist rear locos are stored in memory locations xF600 through xF6FF. Mid
 * consist locos (four max) are stored in memory locations xF700 through xFAFF.
 * If a long address is in use, bits 6 and 7 of the high byte are set. Example:
 * Long address 3 = 0xc0 0x03 Short address 3 = 0x00 0x03
 * 
 * NCE file format:
 * 
 * :F500 (con 0 lead loco) (con 1 lead loco) ....... (con 7 lead loco) :F510
 * (con 8 lead loco) ........ (con 15 lead loco) . . :F5F0 (con 120 lead loco)
 * ..... (con 127 lead loco)
 * 
 * :F600 (con 0 rear loco) (con 1 rear loco) ....... (con 7 rear loco) . . :F6F0
 * (con 120 rear loco) ..... (con 127 rear loco)
 * 
 * :F700 (con 0 mid loco1) (con 0 mid loco2) (con 0 mid loco3) (con 0 mid loco4) . .
 * :FAF0 (con 126 mid loco1) .. (con 126 mid loco4)(con 127 mid loco1) .. (con
 * 127 mid loco4) :0000
 * 
 * @author Dan Boudreau Copyright (C)2008
 * @version $Revision: 1.10.2.2 $
 */


public class NceConsistEngines extends Thread implements jmri.jmrix.nce.NceListener {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	private boolean syncOK = true;		// used to flag status messages
	EngineManager engineManager = EngineManager.instance();
	List<String> engineList;
	List<String> consists;
	
	javax.swing.JLabel textConsist = new javax.swing.JLabel();
	javax.swing.JLabel indexNumber = new javax.swing.JLabel();
	
	private static final int CS_CONSIST_MEM = 0xF500; 	// start of NCE CS Consist memory
	private static final int CS_CON_MEM_REAR = 0x100; 	// array offset rear consist locos
	private static final int CS_CON_MEM_MID = 0x200; 	// array offset mid consist locos
	
	private static final int REPLY_16 = 16;			// reply length of 16 byte expected
	private int replyLen = 0;				// expected byte length
	private int waiting = 0;					// to catch responses not intended for this module
	private int index = 0;					// byte index when reading NCE consist memory
	private static final int CONSIST_LNTH = 128*6*2;		// 128 consists x 6 engines per consists x 2 bytes
	private static final int NUM_CONSIST_READS = CONSIST_LNTH/REPLY_16;	// read 16 bytes each time from NCE memory
	
	private static final String NCE = "nce_";
	
	private static byte[] nceConsistData = new byte [CONSIST_LNTH];
	
	NceTrafficController tc;
	
	public NceConsistEngines(NceTrafficController tc){
		super();
		this.tc = tc;
	}
	

	// we use a thread so the status frame will work!
	public void run() {
		if (tc == null){
			JOptionPane.showMessageDialog(null, "Synchronization failed! No connection!", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(null,
				"Synchronize with NCE command station consists?", "NCE Consist",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}
		// reset
		index = 0;
		waiting = 0;
		syncOK = true;
		
		// create a status frame
	   	JPanel ps = new JPanel();
	   	jmri.util.JmriJFrame fstatus = new jmri.util.JmriJFrame("Reading NCE consist memory");
	   	fstatus.setLocationRelativeTo(null);
	   	fstatus.setSize (300,100);

	   	ps.add (textConsist);
	   	ps.add(indexNumber);
	   	fstatus.getContentPane().add (ps);
		textConsist.setText("Read number: ");
        textConsist.setVisible(true);
        indexNumber.setVisible(true);
		fstatus.setVisible (true);
		
		// now copy NCE memory into array
		for (int readIndex = 0; readIndex < NUM_CONSIST_READS; readIndex++) {
			
			indexNumber.setText(Integer.toString(readIndex));
			fstatus.setVisible (true);

			getNceConsist(readIndex);
			
			if (!syncOK)
				break;
		}
		// kill status panel
		fstatus.dispose();
		
		if (syncOK){
			// now check each engine in the operations to see if there are any matches
			engineList = engineManager.getByNumberList();
			consists = new ArrayList<String>();

			// look for lead engines
			for (int consistNum=1; consistNum<128; consistNum++){
				engineManager.deleteConsist(NCE+consistNum);
				int engNum = getEngineNumberFromArray(consistNum, 0, 2);
				if (engNum != 0){
					log.debug("NCE consist "+consistNum+" has lead engine "+engNum);
					boolean engMatch = false;
					for (int indexEng=0; indexEng<engineList.size(); indexEng++){
						Engine engine = engineManager.getById(engineList.get(indexEng));
						if (engine.getNumber().equals(Integer.toString(engNum))){
							log.debug("found lead engine match "+engine.getNumber());
							Consist engConsist = engineManager.newConsist(NCE+consistNum);
							engConsist.setConsistNumber(consistNum);	// load the consist number
							engine.setConsist(engConsist);
							engMatch = true;
							consists.add(Integer.toString(consistNum));
							break;
						}
					}
					if (!engMatch){
						log.info("Lead engine " +engNum+ " not found in operations for NCE consist "+consistNum);
					}
				}
			}
			// look for rear engines
			syncEngines(CS_CON_MEM_REAR, 2);
			// look for mid engines
			syncEngines(CS_CON_MEM_MID, 8);
			syncEngines(CS_CON_MEM_MID+2, 8);
			syncEngines(CS_CON_MEM_MID+4, 8);
			syncEngines(CS_CON_MEM_MID+6, 8);
		}
		
		if (syncOK) {
			JOptionPane.showMessageDialog(null, "Successful synchronization!",
					"NCE Consist", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Synchronization failed!", "NCE Consist",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void syncEngines(int offset, int step){
		for (int consistNum=1; consistNum<128; consistNum++){
			int engNum = getEngineNumberFromArray(consistNum, offset, step);
			if (engNum != 0){
				log.debug("NCE consist "+consistNum+" has engine "+engNum);
				boolean engMatch = false;
				for (int indexEng=0; indexEng<engineList.size(); indexEng++){
					Engine engine = engineManager.getById(engineList.get(indexEng));
					if (engine.getNumber().equals(Integer.toString(engNum))){
						log.debug("found engine match "+engine.getNumber());
						engMatch = true;
						Consist engConsist = engineManager.getConsistByName(NCE+consistNum);
						if (engConsist != null){
							engine.setConsist(engConsist);
							break;
						}
						log.warn("Engine "+ engNum +" needs lead engine "+getEngineNumberFromArray(consistNum, 0, 2)+" for consist "+consistNum);
						JOptionPane.showMessageDialog(null, "Engine "+ engNum +" needs lead engine "+getEngineNumberFromArray(consistNum, 0, 2)+" for consist "+consistNum, "NCE Consist",
								JOptionPane.ERROR_MESSAGE);
						syncOK = false;
					}
				}
				if (!engMatch){
					log.warn("Engine " +engNum+ " not found in operations for NCE consist "+consistNum);
					if (consists.contains(Integer.toString(consistNum))){
						JOptionPane.showMessageDialog(null, "Missing engine number "+engNum+" for NCE consist "+consistNum, "NCE Consist",
								JOptionPane.ERROR_MESSAGE);
						syncOK = false;
					}
				}
			}
		}
	}
	
	private int getEngineNumberFromArray(int consistNumber, int offset, int step){
		int engH = ((nceConsistData[consistNumber*step +offset] << 8) & 0x3FFF);
		int engL = nceConsistData[(consistNumber*step)+offset+1] & 0xFF;
		return engH + engL;
	}
	
	// Read 16 bytes of NCE CS memory
	private void getNceConsist(int cR) {

		NceMessage m = readConsistMemory(cR);
		tc.sendNceMessage(m, this);
		// wait for read to complete 
		readWait();
	}
	
	// wait up to 10 sec per read
	private boolean readWait() {
		int waitcount = 10;
		while (waiting > 0) {
			synchronized (this) {
				try {
					wait(1000);
				} catch (InterruptedException e) {
				    Thread.currentThread().interrupt(); // retain if needed later
				}
			}
			if (waitcount-- < 0) {
				log.error("read timeout");
				syncOK = false; // need to quit
				return false;
			}
		}
		return true;
	}

	// Reads 16 bytes of NCE consist memory
	private NceMessage readConsistMemory(int num) {

		int nceConsistAddr = (num * REPLY_16) + CS_CONSIST_MEM;
		replyLen = REPLY_16; 			// Expect 16 byte response
		waiting++;

		byte[] bl = NceBinaryCommand.accMemoryRead(nceConsistAddr);
		NceMessage m = NceMessage.createBinaryMessage(tc, bl, REPLY_16);
		return m;
	}
	
	public void message(NceMessage m) {
	} // ignore replies
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NN_NAKED_NOTIFY")
	public void reply(NceReply r) {

		if (waiting <= 0) {
			log.error("unexpected response");
			return;
		}
		if (r.getNumDataElements() != replyLen) {
			log.error("reply length incorrect");
			return;
		}

		// load data buffer
		for (int i = 0; i < REPLY_16; i++) {
			nceConsistData[index++] = (byte) r.getElement(i);
		}
		waiting--;
		
		// wake up thread
		synchronized (this) {
			notify();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(NceConsistEngines.class.getName());
}

