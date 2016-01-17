/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class TimeSliceFecIdentifierDescriptor extends Descriptor {

	private final int time_slicing;
	private final int mpe_fec;
	private final int frame_size;
	private final int max_burst_duration;
	private final int max_average_rate;

	private final int time_slice_fec_id;
	private final byte[] id_selector_byte;



	public TimeSliceFecIdentifierDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		time_slicing = Utils.getInt(b, offset+2, 1, 0x80)>>7;
		mpe_fec = Utils.getInt(b, offset+2, 1, 0x60)>>5;
		frame_size = Utils.getInt(b, offset+2, 1, Utils.MASK_3BITS);
		max_burst_duration = Utils.getInt(b, offset+3, 1, Utils.MASK_8BITS);
		max_average_rate = Utils.getInt(b, offset+4, 1, 0xF0)>>4;
		time_slice_fec_id = Utils.getInt(b, offset+4, 1, Utils.MASK_4BITS);
		id_selector_byte = Utils.copyOfRange(b, offset+5, offset+descriptorLength+2);


	}

	@Override
	public String toString() {
		return super.toString() + " time_slicing"+time_slicing ;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("time_slicing",time_slicing ,getTimeSlicingString(time_slicing))));
		t.add(new DefaultMutableTreeNode(new KVP("mpe_fec",mpe_fec ,getMPE_FECtring(mpe_fec))));
		t.add(new DefaultMutableTreeNode(new KVP("frame_size",frame_size ,getFrameSizeString(frame_size,time_slicing,mpe_fec,time_slice_fec_id))));
		t.add(new DefaultMutableTreeNode(new KVP("max_burst_duration",max_burst_duration ,getMaxBurstString(max_burst_duration,time_slicing,time_slice_fec_id))));
		t.add(new DefaultMutableTreeNode(new KVP("max_average_rate",max_average_rate ,getMaxAvgRateString(max_average_rate,time_slice_fec_id))));
		t.add(new DefaultMutableTreeNode(new KVP("time_slice_fec_id",time_slice_fec_id ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("id_selector_byte",id_selector_byte ,null)));

		return t;
	}


	/**
	 * @param max_burst_duration2
	 * @param time_slicing2
	 * @param time_slice_fec_id2
	 * @return
	 */
	private static String getMaxBurstString(final int max_burst_duration2, final int time_slicing2, final int time_slice_fec_id2) {
		if(time_slicing2==0){
			return "reserved for future use";
		}else if(time_slice_fec_id2==0){
			return (20 *(max_burst_duration2 + 1)) +" mSecs";
		}
		return "not defined";
	}

	/**
	 * @param frame_size2
	 * @param time_slicing2
	 * @param mpe_fec2
	 * @param time_slice_fec_id2
	 * @return
	 */
	private static String getFrameSizeString(final int frame_size2, final int time_slicing2, final int mpe_fec2, final int time_slice_fec_id2) {
		String r = "";
		if(time_slice_fec_id2==0){
			if(time_slicing2==1){
				r = "Max Burst Size:";
				switch (frame_size2) {
				case 0: r=r+ "512 kbits "; break;
				case 1: r=r+ "1024 kbits "; break;
				case 2: r=r+ "1536 kbits "; break;
				case 3: r=r+ "2048 kbits "; break;
				default: r=r+ "reserved for future use ";
				}
			}
			if(mpe_fec2==1){
				r= r+"MPE-FEC Frame rows:";
				switch (frame_size2) {
				case 0: r=r+ "256"; break;
				case 1: r=r+ "512"; break;
				case 2: r=r+ "768"; break;
				case 3: r=r+ "1024"; break;
				default: r=r+ "reserved for future use";
				}
			}
		}else{
			r="undefined";
		}

		return r;
	}


	private static String getMaxAvgRateString(final int max_avg_r, final int time_slice_fec_id2) {
		if(time_slice_fec_id2==0){
			switch (max_avg_r) {
			case 0: return "16 kbps";
			case 1: return "32 kbps";
			case 2: return "64 kbps";
			case 3: return "128 kbps";
			case 4: return "256 kbps";
			case 5: return "512 kbps";
			case 6: return "1024 kbps";
			case 7: return "2048 kbps";
			default: return "reserved for future use ";
			}
		}
		return "undefined";
	}


	public static String getTimeSlicingString(final int time_slicing) {
		if(time_slicing==1){
			return "Time Slicing being used";
		}
		return "Time Slicing is not used";
	}



	public static String getMPE_FECtring(final int m) {
		switch (m) {
		case 0: return "MPE-FEC not used";
		case 1: return "MPE-FEC used";
		default: return "reserved for future use";
		}
	}




}
