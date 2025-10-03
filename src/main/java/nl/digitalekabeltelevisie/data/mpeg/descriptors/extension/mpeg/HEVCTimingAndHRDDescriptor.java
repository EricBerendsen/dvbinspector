/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg;

import static nl.digitalekabeltelevisie.util.Utils.MASK_1BIT;
import static nl.digitalekabeltelevisie.util.Utils.MASK_32BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_7BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;


import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

// based on 2.6.97 HEVC timing and HRD descriptor Rec. ITU-T H.222.0 (06/2021)

public class HEVCTimingAndHRDDescriptor extends MPEGExtensionDescriptor {

	private final int hrd_management_valid_flag;
	private final int target_schedule_idx;
	private final int picture_and_timing_info_present;

	private int _90kHz_flag;
	private int reserved2;
	private long n;
	private long k;
	private long num_units_in_tick;
	private int target_schedule_idx_not_present_flag;
	
	public HEVCTimingAndHRDDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		hrd_management_valid_flag = getInt(b, 3, 1, 0b1000_0000)>>>7;
		target_schedule_idx_not_present_flag  = getInt(b, 3, 1, 0b0100_0000)>>>6;
		target_schedule_idx = getInt(b, 3, 1, 0b0011_1110)>>>1;
		picture_and_timing_info_present = getInt(b, 3, 1, MASK_1BIT);
		int t=0;
		if(picture_and_timing_info_present==1){
			_90kHz_flag = getInt(b, 4, 1, 0x80)>>>7;
			reserved2 = getInt(b, 4, 1, MASK_7BITS);
			t+=1;
			if(_90kHz_flag==0){
				n = getLong(b, 5, 4, MASK_32BITS);
				k = getLong(b, 9, 4, MASK_32BITS);
				t+=8;
			}
			num_units_in_tick = getLong(b, 4+t, 4, MASK_32BITS);
		}
	}

	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("hrd_management_valid_flag", hrd_management_valid_flag).setDescription(getHrdManagmentDescription()));
		t.add(new KVP("target_schedule_idx_not_present_flag", target_schedule_idx_not_present_flag));
		t.add(new KVP("target_schedule_idx", target_schedule_idx));
		t.add(new KVP("picture_and_timing_info_present", picture_and_timing_info_present));
		if (picture_and_timing_info_present == 1) {
			t.add(new KVP("90kHz_flag", _90kHz_flag));
			t.add(new KVP("reserved2", reserved2));
			if (_90kHz_flag == 0) {
				t.add(new KVP("n", n));
				t.add(new KVP("k", k));
			}
			t.add(new KVP("num_units_in_tick", num_units_in_tick));

		}
		return t;
	}

	private String getHrdManagmentDescription() {
		return hrd_management_valid_flag == 1
				? "Buffering Period SEI and Picture Timing SEI messages shall be present in the associated HEVC video stream"
				: "leak method shall be used for the transfer from MBn to EBn";
	}
}
