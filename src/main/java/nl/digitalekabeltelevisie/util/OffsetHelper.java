package nl.digitalekabeltelevisie.util;

public class OffsetHelper {

	private long[] packet_offset;
	private int maxPackets = -1;
	private int packetLength = 0;

	public OffsetHelper(int max_packets, int packetLength) {
		this.maxPackets = max_packets;
		this.packetLength = packetLength;
	}

	public void setEnabled(boolean enableTSPackets) {
		if(enableTSPackets &&(maxPackets!=-1)){
			packet_offset = new long[maxPackets];
		}else{
			packet_offset = null;
		}
			
		
	}

	public boolean isEnabled() {
		return packet_offset != null;
	}

	public void addPacket(int packetNo, long offset) {
		packet_offset[packetNo]=offset;
		
	}

	public int getMaxPacket() {
		return packet_offset.length;
	}

	public long getOffset(int packetNo) {
		return packet_offset[packetNo];
	}

	public void setMaxPackets(int maxPackets) {
		this.maxPackets = maxPackets;
		
	}
	

}
