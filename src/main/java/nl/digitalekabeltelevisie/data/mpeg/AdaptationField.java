/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg;

import static java.lang.Byte.toUnsignedLong;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors.AFDescriptorFactory;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.*;
import nl.digitalekabeltelevisie.util.NtpTimeStamp;


public class AdaptationField implements HTMLSource, TreeNode{

	private static final LookUpList data_field_tag_list = new LookUpList.Builder().
			add(0x00,"Reserved").
			add(0x01,"Announcement switching data field").
			add(0x02,"AU_information data field").
			add(0x03,"PVR_assist_information data field").
			add(0x04,"TSAP_timeline data field").
			add(0x05,0x9F,"Reserved for future use").
			add(0xA0,0xDE,"User defined").
			add(0xDF,"Registered Private Data").
			add(0xE0,0xFF,"User defined").
			build();



	private static final LookUpList AU_coding_format_list = new LookUpList.Builder().
			add(0,"Undefined").
			add(1,"ITU-T Recommendation H.262 / ISO/IEC 13818-2 Video or ISO/IEC 11172-1 constrained parameter video stream").
			add(2,"H.264/AVC video stream as defined in ITU-T Recommendation H.264 / ISO/IEC 14496-10 Video").
			add(3,"VC-1 video stream as defined in SMPTE ST 421").
			add(4,0xF,"Reserved").
			build();

	private static final LookUpList AU_frame_rate_code_list = new LookUpList.Builder().
			add(0, "Forbidden").
			add(1, "23,976").
			add(2, "24").
			add(3, "25").
			add(4, "29,97").
			add(5, "30").
			add(6, "50").
			add(7, "59,94").
			add(8, "60").
			add(9,0xF, "Reserved").
			build();




	/**
	 *
	 * Based on Annex D (normative): Coding of Data Fields in the Private Data Bytes of the Adaptation Field
	 * ETSI TS 101 154 V1.11.1 (2012-11) Digital Video Broadcasting (DVB); Specification for the use of Video and Audio Coding in Broadcasting Applications based on the MPEG-2 Transport Stream
	 *
	 * @author Eric
	 *
	 */
	public static class PrivateDataField implements TreeNode{

		private int data_field_tag;
		private int data_field_length;

		private final byte[] data_byte;
		// Announcement Switching Data
		private int announcement_switching_flag_field;
		//AU_information
		private int AU_coding_format ;
		private int AU_coding_type_information ;
		private int AU_ref_pic_idc ;
		private int AU_pic_struct ;
		private int AU_PTS_present_flag ;
		private int AU_profile_info_present_flag;
		private int AU_stream_info_present_flag ;
		private int AU_trick_mode_info_present_flag ;

		private long AU_PTS_32;

		private int reserved;
		private int AU_frame_rate_code;

		private int AU_profile;
		private int AU_constraint_set0_flag;
		private int AU_constraint_set1_flag;
		private int AU_constraint_set2_flag;
		private int AU_AVC_compatible_flags;
		private int AU_level;

		private int AU_max_I_picture_size;
		private int AU_nominal_I_period;
		private int AU_max_I_period;
		private int reserved2;
		private int AU_Pulldown_info_present_flag;
		private int AU_reserved_zero;
		private int AU_flags_extension_1;
		private boolean extraDataPresent = false;
		private int AU_reserved;
		private byte[] AU_reserved_byte;

		
		public byte[] formatIdentifier;
		public int EBP_fragment_flag;
		public int EBP_segment_flag;
		public int EBP_SAP_flag;
		private int EBP_grouping_flag;
		private int EBP_time_flag;
		private int EBP_concealment_flag;
		private int EBP_extension_flag;
		private int EBP_ext_partition_flag;
		private int EBP_SAP_type;
		private int reserved3;
		private int EBP_grouping_ext_flag;
		
		private List<Integer> groupingList = new  ArrayList<>();
		private long EBP_acquisition_time;
		private int EBP_ext_partitions;

		PrivateDataField(byte [] private_data_byte, int offset){

			data_byte = private_data_byte;
			data_field_tag = getInt(private_data_byte, offset, 1, MASK_8BITS);
			data_field_length = getInt(private_data_byte, offset + 1, 1, MASK_8BITS);
			if(data_field_tag==0x01){ // Announcement Switching Data
				buildAnnouncementSwitchingData(private_data_byte, offset);
			}else if(data_field_tag==0x02){ //AU_information
				buildAU_information(private_data_byte,offset);
			}else if(data_field_tag==0x0DF){ //EBP_info
				buildEBP_info(private_data_byte,offset);
			}else{
				logger.warning("data_field_tag=="+data_field_tag+" not implemented");
			}
		}

		/**
		 * @param private_data_byte
		 */
		public void buildAU_information(byte[] private_data_byte, int offset) {
			if(data_field_length>0){
				AU_coding_format = getInt(private_data_byte, offset + 2, 1, 0xF0) >> 4;
				AU_coding_type_information = getInt(private_data_byte, offset + 2, 1, MASK_4BITS);
				if(data_field_length>1){
					AU_ref_pic_idc = getInt(private_data_byte, offset + 3, 1, 0xC0) >> 6;
					AU_pic_struct = getInt(private_data_byte, offset + 3, 1, 0x30) >> 4;
					AU_PTS_present_flag = getInt(private_data_byte, offset + 3, 1, 0x08) >> 3;
					AU_profile_info_present_flag = getInt(private_data_byte, offset + 3, 1, 0x04) >> 2;
					AU_stream_info_present_flag = getInt(private_data_byte, offset + 3, 1, 0x02) >> 1;
					AU_trick_mode_info_present_flag = getInt(private_data_byte, offset + 3, 1, 0x01);
					int localOffset = offset + 4;
					if (AU_PTS_present_flag == 1) {
						AU_PTS_32 = getLong(private_data_byte, localOffset, 4, MASK_32BITS);
						localOffset += 4;
					}
					if(AU_stream_info_present_flag == 1){
						reserved = getInt(private_data_byte, localOffset,1, 0xF0)>>4;
						AU_frame_rate_code= getInt(private_data_byte, localOffset,1, MASK_4BITS);
						localOffset ++;
					}
					if (AU_profile_info_present_flag == 1) {
						AU_profile = getInt(private_data_byte, localOffset,1, MASK_8BITS);
						localOffset++;
						AU_constraint_set0_flag = getInt(private_data_byte, localOffset,1, 0x80)>>7;
						AU_constraint_set1_flag = getInt(private_data_byte, localOffset,1, 0x40)>>6;
						AU_constraint_set2_flag = getInt(private_data_byte, localOffset,1, 0x20)>>5;
						AU_AVC_compatible_flags = getInt(private_data_byte, localOffset,1, MASK_5BITS);
						localOffset++;
						AU_level = getInt(private_data_byte, localOffset,1, MASK_8BITS);
						localOffset++;
					}
					if (AU_trick_mode_info_present_flag == 1) {
						AU_max_I_picture_size = getInt(private_data_byte, localOffset,2, 0xFFF0)>>4;
						localOffset++;
						AU_nominal_I_period = getInt(private_data_byte, localOffset,2, 0x0FF0)>>4;
						localOffset++;
						AU_max_I_period = getInt(private_data_byte, localOffset,2, 0x0FF0)>>4;
						localOffset++;
						reserved2 = getInt(private_data_byte, localOffset,1, MASK_4BITS);
						localOffset++;
					}
					if(localOffset-2 < data_field_length){
						extraDataPresent  = true;
						AU_Pulldown_info_present_flag = getInt(private_data_byte, localOffset,1, 0x80)>>7;
						AU_reserved_zero = getInt(private_data_byte, localOffset,1, 0x7E)>>1;
						AU_flags_extension_1  = getInt(private_data_byte, localOffset,1, MASK_1BIT);
						localOffset ++;
						if(AU_flags_extension_1==1){
							AU_reserved = getInt(private_data_byte, localOffset,1, MASK_8BITS);
							localOffset ++;
						}
					}
					if(localOffset-2 < data_field_length){
						AU_reserved_byte = getBytes(private_data_byte, localOffset, data_field_length - localOffset + 2);
					}
				}
			}
		}

		/**
		 * @param private_data_byte
		 */
		public void buildAnnouncementSwitchingData(byte[] private_data_byte, int offset) {
			announcement_switching_flag_field = getInt(private_data_byte, offset + 2, 2, MASK_16BITS);
		}
		
		
		// based on OpenCable™ Specifications 
		// Encoder Boundary Point Specification 
		// OC-SP-EBP-I01-130118
		// 5.2 Structure 

		public void buildEBP_info(byte[] private_data_byte, int offset) {
			
			formatIdentifier = getBytes(private_data_byte,offset+2,4);
			EBP_fragment_flag  = getInt(private_data_byte, offset + 6, 1, 0b1000_0000)>>7;
			EBP_segment_flag  = getInt(private_data_byte, offset + 6, 1, 0b0100_0000)>>6;
			EBP_SAP_flag  = getInt(private_data_byte, offset + 6, 1, 0b0010_0000)>>5;
			EBP_grouping_flag  = getInt(private_data_byte, offset + 6, 1, 0b0001_0000)>>4;
			EBP_time_flag  = getInt(private_data_byte, offset + 6, 1, 0b0000_1000)>>3;
			EBP_concealment_flag  = getInt(private_data_byte, offset + 6, 1, 0b0000_0100)>>2;
			reserved  = getInt(private_data_byte, offset + 6, 1, 0b0000_0010)>>1;
			EBP_extension_flag = getInt(private_data_byte, offset + 6, 1, 0b0000_0001);
			
			int localOffset = offset + 7;
			if (EBP_extension_flag==1) {
				EBP_ext_partition_flag  = getInt(private_data_byte, localOffset, 1, 0b1000_0000)>>7;
				reserved2 = getInt(private_data_byte, localOffset, 1, 0b0111_1111);
				localOffset++;
			}   
			 if (EBP_SAP_flag==1 ){
				 EBP_SAP_type  = getInt(private_data_byte, localOffset, 1, 0b1110_0000)>>5;
				 reserved3 = getInt(private_data_byte, localOffset, 1, 0b0001_1111);
				 localOffset++;
			 }
			 
			if (EBP_grouping_flag == 1) {
				int grouping = getInt(private_data_byte, localOffset++, 1, MASK_8BITS);
				groupingList.add(grouping);
				EBP_grouping_ext_flag = (grouping & 0b1000_0000) >> 7;
				while (EBP_grouping_ext_flag == 1) {
					grouping = getInt(private_data_byte, localOffset++, 1, MASK_8BITS);
					EBP_grouping_ext_flag = (grouping & 0b1000_0000) >> 7;
					groupingList.add(grouping);
				}
			}
			
			if (EBP_time_flag == 1) {
				EBP_acquisition_time = getLong(private_data_byte, localOffset, 8, MASK_64BITS);
				localOffset += 8;
			}		
			
			if (EBP_ext_partition_flag==1 ){
				EBP_ext_partitions = getInt(private_data_byte, localOffset++, 1, MASK_8BITS);
			}
			
			 
		}
		

		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Private Data Field ("+getDataFieldTagString(data_field_tag)+")"));
			t.add(new DefaultMutableTreeNode(new KVP("data_field_tag",data_field_tag,getDataFieldTagString(data_field_tag))));
			t.add(new DefaultMutableTreeNode(new KVP("data_field_length",data_field_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("data_field_data", data_byte,2,data_field_length,null)));

			if(data_field_tag==0x01){ // Announcement Switching Data
				t.add(new DefaultMutableTreeNode(new KVP("announcement_switching_flag_field",announcement_switching_flag_field,null)));
			}else if(data_field_tag==0x02){ //AU_information
				if(data_field_length>0){
					t.add(new DefaultMutableTreeNode(new KVP("AU_coding_format",AU_coding_format,getAU_coding_formatString(AU_coding_format))));
					t.add(new DefaultMutableTreeNode(new KVP("AU_coding_type_information",AU_coding_type_information,null)));
					if(data_field_length>1){
						t.add(new DefaultMutableTreeNode(new KVP("AU_ref_pic_idc",AU_ref_pic_idc,null)));
						t.add(new DefaultMutableTreeNode(new KVP("AU_pic_struct",AU_pic_struct,null)));
						t.add(new DefaultMutableTreeNode(new KVP("AU_PTS_present_flag",AU_PTS_present_flag,null)));
						t.add(new DefaultMutableTreeNode(new KVP("AU_profile_info_present_flag",AU_profile_info_present_flag,null)));
						t.add(new DefaultMutableTreeNode(new KVP("AU_stream_info_present_flag",AU_stream_info_present_flag,null)));
						t.add(new DefaultMutableTreeNode(new KVP("AU_trick_mode_info_present_flag",AU_trick_mode_info_present_flag,null)));
						if(AU_PTS_present_flag == 1){
							t.add(new DefaultMutableTreeNode(new KVP("AU_PTS_32",AU_PTS_32,null)));
						}
						if(AU_stream_info_present_flag == 1){
							t.add(new DefaultMutableTreeNode(new KVP("Reserved",reserved,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_frame_rate_code",AU_frame_rate_code,getAUFrameRateCodeString(AU_frame_rate_code))));
						}
						if (AU_profile_info_present_flag == 1) {
							t.add(new DefaultMutableTreeNode(new KVP("AU_profile",AU_profile,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_constraint_set0_flag",AU_constraint_set0_flag,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_constraint_set1_flag",AU_constraint_set1_flag,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_constraint_set2_flag",AU_constraint_set2_flag,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_AVC_compatible_flags",AU_AVC_compatible_flags,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_level",AU_level,null)));
						}
						if (AU_trick_mode_info_present_flag == 1) {
							t.add(new DefaultMutableTreeNode(new KVP("AU_max_I_picture_size",AU_max_I_picture_size,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_nominal_I_period",AU_nominal_I_period,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_max_I_period",AU_max_I_period,null)));
							t.add(new DefaultMutableTreeNode(new KVP("Reserved",reserved2,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_level",AU_level,null)));
						}
						if(extraDataPresent){
							t.add(new DefaultMutableTreeNode(new KVP("AU_Pulldown_info_present_flag",AU_Pulldown_info_present_flag,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_reserved_zero",AU_reserved_zero,null)));
							t.add(new DefaultMutableTreeNode(new KVP("AU_flags_extension_1",AU_flags_extension_1,null)));
							if(AU_flags_extension_1==1){
								t.add(new DefaultMutableTreeNode(new KVP("AU_reserved",AU_reserved,null)));
							}
						}
						if(AU_reserved_byte!=null){
							t.add(new DefaultMutableTreeNode(new KVP("AU_reserved_byte",AU_reserved_byte,null)));
						}
					}
				}
			} else if (data_field_tag == 0xDF) {
				t.add(new DefaultMutableTreeNode(new KVP("format_identifier", formatIdentifier, null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_fragment_flag", EBP_fragment_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_segment_flag", EBP_segment_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_SAP_flag", EBP_SAP_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_grouping_flag", EBP_grouping_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_time_flag", EBP_time_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_concealment_flag", EBP_concealment_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved, null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_extension_flag", EBP_extension_flag, null)));
				if (EBP_extension_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("EBP_ext_partition_flag", EBP_ext_partition_flag, null)));
					t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved2, null)));
				}
				if (EBP_SAP_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("EBP_SAP_type", EBP_SAP_type, null)));
					t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved3, null)));
				}

				 
				if (EBP_grouping_flag == 1) {
					DefaultMutableTreeNode list = new DefaultMutableTreeNode(new KVP("GroupingList"));
					for(Integer grouping:groupingList) {
						EBP_grouping_ext_flag = (grouping & 0b1000_0000) >> 7;
						int EBP_grouping_id = (grouping & 0b0111_1111);
						list.add(new DefaultMutableTreeNode(new KVP("EBP_grouping_ext_flag", EBP_grouping_ext_flag, null)));
						list.add(new DefaultMutableTreeNode(new KVP("EBP_grouping_id", EBP_grouping_id, null)));
					}
					t.add(list);
				}

				
				if (EBP_time_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("EBP_acquisition_time", EBP_acquisition_time, new NtpTimeStamp(EBP_acquisition_time).toUTCString())));
				}		
				
				if (EBP_ext_partition_flag==1 ){
					t.add(new DefaultMutableTreeNode(new KVP("EBP_ext_partitions", EBP_ext_partitions , null)));

				}
				

			} else {
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("data_field_tag=="+data_field_tag)));
			}
			return t;
		}

		/**
		 * @param aU_frame_rate_code2
		 * @return
		 */
		private String getAUFrameRateCodeString(int aU_frame_rate_code2) {
			return AU_frame_rate_code_list.get(aU_frame_rate_code2);
		}

		public int getData_field_tag() {
			return data_field_tag;
		}

		public void setData_field_tag(int data_field_tag) {
			this.data_field_tag = data_field_tag;
		}

		public int getData_field_length() {
			return data_field_length;
		}

		public void setData_field_length(int data_field_length) {
			this.data_field_length = data_field_length;
		}

		public byte[] getData_byte() {
			return data_byte;
		}

		public int getAnnouncement_switching_flag_field() {
			return announcement_switching_flag_field;
		}

		public int getAU_coding_format() {
			return AU_coding_format;
		}

		public int getAU_coding_type_information() {
			return AU_coding_type_information;
		}

		public int getAU_ref_pic_idc() {
			return AU_ref_pic_idc;
		}

		public int getAU_pic_struct() {
			return AU_pic_struct;
		}

		public int getAU_PTS_present_flag() {
			return AU_PTS_present_flag;
		}

		public int getAU_profile_info_present_flag() {
			return AU_profile_info_present_flag;
		}

		public int getAU_stream_info_present_flag() {
			return AU_stream_info_present_flag;
		}

		public int getAU_trick_mode_info_present_flag() {
			return AU_trick_mode_info_present_flag;
		}

		public long getAU_PTS_32() {
			return AU_PTS_32;
		}

		public int getReserved() {
			return reserved;
		}

		public int getAU_frame_rate_code() {
			return AU_frame_rate_code;
		}

		public int getAU_profile() {
			return AU_profile;
		}

		public int getAU_constraint_set0_flag() {
			return AU_constraint_set0_flag;
		}

		public int getAU_constraint_set1_flag() {
			return AU_constraint_set1_flag;
		}

		public int getAU_constraint_set2_flag() {
			return AU_constraint_set2_flag;
		}

		public int getAU_AVC_compatible_flags() {
			return AU_AVC_compatible_flags;
		}

		public int getAU_level() {
			return AU_level;
		}

		public int getAU_max_I_picture_size() {
			return AU_max_I_picture_size;
		}

		public int getAU_nominal_I_period() {
			return AU_nominal_I_period;
		}

		public int getAU_max_I_period() {
			return AU_max_I_period;
		}

		public int getReserved2() {
			return reserved2;
		}

		public int getAU_Pulldown_info_present_flag() {
			return AU_Pulldown_info_present_flag;
		}

		public int getAU_reserved_zero() {
			return AU_reserved_zero;
		}

		public int getAU_flags_extension_1() {
			return AU_flags_extension_1;
		}

		public boolean isExtraDataPresent() {
			return extraDataPresent;
		}

		public int getAU_reserved() {
			return AU_reserved;
		}

		public byte[] getAU_reserved_byte() {
			return AU_reserved_byte;
		}

	}

	private static Logger	logger	= Logger.getLogger(AdaptationField.class.getName());

	private int adaptation_field_length = -1;
	private boolean discontinuity_indicator = false;
	private boolean random_access_indicator = false;
	private boolean elementary_stream_priority_indicator = false;
	private boolean PCR_flag = false;
	private boolean OPCR_flag = false;
	private boolean splicing_point_flag = false;
	private boolean transport_private_data_flag = false;
	private boolean adaptation_field_extension_flag = false;
	private PCR program_clock_reference = null;
	private PCR original_program_clock_reference = null;
	private int splice_countdown = 0;

	private int transport_private_data_length = 0;
	private byte[] private_data_byte;
	private List<PrivateDataField> privatedataFields = new ArrayList<>();

	private int adaptation_field_extension_length;

	private byte[] adaptation_field_extension_byte;
	private boolean ltw_flag;
	private boolean piecewise_rate_flag;
	private boolean seamless_splice_flag;
	private boolean af_descriptor_not_present_flag = true;

	private boolean ltw_valid_flag;
	private int ltw_offset;
	private int piecewise_rate;

	private List<Descriptor> afDescriptorList;




	public AdaptationField(final byte[] data) {
		adaptation_field_length =   getInt(data,0,1,MASK_8BITS);
		if(adaptation_field_length >0) {
			discontinuity_indicator = getBitAsBoolean(data[1],1);
			random_access_indicator  = getBitAsBoolean(data[1],2);
			elementary_stream_priority_indicator = getBitAsBoolean(data[1],3);
			PCR_flag = getBitAsBoolean(data[1],4);
			OPCR_flag = getBitAsBoolean(data[1],5);
			splicing_point_flag = getBitAsBoolean(data[1],6);
			transport_private_data_flag = getBitAsBoolean(data[1],7);
			adaptation_field_extension_flag = getBitAsBoolean(data[1],8);
			int offset = 2;
			if(PCR_flag){
				program_clock_reference = getPCRfromBytes(data, offset);
				offset +=6; //33+6+9 bits = 6 bytes
			}
			if(OPCR_flag){
				original_program_clock_reference = getPCRfromBytes(data, offset);
				offset +=6; //33+6+9 bits = 6 bytes
			}
			if(splicing_point_flag){
				splice_countdown =  getInt(data, offset,1,MASK_8BITS);
				logger.info("splicing_point_flag is set, splice_countdown="+splice_countdown);
				offset +=1;
			}
			if(transport_private_data_flag){
				transport_private_data_length = getInt(data, offset,1,MASK_8BITS);
				offset += 1;
				private_data_byte = getBytes(data, offset, Math.min(transport_private_data_length, adaptation_field_length+ 1 - offset));
				if(private_data_byte.length>0){
					privatedataFields = buildPrivatedataFieldsList(private_data_byte);
				}
				offset += transport_private_data_length ;
			}
			if(adaptation_field_extension_flag&& data.length> offset +2){ //extension is at least 2 bytes
				adaptation_field_extension_length =  getInt(data, offset,1,MASK_8BITS);
				offset +=1;
				adaptation_field_extension_byte = getBytes(data, offset, Math.min(adaptation_field_extension_length, adaptation_field_length+ 1 - offset));
				int adaptation_field_extension_offset = 0;
				ltw_flag = getBitAsBoolean(adaptation_field_extension_byte[adaptation_field_extension_offset],1);
				piecewise_rate_flag =  getBitAsBoolean(adaptation_field_extension_byte[adaptation_field_extension_offset],2);
				seamless_splice_flag =  getBitAsBoolean(adaptation_field_extension_byte[adaptation_field_extension_offset],3);
				af_descriptor_not_present_flag = getBitAsBoolean(adaptation_field_extension_byte[adaptation_field_extension_offset],4);
				adaptation_field_extension_offset+=1;
				if(ltw_flag){
					ltw_valid_flag = getBitAsBoolean(adaptation_field_extension_byte[adaptation_field_extension_offset],1);
					ltw_offset = getInt(adaptation_field_extension_byte,adaptation_field_extension_offset,2,MASK_15BITS);
					adaptation_field_extension_offset+=2;
				}
				if (piecewise_rate_flag) {
					// reserved
					piecewise_rate = getInt(adaptation_field_extension_byte, adaptation_field_extension_offset, 3, MASK_22BITS);
					adaptation_field_extension_offset += 3;
				}

				if (seamless_splice_flag) {
					// TODO not implemented
					adaptation_field_extension_offset += 5;
					logger.info("seamless_splice_flag set, not implemented");
				}
				if (!af_descriptor_not_present_flag){
					afDescriptorList = AFDescriptorFactory.buildDescriptorList(adaptation_field_extension_byte, adaptation_field_extension_offset, adaptation_field_extension_length - adaptation_field_extension_offset);
				}

			}
		}
	}


	/**
	 * @param private_data_byte2
	 * @return
	 */
	private List<PrivateDataField> buildPrivatedataFieldsList(byte[] private_data_byte2) {

		List<PrivateDataField> result = new ArrayList<>();
		int offset = 0;
		while(offset<private_data_byte2.length){
			PrivateDataField pdf = new PrivateDataField(private_data_byte2, offset);
			result.add(pdf);
			offset += pdf.getData_field_length()+2;
		}
		return result;
	}


	private static PCR getPCRfromBytes(final byte[] array, final int offset) {
		long pcr = toUnsignedLong(array[offset]) << 25;
		pcr |= toUnsignedLong(array[offset + 1]) << 17;
		pcr |= toUnsignedLong(array[offset + 2]) << 9;
		pcr |= toUnsignedLong(array[offset + 3]) << 1;
		pcr |= (0x80L & toUnsignedLong(array[offset + 4])) >>> 7;
		final long reserved = (0x7EL & toUnsignedLong(array[offset + 4])) >>> 1;

		long pcr_extension = (0x01L & toUnsignedLong(array[offset + 4])) << 8;
		pcr_extension |= toUnsignedLong(array[offset + 5]);

		return new PCR(pcr, reserved, pcr_extension);
	}

	public boolean isAdaptation_field_extension_flag() {
		return adaptation_field_extension_flag;
	}


	public void setAdaptation_field_extension_flag(
			final boolean adaptation_field_extension_flag) {
		this.adaptation_field_extension_flag = adaptation_field_extension_flag;
	}


	public int getAdaptation_field_length() {
		return adaptation_field_length;
	}


	public void setAdaptation_field_length(final int adaptation_field_length) {
		this.adaptation_field_length = adaptation_field_length;
	}


	public boolean isDiscontinuity_indicator() {
		return discontinuity_indicator;
	}


	public void setDiscontinuity_indicator(final boolean discontinuity_indicator) {
		this.discontinuity_indicator = discontinuity_indicator;
	}


	public boolean isElementary_stream_priority_indicator() {
		return elementary_stream_priority_indicator;
	}


	public void setElementary_stream_priority_indicator(
			final boolean elementary_stream_priority_indicator) {
		this.elementary_stream_priority_indicator = elementary_stream_priority_indicator;
	}


	public boolean isOPCR_flag() {
		return OPCR_flag;
	}


	public void setOPCR_flag(final boolean opcr_flag) {
		OPCR_flag = opcr_flag;
	}


	public PCR getOriginal_program_clock_reference() {
		return original_program_clock_reference;
	}


	public void setOriginal_program_clock_reference(
			final PCR original_program_clock_reference_base) {
		this.original_program_clock_reference = original_program_clock_reference_base;
	}


	public boolean isPCR_flag() {
		return PCR_flag;
	}


	public void setPCR_flag(final boolean pcr_flag) {
		PCR_flag = pcr_flag;
	}


	public PCR getProgram_clock_reference() {
		return program_clock_reference;
	}


	public void setProgram_clock_reference(final PCR program_clock_reference_base) {
		this.program_clock_reference = program_clock_reference_base;
	}


	public boolean isRandom_access_indicator() {
		return random_access_indicator;
	}


	public void setRandom_access_indicator(final boolean random_access_indicator) {
		this.random_access_indicator = random_access_indicator;
	}


	public boolean isSplicing_point_flag() {
		return splicing_point_flag;
	}


	public void setSplicing_point_flag(final boolean splicing_point_flag) {
		this.splicing_point_flag = splicing_point_flag;
	}


	public boolean isTransport_private_data_flag() {
		return transport_private_data_flag;
	}


	public void setTransport_private_data_flag(final boolean transport_private_data_flag) {
		this.transport_private_data_flag = transport_private_data_flag;
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
	 */
	@Override
	public String getHTML() {
		StringBuilder s = new StringBuilder();

		s.append("<br>adaptation_field_length: ").append(getHexAndDecimalFormattedString(getAdaptation_field_length()));
		if(getAdaptation_field_length()==0){
			return s.toString();
		}

		s.append("<br>discontinuity_indicator: ").append(getBooleanAsInt(discontinuity_indicator));
		s.append("<br>random_access_indicator: ").append(getBooleanAsInt(random_access_indicator));
		s.append("<br>elementary_stream_priority_indicator: ").append(getBooleanAsInt(elementary_stream_priority_indicator));
		s.append("<br>PCR_flag: ").append(getBooleanAsInt(PCR_flag));
		s.append("<br>OPCR_flag: ").append(getBooleanAsInt(OPCR_flag));
		s.append("<br>splicing_point_flag: ").append(getBooleanAsInt(splicing_point_flag));
		s.append("<br>transport_private_data_flag: ").append(getBooleanAsInt(transport_private_data_flag));
		s.append("<br>adaptation_field_extension_flag: ").append(getBooleanAsInt(adaptation_field_extension_flag));
		if(PCR_flag){
			s.append("<br>PCR: [base] ").append(getHexAndDecimalFormattedString(program_clock_reference.getProgram_clock_reference_base())).append(" : [extension] ").append(getHexAndDecimalFormattedString(program_clock_reference.getProgram_clock_reference_extension())).append(" (").append(printPCRTime(program_clock_reference.getProgram_clock_reference())).append(")");
		}
		if(OPCR_flag){
			s.append("<br>OPCR: ").append(getHexAndDecimalFormattedString(original_program_clock_reference.getProgram_clock_reference())).append(" (").append(printPCRTime(original_program_clock_reference.getProgram_clock_reference())).append(")");
		}
		if(splicing_point_flag){
			s.append("<br>splice_countdown: ").append(getHexAndDecimalFormattedString(splice_countdown));
		}
		if(transport_private_data_flag){
			s.append("<br>transport_private_data_length: ").append(getHexAndDecimalFormattedString(transport_private_data_length));
			s.append("<br>private_data_byte: ").append("0x").append(toHexString(private_data_byte)).append(" \"").append(
					toSafeString(private_data_byte)).append("\"");
			for (PrivateDataField privatedataField : privatedataFields) {
				final DefaultMutableTreeNode treeNode = privatedataField.getJTreeNode(0);
				s.append("<br><br><b>").append(getDataFieldTagString(privatedataField.getData_field_tag())).append("</b>");
				s.append("<br>").append(Utils.getChildrenAsHTML(treeNode));
				
			}
		}
		if(adaptation_field_extension_flag){
			s.append("<br>adaptation_field_extension_length: ").append(getHexAndDecimalFormattedString(adaptation_field_extension_length));
			s.append("<br>adaptation_field_extension_byte: ").append("0x").append(toHexString(adaptation_field_extension_byte)).append(" \"").append(
					toSafeString(adaptation_field_extension_byte)).append("\"");
			
			s.append("<br>ltw_flag: ").append(getBooleanAsInt(ltw_flag));
			s.append("<br>piecewise_rate_flag: ").append(getBooleanAsInt(piecewise_rate_flag));
			s.append("<br>seamless_splice_flag: ").append(getBooleanAsInt(seamless_splice_flag));
			s.append("<br>af_descriptor_not_present_flag: ").append(getBooleanAsInt(af_descriptor_not_present_flag));
			if(ltw_flag){
				s.append("<br>ltw_valid_flag: ").append(getBooleanAsInt(ltw_valid_flag));
				s.append("<br>ltw_offset: ").append(getHexAndDecimalFormattedString(ltw_offset));

			}
			if(piecewise_rate_flag){
				s.append("<br>piecewise_rate: ").append(getHexAndDecimalFormattedString(piecewise_rate));
			}
			if(seamless_splice_flag){
				s.append("<br>seamless_splice_flag: <span style=\"color:red\">Not implemented, please report!</span>");
				logger.info("seamless_splice_flag: Not implemented, please report!");
			}
			if(!af_descriptor_not_present_flag){
				for (Descriptor afDescriptor : afDescriptorList) {
					final DefaultMutableTreeNode treeNode = afDescriptor.getJTreeNode(0);
					s.append("<br><br><b>").append(afDescriptor.getDescriptorname()).append("</b>");
					s.append("<br>").append(Utils.getChildrenAsHTML(treeNode));
					
				}
			}
		}

		return s.toString();
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("adaptation_field"));
		t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_length",getAdaptation_field_length() ,null)));
		if(getAdaptation_field_length()==0){
			return t;
		}
		t.add(new DefaultMutableTreeNode(new KVP("discontinuity_indicator",getBooleanAsInt(discontinuity_indicator) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("random_access_indicator",getBooleanAsInt(random_access_indicator) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("elementary_stream_priority_indicator",getBooleanAsInt(elementary_stream_priority_indicator) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("PCR_flag",getBooleanAsInt(PCR_flag) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("OPCR_flag",getBooleanAsInt(OPCR_flag) ,null)));

		t.add(new DefaultMutableTreeNode(new KVP("splicing_point_flag",getBooleanAsInt(splicing_point_flag) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("transport_private_data_flag",getBooleanAsInt(transport_private_data_flag) ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_extension_flag",getBooleanAsInt(adaptation_field_extension_flag) ,null)));

		if(PCR_flag){
			t.add(program_clock_reference.getJTreeNode(modus, "PCR"));
		}

		if(OPCR_flag){
			t.add(original_program_clock_reference.getJTreeNode(modus, "OPCR"));
		}


		if(splicing_point_flag){
			t.add(new DefaultMutableTreeNode(new KVP("splice_countdown",splice_countdown ,null)));
		}
		if(transport_private_data_flag){
			t.add(new DefaultMutableTreeNode(new KVP("transport_private_data_length",transport_private_data_length ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",private_data_byte ,null)));
			Utils.addListJTree(t,privatedataFields,modus,"Private Data Fields");


		}

		if(adaptation_field_extension_flag){
			t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_extension_length",adaptation_field_extension_length ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("adaptation_field_extension_byte",adaptation_field_extension_byte ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("ltw_flag",getBooleanAsInt(ltw_flag) ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("piecewise_rate_flag",getBooleanAsInt(piecewise_rate_flag) ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("seamless_splice_flag",getBooleanAsInt(seamless_splice_flag) ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("af_descriptor_not_present_flag",getBooleanAsInt(af_descriptor_not_present_flag) ,null)));
			
			if(ltw_flag){
				t.add(new DefaultMutableTreeNode(new KVP("ltw_valid_flag",getBooleanAsInt(ltw_valid_flag) ,null)));
				t.add(new DefaultMutableTreeNode(new KVP("ltw_offset",ltw_offset ,null)));
			}
			if(piecewise_rate_flag){
				t.add(new DefaultMutableTreeNode(new KVP("piecewise_rate",piecewise_rate,null)));

			}
			if(seamless_splice_flag){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("seamless_splice_flag")));

			}
			if(!af_descriptor_not_present_flag){
				Utils.addListJTree(t,afDescriptorList,modus,"af_descriptors");
			}
		}

		return t;

	}
	/**
	 * @param data_field_tag2
	 * @return
	 */
	private static String getDataFieldTagString(int data_field_tag2) {
		return data_field_tag_list.get(data_field_tag2);
	}

	/**
	 * @return
	 */
	private static String getAU_coding_formatString(int AU_coding_format) {
		return AU_coding_format_list.get(AU_coding_format);
	}


	public static LookUpList getData_field_tag_list() {
		return data_field_tag_list;
	}


	public static LookUpList getAU_coding_format_list() {
		return AU_coding_format_list;
	}


	public static LookUpList getAU_frame_rate_code_list() {
		return AU_frame_rate_code_list;
	}


	public int getSplice_countdown() {
		return splice_countdown;
	}


	public int getTransport_private_data_length() {
		return transport_private_data_length;
	}


	public byte[] getPrivate_data_byte() {
		return private_data_byte;
	}


	public List<PrivateDataField> getPrivatedataFields() {
		return privatedataFields;
	}


	public int getAdaptation_field_extension_length() {
		return adaptation_field_extension_length;
	}


	public byte[] getAdaptation_field_extension_byte() {
		return adaptation_field_extension_byte;
	}


	public boolean isLtw_flag() {
		return ltw_flag;
	}


	public boolean isPiecewise_rate_flag() {
		return piecewise_rate_flag;
	}


	public boolean isSeamless_splice_flag() {
		return seamless_splice_flag;
	}


	public boolean isAf_descriptor_not_present_flag() {
		return af_descriptor_not_present_flag;
	}


	public boolean isLtw_valid_flag() {
		return ltw_valid_flag;
	}


	public int getLtw_offset() {
		return ltw_offset;
	}


	public int getPiecewise_rate() {
		return piecewise_rate;
	}


	public List<Descriptor> getAfDescriptorList() {
		return afDescriptorList;
	}

}
