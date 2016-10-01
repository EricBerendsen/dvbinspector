package nl.digitalekabeltelevisie.util;

public class OffsetHelper {

	private RangeHashMap<Integer, Long> rangeHashMap = new RangeHashMap<>();
	private int maxPackets = -1;
	private int packetLength = 0;
	private boolean enabled;
	
	RangeHashMap<Integer, Long>.Entry currentEntry = null;

	public OffsetHelper(int max_packets, int packetLength) {
		this.maxPackets = max_packets;
		this.packetLength = packetLength;
	}

	public void setEnabled(boolean enableTSPackets) {
		this.enabled = enableTSPackets;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void addPacket(int packetNo, long offset) {
		if(currentEntry == null){
			currentEntry = rangeHashMap.new Entry(0,maxPackets, offset); 
			rangeHashMap.put(0, currentEntry);
		}else if(calculateOffset(packetNo, currentEntry) != offset){
			currentEntry.setUpper(packetNo - 1);
			currentEntry = rangeHashMap.new Entry(packetNo, maxPackets, offset);
			rangeHashMap.put(packetNo, currentEntry);
		}
		
		
	}

	public int getMaxPacket() {
		return maxPackets;
	}

	public long getOffset(int packetNo) {
		RangeHashMap<Integer, Long>.Entry entry = rangeHashMap.findEntry(packetNo);
		return calculateOffset(packetNo, entry); 
	}

	private long calculateOffset(int packetNo, RangeHashMap<Integer, Long>.Entry entry) {
		return entry.getValue() + ((long)(packetNo - entry.getLower()) * packetLength);
	}

	

}
