/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.data.mpeg.pes.video266.ProfileTierLevel.general_profile_idc_list;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;
import nl.digitalekabeltelevisie.util.Utils;

/**
*
* Based on 2.6.129 VVC video descriptor, ISO/IEC 13818-1:2021 / Rec. ITU-T H.222.0 (06/2021)
* 
* @author Eric
*
*/

public class VVCVideoDescriptor extends Descriptor {
	
	private static final LookUpList hdr_wgc_idc_list = new LookUpList.Builder().
			add(0, "SDR").
			add(1, "WCG only").
			add(2, "Both HDR and WCG").
			add(3, "No indication is made regarding HDR/WCG or SDR").
			build();

	
	private static final LookUpList video_properties_sdr_list = new LookUpList.Builder().
			add(0,"Video property CICP combination not specified or unknown").
			add(1,"[1,1,1,0]- BT709_YCC").
			add(2,"[1,1,0,0]- BT709_RGB").
			add(3,"[6,6,6,0]- BT601_525").
			add(4,"[5,6,5,0]- BT601_625").
			add(5,"[1,1,0,1]- FR709_RGB").
			add(6,15,"Reserved").
			build();
			
	private static final LookUpList video_properties_wgc_list = new LookUpList.Builder().
			add(0, "Video property CICP combination not specified or unknown").
			add(1, "[9,14,9,0]- BT2020_YCC_NCL").
			add(2, "[9,14,0,0]- BT2020_RGB").
			add(3, "[9,14,0,1]- FR2020_RGB").
			add(4, "[12,1,6,1]- FRP3D65_YCC").
			add(5,15 ,"Reserved").
			build();
			
	private static final LookUpList video_properties_hdr_wgc_list = new LookUpList.Builder().
			
			add(0, "Video property CICP combination not specified or unknown").
			add(1, "[9,16,9,0]- BT2100_PQ_YCC").
			add(2, "[9,18,9,0]- BT2100_HLG_YCC").
			add(3, "[9,16,14,0]- BT2100_PQ_ICTCP").
			add(4, "[9,16,0,0]- BT2100_PQ_RGB").
			add(5, "[9,18,0,0]- BT2100_HLG_RGB").
			add(6,15, "Reserved").
			build();
			
			
		private static final LookUpList video_properties_no_indication_list = new LookUpList.Builder().
				
			add(0, "Video property CICP combination not specified or unknown").
			add(1,15, "Reserved or private").
			build();
				
				
	private int profile_idc;
	private int tier_flag;
	private int num_sub_profiles;
	private List<Long> sub_profile_idc_list = new ArrayList<>();
	private int progressive_source_flag;
	private int interlaced_source_flag;
	private int non_packed_constraint_flag;
	private int frame_only_constraint_flag;
	private int reserved_zero_4bits;
	private int level_idc;
	private int temporal_layer_subset_flag;
	private int VVC_still_present_flag;
	private int VVC_24hr_picture_present_flag;
	private int reserved;
	private int HDR_WCG_idc;
	private int video_properties_tag;
	private int reserved2;
	private int reserved3;
	private int temporal_id_min;
	private int reserved4;
	private int temporal_id_max;


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public VVCVideoDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset,parent);
		profile_idc = Utils.getInt(b, offset+2, 1, 0b1111_1110)>>1;
		tier_flag = Utils.getInt(b, offset+2, 1, MASK_1BIT);
		num_sub_profiles = Utils.getInt(b, offset+3, 1, MASK_8BITS);
		int localOffset = offset + 4;
		for (int i = 0; i < num_sub_profiles; i++) {
			long sub_profile_idc = Utils.getLong(b, localOffset, 4, MASK_32BITS);
			sub_profile_idc_list.add(sub_profile_idc);
			localOffset += 4;
			
		}
		
		progressive_source_flag = Utils.getInt(b, localOffset, 1, 0b1000_0000)>>7;
		interlaced_source_flag = Utils.getInt(b, localOffset, 1, 0b0100_0000)>>6;
		non_packed_constraint_flag = Utils.getInt(b, localOffset, 1, 0b0010_0000)>>5;
		frame_only_constraint_flag = Utils.getInt(b, localOffset, 1, 0b0001_0000)>>4;
		reserved_zero_4bits = Utils.getInt(b, localOffset++, 1, MASK_4BITS);
				
		level_idc = Utils.getInt(b, localOffset++, 1, MASK_8BITS);
		
		temporal_layer_subset_flag = Utils.getInt(b, localOffset, 1, 0b1000_0000)>>7;
		VVC_still_present_flag = Utils.getInt(b, localOffset, 1, 0b0100_0000)>>6;
		VVC_24hr_picture_present_flag = Utils.getInt(b, localOffset, 1, 0b0010_0000)>>5;
		reserved = Utils.getInt(b, localOffset++, 1, MASK_5BITS);

		HDR_WCG_idc = Utils.getInt(b, localOffset, 1, 0b1100_0000)>>6;
		reserved2 = Utils.getInt(b, localOffset, 1, 0b0011_0000)>>4;
		video_properties_tag = Utils.getInt(b, localOffset++, 1, MASK_4BITS);
		
		if ( temporal_layer_subset_flag == 1) {
			reserved3 = Utils.getInt(b, localOffset, 1, 0b1111_1000)>>3;
			temporal_id_min = Utils.getInt(b, localOffset++, 1, MASK_3BITS);
			reserved4 = Utils.getInt(b, localOffset, 1, 0b1111_1000)>>3;
			temporal_id_max = Utils.getInt(b, localOffset++, 1, MASK_3BITS);
		}
	}

	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("profile_idc",profile_idc,general_profile_idc_list.get(profile_idc, "unknown/reserved"))));
		t.add(new DefaultMutableTreeNode(new KVP("tier_flag",tier_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_sub_profiles",num_sub_profiles,null)));
		
		for (int i = 0; i < num_sub_profiles; i++) {
			long sub_profile_idc = sub_profile_idc_list.get(i);
			t.add(new DefaultMutableTreeNode(new KVP("sub_profile_idc["+i+"]",sub_profile_idc,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("progressive_source_flag",progressive_source_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("interlaced_source_flag",interlaced_source_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("non_packed_constraint_flag",non_packed_constraint_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_only_constraint_flag",frame_only_constraint_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_4bits",reserved_zero_4bits,null)));


		t.add(new DefaultMutableTreeNode(new KVP("level_idc",level_idc,null)));
		
		t.add(new DefaultMutableTreeNode(new KVP("temporal_layer_subset_flag",temporal_layer_subset_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("VVC_still_present_flag",VVC_still_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("VVC_24hr_picture_present_flag",VVC_24hr_picture_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
		
		t.add(new DefaultMutableTreeNode(new KVP("HDR_WCG_idc",HDR_WCG_idc,hdr_wgc_idc_list.get(HDR_WCG_idc))));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("video_properties_tag",video_properties_tag,getVideoPropertiesTagString(HDR_WCG_idc, video_properties_tag))));
		
		if ( temporal_layer_subset_flag == 1) {
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved3,null)));
		t.add(new DefaultMutableTreeNode(new KVP("temporal_id_min",temporal_id_min,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved4,null)));
		t.add(new DefaultMutableTreeNode(new KVP("temporal_id_max",temporal_id_max,null)));
		}

		return t;
	}


	/**
	 * @param hDR_WCG_idc2
	 * @param video_properties_tag2
	 * @return
	 */
	private static String getVideoPropertiesTagString(int hDR_WCG_idc, int video_properties_tag) {
		
		return switch (hDR_WCG_idc) {
			case 0 -> video_properties_sdr_list.get(video_properties_tag);
			case 1 -> video_properties_wgc_list.get(video_properties_tag);
			case 2 -> video_properties_hdr_wgc_list.get(video_properties_tag);
			case 3 -> video_properties_no_indication_list.get(video_properties_tag);
	
			default -> "Unexpected value: " + hDR_WCG_idc;	
		
		
		};
	}

}
