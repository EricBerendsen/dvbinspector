package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;



// based on TS 101 154 V2.2.1 (2015-06) 6.6.7 AC-4 Sync Frame Format
// NOTE in newer versions this is void and refers to ETSI TS 103 190-2 [46], annex C. 
// However annex C of TS 103 190-2 V1.2.1 (2018-02) is Void, so use TS 103 190-2 V1.1.1 (2015-09)

public class AC4SyncFrame implements TreeNode {

	private int sync_word;
	private int frame_size;
	private int frame_size2;
	RawAC4Frame raw_ac4_frame;
	private int crc_word;
	
	/**
	 * @param data
	 * @param offset
	 */
	public AC4SyncFrame(byte[] data, int offset) {
		int offset1 = offset;
		sync_word = Utils.getInt(data, offset1, 2, Utils.MASK_16BITS);
		offset1 +=2;
		frame_size  = Utils.getInt(data, offset1, 2, Utils.MASK_16BITS);
		offset1 +=2;
		if(frame_size==0xFFFF) {
			frame_size2  = Utils.getInt(data, offset1, 3, Utils.MASK_24BITS);
			offset1 +=3;
		}
		raw_ac4_frame = new RawAC4Frame(data, offset1);
		offset1 += getAC4FrameSize();
		if (sync_word == 0xAC41) {
			crc_word = Utils.getInt(data, offset1, 2, Utils.MASK_16BITS);
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("AC4SyncFrame");
		t.add(new KVP("sync_word", sync_word, getSyncWordString(sync_word)));
		t.add(new KVP("frame_size", frame_size));
		if (frame_size == 0xFFFF) {
			t.add(new KVP("frame_size2", frame_size2));
		}
		t.add(raw_ac4_frame.getJTreeNode(modus));
		if (sync_word == 0xAC41) {
			t.add(new KVP("crc_word", crc_word));
		}

		return t;
	}

	/**
	 * @param sync_word
	 * @return
	 */
	private static String getSyncWordString(int sync_word) {
		if(sync_word == 0xAC41){
			return "CRC word is present";
		}
		if(sync_word == 0xAC40 ){
			return "CRC word is not present";
		}
		return "SyncWord not legal (should be 0xAC41 or 0xAC40)";
	}

	/**
	 * @return
	 */
	public int getAC4FrameSize() {
		if(frame_size==0xFFFF) {
			return frame_size2;
		}
		return frame_size;
	}
	
	public int getSyncFrameSize() {
		int s = getAC4FrameSize() 
				+ 2 // syncWord
				+ 2; // frame_size
		if(frame_size==0xFFFF) {
			s += 3; //  frame_size2
		}
		if (sync_word == 0xAC41) {
			s += 2; // crc_word
		}
		return s;
	}

	public int getSync_word() {
		return sync_word;
	}

	public int getFrame_size() {
		return frame_size;
	}

	public int getFrame_size2() {
		return frame_size2;
	}

	public RawAC4Frame getRaw_ac4_frame() {
		return raw_ac4_frame;
	}

	public int getCrc_word() {
		return crc_word;
	}

}
