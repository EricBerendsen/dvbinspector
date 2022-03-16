package nl.digitalekabeltelevisie.main;

import nl.digitalekabeltelevisie.util.CLI.DVBinspectorCLI;

public class DVBinspectorCLIMain {

	/**
	 * Run an instance of DVB Inspector CLI
	 * @param args - Given arguments
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		DVBinspectorCLI dvb = new DVBinspectorCLI(args);
		dvb.run();
		
		System.exit(1);
	}
}