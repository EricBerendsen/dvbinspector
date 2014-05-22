package nl.digitalekabeltelevisie.test;

import nl.digitalekabeltelevisie.data.mpeg.PCR;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

/**
 * Sample program to extract and print the EIT information
 * @author Eric
 *
 */
public class ExtractDebugInfoVOD {

	private static PCR lastPcr;

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		TransportStream transportStream = null;;
		try {
			transportStream = new TransportStream("d:\\ts\\Ziggo Oost 393000 08-31 11-17-00.ts");
			transportStream.setEnableTSPackets(true);
		   // parse general (P)SI information
		   transportStream.parseStream();
		} catch (final Exception e) {
		   e.printStackTrace();
		}

		int packets = transportStream.getNo_packets();
		System.out.println("packets:"+packets);

		int pcrPid = 65;
		int infoPid= 67;
		StringBuilder r = new StringBuilder();

		for (int i = 0; i < packets; i++) {
			TSPacket packet = transportStream.getTSPacket(i);
			if(packet.getPID()==pcrPid){
//				if((r.length()>0)&&(!r.startsWith("{\"type\":\"dirty\",\"dirty\""))){
//
//					System.out.println(Utils.printPCRTime(lastPcr.getProgram_clock_reference())+": " +r);
//				}
//				if((r.length()>0)){
//
//					System.out.print(r);
//				}
//				r = new StringBuilder();
				lastPcr = packet.getAdaptationField().getProgram_clock_reference();
			}else if(packet.getPID()==infoPid){
				byte [] b = packet.getData();
				int j = 0;
				while ((j<b.length)&&(b[j]!=0)){
					r.append((char)b[j]);
					j++;

				}
				if((j<b.length)&&(b[j]==0)){
					System.out.println(r);
					r = new StringBuilder();
				}


			}
		}





	}

}
