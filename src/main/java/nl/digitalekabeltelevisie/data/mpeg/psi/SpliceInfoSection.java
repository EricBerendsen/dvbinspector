/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;


/**
 * based on ANSI/SCTE 35 2017
 * 
 * http://www.scte.org/SCTEDocs/Standards/SCTE%2035%202017.pdf
 * 
 * @author Eric
 *
 */
public class SpliceInfoSection extends TableSection {
	
	public class BandwidthReservation implements TreeNode {
		
		private BandwidthReservation(BitSource bs){
			super();
		}


		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			return new DefaultMutableTreeNode(new KVP("bandwidth_reservation"));
		}

	}

	private static final Logger logger = Logger.getLogger(SpliceInfoSection.class.getName());
	
	public class SpliceTime implements TreeNode{
		
		private int time_specified_flag;
		private long pts_time;
		
		private SpliceTime(BitSource bs){
			time_specified_flag = bs.readBits(1);
			if(time_specified_flag==1){
				bs.readBits(6);
				pts_time = bs.readBitsLong(33);
			}else{
				bs.readBits(7);
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {

			String preRoolTime = getPreRollTimeString();
			
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("splice_time"+preRoolTime));
			t.add(new DefaultMutableTreeNode(new KVP("time_specified_flag",time_specified_flag,null)));
			if(time_specified_flag==1){
				t.add(new DefaultMutableTreeNode(new KVP("pts_time",pts_time,Utils.printTimebase90kHz(pts_time))));
			}
			return t;
		}

		private String getPreRollTimeString() {
			String preRollTime = "";
			if (time_specified_flag == 1) {
				PID parentPid = getParentPID();
				TransportStream ts = parentPid.getParentTransportStream();
				PMTs pmts = ts.getPsi().getPmts();

				List<PMTsection> pmtList = pmts.findPMTsFromComponentPID(parentPid.getPid());
				if (pmtList.size() >= 1) {
					PMTsection pmt = pmtList.get(0);
					PID pcrPid = ts.getPID(pmt.getPcrPid());

					Long packetPcrTime = pcrPid.getPacketPcrTime(getFirst_packet_no()); // 27 Mhz
					if (packetPcrTime != null) {
						// ptsTime = 90 kHz clock
						double preRollSecs = ((double) (getSpliceTimeAdjusted() - (packetPcrTime / 300))) / 90_000L;
						preRollTime = String.format(" (preroll time = %3.3f secs)", preRollSecs);
					}
				}
			}
			return preRollTime;
		}

		public int getTime_specified_flag() {
			return time_specified_flag;
		}

		public long getPts_time() {
			return pts_time;
		}
		
		public long getSpliceTimeAdjusted() {
			long time = (pts_time + pts_adjustment) & Utils.MASK_33BITS;
			return time;
		}

	}
	
	
	private class BreakDuration implements TreeNode {

		private int auto_return;
		private int reserved;
		private long duration;

		private BreakDuration(BitSource bs) {
			auto_return = bs.readBits(1);
			reserved = bs.readBits(6);
			duration = bs.readBitsLong(33);
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("break_duration"));
			t.add(new DefaultMutableTreeNode(new KVP("auto_return", auto_return, null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved, null)));
			t.add(new DefaultMutableTreeNode(new KVP("duration", duration, Utils.printTimebase90kHz(duration))));
			return t;
		}
	}
	
	public class SpliceInsert implements TreeNode{
		
		private long splice_event_id;
		private int splice_event_cancel_indicator;
		private int out_of_network_indicator;
		private int program_splice_flag;
		private int duration_flag;
		private int splice_immediate_flag;
		
		private int unique_program_id;
		private int avail_num;
		private int avails_expected;
		private int component_count;
		private SpliceTime splice_time;
		private BreakDuration break_duration;
		private List<Integer> componentTags = new ArrayList<>();
		private List<SpliceTime> componentSpliceTimes = new ArrayList<>();
		
		private SpliceInsert(BitSource bitSource){
			splice_event_id =  bitSource.readBitsLong(32); //getLong(data, 14, 4, MASK_32BITS);
			splice_event_cancel_indicator = bitSource.readBits(1); //Utils.getInt(data,18,1,0x80)>>7;
			bitSource.readBits(7); // reserved
			if(splice_event_cancel_indicator==0){
				out_of_network_indicator =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x80)>>7;
				program_splice_flag =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x40)>>6;
				duration_flag =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x20)>>5;
				splice_immediate_flag =  bitSource.readBits(1); //Utils.getInt(data,19,1,0x10)>>4;
				 bitSource.readBits(4); // reserved
				if((program_splice_flag == 1) && (splice_immediate_flag == 0)){
					splice_time = new SpliceTime(bitSource);
				}
				if(program_splice_flag == 0) {
					component_count = bitSource.readBits(8);// Utils.getInt(data,20,1,Utils.MASK_8BITS);
					for(int i=0;i<component_count;i++) {
						int component_tag = bitSource.readBits(8) ; //8 uimsbf
						componentTags .add(component_tag);
						 if(splice_immediate_flag == 0){
							 SpliceTime component_splice_time = new SpliceTime(bitSource);
							 componentSpliceTimes.add(component_splice_time);
						}
					}
				}
				if(duration_flag == 1){
					break_duration = new BreakDuration(bitSource);
				}
				unique_program_id = bitSource.readBits(16); // bs.readBits(16);
				avail_num =  bitSource.readBits(8); //bs.readBits(8);
				avails_expected =  bitSource.readBits(8); //bs.readBits(8);
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("splice_insert"));

			t.add(new DefaultMutableTreeNode(new KVP("splice_event_id",splice_event_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("splice_event_cancel_indicator",splice_event_cancel_indicator,splice_event_cancel_indicator==1?"a previously sent splice event, identified by splice_event_id, has been cancelled":null)));
			if(splice_event_cancel_indicator==0){
				t.add(new DefaultMutableTreeNode(new KVP("out_of_network_indicator",out_of_network_indicator,out_of_network_indicator==1?"the splice event is an opportunity to exit from the network feed":"the splice event is an opportunity to return to the network feed")));
				t.add(new DefaultMutableTreeNode(new KVP("program_splice_flag",program_splice_flag,program_splice_flag==1?"the message refers to a Program Splice Point":"each component that is intended to be spliced will be listed separately")));
				t.add(new DefaultMutableTreeNode(new KVP("duration_flag",duration_flag,duration_flag==1?"break_duration() field present":"break_duration() field not present")));
				t.add(new DefaultMutableTreeNode(new KVP("splice_immediate_flag",splice_immediate_flag,splice_immediate_flag==1?"splice mode shall be the Splice Immediate Mode":"splice_time() field present")));
				if((program_splice_flag == 1) && (splice_immediate_flag == 0)){
					t.add(splice_time.getJTreeNode(modus));
				}
				if(program_splice_flag == 0) {
					t.add(new DefaultMutableTreeNode(new KVP("component_count",component_count,null)));
					for(int i=0;i<component_count;i++) {
						t.add(new DefaultMutableTreeNode(new KVP("component_tag ["+i+"]",componentTags.get(i),null)));
						 if(splice_immediate_flag == 0){
								t.add(componentSpliceTimes.get(i).getJTreeNode(modus));
						}
					}
				}
				if(duration_flag == 1){
					t.add(break_duration.getJTreeNode(modus));
				}
				
				t.add(new DefaultMutableTreeNode(new KVP("unique_program_id",unique_program_id,null)));
				t.add(new DefaultMutableTreeNode(new KVP("avail_num",avail_num,null)));
				t.add(new DefaultMutableTreeNode(new KVP("avails_expected",avails_expected,null)));
			}
			return t;
		}

		public long getSplice_event_id() {
			return splice_event_id;
		}

		public int getSplice_event_cancel_indicator() {
			return splice_event_cancel_indicator;
		}

		public int getOut_of_network_indicator() {
			return out_of_network_indicator;
		}

		public int getProgram_splice_flag() {
			return program_splice_flag;
		}

		public int getDuration_flag() {
			return duration_flag;
		}

		public int getSplice_immediate_flag() {
			return splice_immediate_flag;
		}

		public int getUnique_program_id() {
			return unique_program_id;
		}

		public int getAvail_num() {
			return avail_num;
		}

		public int getAvails_expected() {
			return avails_expected;
		}

		public int getComponent_count() {
			return component_count;
		}

		public SpliceTime getSplice_time() {
			return splice_time;
		}

		public BreakDuration getBreak_duration() {
			return break_duration;
		}

		public List<Integer> getComponentTags() {
			return componentTags;
		}

		public List<SpliceTime> getComponentSpliceTimes() {
			return componentSpliceTimes;
		}	
	}
	
	public class TimeSignal implements TreeNode{
		
		private SpliceTime splice_time;

		public SpliceTime getSplice_time() {
			return splice_time;
		}

		private TimeSignal(BitSource bs){
			super();
			splice_time = new SpliceTime(bs);
		}
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("time_signal"));
			t.add(splice_time.getJTreeNode(modus));
			return t;
		}
		
	}
	
	private class SpliceNull implements TreeNode{

		private SpliceNull(BitSource bs){
			super();
		}
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			return new DefaultMutableTreeNode(new KVP("splice_null"));
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
	private int descriptor_loop_length;
	
	List<Descriptor> splice_descriptors = new ArrayList<>();
	private TreeNode splice_command;


	public SpliceInfoSection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		final byte[] data = raw_data.getData();
		BitSource bitSource = new BitSource(data, 3, sectionLength);
		protocol_version = bitSource.readBits(8); // Utils.getInt(data,3,1,Utils.MASK_8BITS);
		encrypted_packet =  bitSource.readBits(1); //Utils.getInt(data,4,1,0x80)>>7;
		encryption_algorithm =  bitSource.readBits(6); //Utils.getInt(data,4,1,0x7E)>>1;
		
		pts_adjustment =  bitSource.readBitsLong(33); //getLong(data,4, 5, MASK_33BITS);
		cw_index =  bitSource.readBits(8); //Utils.getInt(data,9,1,Utils.MASK_8BITS);
		tier =  bitSource.readBits(12); //Utils.getInt(data,10,2,0xFFF0)>>4;
		splice_command_length =  bitSource.readBits(12); //Utils.getInt(data,11,2,Utils.MASK_12BITS);
		if(encrypted_packet==0){
			splice_command_type=  bitSource.readBits(8); //Utils.getInt(data,13,1,Utils.MASK_8BITS);
			if(splice_command_type==0){
				splice_command = new SpliceNull(bitSource);
			}else if(splice_command_type==5){
				splice_command = new SpliceInsert(bitSource);
			}else if(splice_command_type==6){
				splice_command = new TimeSignal(bitSource);
			}else if(splice_command_type==7){
				splice_command = new BandwidthReservation(bitSource);
			}else{
				logger.info("Not implemented: splice_command_type="+splice_command_type+" ("+getSpliceCommandTypeString(splice_command_type)+")");
			}
			
			int loopstart = 0;
			if ((splice_command_type == 0) || (splice_command_type == 5)|| (splice_command_type == 7)) {
				loopstart = bitSource.getNextFullByteOffset();
			} else if (splice_command_length != 0xFFF) {
				loopstart = 14 + splice_command_length;
			}
			if (loopstart != 0) {
				descriptor_loop_length = Utils.getInt(data, loopstart, 2, Utils.MASK_12BITS);
				splice_descriptors = DescriptorFactory.buildDescriptorList(data,
						loopstart + 2,
						descriptor_loop_length,
						this);
			}
		
		}
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
		t.add(new DefaultMutableTreeNode(new KVP("splice_command_length",splice_command_length,splice_command_length==0xFFF?"The value of 0xFFF provides backwards compatibility and shall be ignored by downstream equipment":null)));
		if(encrypted_packet==0){
			t.add(new DefaultMutableTreeNode(new KVP("splice_command_type",splice_command_type,getSpliceCommandTypeString(splice_command_type))));

			if(splice_command_type==0){
				t.add(splice_command.getJTreeNode(modus));
			}else if(splice_command_type==5){
				t.add(splice_command.getJTreeNode(modus));
			}else if(splice_command_type==6){
				t.add(splice_command.getJTreeNode(modus));
			}else if(splice_command_type==7){
				t.add(splice_command.getJTreeNode(modus));
			}else{
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("splice_command_type ="+splice_command_type+" ("+ getSpliceCommandTypeString(splice_command_type)+")")));
				return t;
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


	public int getProtocol_version() {
		return protocol_version;
	}


	public int getEncrypted_packet() {
		return encrypted_packet;
	}


	public int getEncryption_algorithm() {
		return encryption_algorithm;
	}


	public long getPts_adjustment() {
		return pts_adjustment;
	}


	public int getCw_index() {
		return cw_index;
	}


	public int getTier() {
		return tier;
	}


	public int getSplice_command_length() {
		return splice_command_length;
	}


	public int getSplice_command_type() {
		return splice_command_type;
	}


	public int getDescriptor_loop_length() {
		return descriptor_loop_length;
	}


	public List<Descriptor> getSplice_descriptors() {
		return splice_descriptors;
	}


	public TreeNode getSplice_command() {
		return splice_command;
	}

}
