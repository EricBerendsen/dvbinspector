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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AVCTimingAndHRDDescriptor extends Descriptor {

	private final int hrd_management_valid_flag;
	private final int reserved1;
	private final int picture_and_timing_info_present;

	private int _90kHz_flag;
	private int reserved2;
	private long n;
	private long k;
	private long num_units_in_tick;
	private final int fixed_frame_rate_flag;
	private final int temporal_poc_flag;
	private final int picture_to_display_conversion_flag;
	private final int reserved3;

	public AVCTimingAndHRDDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		hrd_management_valid_flag = getInt(b, offset+2, 1, 0x80)>>7;
		reserved1 = getInt(b, offset+2, 1, 0x7e)>>1;
		picture_and_timing_info_present = getInt(b, offset+2, 1, MASK_1BIT);
		int t=0;
		if(picture_and_timing_info_present==1){
			_90kHz_flag = getInt(b, offset+3, 1, 0x80)>>7;
			reserved2 = getInt(b, offset+3, 1, MASK_7BITS);
			t+=1;
			if(_90kHz_flag==0){
				n = getLong(b, offset+4, 4, MASK_32BITS);
				k = getLong(b, offset+8, 4, MASK_32BITS);
				t+=8;
			}
			num_units_in_tick = getLong(b, offset+3+t, 4, MASK_32BITS);
			t+=4;

		}
		fixed_frame_rate_flag = getInt(b, offset+3+t, 1, 0x80)>>7;
		temporal_poc_flag = getInt(b, offset+3+t, 1, 0x40)>>6;
		picture_to_display_conversion_flag = getInt(b, offset+3+t, 1, 0x20)>>5;
		reserved3 = getInt(b, offset+3+t, 1, MASK_5BITS);
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("hrd_management_valid_flag",hrd_management_valid_flag,hrd_management_valid_flag==1?"SEI buffering period shall be contained in the AVC":"alternative method shall be used for transfer from MBn to EBn")));
		t.add(new DefaultMutableTreeNode(new KVP("reserved1",reserved1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("picture_and_timing_info_present",picture_and_timing_info_present,null)));
		if(picture_and_timing_info_present==1){
			t.add(new DefaultMutableTreeNode(new KVP("90kHz_flag",_90kHz_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved2",reserved2,null)));
			if(_90kHz_flag==0){
				t.add(new DefaultMutableTreeNode(new KVP("n",n,null)));
				t.add(new DefaultMutableTreeNode(new KVP("k",k,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("num_units_in_tick",num_units_in_tick,null)));

		}
		t.add(new DefaultMutableTreeNode(new KVP("fixed_frame_rate_flag",fixed_frame_rate_flag,fixed_frame_rate_flag==1?"coded frame rate shall be constant within AVC video elementary stream":"no information about AVC video stream frame rate in the descriptor")));
		t.add(new DefaultMutableTreeNode(new KVP("temporal_poc_flag",temporal_poc_flag,temporal_poc_flag==1?"AVC video stream shall transmit picture order count (POC) information":"information about relationship between POC information of AVC video stream and the time shall not be transmitted")));
		t.add(new DefaultMutableTreeNode(new KVP("picture_to_display_conversion_flag",picture_to_display_conversion_flag,picture_to_display_conversion_flag==1?"AVC video stream shall transmit information about displaying coded pictures":"pic_struct_present_flag VUI parameter of AVC video stream shall have the value of 0")));
		t.add(new DefaultMutableTreeNode(new KVP("reserved3",reserved3,null)));
		return t;
	}
}
