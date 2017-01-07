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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.*;


/**
 * based on ANSI/SCTE 35 2014
 * 
 * http://www.scte.org/documents/pdf/Standards/ANSI_SCTE%2035%202014.pdf
 * 
 * @author Eric
 *
 */
public class SpliceInfoSection extends TableSection {
	
	private class SpliceTime implements TreeNode{
		
		private int time_specified_flag;
		private long pts_time;
		
		private SpliceTime(BitSource bs){
			time_specified_flag = bs.readBits(1);
			if(time_specified_flag==1){
				bs.readBits(1);
				pts_time = bs.readBitsLong(33);
			}else{
				bs.readBits(1);
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("splice_time"));
			t.add(new DefaultMutableTreeNode(new KVP("time_specified_flag",time_specified_flag,null)));
			if(time_specified_flag==1){
				t.add(new DefaultMutableTreeNode(new KVP("pts_time",pts_time,null)));
			}


			return t;
		}
	}

	private int protocol_version;
	private int encrypted_packet;
	private int encryption_algorithm;
	private long pts_adjustment;
	private int cw_index;
	private int tier;
	private int splice_command_length;
	private int splice_command_type;
	private long splice_event_id;
	private int splice_event_cancel_indicator;
	private int out_of_network_indicator;
	private int program_splice_flag;
	private int duration_flag;
	private int splice_immediate_flag;
	
	
	List<Descriptor> splice_descriptors = new ArrayList<>();
	private int unique_program_id;
	private int avail_num;
	private int avails_expected;
	private int descriptor_loop_length;

	public SpliceInfoSection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		final byte[] data = raw_data.getData();
		protocol_version = Utils.getInt(data,3,1,Utils.MASK_8BITS);
		encrypted_packet = Utils.getInt(data,4,1,0x80)>>7;
		encryption_algorithm = Utils.getInt(data,4,1,0x7E)>>1;
		
		pts_adjustment = getLong(data,4, 5, MASK_33BITS);
		cw_index = Utils.getInt(data,9,1,Utils.MASK_8BITS);
		tier = Utils.getInt(data,10,2,0xFFF0)>>4;
		splice_command_length = Utils.getInt(data,11,2,Utils.MASK_12BITS);
		if(encrypted_packet==0){
			splice_command_type= Utils.getInt(data,13,1,Utils.MASK_8BITS);
			if(splice_command_type==5){
				splice_event_id = getLong(data, 14, 4, MASK_32BITS);
				splice_event_cancel_indicator = Utils.getInt(data,18,1,0x80)>>7;
				if(splice_event_cancel_indicator==0){
					out_of_network_indicator = Utils.getInt(data,19,1,0x80)>>7;
					program_splice_flag = Utils.getInt(data,19,1,0x40)>>6;
					duration_flag = Utils.getInt(data,19,1,0x20)>>5;
					splice_immediate_flag = Utils.getInt(data,19,1,0x10)>>4;
					
					BitSource bs = new BitSource(data, 20);
					if((program_splice_flag == 1) && (splice_immediate_flag == 0)){
						//splice_time
					}
					if(program_splice_flag == 0) {
	//					component_count = Utils.getInt(data,20,1,Utils.MASK_8BITS);
	//					int offset = 20;
	//					for(int i=0;i<component_count;i++) {
	//					component_tag 8 uimsbf
	//					 if(splice_immediate_flag == ‘0’)
	//					 splice_time()
	//					}
					}
					if(duration_flag == 1){
						//break_duration()
					}
					unique_program_id = bs.readBits(16);
					avail_num = bs.readBits(8);
					avails_expected = bs.readBits(8);
					
					
				}
				
			}
			
			int loopstart = 14 + splice_command_length;
			
			descriptor_loop_length = Utils.getInt(data,loopstart,2,Utils.MASK_12BITS);
			splice_descriptors = DescriptorFactory.buildDescriptorList(data, loopstart+2, descriptor_loop_length, this);
		
		}
	}


	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("SpliceInfoSection");
		//b.append(Utils.toHexString(UTC_time)).append(", UTC_timeString=").append(getUTC_timeString()).append(", length=").append(getSectionLength());
		return b.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("protocol_version",protocol_version,null)));
		t.add(new DefaultMutableTreeNode(new KVP("encrypted_packet",encrypted_packet,encrypted_packet==1?"portions of the splice_info_section, starting with splice_command_type and ending with and including E_CRC_32, are encrypted":"no part of this message is encrypted")));
		t.add(new DefaultMutableTreeNode(new KVP("encryption_algorithm",encryption_algorithm,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pts_adjustment",pts_adjustment,null)));
		t.add(new DefaultMutableTreeNode(new KVP("cw_index",cw_index,null)));
		t.add(new DefaultMutableTreeNode(new KVP("tier",tier,null)));
		t.add(new DefaultMutableTreeNode(new KVP("splice_command_length",splice_command_length,null)));
		if(encrypted_packet==0){
			t.add(new DefaultMutableTreeNode(new KVP("splice_command_type",splice_command_type,getSpliceCommandTypeString(splice_command_type))));
			if(splice_command_type==5){
				t.add(new DefaultMutableTreeNode(new KVP("splice_event_id",splice_event_id,null)));
				t.add(new DefaultMutableTreeNode(new KVP("splice_event_cancel_indicator",splice_event_cancel_indicator,splice_event_cancel_indicator==1?"a previously sent splice event, identified by splice_event_id, has been cancelled":null)));
				if(splice_event_cancel_indicator==0){
					t.add(new DefaultMutableTreeNode(new KVP("out_of_network_indicator",out_of_network_indicator,out_of_network_indicator==1?"the splice event is an opportunity to exit from the network feed":"the splice event is an opportunity to return to the network feed")));
					t.add(new DefaultMutableTreeNode(new KVP("program_splice_flag",program_splice_flag,program_splice_flag==1?"the message refers to a Program Splice Point":"each component that is intended to be spliced will be listed separately")));
					t.add(new DefaultMutableTreeNode(new KVP("duration_flag",duration_flag,duration_flag==1?"break_duration() field present":"break_duration() field not present")));
					t.add(new DefaultMutableTreeNode(new KVP("splice_immediate_flag",splice_immediate_flag,splice_immediate_flag==1?"splice mode shall be the Splice Immediate Mode":"splice_time() field present")));
					if((program_splice_flag == 1) && (splice_immediate_flag == 0)){
						t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("splice_time()")));
						return t;
					}else if(program_splice_flag == 0) {
						t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("component_count")));
						return t;
					}else if(duration_flag == 1){
						t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("break_duration")));
						return t;
					}else{
						t.add(new DefaultMutableTreeNode(new KVP("unique_program_id",unique_program_id,null)));
						t.add(new DefaultMutableTreeNode(new KVP("avail_num",avail_num,null)));
						t.add(new DefaultMutableTreeNode(new KVP("avails_expected",avails_expected,null)));
						
					}
				}
			}else{
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("splice_command_type ="+splice_command_type+" ("+ getSpliceCommandTypeString(splice_command_type)+")")));
			}
			t.add(new DefaultMutableTreeNode(new KVP("descriptor_loop_length",descriptor_loop_length,null)));
			Utils.addListJTree(t,splice_descriptors,modus,"splice_descriptors");
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("this portion of the splice_info_section, starting with splice_command_type and ending with and including E_CRC_32, is encrypted")));
		}
		
		
		return t;
	}

	private static String getSpliceCommandTypeString(int splice_command_type){
		switch (splice_command_type) {
		case 0:
			return "splice_null";
		case 4:
			return "splice_schedule";
		case 5:
			return "splice_insert";
		case 6:
			return "time_signal";
		case 7:
			return "bandwidth_reservation";
		case 255:
			return "private_command";

		default:
			return "Reserved";
		}
	}

}
