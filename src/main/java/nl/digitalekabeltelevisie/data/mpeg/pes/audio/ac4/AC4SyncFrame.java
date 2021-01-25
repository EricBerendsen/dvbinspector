package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
*
*  This file is part of DVB Inspector.
*
*  DVB Inspector is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  DVB Inspector is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
*
*  The author requests that he be notified of any application, applet, or
*  other binary that makes use of this code, but that's more out of curiosity
*  than anything and is not required.
*
*/


import javax.swing.tree.DefaultMutableTreeNode;

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
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AC4SyncFrame"));
		t.add(new DefaultMutableTreeNode(new KVP("sync_word",sync_word,getSyncWordString(sync_word))));
		t.add(new DefaultMutableTreeNode(new KVP("frame_size",frame_size,null)));
		if(frame_size==0xFFFF) {
			t.add(new DefaultMutableTreeNode(new KVP("frame_size2",frame_size2,null)));
		}
		t.add(raw_ac4_frame.getJTreeNode(modus));
		if (sync_word == 0xAC41) {
			t.add(new DefaultMutableTreeNode(new KVP("crc_word",crc_word,null)));
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
